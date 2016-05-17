/*
 * Copyright 2015-2016 LMAX Ltd.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.lmax.tool.disruptor.reflect;

import com.lmax.tool.disruptor.Resetable;

final class ObjectArrayHolder implements Resetable
{
    private Object[] args;

    public void set(final Object[] args)
    {
        this.args = args;
    }

    public Object[] get()
    {
        return args;
    }

    @Override
    public void reset()
    {
        args = null;
    }
}
