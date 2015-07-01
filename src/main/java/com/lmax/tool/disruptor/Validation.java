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

public enum Validation
{
    VALIDATION;

    public void ensureDisruptorInstanceHasAnExceptionHandler(final Disruptor<?> disruptor)
    {
        try
        {
            final Field field = Disruptor.class.getDeclaredField("exceptionHandler");
            field.setAccessible(true);
            if(field.get(disruptor) == null)
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

    public void ensureDisruptorProxyIsAnnotatedWithDisruptorProxyAnnotation(final Class<?> disruptorProxyInterface)
    {
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
