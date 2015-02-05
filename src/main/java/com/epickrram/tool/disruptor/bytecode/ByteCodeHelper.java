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

package com.epickrram.tool.disruptor.bytecode;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtMethod;
import javassist.Modifier;
import javassist.NotFoundException;

import java.util.concurrent.atomic.AtomicInteger;

enum ByteCodeHelper
{
    INSTANCE;

    private static final AtomicInteger UNIQUE_GENERATED_CLASS_NAME_COUNTER = new AtomicInteger();
    static final boolean DEBUG = false;

    static int getUniqueIdentifier()
    {
        return UNIQUE_GENERATED_CLASS_NAME_COUNTER.incrementAndGet();
    }

    static CtClass makeClass(final ClassPool classPool, final String className)
    {
        if(DEBUG)
        {
            System.out.println("Making class " + className);
        }

        return classPool.makeClass(className);
    }

    static void createMethod(final CtClass ctClass, final String methodSrc)
    {
        if(DEBUG)
        {
            System.out.println("Creating method for class " + ctClass.getName());
            System.out.println(methodSrc);
        }
        try
        {
            ctClass.addMethod(CtMethod.make(methodSrc, ctClass));
        }
        catch (CannotCompileException e)
        {
            throw new RuntimeException("Unable to compile class", e);
        }
    }

    static void createField(final CtClass ctClass, final String fieldSrc)
    {
        if(DEBUG)
        {
            System.out.println("Creating field for class " + ctClass.getName());
            System.out.println(fieldSrc);
        }
        try
        {
            ctClass.addField(CtField.make(fieldSrc, ctClass));
        }
        catch (CannotCompileException e)
        {
            throw new RuntimeException("Unable to generate field: " + fieldSrc, e);
        }
    }

    static void makePublicFinal(final CtClass ctClass)
    {
        ctClass.setModifiers(Modifier.PUBLIC | Modifier.FINAL);
    }

    static void addInterface(final CtClass ctClass, final Class<?> interfaceClass,
                             final ClassPool classPool)
    {
        try
        {
            ctClass.addInterface(classPool.get(interfaceClass.getName()));
        }
        catch (NotFoundException e)
        {
            throw new RuntimeException("Cannot load class: " + interfaceClass.getName(), e);
        }
    }
}
