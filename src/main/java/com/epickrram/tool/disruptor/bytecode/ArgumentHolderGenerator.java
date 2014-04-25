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


import com.epickrram.tool.disruptor.Resetable;
import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtNewConstructor;

import java.util.HashMap;
import java.util.Map;

import static com.epickrram.tool.disruptor.bytecode.ByteCodeHelper.*;

public final class ArgumentHolderGenerator
{
    private final ArgumentHolderHelper helper = new ArgumentHolderHelper();
    private final ClassPool classPool;

    private String generatedClassName;
    private Class<?> generatedClass;
    private Map<Class<?>,Integer> parameterTypeCounts;
    private Map<Class<?>, Character> parameterFieldSuffix = new HashMap<Class<?>, Character>();

    public ArgumentHolderGenerator(final ClassPool classPool)
    {
        this.classPool = classPool;
    }

    public void createArgumentHolderClass(final Class<?> type)
    {
        final CtClass ctClass = makeClass(classPool, "_argumentHolder_" + type.getSimpleName() + "_" + getUniqueIdentifier());

        parameterTypeCounts = helper.getParameterTypeCounts(type);
        createFields(ctClass);
        createMethod(ctClass, generateResetMethod());
        addInterface(ctClass, Resetable.class, classPool);

        try
        {
            ctClass.addConstructor(CtNewConstructor.defaultConstructor(ctClass));
            makePublicFinal(ctClass);
            generatedClass = ctClass.toClass();
        }
        catch (CannotCompileException e)
        {
            throw new RuntimeException("Cannot generate argument holder object", e);
        }

        generatedClassName = ctClass.getName();

    }

    public void resetFieldNames()
    {
        parameterFieldSuffix.clear();
        for (Class<?> parameterType : parameterTypeCounts.keySet())
        {
            parameterFieldSuffix.put(parameterType, 'a');
        }
    }

    public String getNextFieldNameForType(final Class<?> parameterType)
    {
        final Character suffix = parameterFieldSuffix.get(parameterType);
        parameterFieldSuffix.put(parameterType, (char) (suffix + 1));
        return getSanitisedFieldName(parameterType) + "_" + suffix;
    }

    public String getGeneratedClassName()
    {
        return generatedClassName;
    }

    public Class<?> getGeneratedClass()
    {
        return generatedClass;
    }

    private void createFields(final CtClass ctClass)
    {
        for (Map.Entry<Class<?>, Integer> entry : parameterTypeCounts.entrySet())
        {
            final StringBuilder buffer = new StringBuilder();
            final char suffix = 'a';
            final int parameterCount = entry.getValue();

            final Class<?> parameterType = entry.getKey();
            final String parameterTypeName = sanitiseParameterType(parameterType);
            for(int i = 0; i < parameterCount; i++)
            {
                buffer.setLength(0);
                buffer.append("public ").append(parameterTypeName).append(' ');
                buffer.append(sanitiseParameterName(parameterTypeName));
                buffer.append('_').append((char) (suffix + i));
                buffer.append(";\n");
                createField(ctClass, buffer.toString());
            }
        }
    }

    private String generateResetMethod()
    {
        final StringBuilder buffer = new StringBuilder();

        buffer.append("public void reset() {\n");
        for (Map.Entry<Class<?>, Integer> entry : parameterTypeCounts.entrySet())
        {
            final char suffix = 'a';
            final int parameterCount = entry.getValue();

            final Class<?> parameterType = entry.getKey();
            if(!parameterType.isPrimitive())
            {
                for(int i = 0; i < parameterCount; i++)
                {
                    buffer.append(getSanitisedFieldName(parameterType)).append("_").
                            append((char) (suffix + i)).append(" = null;\n");
                }
            }
        }
        buffer.append("}\n");

        return buffer.toString();
    }

    private static String getSanitisedFieldName(final Class<?> parameterType)
    {
        return sanitiseParameterName(sanitiseParameterType(parameterType));
    }

    private static String sanitiseParameterName(final String parameterTypeName)
    {
        return parameterTypeName.replace('[','_').replace(']', '_').replace('.', '_');
    }

    private static String sanitiseParameterType(final Class<?> parameterType)
    {
        if(parameterType.isArray())
        {
            return parameterType.getComponentType().getName() + "[]";
        }

        return parameterType.getName();
    }
}
