package com.epickrram.tool.disruptor.reflect;

import com.epickrram.tool.disruptor.*;

public final class ReflectiveRingBufferProxyGeneratorTest extends AbstractRingBufferProxyGeneratorTest
{
    public ReflectiveRingBufferProxyGeneratorTest()
    {
        super(GeneratorType.JDK_REFLECTION);
    }
}
