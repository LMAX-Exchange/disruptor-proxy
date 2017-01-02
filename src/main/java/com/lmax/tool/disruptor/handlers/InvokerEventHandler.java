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

package com.lmax.tool.disruptor.handlers;

import com.lmax.disruptor.EventHandler;
import com.lmax.tool.disruptor.ProxyMethodInvocation;

/**
 * A Disruptor event handler that will invoke an operation on the supplied implementation
 * @param <T> the type of the implementation object to be invoked
 */
final class InvokerEventHandler<T> implements EventHandler<ProxyMethodInvocation>
{
    private final T implementation;
    private final boolean reset;

    public InvokerEventHandler(final T implementation, boolean reset)
    {
        this.implementation = implementation;
        this.reset = reset;
    }

    public InvokerEventHandler(final T implementation)
    {
        this(implementation, true);
    }

    @Override
    public void onEvent(final ProxyMethodInvocation event, final long sequence, final boolean endOfBatch) throws Exception
    {
        event.getInvoker().invokeWithArgumentHolder(implementation, event.getArgumentHolder());
        if (reset)
        {
            event.reset();
        }
    }
}
