/*
 * Copyright 2015 LMAX Ltd.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package com.lmax.tool.disruptor;

import com.lmax.disruptor.dsl.Disruptor;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

public final class ConfigurableValidator implements RingBufferProxyValidation, ValidationConfig
{
    private final boolean validateProxyInterfaces;
    private final boolean validateExceptionHandler;

    public ConfigurableValidator(final boolean validateProxyInterfaces, final boolean validateExceptionHandler)
    {
        this.validateProxyInterfaces = validateProxyInterfaces;
        this.validateExceptionHandler = validateExceptionHandler;
    }

    public ConfigurableValidator(final ValidationConfig validationConfig)
    {
        this(validationConfig.validateProxyInterfaces(), validationConfig.validateExceptionHandler());
    }

    @Override
    public void validateAll(final Disruptor<?> disruptor, final Class<?> disruptorProxyInterface)
    {
        ensureThatProxyInterfaceIsAnInterface(disruptorProxyInterface);
        ensureDisruptorInstanceHasAnExceptionHandler(disruptor);
        ensureDisruptorProxyIsAnnotatedWithDisruptorProxyAnnotation(disruptorProxyInterface);
    }

    private void ensureThatProxyInterfaceIsAnInterface(final Class<?> disruptorProxyInterface)
    {
        if(!disruptorProxyInterface.isInterface())
        {
            throw new IllegalArgumentException("Not an interface: " + disruptorProxyInterface);
        }
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

    @Override
    public boolean validateProxyInterfaces()
    {
        return validateProxyInterfaces;
    }

    @Override
    public boolean validateExceptionHandler()
    {
        return validateExceptionHandler;
    }
}
