package com.lmax.tool.disruptor;

/**
 * Default implementation of DropListener
 */
public enum NoOpDropListener implements DropListener
{
    INSTANCE;

    @Override
    public void onDrop()
    {
        // no-op
    }
}
