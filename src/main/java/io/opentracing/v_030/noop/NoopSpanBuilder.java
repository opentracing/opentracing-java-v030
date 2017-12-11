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
package io.opentracing.v_030.noop;

import io.opentracing.v_030.ActiveSpan;
import io.opentracing.v_030.BaseSpan;
import io.opentracing.v_030.Span;
import io.opentracing.v_030.SpanContext;
import io.opentracing.v_030.Tracer;

import java.util.Collections;
import java.util.Map;

public interface NoopSpanBuilder extends Tracer.SpanBuilder, NoopSpanContext {
    static final NoopSpanBuilder INSTANCE = new NoopSpanBuilderImpl();
}

final class NoopSpanBuilderImpl implements NoopSpanBuilder {

    @Override
    public Tracer.SpanBuilder addReference(String refType, SpanContext referenced) {
        return this;
    }

    @Override
    public Tracer.SpanBuilder asChildOf(SpanContext parent) {
        return this;
    }

    @Override
    public Tracer.SpanBuilder ignoreActiveSpan() { return this; }

    @Override
    public Tracer.SpanBuilder asChildOf(BaseSpan parent) {
        return this;
    }

    @Override
    public Tracer.SpanBuilder withTag(String key, String value) {
        return this;
    }

    @Override
    public Tracer.SpanBuilder withTag(String key, boolean value) {
        return this;
    }

    @Override
    public Tracer.SpanBuilder withTag(String key, Number value) {
        return this;
    }

    @Override
    public Tracer.SpanBuilder withStartTimestamp(long microseconds) {
        return this;
    }

    @Override
    public Span start() {
        return startManual();
    }

    @Override
    public ActiveSpan startActive() {
        return NoopActiveSpanSource.NoopActiveSpan.INSTANCE;
    }

    @Override
    public Span startManual() {
        return NoopSpanImpl.INSTANCE;
    }

    @Override
    public Iterable<Map.Entry<String, String>> baggageItems() {
        return Collections.EMPTY_MAP.entrySet();
    }

    @Override
    public String toString() { return NoopSpanBuilder.class.getSimpleName(); }
}
