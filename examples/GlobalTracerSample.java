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
import io.opentracing.v_030.shim.TracerShim;
import io.opentracing.v_030.util.GlobalTracer;

public final class GlobalTracerSample {

    public void init() {
        io.opentracing.Tracer tracer = null; // use your own 0.31 Tracer here.
        io.opentracing.util.GlobalTracer.register(tracer); // Optional, in case the 0.31 Tracer wants to be used.

        GlobalTracer.register(new TracerShim(tracer));
    }

    public Object sendMessage(Object input) {
        io.opentracing.Scope scope = null;
        try {
            scope = io.opentracing.util.GlobalTracer.get().buildSpan("foo").startActive();
            // Send the input somewhere, and do some work on it.
            return "<value>";
        } finally {
            scope.close();
        }
    }

    public Object getLatestValue() {
        ActiveSpan span = null;
        try {
            span = GlobalTracer.get().buildSpan("bar").startActive();
            // Get the value from some proxy/library.
            return "<value-n>";
        } finally {
            span.close();
        }
    }
}
