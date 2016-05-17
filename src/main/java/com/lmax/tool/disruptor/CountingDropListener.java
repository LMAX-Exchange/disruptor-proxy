package com.lmax.tool.disruptor;

public final class CountingDropListener implements DropListener
{
    private int dropCount = 0;

    @Override
    public void onDrop()
    {
        dropCount++;
    }

    public int getDropCount()
    {
        return dropCount;
    }
}
