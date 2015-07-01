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
    public RingBufferProxyGenerator create(final GeneratorType generatorType)
    {
        final ConfigurableValidator backwardsCompatibleValidator = new ConfigurableValidator(false, true);
        return create(generatorType, backwardsCompatibleValidator);
    }

    /**
     * Creates a RingBufferProxyGenerator
     * @param generatorType the type of generator
     * @param validator configure how much validation the ringBufferProxyGenerator should have
     * @return the RingBufferProxyGenerator
     */
    public RingBufferProxyGenerator create(final GeneratorType generatorType, final ConfigurableValidator validator)
    {
        try
        {
            final Class<?> clazz = Class.forName(generatorType.getGeneratorClassName());
            final Constructor<?> constructorForRingBufferProxyGenerator = clazz.getConstructor(ConfigurableValidator.class);
            return (RingBufferProxyGenerator) constructorForRingBufferProxyGenerator.newInstance(validator);
        }
        catch (Exception e)
        {
            throw new IllegalStateException(String.format("Unable to instantiate generator %s",
                    generatorType.getGeneratorClassName()), e);
        }
    }
}
