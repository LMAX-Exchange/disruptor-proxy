package com.lmax.tool.disruptor;

import com.lmax.disruptor.dsl.Disruptor;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

public class ConfigurableValidator implements Validation
{
    private final boolean validateProxyInterfaces;
    private final boolean validateExceptionHandler;

    public ConfigurableValidator(boolean validateProxyInterfaces, boolean validateExceptionHandler)
    {
        this.validateProxyInterfaces = validateProxyInterfaces;
        this.validateExceptionHandler = validateExceptionHandler;
    }

    @Override
    public void validateAll(final Disruptor<?> disruptor, final Class<?> disruptorProxyInterface)
    {
        ensureDisruptorInstanceHasAnExceptionHandler(disruptor);
        ensureDisruptorProxyIsAnnotatedWithDisruptorProxyAnnotation(disruptorProxyInterface);
    }

    private void ensureDisruptorInstanceHasAnExceptionHandler(final Disruptor<?> disruptor)
    {
        if (!validateExceptionHandler)
        {
            return;
        }
        try
        {
            final Field field = Disruptor.class.getDeclaredField("exceptionHandler");
            field.setAccessible(true);
            if (field.get(disruptor) == null)
            {
                throw new IllegalStateException("Please supply an ExceptionHandler to the Disruptor instance. " +
                        "The default Disruptor behaviour is to stop processing when an exception occurs.");
            }
        }
        catch (NoSuchFieldException e)
        {
            throw new RuntimeException("Unable to inspect Disruptor instance", e);
        }
        catch (IllegalAccessException e)
        {
            throw new RuntimeException("Unable to inspect Disruptor instance", e);
        }
    }

    // visible for testing
    void ensureDisruptorProxyIsAnnotatedWithDisruptorProxyAnnotation(final Class<?> disruptorProxyInterface)
    {
        if (!validateProxyInterfaces)
        {
            return;
        }
        for (Annotation annotation : disruptorProxyInterface.getAnnotations())
        {
            if (annotation instanceof DisruptorProxy)
            {
                return;
            }
        }
        throw new IllegalArgumentException("Please supply a disruptor proxy interface that is annotated with " + DisruptorProxy.class);
    }
}
