package com.lmax.tool.disruptor;

import org.junit.Test;

public class RingBufferProxyGeneratorFactoryTest
{
    @Test
    public void shouldBeAbleToCreateRingBufferProxyGenerator() throws Exception
    {
        for (GeneratorType generatorType : GeneratorType.values())
        {
            new RingBufferProxyGeneratorFactory().create(generatorType);
        }
        // no exception is thrown
    }
}