package com.epickrram.tool.disruptor;

public final class BatchAwareListenerImpl implements Listener, BatchListener
{
    private volatile int batchCount = 0;

    @Override
    public void onString(final String value)
    {
    }

    @Override
    public void onFloatAndInt(final Float floatValue, final int intValue)
    {
    }

    @Override
    public void onVoid()
    {
    }

    @Override
    public void onObjectArray(final Double[] value)
    {
    }

    public int getBatchCount()
    {
        return batchCount;
    }

    @Override
    public void onEndOfBatch()
    {
        batchCount++;
    }
}
