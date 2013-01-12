package com.epickrram.tool.disruptor;

import com.lmax.disruptor.EventHandler;

public final class InvokerEventHandler<T> implements EventHandler<ProxyMethodInvocation>
{
    private final T implementation;

    public InvokerEventHandler(final T implementation)
    {
        this.implementation = implementation;
    }

    @Override
    public void onEvent(final ProxyMethodInvocation event, final long sequence, final boolean endOfBatch) throws Exception
    {
        event.getInvoker().invoke(implementation, event.getArguments());
        event.reset();
    }
}
