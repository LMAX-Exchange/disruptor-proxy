package com.epickrram.tool.disruptor.bytecode;

//////////////////////////////////////////////////////////////////////////////////
//   Copyright 2014   Mark Price     mark at epickrram.com                      //
//                                                                              //
//   Licensed under the Apache License, Version 2.0 (the "License");            //
//   you may not use this file except in compliance with the License.           //
//   You may obtain a copy of the License at                                    //
//                                                                              //
//       http://www.apache.org/licenses/LICENSE-2.0                             //
//                                                                              //
//   Unless required by applicable law or agreed to in writing, software        //
//   distributed under the License is distributed on an "AS IS" BASIS,          //
//   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.   //
//   See the License for the specific language governing permissions and        //
//   limitations under the License.                                             //
//////////////////////////////////////////////////////////////////////////////////


import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import static java.lang.Math.max;

public final class ArgumentHolderHelper
{
    public Map<Class<?>, Integer> getParameterTypeCounts(final Class<?> type)
    {
        final Method[] methods = type.getDeclaredMethods();
        final Map<Class<?>, Integer> parameterTypeCounts = new HashMap<Class<?>, Integer>();
        for (Method method : methods)
        {
            final Map<Class<?>, Integer> methodTypeCounts = new HashMap<Class<?>, Integer>();
            final Class<?>[] parameterTypes = method.getParameterTypes();
            for (Class<?> parameterType : parameterTypes)
            {
                ensureTypeCountExists(methodTypeCounts, parameterType);

                methodTypeCounts.put(parameterType, methodTypeCounts.get(parameterType) + 1);
            }

            for (Class<?> parameterType : methodTypeCounts.keySet())
            {
                if(parameterTypeCounts.containsKey(parameterType))
                {
                    parameterTypeCounts.put(parameterType,
                            max(parameterTypeCounts.get(parameterType),
                                    methodTypeCounts.get(parameterType)));
                }
                else
                {
                    parameterTypeCounts.put(parameterType, methodTypeCounts.get(parameterType));
                }
            }
        }
        return parameterTypeCounts;
    }

    private void ensureTypeCountExists(final Map<Class<?>, Integer> parameterTypeCounts, final Class<?> parameterType)
    {
        if(!parameterTypeCounts.containsKey(parameterType))
        {
            parameterTypeCounts.put(parameterType, 0);
        }
    }
}