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

package com.lmax.tool.disruptor;

import com.lmax.disruptor.dsl.Disruptor;

/**
 * Creates an implementation of the specified interface, backed by a Disruptor instance
 */
public interface RingBufferProxyGenerator
{
    /**
     * Create a disruptor proxy with a single implementation instance
     *
     * @param <T> the type of the implementation
     * @param definition the type of the implementation
     * @param disruptor a disruptor instance
     * @param overflowStrategy an indicator of what action should be taken when the ring-buffer is full
     * @param implementation the implementation object to be invoked by the Disruptor event handler
     * @return an implementation of type T
     */
    <T> T createRingBufferProxy(final Class<T> definition, final Disruptor<ProxyMethodInvocation> disruptor,
                                final OverflowStrategy overflowStrategy, final T implementation);

    /**
     * Create a disruptor proxy with multiple implementation instances
     *
     * @param definition the type of the implementation
     * @param disruptor a disruptor instance
     * @param overflowStrategy an indicator of what action should be taken when the ring-buffer is full
     * @param implementations the implementation objects to be invoked by the Disruptor event handler (each on its own Thread)
     * @param <T> the type of the implementation
     * @return an implementation of type T
     */
    @SuppressWarnings("varargs")
    <T> T createRingBufferProxy(final Class<T> definition, final Disruptor<ProxyMethodInvocation> disruptor,
                                final OverflowStrategy overflowStrategy, final T... implementations);
}
