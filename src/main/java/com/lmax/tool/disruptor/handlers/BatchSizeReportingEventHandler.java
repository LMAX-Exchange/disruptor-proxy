package com.lmax.tool.disruptor.handlers;

import com.lmax.disruptor.EventHandler;
import com.lmax.tool.disruptor.BatchSizeListener;
import com.lmax.tool.disruptor.ProxyMethodInvocation;

class BatchSizeReportingEventHandler implements EventHandler<ProxyMethodInvocation>
{
    private final EventHandler<ProxyMethodInvocation> delegate;
    private final BatchSizeListener batchSizeListener;

    private int batchCounter;

    BatchSizeReportingEventHandler(final BatchSizeListener batchSizeListener, final EventHandler<ProxyMethodInvocation> delegate)
    {
        this.batchSizeListener = batchSizeListener;
        this.delegate = delegate;
    }

    @Override
    public void onEvent(final ProxyMethodInvocation event, final long sequence, final boolean endOfBatch) throws Exception
    {
        try
        {
            ++batchCounter;
            delegate.onEvent(event, sequence, endOfBatch);
        }
        finally
        {
            if (endOfBatch)
            {
                batchSizeListener.onEndOfBatch(batchCounter);
                batchCounter = 0;
            }
        }
    }
}
