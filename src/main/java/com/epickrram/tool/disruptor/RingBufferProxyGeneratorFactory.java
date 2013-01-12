package com.epickrram.tool.disruptor;

public final class RingBufferProxyGeneratorFactory
{
    public RingBufferProxyGenerator create(final GeneratorType generatorType)
    {
        try
        {
            return (RingBufferProxyGenerator) Class.forName(generatorType.getGeneratorClassName()).newInstance();
        }
        catch (Exception e)
        {
            throw new IllegalStateException(String.format("Unable to instantiate generator %s",
                    generatorType.getGeneratorClassName()), e);
        }
    }
}
