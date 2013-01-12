package com.epickrram.tool.disruptor;

public enum GeneratorType
{
    JDK_REFLECTION("com.epickrram.tool.disruptor.reflect.ReflectiveRingBufferProxyGenerator"),
    BYTECODE_GENERATION("com.epickrram.tool.disruptor.bytecode.GeneratedRingBufferProxyGenerator");

    private final String generatorClassname;

    private GeneratorType(final String generatorClassName)
    {
        this.generatorClassname = generatorClassName;
    }

    public String getGeneratorClassName()
    {
        return generatorClassname;
    }
}
