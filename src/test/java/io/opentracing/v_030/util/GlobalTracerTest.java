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
package io.opentracing.v_030.util;

import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import io.opentracing.v_030.SpanContext;
import io.opentracing.v_030.Tracer;
import io.opentracing.v_030.noop.NoopSpanBuilder;
import io.opentracing.v_030.noop.NoopTracerFactory;
import io.opentracing.v_030.propagation.Format;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class GlobalTracerTest {

    private static void _setGlobal(Tracer tracer) {
        try {
            Field globalTracerField = GlobalTracer.class.getDeclaredField("tracer");
            globalTracerField.setAccessible(true);
            globalTracerField.set(null, tracer);
            globalTracerField.setAccessible(false);
        } catch (Exception e) {
            throw new RuntimeException("Error reflecting globalTracer: " + e.getMessage(), e);
        }
    }

    @Before
    @After
    public void clearGlobalTracer() {
        _setGlobal(NoopTracerFactory.create());
    }

    @Test
    public void testGet_SingletonReference() {
        Tracer tracer = GlobalTracer.get();
        assertThat(tracer, is(instanceOf(GlobalTracer.class)));
        assertThat(tracer, is(sameInstance(GlobalTracer.get())));
    }

    @Test
    public void testMultipleRegistrations() {
        GlobalTracer.register(mock(Tracer.class));
        try {
            GlobalTracer.register(mock(Tracer.class));
            fail("Duplicate registration exception expected.");
        } catch (RuntimeException expected) {
            assertThat("Duplicate registration message", expected.getMessage(), is(notNullValue()));
        }
    }

    /**
     * Check leniency for duplicate registration with the same tracer by mistake.
     */
    @Test
    public void testMultipleRegistrations_sameTracer() {
        Tracer mockTracer = mock(Tracer.class);
        GlobalTracer.register(mockTracer);
        GlobalTracer.register(mockTracer);
        // 'test' that double registration of the same tracer does not throw exception
    }

    @Test
    public void testRegisterGlobalTracer() {
        assertThat(GlobalTracer.get().buildSpan("foo"), is(instanceOf(NoopSpanBuilder.class)));
        GlobalTracer.register(GlobalTracer.get());
        assertThat(GlobalTracer.get().buildSpan("foo"), is(instanceOf(NoopSpanBuilder.class)));
    }

    @Test(expected = NullPointerException.class)
    public void testRegisterNull() {
        GlobalTracer.register(null);
    }

    @Test
    public void testNoopTracerByDefault() {
        Tracer.SpanBuilder spanBuilder = GlobalTracer.get().buildSpan("my-operation");
        assertThat(spanBuilder, is(instanceOf(NoopSpanBuilder.class)));
    }

    @Test
    public void testDelegation_buildSpan() {
        Tracer mockTracer = mock(Tracer.class);
        GlobalTracer.register(mockTracer);
        GlobalTracer.get().buildSpan("my-operation");

        verify(mockTracer).buildSpan(eq("my-operation"));
        verifyNoMoreInteractions(mockTracer);
    }

    @Test
    public void testDelegation_inject() {
        Tracer mockTracer = mock(Tracer.class);
        SpanContext mockContext = mock(SpanContext.class);
        Format<Object> mockFormat = mock(Format.class);
        Object mockCarrier = mock(Object.class);
        GlobalTracer.register(mockTracer);
        GlobalTracer.get().inject(mockContext, mockFormat, mockCarrier);

        verify(mockTracer).inject(eq(mockContext), eq(mockFormat), eq(mockCarrier));
        verifyNoMoreInteractions(mockTracer, mockContext, mockFormat, mockCarrier);
    }

    @Test
    public void testDelegation_extract() {
        Tracer mockTracer = mock(Tracer.class);
        Format<Object> mockFormat = mock(Format.class);
        Object mockCarrier = mock(Object.class);
        GlobalTracer.register(mockTracer);
        GlobalTracer.get().extract(mockFormat, mockCarrier);

        verify(mockTracer).extract(eq(mockFormat), eq(mockCarrier));
        verifyNoMoreInteractions(mockTracer, mockFormat, mockCarrier);
    }

    @Test
    public void concurrencyTest() throws InterruptedException, ExecutionException {
        final int threadCount = 10;
        ExecutorService threadpool = Executors.newFixedThreadPool(2 * threadCount);
        try {
            // Try to do ten register() calls and ten buildSpan() calls concurrently.
            List<Callable<Void>> registerCalls = new ArrayList<Callable<Void>>();
            List<Callable<Tracer.SpanBuilder>> buildSpanCalls = new ArrayList<Callable<Tracer.SpanBuilder>>();
            for (int i = 0; i < threadCount; i++) {
                registerCalls.add(new Callable<Void>() {
                    @Override
                    public Void call() throws Exception {
                        GlobalTracer.register(mock(Tracer.class));
                        return null;
                    }
                });
                buildSpanCalls.add(new Callable<Tracer.SpanBuilder>() {
                    @Override
                    public Tracer.SpanBuilder call() throws Exception {
                        return GlobalTracer.get().buildSpan("my-operation");
                    }
                });
            }

            // Schedule the threads.
            List<Future<Void>> registerResults = threadpool.invokeAll(registerCalls);
            List<Future<Tracer.SpanBuilder>> buildSpanResults = threadpool.invokeAll(buildSpanCalls);

            boolean registered = false; // there may only be one registration success.
            int exceptions = 0;
            for (Future<Void> result : registerResults) {
                try {
                    result.get(); // void, but should throw exception 9 times
                    assertThat("previous registration", registered, is(false));
                    registered = true;
                } catch (ExecutionException expected) {
                    exceptions++;
                }
            }
            assertThat("Tracer registration", registered, is(true));
            assertThat("Registration exceptions", exceptions, is(threadCount - 1));

            for (Future<Tracer.SpanBuilder> result : buildSpanResults) {
                // each spanbuilder must be either null (from registered mock tracer) or noop
                assertThat("SpanBuilder", result.get(), anyOf(nullValue(), instanceOf(NoopSpanBuilder.class)));
            }

        } finally {
            threadpool.shutdown();
        }
    }

    @Test
    public void testIsRegistered() {
        assertThat("Should not be registered", GlobalTracer.isRegistered(), is(false));
        GlobalTracer.register(mock(Tracer.class));
        assertThat("Should be registered", GlobalTracer.isRegistered(), is(true));
    }

}
