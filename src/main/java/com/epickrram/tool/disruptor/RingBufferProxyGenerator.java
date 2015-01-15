package com.epickrram.tool.disruptor;

import com.lmax.disruptor.dsl.Disruptor;

public interface RingBufferProxyGenerator
{
    <T> T createRingBufferProxy(final T implementation, final Class<T> definition,
                                final Disruptor<ProxyMethodInvocation> disruptor, final OverflowStrategy overflowStrategy);

    @SuppressWarnings("varargs")
    <T> T createRingBufferProxy(final Class<T> definition, final Disruptor<ProxyMethodInvocation> disruptor,
                                final OverflowStrategy overflowStrategy, final T... implementations);
}
