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

import java.nio.ByteBuffer;
import java.nio.BufferOverflowException;

import io.opentracing.v_030.ActiveSpan;
import io.opentracing.v_030.Tracer;
import io.opentracing.v_030.propagation.Format;
import io.opentracing.v_030.shim.TracerShim;

public final class BinaryFormatSample {
    Tracer tracer;

    public void SampleTest(io.opentracing.Tracer upstreamTracer) {
        this.tracer = new TracerShim(upstreamTracer);
    }

    public Object getResult(Object input) {
        try (ActiveSpan span = tracer.buildSpan("get-result").startActive()) {

            ByteBuffer buff = ByteBuffer.allocate(128); // Reserve enough space
            try {
                tracer.inject(span.context(), Format.Builtin.BINARY, buff);
            } catch (BufferOverflowException e) {
                // Recover somehow...
            }

            Object proxyResult = proxyCall(input, buff.array());
            return proxyResult;
        }
    }
}
