package com.epickrram.tool.disruptor;

public interface Invoker
{
    void invoke(final Object implementation, final Object[] args);
    void invokeWithArgumentHolder(final Object implementation, final Object argumentHolder);
}
