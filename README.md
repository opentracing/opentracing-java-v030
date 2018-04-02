[![Build Status][ci-img]][ci] [![Coverage Status][cov-img]][cov] [![Released Version][maven-img]][maven]

# OpenTracing-Java 0.30 compatibility layer.

The `opentracing-v030` artifact provides a 0.30 API compatibility layer which comprises:
1. Exposing all the the 0.30 packages under `io.opentracing.v_030` (`io.opentracing.v_030.propagation`, `io.opentracing.v_30.util`, etc).
2. A Shim layer to wrap 0.31 Tracer and expose it under the 0.30 API.

## Shim Layer

The basic shim layer is exposed through `TracerShim`, which wraps a `io.opentracing.Tracer` object and exposes it under the `io.opentracing.v_030.Tracer` interface:

```java
import io.opentracing.v_030.ActiveSpan;
import io.opentracing.v_030.Tracer;
import io.opentracing.v_030.shim.TracerShim;

io.opentracing.Tracer upstreamTracer = new CustomTracer(..., new CustomScopeManager());
Tracer tracer = new TracerShim(yourUpstreamTracer);
```

## Continuation support.

`TracerShim` does not support `ActiveSpan.capture()` nor `Continuation`s. For this usage, `AutoFinishTracerShim` must be used, along `io.opentracing.util.AutoFinishScopeManager` as `Tracer.scopeManager()` (which uses thread-local storage and preserves the reference-count system used in 0.30).

```java
import io.opentracing.v_030.ActiveSpan;
import io.opentracing.v_030.Tracer;
import io.opentracing.v_030.shim.AutoFinishTracerShim;
import io.opentracing.util.AutoFinishScopeManager;

io.opentracing.Tracer upstreamTracer = new CustomTracer(..., new AutoFinishScopeManager());
Tracer tracer = new TracerShim(yourUpstreamTracer);

try (ActiveSpan span = tracer.buildSpan("parent").startActive()) {
    ActiveSpan.Continuation cont = span.capture();
    ...
}
```

## Integration with existing instrumentation code.

To support code being instrumented using the 0.30 API it is required to update the `import` statements (probably with the help of a refactoring tool) from `io.opentracing` to `io.opentracing.v_030`:

```java
import io.opentracing.v_030.Tracer; // Previously io.opentracing.Tracer
import io.opentracing.v_030.Span; // Previously io.opentracing.Span
```

This is done as `io.opentracing` refers to the new 0.31 API. It is possible to keep both API versions working side by side with help of the Shim layer, allowing an incremental adoption of the new API:

```java
import io.opentracing.v_030.Tracer;

io.opentracing.Tracer upstreamTracer = ...;
Tracer tracer = new TracerShim(tracer);

class Engine {
    public void getResult(Object input) {
        try (ActiveSpan span = tracer.buildSpan("engine-result").startActive()) {
            Object proxyResult = proxyCall(input);
	    // Do something with proxyResult...

            span.setTag("processed-value", processedValue.toString());
            return processedValue;
        }
    }

    Object proxyCall(Object input) {
        // Will implicitly use the active Span crated in getResult() as the active/parent Span.
        try (Scope scope = upstreamTracer.buildSpan("engine-proxy-call").startActive(true)) {
            Object result = library.invoke(input);
            scope.span().setTag("proxy-value", result.toString());
            return result;
        }
    }
}
```

Instead of keeping both 0.30 and 0.31 `Tracers`s around, it's possible to register them using the `GlobalTracer` classes:

```java
void init() {
    io.opentracing.Tracer upstreamTracer = ...;
    io.opentracing.v_030.Tracer tracer = new TracerShim(upstreamTracer);

    io.opentracing.util.GlobalTracer.register(upstreamTracer);
    io.opentracing.v_030.util.GlobalTracer.register(tracer);
}
```

Now both `GlobalTracer` instances will refer to the same `Tracer`, and can be used anywhere.

### Formats

The builtin `TEXT_MAP`, `HTTP_HEADERS` and `BINARY` formats will be automatically translated by the Shim layer. No support exist for custom formats at the moment, however.

## Extending the Shim layer

When the Shim layer is required without the reference-count system, it's possible to provide a custom class extending `TracerShim`, which will need to provide a custom `ActiveSpanShim` instance upon `Span` activation:

```java
import io.opentracing.v_030.ActiveSpan;
import io.opentracing.v_030.Tracer;
import io.opentracing.v_030.shim.TracerShim;


public class CustomTracerShim extends TracerShim {
    public CustomTracerShim(io.opentracing.Tracer tracer) {
        super(tracer);
    }

    @Override
    public ActiveSpanShim createActiveSpanShim(Scope scope) {
        return CustomActiveSpanShim(scope);
    }

    static final class CustomActiveSpanShim extends ActiveSpanShim {
        public CustomActiveSpanShim(Scope scope) {
            super(scope);
        }

        @Override
        public Continuation capture() {
            ...
        }
    }
}
```

The returned `ActiveSpanShim` instance must react properly to `ActiveSpan.capture()` and return a `ActiveSpan.Continuation` object than can later be reactivated. Observe the default implementation of `ActiveSpanShim.capture()` throws `UnsupportedOperationException`.

## Contributing

See [Contributing](CONTRIBUTING.md) for matters such as license headers.

  [ci-img]: https://travis-ci.org/opentracing/opentracing-java-v030.svg?branch=master
  [ci]: https://travis-ci.org/opentracing/opentracing-java-v030
  [cov-img]: https://coveralls.io/repos/github/opentracing/opentracing-java-v030/badge.svg?branch=master
  [cov]: https://coveralls.io/github/opentracing/opentracing-java-v030?branch=master
  [maven-img]: https://img.shields.io/maven-central/v/io.opentracing/opentracing-v030.svg
  [maven]: http://search.maven.org/#search%7Cga%7C1%7Copentracing-v030

## License

By contributing to OpenTracing documentation repository, you agree that your contributions will be licensed under its [Apache 2.0 License](./LICENSE).
