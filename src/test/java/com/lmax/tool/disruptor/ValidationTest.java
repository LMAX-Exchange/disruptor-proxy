package com.lmax.tool.disruptor;

import org.junit.Test;

public class ValidationTest
{
    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionWhenInterfaceIsNotAnnotatedWithDisruptorProxy() throws Exception
    {
        new ConfigurableValidator(true, false).ensureDisruptorProxyIsAnnotatedWithDisruptorProxyAnnotation(MyProxyWithoutAnnotation.class);
    }

    @Test
    public void shouldNotThrowExceptionWhenInterfaceIsAnnotatedWithDisruptorProxy() throws Exception
    {
        new ConfigurableValidator(true, false).ensureDisruptorProxyIsAnnotatedWithDisruptorProxyAnnotation(MyProxyWithAnnotation.class);
    }

    private interface MyProxyWithoutAnnotation
    {
    }

    @DisruptorProxy
    private interface MyProxyWithAnnotation
    {
    }

}