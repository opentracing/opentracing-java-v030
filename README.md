[![Build Status][ci-img]][ci]

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
import io.opentracing.v_030.util.AutoFinishScopeManager;

io.opentracing.Tracer upstreamTracer = new CustomTracer(..., new AutoFinishScopeManager());
Tracer tracer = new TracerShim(yourUpstreamTracer);

try (ActiveSpan span = tracer.buildSpan("parent").startActive()) {
    ActiveSpan.Continuation cont = span.capture();
    ...
}
```

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
