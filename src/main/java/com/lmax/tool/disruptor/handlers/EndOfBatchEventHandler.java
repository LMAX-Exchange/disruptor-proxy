package com.lmax.tool.disruptor.handlers;

import com.lmax.disruptor.EventHandler;
import com.lmax.tool.disruptor.BatchListener;
import com.lmax.tool.disruptor.ProxyMethodInvocation;

/**
 * A Disruptor event handler that will notify the supplied batchListener at the end of a batch
 */
final class EndOfBatchEventHandler implements EventHandler<ProxyMethodInvocation>
{
    private final BatchListener batchListener;
    private final EventHandler<ProxyMethodInvocation> delegate;

    public EndOfBatchEventHandler(final BatchListener batchListener, final EventHandler<ProxyMethodInvocation> delegate)
    {
        this.batchListener = batchListener;
        this.delegate = delegate;
    }

    @Override
    public void onEvent(final ProxyMethodInvocation event, final long sequence, final boolean endOfBatch) throws Exception
    {
        delegate.onEvent(event, sequence, endOfBatch);

        if (endOfBatch)
        {
            batchListener.onEndOfBatch();
        }
    }
}
