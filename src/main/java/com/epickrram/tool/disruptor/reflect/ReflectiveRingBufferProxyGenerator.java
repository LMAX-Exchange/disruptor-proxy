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
import com.epickrram.tool.disruptor.InvokerEventHandler;
import com.epickrram.tool.disruptor.OverflowStrategy;
import com.epickrram.tool.disruptor.ProxyMethodInvocation;
import com.epickrram.tool.disruptor.ResetHandler;
import com.epickrram.tool.disruptor.RingBufferProxyGenerator;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static java.lang.Thread.currentThread;
import static java.lang.reflect.Proxy.newProxyInstance;

/**
 * {@inheritDoc}
 */
public final class ReflectiveRingBufferProxyGenerator implements RingBufferProxyGenerator
{
    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    @Override
    public <T> T createRingBufferProxy(final T implementation, final Class<T> definition,
                                       final Disruptor<ProxyMethodInvocation> disruptor,
                                       final OverflowStrategy overflowStrategy)
    {
        final RingBufferInvocationHandler invocationHandler = createInvocationHandler(definition, disruptor, overflowStrategy);
        preallocateArgumentHolders(disruptor.getRingBuffer());

        disruptor.handleEventsWith(new InvokerEventHandler<T>(implementation));

        return generateProxy(definition, invocationHandler);
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    @Override
    public <T> T createRingBufferProxy(final Class<T> definition, final Disruptor<ProxyMethodInvocation> disruptor,
                                       final OverflowStrategy overflowStrategy, final T... implementations)
    {
        if (implementations.length < 1)
        {
            throw new IllegalArgumentException("Must have at least one implementation");
        }
        else if (implementations.length == 1)
        {
            return createRingBufferProxy(implementations[0], definition, disruptor, overflowStrategy);
        }

        final RingBufferInvocationHandler invocationHandler = createInvocationHandler(definition, disruptor, overflowStrategy);
        preallocateArgumentHolders(disruptor.getRingBuffer());

        final InvokerEventHandler<T>[] handlers = new InvokerEventHandler[implementations.length];
        for (int i = 0; i < implementations.length; i++)
        {
            handlers[i] = new InvokerEventHandler<T>(implementations[i], false);
            disruptor.handleEventsWith(handlers[i]);
        }
        disruptor.after(handlers).then(new ResetHandler());

        return generateProxy(definition, invocationHandler);
    }

    private static <T> RingBufferInvocationHandler createInvocationHandler(final Class<T> definition,
                                                                           final Disruptor<ProxyMethodInvocation> disruptor,
                                                                           final OverflowStrategy overflowStrategy)
    {
        final Map<Method, Invoker> methodToInvokerMap = createMethodToInvokerMap(definition);
        return new RingBufferInvocationHandler(disruptor.getRingBuffer(), methodToInvokerMap, overflowStrategy);
    }

    @SuppressWarnings("unchecked")
    private static <T> T generateProxy(final Class<T> definition, final RingBufferInvocationHandler invocationHandler)
    {
        return (T) newProxyInstance(currentThread().getContextClassLoader(), new Class<?>[]{definition}, invocationHandler);
    }

    private static void preallocateArgumentHolders(final RingBuffer<ProxyMethodInvocation> ringBuffer)
    {
        final int bufferSize = ringBuffer.getBufferSize();
        for(int i = 0; i < bufferSize; i++)
        {
            ringBuffer.get(i).setArgumentHolder(new ObjectArrayHolder());
        }
    }

    private static <T> Map<Method, Invoker> createMethodToInvokerMap(final Class<T> definition)
    {
        final Map<Method, Invoker> methodToInvokerMap = new ConcurrentHashMap<Method, Invoker>();
        final Method[] declaredMethods = definition.getDeclaredMethods();

        for (final Method declaredMethod : declaredMethods)
        {
            methodToInvokerMap.put(declaredMethod, new ReflectiveMethodInvoker(declaredMethod));
        }

        return methodToInvokerMap;
    }
}