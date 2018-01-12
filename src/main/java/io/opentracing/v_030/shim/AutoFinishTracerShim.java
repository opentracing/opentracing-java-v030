/*
 * Copyright 2016-2018 The OpenTracing Authors
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
package io.opentracing.v_030.shim;

import io.opentracing.Scope;
import io.opentracing.ScopeManager;
import io.opentracing.SpanContext;
import io.opentracing.propagation.Format;
import io.opentracing.util.AutoFinishScope;
import io.opentracing.util.AutoFinishScopeManager;
import io.opentracing.v_030.ActiveSpan;

public class AutoFinishTracerShim extends TracerShim {
    public AutoFinishTracerShim(io.opentracing.Tracer tracer) {
        super(tracer);

        if (!(tracer.scopeManager() instanceof AutoFinishScopeManager)) {
            throw new IllegalArgumentException("tracer.scopeManager is not AutoFinishScopeManager");
        }
    }

    @Override
    protected ActiveSpanShim createActiveSpanShim(Scope scope) {
        return new AutoFinishActiveSpanShim(scope);
    }

    final static class AutoFinishActiveSpanShim extends ActiveSpanShim {
        public AutoFinishActiveSpanShim(Scope scope) {
            super(scope);
        }

        @Override
        public Continuation capture() {
            return new Continuation(((AutoFinishScope)scope()).capture());
        }

        private final class Continuation implements ActiveSpan.Continuation {
            AutoFinishScope.Continuation continuation;

            Continuation(AutoFinishScope.Continuation continuation) {
                this.continuation = continuation;
            }

            @Override
            public ActiveSpanShim activate() {
                return new AutoFinishActiveSpanShim(continuation.activate());
            }
        }
    }
}
