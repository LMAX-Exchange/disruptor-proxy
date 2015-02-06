/*
 * Copyright 2015 LMAX Ltd.
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

package com.epickrram.tool.disruptor.reflect;

import com.epickrram.tool.disruptor.Invoker;
import com.epickrram.tool.disruptor.OverflowStrategy;
import com.epickrram.tool.disruptor.ProxyMethodInvocation;
import com.lmax.disruptor.RingBuffer;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Map;

final class RingBufferInvocationHandler<T> implements InvocationHandler
{
    private final RingBuffer<ProxyMethodInvocation> ringBuffer;
    private final Map<Method, Invoker> methodToInvokerMap;
    private final OverflowStrategy overflowStrategy;

    RingBufferInvocationHandler(final RingBuffer<ProxyMethodInvocation> ringBuffer,
                                final Map<Method, Invoker> methodToInvokerMap,
                                final OverflowStrategy overflowStrategy)
    {
        this.ringBuffer = ringBuffer;
        this.methodToInvokerMap = methodToInvokerMap;
        this.overflowStrategy = overflowStrategy;
    }

    @Override
    public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable
    {
        if(overflowStrategy == OverflowStrategy.DROP && !ringBuffer.hasAvailableCapacity(1))
        {
            return null;
        }
        final long sequence = ringBuffer.next();
        try
        {
            final ProxyMethodInvocation proxyMethodInvocation = ringBuffer.get(sequence);
            final ObjectArrayHolder argumentHolder = (ObjectArrayHolder) proxyMethodInvocation.getArgumentHolder();
            if(args != null)
            {
                final Object[] copyOfArgs = new Object[args.length];
                System.arraycopy(args, 0, copyOfArgs, 0, args.length);
                argumentHolder.set(copyOfArgs);
            }
            else
            {
                argumentHolder.set(null);
            }
            proxyMethodInvocation.setInvoker(methodToInvokerMap.get(method));
        }
        finally
        {
            ringBuffer.publish(sequence);
        }
        return null;
    }
}
