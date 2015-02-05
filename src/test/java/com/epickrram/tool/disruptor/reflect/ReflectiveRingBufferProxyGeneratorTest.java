package com.epickrram.tool.disruptor.reflect;

import com.epickrram.tool.disruptor.AbstractRingBufferProxyGeneratorTest;
import com.epickrram.tool.disruptor.GeneratorType;

public final class ReflectiveRingBufferProxyGeneratorTest extends AbstractRingBufferProxyGeneratorTest
{
    public ReflectiveRingBufferProxyGeneratorTest()
    {
        super(GeneratorType.JDK_REFLECTION);
    }
}
