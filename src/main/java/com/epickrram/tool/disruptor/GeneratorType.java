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
