/*
 * Copyright 2015-2016 LMAX Ltd.
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

import java.lang.reflect.Constructor;

/**
 * A utility class to load a RingBufferProxyGenerator for the supplied type
 */
public final class RingBufferProxyGeneratorFactory
{
    /**
     * Creates a RingBufferProxyGenerator
     * @param generatorType the type of generator
     * @return the RingBufferProxyGenerator
     */
    public RingBufferProxyGenerator newProxy(final GeneratorType generatorType)
    {
        final ConfigurableValidator validateAsMuchAsPossibleValidator = new ConfigurableValidator(true, true);
        return newProxy(generatorType, validateAsMuchAsPossibleValidator);
    }

    /**
     * Creates a RingBufferProxyGenerator
     * @param generatorType the type of generator
     * @param config configure how much validation the ringBufferProxyGenerator should have
     * @return the RingBufferProxyGenerator
     */
    public RingBufferProxyGenerator newProxy(final GeneratorType generatorType, final ValidationConfig config)
    {
        return newProxy(generatorType, config, NoOpDropListener.INSTANCE);
    }

    /**
     * Creates a RingBufferProxyGenerator
     * @param generatorType the type of generator
     * @param config configure how much validation the ringBufferProxyGenerator should have
     * @param dropListener the supplied DropListener will be notified if the ring-buffer is full when OverflowStrategy is DROP
     * @return the RingBufferProxyGenerator
     */
    public RingBufferProxyGenerator newProxy(final GeneratorType generatorType, final ValidationConfig config, final DropListener dropListener)
    {
        try
        {
            final Class<?> clazz = Class.forName(generatorType.getGeneratorClassName());
            ConfigurableValidator validator = new ConfigurableValidator(config.validateProxyInterfaces(), config.validateExceptionHandler());
            final Constructor<?> constructorForRingBufferProxyGenerator = clazz.getConstructor(RingBufferProxyValidation.class, DropListener.class);
            return (RingBufferProxyGenerator) constructorForRingBufferProxyGenerator.newInstance(validator, dropListener);
        }
        catch (Exception e)
        {
            throw new IllegalStateException(String.format("Unable to instantiate generator %s",
                    generatorType.getGeneratorClassName()), e);
        }
    }

    /**
     * @deprecated prefer newProxy().
     *
     * This method is left to preserve the existing behaviour now configurable in ValidationConfig.
     *
     * @param generatorType the type of generator
     * @return the RingBufferProxyGenerator
     */
    @Deprecated
    public RingBufferProxyGenerator create(final GeneratorType generatorType)
    {
        final ConfigurableValidator backwardsCompatibleValidator = new ConfigurableValidator(false, true);
        return newProxy(generatorType, backwardsCompatibleValidator);
    }
}
