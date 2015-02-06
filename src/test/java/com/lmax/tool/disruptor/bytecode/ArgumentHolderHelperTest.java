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

package com.lmax.tool.disruptor.bytecode;

import org.junit.Test;

import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class ArgumentHolderHelperTest
{
    private final ArgumentHolderHelper argumentHolderHelper = new ArgumentHolderHelper();

    @Test
    public void shouldDetermineParameterTypeCounts() throws Exception
    {
        final Map<Class<?>, Integer> parameterTypeCounts =
                argumentHolderHelper.getParameterTypeCounts(TestInterface.class);

        assertThat(parameterTypeCounts.get(String.class), is(1));
        assertThat(parameterTypeCounts.get(int.class), is(3));
        assertThat(parameterTypeCounts.get(Double.class), is(1));
        assertThat(parameterTypeCounts.get(double.class), is(1));
    }

    private interface TestInterface
    {
        void one(final String s);
        void two(final String s, final int i);
        void three(final int i0, final int i1, final int i3);
        void four(final Double d0, final double d1);
    }
}