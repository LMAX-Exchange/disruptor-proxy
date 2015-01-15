package com.epickrram.tool.disruptor;

import com.lmax.disruptor.EventHandler;

public final class InvokerEventHandler<T> implements EventHandler<ProxyMethodInvocation>
{
    private final T implementation;
    private final boolean isBatchListener;
    private final boolean reset;

    public InvokerEventHandler(final T implementation, boolean reset)
    {
        this.implementation = implementation;
        this.reset = reset;
        this.isBatchListener = implementation instanceof BatchListener;
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

        if (isBatchListener && endOfBatch)
        {
            BatchListener batchListener = (BatchListener) implementation;
            batchListener.onEndOfBatch();
        }
    }
}
