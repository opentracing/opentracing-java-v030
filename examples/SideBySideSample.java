/*
 * Copyright 2016-2017 The OpenTracing Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package io.opentracing.v_030.examples;

import io.opentracing.v_030.ActiveSpan;
import io.opentracing.v_030.Span;
import io.opentracing.v_030.Tracer;

public final class SideBySideSample {
    io.opentracing.Tracer upstreamTracer;
    Tracer tracer;

    public void SampleTest(io.opentracing.Tracer upstreamTracer, Tracer tracer) {
        this.upstreamTracer = upstreamTracer;
        this.tracer = tracer;
    }

    public Object getResult(Object input) {
        try (ActiveSpan span = tracer.buildSpan("engine-result").startActive()) {
            Object proxyResult = proxyCall(input);
            // Do something with proxyResult...

            Object processedValue = "<value-n>";
            span.setTag("processed-value", processedValue.toString());
            return processedValue;
        }
    }

    Object proxyCall(Object input) {
        // Will implicitly use the active Span created in getResult() as the active/parent Span.
        try (io.opentracing.Scope scope = upstreamTracer.buildSpan("engine-proxy-call").startActive(true)) {
            Object result = "<remote-value-n>";
            scope.span().setTag("proxy-value", result.toString());
            return result;
        }
    }
}
