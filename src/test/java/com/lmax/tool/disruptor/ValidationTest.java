package com.lmax.tool.disruptor;

import org.junit.Test;

public class ValidationTest
{
    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionWhenInterfaceIsNotAnnotatedWithDisruptorProxy() throws Exception
    {
        Validation.VALIDATION.ensureDisruptorProxyIsAnnotatedWithDisruptorProxyAnnotation(MyProxyWithoutAnnotation.class);
    }

    @Test
    public void shouldNotThrowExceptionWhenInterfaceIsAnnotatedWithDisruptorProxy() throws Exception
    {
        Validation.VALIDATION.ensureDisruptorProxyIsAnnotatedWithDisruptorProxyAnnotation(MyProxyWithAnnotation.class);
    }

    private interface MyProxyWithoutAnnotation
    {
    }

    @DisruptorProxy
    private interface MyProxyWithAnnotation
    {
    }

}