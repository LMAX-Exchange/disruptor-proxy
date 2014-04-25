package com.epickrram.tool.disruptor;

import com.lmax.disruptor.EventHandler;

public final class InvokerEventHandler<T> implements EventHandler<ProxyMethodInvocation>
{
    private final T implementation;
    private final boolean isBatchListener;

    public InvokerEventHandler(final T implementation)
    {
        this.implementation = implementation;
        this.isBatchListener = implementation instanceof BatchListener;
    }

    @Override
    public void onEvent(final ProxyMethodInvocation event, final long sequence, final boolean endOfBatch) throws Exception
    {
        event.getInvoker().invokeWithArgumentHolder(implementation, event.getArgumentHolder());
        event.reset();

        if (isBatchListener && endOfBatch)
        {
            BatchListener batchListener = (BatchListener) implementation;
            batchListener.onEndOfBatch();
        }
    }
}
