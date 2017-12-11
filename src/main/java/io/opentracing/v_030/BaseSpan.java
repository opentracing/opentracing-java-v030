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
package io.opentracing.v_030;

import java.util.Map;

/**
 * {@link BaseSpan} represents the OpenTracing specification's span contract with the exception of methods to finish
 * said span. For those, either use {@link Span#finish()} or {@link ActiveSpan#deactivate()} depending on the
 * programming model.
 *
 * @see Span
 * @see ActiveSpan
 * @see Tracer.SpanBuilder#startManual()
 * @see Tracer.SpanBuilder#startActive()
 */
public interface BaseSpan<S extends BaseSpan> {
    /**
     * Retrieve the associated SpanContext.
     *
     * This may be called at any time, including after calls to finish().
     *
     * @return the SpanContext that encapsulates Span state that should propagate across process boundaries.
     */
    SpanContext context();

    /**
     * Set a key:value tag on the Span.
     */
    S setTag(String key, String value);

    /** Same as {@link #setTag(String, String)}, but for boolean values. */
    S setTag(String key, boolean value);

    /** Same as {@link #setTag(String, String)}, but for numeric values. */
    S setTag(String key, Number value);

    /**
     * Log key:value pairs to the Span with the current walltime timestamp.
     *
     * <p><strong>CAUTIONARY NOTE:</strong> not all Tracer implementations support key:value log fields end-to-end.
     * Caveat emptor.
     *
     * <p>A contrived example (using Guava, which is not required):
     * <pre><code>
     span.log(
     ImmutableMap.Builder<String, Object>()
     .put("event", "soft error")
     .put("type", "cache timeout")
     .put("waited.millis", 1500)
     .build());
     </code></pre>
     *
     * @param fields key:value log fields. Tracer implementations should support String, numeric, and boolean values;
     *               some may also support arbitrary Objects.
     * @return the Span, for chaining
     * @see Span#log(String)
     */
    S log(Map<String, ?> fields);

    /**
     * Like log(Map&lt;String, Object&gt;), but with an explicit timestamp.
     *
     * <p><strong>CAUTIONARY NOTE:</strong> not all Tracer implementations support key:value log fields end-to-end.
     * Caveat emptor.
     *
     * @param timestampMicroseconds The explicit timestamp for the log record. Must be greater than or equal to the
     *                              Span's start timestamp.
     * @param fields key:value log fields. Tracer implementations should support String, numeric, and boolean values;
     *               some may also support arbitrary Objects.
     * @return the Span, for chaining
     * @see Span#log(long, String)
     */
    S log(long timestampMicroseconds, Map<String, ?> fields);

    /**
     * Record an event at the current walltime timestamp.
     *
     * Shorthand for
     *
     * <pre><code>
     span.log(Collections.singletonMap("event", event));
     </code></pre>
     *
     * @param event the event value; often a stable identifier for a moment in the Span lifecycle
     * @return the Span, for chaining
     */
    S log(String event);

    /**
     * Record an event at a specific timestamp.
     *
     * Shorthand for
     *
     * <pre><code>
     span.log(timestampMicroseconds, Collections.singletonMap("event", event));
     </code></pre>
     *
     * @param timestampMicroseconds The explicit timestamp for the log record. Must be greater than or equal to the
     *                              Span's start timestamp.
     * @param event the event value; often a stable identifier for a moment in the Span lifecycle
     * @return the Span, for chaining
     */
    S log(long timestampMicroseconds, String event);

    /**
     * Sets a baggage item in the Span (and its SpanContext) as a key/value pair.
     *
     * Baggage enables powerful distributed context propagation functionality where arbitrary application data can be
     * carried along the full path of request execution throughout the system.
     *
     * Note 1: Baggage is only propagated to the future (recursive) children of this SpanContext.
     *
     * Note 2: Baggage is sent in-band with every subsequent local and remote calls, so this feature must be used with
     * care.
     *
     * @return this Span instance, for chaining
     */
    S setBaggageItem(String key, String value);

    /**
     * @return the value of the baggage item identified by the given key, or null if no such item could be found
     */
    String getBaggageItem(String key);

    /**
     * Sets the string name for the logical operation this span represents.
     *
     * @return this Span instance, for chaining
     */
    S setOperationName(String operationName);
}
