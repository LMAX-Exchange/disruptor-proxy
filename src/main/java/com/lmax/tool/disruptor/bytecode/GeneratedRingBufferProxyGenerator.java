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

import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.tool.disruptor.Invoker;
import com.lmax.tool.disruptor.InvokerEventHandler;
import com.lmax.tool.disruptor.OverflowStrategy;
import com.lmax.tool.disruptor.ProxyMethodInvocation;
import com.lmax.tool.disruptor.ResetHandler;
import com.lmax.tool.disruptor.Resetable;
import com.lmax.tool.disruptor.RingBufferProxyGenerator;
import com.lmax.tool.disruptor.RingBufferProxyValidation;
import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtMethod;
import javassist.CtNewConstructor;
import javassist.LoaderClassPath;
import javassist.NotFoundException;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import static com.lmax.tool.disruptor.bytecode.ByteCodeHelper.addInterface;
import static com.lmax.tool.disruptor.bytecode.ByteCodeHelper.createField;
import static com.lmax.tool.disruptor.bytecode.ByteCodeHelper.createMethod;
import static com.lmax.tool.disruptor.bytecode.ByteCodeHelper.getUniqueIdentifier;
import static com.lmax.tool.disruptor.bytecode.ByteCodeHelper.makeClass;
import static com.lmax.tool.disruptor.bytecode.ByteCodeHelper.makePublicFinal;

/**
 * {@inheritDoc}
 */
public final class GeneratedRingBufferProxyGenerator implements RingBufferProxyGenerator
{
    private final ClassPool classPool;
    private final RingBufferProxyValidation validator;

