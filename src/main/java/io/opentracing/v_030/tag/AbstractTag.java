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
package io.opentracing.v_030.tag;

import io.opentracing.v_030.BaseSpan;

public abstract class AbstractTag<T> {
    protected final String key;

    public AbstractTag(String tagKey) {
        this.key = tagKey;
    }

    public String getKey() {
        return key;
    }

    protected abstract void set(BaseSpan<?> span, T tagValue);
}
