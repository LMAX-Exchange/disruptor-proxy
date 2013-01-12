package com.epickrram.tool.disruptor;

public interface Invoker
{
    void invoke(final Object implementation, final Object[] args);
}
