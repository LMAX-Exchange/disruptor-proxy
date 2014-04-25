package com.epickrram.tool.disruptor;

public interface Invoker
{
    void invokeWithArgumentHolder(final Object implementation, final Object argumentHolder);
}
