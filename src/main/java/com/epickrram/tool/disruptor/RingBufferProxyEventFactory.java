package com.epickrram.tool.disruptor;

import com.lmax.disruptor.EventFactory;

public final class RingBufferProxyEventFactory implements EventFactory<ProxyMethodInvocation>
{
    public static final EventFactory<ProxyMethodInvocation> FACTORY = new RingBufferProxyEventFactory();

    @Override
    public ProxyMethodInvocation newInstance()
    {
        return new ProxyMethodInvocation();
    }
}
