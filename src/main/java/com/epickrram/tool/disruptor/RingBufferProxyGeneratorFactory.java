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
