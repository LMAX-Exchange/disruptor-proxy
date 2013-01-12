package com.epickrram.tool.disruptor;

import com.lmax.disruptor.dsl.Disruptor;

public interface RingBufferProxyGenerator
{
    <T> T createRingBufferProxy(final T implementation, final Class<T> definition, final Disruptor<ProxyMethodInvocation> disruptor);
}
