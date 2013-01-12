//////////////////////////////////////////////////////////////////////////////////
//   Copyright 2011   Mark Price     mark at epickrram.com                      //
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


package com.epickrram.tool.disruptor.bytecode;

public final class Primitives
{
    public static String getWrapperClassName(final Class<?> parameterType)
    {
        if(parameterType == byte.class)
        {
            return Byte.class.getName();
        }
        else if(parameterType == short.class)
        {
            return Short.class.getName();
        }
        else if(parameterType == int.class)
        {
            return Integer.class.getName();
        }
        else if(parameterType == long.class)
        {
            return Long.class.getName();
        }
        else if(parameterType == boolean.class)
        {
            return Boolean.class.getName();
        }
        else if(parameterType == float.class)
        {
            return Float.class.getName();
        }
        else if(parameterType == double.class)
        {
            return Double.class.getName();
        }
        else if(parameterType == char.class)
        {
            return Character.class.getName();
        }
        throw new IllegalArgumentException("Unknown primitive type: " + parameterType);
    }
}