    public GeneratedRingBufferProxyGenerator(final RingBufferProxyValidation validator)
    {
        this.validator = validator;
        classPool = configureClassPool();
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    @Override
    public <T> T createRingBufferProxy(final Class<T> proxyInterface, final Disruptor<ProxyMethodInvocation> disruptor, final OverflowStrategy overflowStrategy, final T implementation)
    {
        validator.validateAll(disruptor, proxyInterface);

        disruptor.handleEventsWith(new InvokerEventHandler<T>(implementation));

        final ArgumentHolderGenerator argumentHolderGenerator = new ArgumentHolderGenerator(classPool);
        argumentHolderGenerator.createArgumentHolderClass(proxyInterface);

        prefillArgumentHolderObjects(disruptor.getRingBuffer(), argumentHolderGenerator);

        final Map<Method, Invoker> methodToInvokerMap = createMethodToInvokerMap(proxyInterface, argumentHolderGenerator);

        return generateProxy(proxyInterface, disruptor.getRingBuffer(), methodToInvokerMap, overflowStrategy, argumentHolderGenerator);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T> T createRingBufferProxy(final Class<T> proxyInterface, final Disruptor<ProxyMethodInvocation> disruptor,
                                       final OverflowStrategy overflowStrategy, final T... implementations)
    {
        validator.validateAll(disruptor, proxyInterface);

        if (implementations.length < 1)
        {
            throw new IllegalArgumentException("Must have at least one implementation");
        }
        else if (implementations.length == 1)
        {
            return createRingBufferProxy(proxyInterface, disruptor, overflowStrategy, implementations[0]);
        }

        final InvokerEventHandler<T>[] handlers = new InvokerEventHandler[implementations.length];
        for (int i = 0; i < implementations.length; i++)
        {
            handlers[i] = new InvokerEventHandler<T>(implementations[i], false);
            disruptor.handleEventsWith(handlers[i]);
        }
        disruptor.after(handlers).then(new ResetHandler());

        final ArgumentHolderGenerator argumentHolderGenerator = new ArgumentHolderGenerator(classPool);
        argumentHolderGenerator.createArgumentHolderClass(proxyInterface);

        prefillArgumentHolderObjects(disruptor.getRingBuffer(), argumentHolderGenerator);

        final Map<Method, Invoker> methodToInvokerMap = createMethodToInvokerMap(proxyInterface, argumentHolderGenerator);

        return generateProxy(proxyInterface, disruptor.getRingBuffer(), methodToInvokerMap, overflowStrategy, argumentHolderGenerator);
    }

    private void prefillArgumentHolderObjects(final RingBuffer<ProxyMethodInvocation> ringBuffer,
                                              final ArgumentHolderGenerator argumentHolderGenerator)
    {
        final int bufferSize = ringBuffer.getBufferSize();
        for(int i = 0; i < bufferSize; i++)
        {
            ringBuffer.get(i).setArgumentHolder((Resetable) instantiate(argumentHolderGenerator.getGeneratedClass(), new Class[] {}));
        }
    }

    private <T> T generateProxy(final Class<T> proxyInterface, final RingBuffer<ProxyMethodInvocation> ringBuffer,
                                final Map<Method, Invoker> methodToInvokerMap, final OverflowStrategy overflowStrategy,
                                final ArgumentHolderGenerator argumentHolderGenerator)
    {
        final CtClass ctClass = makeClass(classPool, "_proxy" + proxyInterface.getSimpleName() + '_' +
                getUniqueIdentifier());

        addInterface(ctClass, proxyInterface, classPool);
        makePublicFinal(ctClass);

        createFields(methodToInvokerMap, ctClass);
        createConstructor(ctClass);

        for (final Method method : proxyInterface.getDeclaredMethods())
        {
            createRingBufferPublisherMethod(ctClass, method, methodToInvokerMap.get(method), overflowStrategy, argumentHolderGenerator);
        }

        return instantiateProxy(ctClass, ringBuffer);
    }

    private <T> T instantiateProxy(final CtClass ctClass, final RingBuffer<ProxyMethodInvocation> ringBuffer)
    {
        try
        {
            return instantiate(ctClass.toClass(), new Class[]{RingBuffer.class}, ringBuffer);
        }
        catch (CannotCompileException e)
        {
            throw new RuntimeException("Unable to compile class", e);
        }
    }

    @SuppressWarnings("unchecked")
    private <T> T instantiate(final Class generatedClass, final Class[] constructorArgumentTypes,
                              final Object... args)
    {
        try
        {

            final Constructor jdkConstructor = generatedClass.getConstructor(constructorArgumentTypes);
            return (T) jdkConstructor.newInstance(args);
        }
        catch (NoSuchMethodException e)
        {
            throw new RuntimeException("Unable to instantiate class", e);
        }
        catch (InstantiationException e)
        {
            throw new RuntimeException("Unable to instantiate class", e);
        }
        catch (IllegalAccessException e)
        {
            throw new RuntimeException("Unable to instantiate class", e);
        }
        catch (InvocationTargetException e)
        {
            throw new RuntimeException("Unable to instantiate class", e);
        }
    }

    private void createConstructor(final CtClass ctClass)
    {
        try
        {
            final CtConstructor ctConstructor = new CtConstructor(new CtClass[]{classPool.getCtClass(RingBuffer.class.getName())}, ctClass);
            ctConstructor.setBody("{ringBuffer = $1;}");
            ctClass.addConstructor(ctConstructor);
        }
        catch (NotFoundException e)
        {
            throw new RuntimeException("Unable to create constructor", e);
        }
        catch (CannotCompileException e)
        {
            throw new RuntimeException("Unable to create constructor", e);
        }
    }

    private void createFields(final Map<Method, Invoker> methodToInvokerMap, final CtClass ctClass)
    {
        createField(ctClass, "private final " + RingBuffer.class.getName() + " ringBuffer;");

        for (final Method method : methodToInvokerMap.keySet())
        {
            final Invoker invoker = methodToInvokerMap.get(method);

            createField(ctClass, "private final " + invoker.getClass().getName() + " _" +
                    invoker.getClass().getName() + " = new " + invoker.getClass().getName() + "();");
        }
    }

    private void createRingBufferPublisherMethod(final CtClass ctClass, final Method method, final Invoker invoker,
                                                 final OverflowStrategy overflowStrategy,
                                                 final ArgumentHolderGenerator argumentHolderGenerator)
    {
        final StringBuilder methodSrc = new StringBuilder("public void ").append(method.getName()).append("(");
        final Class<?>[] parameterTypes = method.getParameterTypes();
        char paramId = 'a';
        for (int i = 0, parameterTypesLength = parameterTypes.length; i < parameterTypesLength; i++)
        {
            final Class<?> parameterType = parameterTypes[i];

            if (parameterType.isArray())
            {
                methodSrc.append(parameterType.getComponentType().getName()).append("[] ").append(paramId++);
            }
            else
            {
                methodSrc.append(parameterType.getName()).append(' ').append(paramId++);
            }

            if (i < parameterTypesLength - 1)
            {
                methodSrc.append(", ");
            }
        }
        methodSrc.append(")\n{\n");

        handleOverflowStrategy(overflowStrategy, methodSrc);

        methodSrc.append("final long sequence = ringBuffer.next();\n").append("try\n").
                append("{\n").
                append("final ProxyMethodInvocation proxyMethodInvocation = (ProxyMethodInvocation) ringBuffer.get(sequence);\n");

        final String argumentHolderClass = argumentHolderGenerator.getGeneratedClassName();

        methodSrc.append("final ").append(argumentHolderClass).append(" holder = (").
                append(argumentHolderClass).append(") proxyMethodInvocation.getArgumentHolder();\n");

        argumentHolderGenerator.resetFieldNames();

        for (int i = 0; i < parameterTypes.length; i++)
        {
            final Class<?> parameterType = parameterTypes[i];
            final String holderField = argumentHolderGenerator.getNextFieldNameForType(parameterType);
            methodSrc.append("holder.").append(holderField).append(" = ").append((char) ('a' + i)).append(";");
        }

        methodSrc.append("proxyMethodInvocation.setInvoker(_").append(invoker.getClass().getName()).
                append(");\n").
                append("}\n").
                append("catch(Throwable t){t.printStackTrace();}\n").
                append("finally\n").
                append("{\n").
                append("ringBuffer.publish(sequence);\n").
                append("}\n");
        methodSrc.append("}\n");

        createMethod(ctClass, methodSrc.toString());
    }

    private void handleOverflowStrategy(final OverflowStrategy overflowStrategy, final StringBuilder methodSrc)
    {
        if (overflowStrategy == OverflowStrategy.DROP)
        {
            methodSrc.append("if(!ringBuffer.hasAvailableCapacity(1))\n");
            methodSrc.append("{");
            methodSrc.append("return;\n");
            methodSrc.append("}\n");
        }
    }

    @SuppressWarnings("unchecked")
    private <T> Invoker generateInvoker(final Class<T> proxyInterface, final Method method, final ArgumentHolderGenerator argumentHolderGenerator)
    {
        final StringBuilder invokerClassName = new StringBuilder("_invoker").append(proxyInterface.getSimpleName()).
                append(method.getName()).append('_').append(getUniqueIdentifier());

        final Class<?>[] parameterTypes = method.getParameterTypes();
        for (final Class<?> paramType : parameterTypes)
        {
            if (paramType.isArray())
            {
                invokerClassName.append(paramType.getComponentType().getSimpleName()).append("Array");
            }
            else
            {
                invokerClassName.append(paramType.getSimpleName());
            }
        }

        final CtClass ctClass = makeClass(classPool, invokerClassName.toString());
        addInterface(ctClass, Invoker.class, classPool);
        makePublicFinal(ctClass);

        final StringBuilder methodSrc = new StringBuilder("public void invokeWithArgumentHolder(").append("Object").
                append(" implementation, Object argumentHolder) {\n");

        if (parameterTypes.length != 0)
        {
            methodSrc.append("final ").append(argumentHolderGenerator.getGeneratedClassName()).append(" holder = ").
                    append("(").append(argumentHolderGenerator.getGeneratedClassName()).append(") argumentHolder;\n");
        }

        methodSrc.append("((").append(proxyInterface.getName().replace('$', '.')).
                append(")implementation).").append(method.getName()).append('(');


        appendParametersFromArgumentHolder(parameterTypes, methodSrc, argumentHolderGenerator);

        methodSrc.append(");\n}\n");

        return generateInvoker(ctClass, methodSrc);
    }

    private Invoker generateInvoker(final CtClass ctClass, final StringBuilder methodSrc)
    {
        try
        {
            ctClass.addMethod(CtMethod.make(methodSrc.toString(), ctClass));
            ctClass.addConstructor(CtNewConstructor.defaultConstructor(ctClass));
            final Class generatedClass = ctClass.toClass();
            return (Invoker) generatedClass.newInstance();
        }
        catch (CannotCompileException e)
        {
            throw new RuntimeException("Unable to compile generated source: " + methodSrc, e);
        }
        catch (InstantiationException e)
        {
            throw new RuntimeException("Unable to compile generated source: " + methodSrc, e);
        }
        catch (IllegalAccessException e)
        {
            throw new RuntimeException("Unable to compile generated source: " + methodSrc, e);
        }
    }

    private <T> Map<Method, Invoker> createMethodToInvokerMap(final Class<T> proxyInterface,
                                                              final ArgumentHolderGenerator argumentHolderGenerator)
    {
        final Map<Method, Invoker> methodToInvokerMap = new HashMap<Method, Invoker>();

        final Method[] declaredMethods = proxyInterface.getDeclaredMethods();

        for (Method declaredMethod : declaredMethods)
        {
            methodToInvokerMap.put(declaredMethod, generateInvoker(proxyInterface, declaredMethod, argumentHolderGenerator));
        }

        return methodToInvokerMap;
    }

    private void appendParametersFromArgumentHolder(final Class<?>[] parameterTypes, final StringBuilder methodSrc,
                                                    final ArgumentHolderGenerator argumentHolderGenerator)
    {
        argumentHolderGenerator.resetFieldNames();
        for (int i = 0; i < parameterTypes.length; i++)
        {
            final Class<?> parameterType = parameterTypes[i];
            methodSrc.append("holder.").append(argumentHolderGenerator.getNextFieldNameForType(parameterType));

            if (i < parameterTypes.length - 1)
            {
                methodSrc.append(", ");
            }
        }
    }

    private ClassPool configureClassPool()
    {
        final ClassPool classPool = new ClassPool(ClassPool.getDefault());
        classPool.appendClassPath(new LoaderClassPath(Thread.currentThread().getContextClassLoader()));
        classPool.appendClassPath(new LoaderClassPath(ClassLoader.getSystemClassLoader()));
        classPool.importPackage("com.lmax.tool.disruptor");
        return classPool;
    }
}