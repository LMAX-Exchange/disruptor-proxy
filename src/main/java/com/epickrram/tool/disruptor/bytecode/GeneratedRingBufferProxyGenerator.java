package com.epickrram.tool.disruptor.bytecode;

import com.epickrram.tool.disruptor.*;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;
import javassist.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public final class GeneratedRingBufferProxyGenerator implements RingBufferProxyGenerator
{
    private static final AtomicInteger UNIQUE_GENERATED_CLASS_NAME_COUNTER = new AtomicInteger();
    private static final boolean DEBUG = true;

    private final ClassPool classPool;

    public GeneratedRingBufferProxyGenerator()
    {
        classPool = configureClassPool();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T createRingBufferProxy(final T implementation, final Class<T> definition,
                                       final Disruptor<ProxyMethodInvocation> disruptor,
                                       final OverflowStrategy overflowStrategy)
    {
        disruptor.handleEventsWith(new InvokerEventHandler<T>(implementation));

        final Map<Method, Invoker> methodToInvokerMap = createMethodToInvokerMap(definition);

        return generateProxy(definition, disruptor.getRingBuffer(), methodToInvokerMap, overflowStrategy);
    }

    private <T> T generateProxy(final Class<T> definition, final RingBuffer<ProxyMethodInvocation> ringBuffer,
                                final Map<Method, Invoker> methodToInvokerMap, final OverflowStrategy overflowStrategy)
    {
        final StringBuilder proxyClassName = new StringBuilder("_proxy").append(definition.getSimpleName()).append('_').append(UNIQUE_GENERATED_CLASS_NAME_COUNTER.incrementAndGet());
        final CtClass ctClass = classPool.makeClass(proxyClassName.toString());
        
        addInterface(ctClass, definition);
        makePublicFinal(ctClass);

        createFields(methodToInvokerMap, ctClass);
        createConstructor(ctClass);

        for(final Method method : definition.getDeclaredMethods())
        {
            createRingBufferPublisherMethod(ctClass, method, methodToInvokerMap.get(method), overflowStrategy);
        }

        return instantiate(ctClass, ringBuffer);
    }

    @SuppressWarnings("unchecked")
    private <T> T instantiate(final CtClass ctClass, final RingBuffer<ProxyMethodInvocation> ringBuffer)
    {
        try
        {
            final Class generatedClass = ctClass.toClass();

            final Constructor jdkConstructor = generatedClass.getConstructor(new Class[]{RingBuffer.class});
            return (T) jdkConstructor.newInstance(ringBuffer);
        }
        catch (CannotCompileException e)
        {
            throw new RuntimeException("Unable to compile class", e);
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
        final StringBuilder ringBufferFieldSrc = new StringBuilder();
        ringBufferFieldSrc.append("private final ").append(RingBuffer.class.getName()).append(" ringBuffer;");
        createField(ctClass, ringBufferFieldSrc.toString());

        for(final Method method : methodToInvokerMap.keySet())
        {
            final Invoker invoker = methodToInvokerMap.get(method);
            final StringBuilder fieldSrc = new StringBuilder("private final ").
                    append(invoker.getClass().getName()).append(" _").
                    append(invoker.getClass().getName()).append(" = new ").
                    append(invoker.getClass().getName()).append("();");

            createField(ctClass, fieldSrc.toString());
        }
    }

    private void createRingBufferPublisherMethod(final CtClass ctClass, final Method method, final Invoker invoker,
                                                 final OverflowStrategy overflowStrategy)
    {
        final StringBuilder methodSrc = new StringBuilder("public void ").append(method.getName()).append("(");
        final Class<?>[] parameterTypes = method.getParameterTypes();
        char paramId = 'a';
        for (int i = 0, parameterTypesLength = parameterTypes.length; i < parameterTypesLength; i++)
        {
            final Class<?> parameterType = parameterTypes[i];

            if(parameterType.isArray())
            {
                methodSrc.append(parameterType.getComponentType().getName()).append("[] ").append(paramId++);
            }
            else
            {
                methodSrc.append(parameterType.getName()).append(' ').append(paramId++);
            }

            if(i < parameterTypesLength - 1)
            {
                methodSrc.append(", ");
            }
        }
        methodSrc.append(")\n{\n");

        if(overflowStrategy == OverflowStrategy.DROP)
        {
            methodSrc.append("if(!ringBuffer.hasAvailableCapacity(1))\n");
            methodSrc.append("{");
            methodSrc.append("return;\n");
            methodSrc.append("}\n");
        }

        methodSrc.append("final long sequence = ringBuffer.next();\n").append("try\n").
                append("{\n").
                append("final ProxyMethodInvocation proxyMethodInvocation = (ProxyMethodInvocation) ringBuffer.getPreallocated(sequence);\n").

                append("proxyMethodInvocation.ensureCapacity(").
                append(parameterTypes.length).
                append(");\n").
                append("final Object[] args = proxyMethodInvocation.getArguments();\n");

        for(int i = 0; i < parameterTypes.length; i++)
        {
            final Class<?> parameterType = parameterTypes[i];
            if(parameterType.isPrimitive())
            {
                methodSrc.append("args[").append(i).append("] = ").append(Primitives.getWrapperClassName(parameterType)).
                        append(".valueOf(").
                        append((char) ('a' + i)).append(");\n");
            }
            else
            {
                methodSrc.append("args[").append(i).append("] = ").append((char) ('a' + i)).append(";\n");
            }
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

        if(DEBUG)
        {
            System.out.println(methodSrc);
        }

        try
        {
            ctClass.addMethod(CtMethod.make(methodSrc.toString(), ctClass));
        }
        catch (CannotCompileException e)
        {
            throw new RuntimeException("Unable to compile class", e);
        }
    }

    private void createField(final CtClass ctClass, final String fieldSrc)
    {
        try
        {
            if(DEBUG)
            {
                System.out.println(fieldSrc);
            }

            ctClass.addField(CtField.make(fieldSrc, ctClass));
        }
        catch (CannotCompileException e)
        {
            throw new RuntimeException("Unable to generate field: " + fieldSrc, e);
        }
    }

    @SuppressWarnings("unchecked")
    private <T> Invoker generateInvoker(final Class<T> definition, final Method method)
    {
        final StringBuilder invokerClassName = new StringBuilder("_invoker").append(definition.getSimpleName()).
                append(method.getName()).append('_').append(UNIQUE_GENERATED_CLASS_NAME_COUNTER.incrementAndGet());

        final Class<?>[] parameterTypes = method.getParameterTypes();
        for(final Class<?> paramType : parameterTypes)
        {
            if(paramType.isArray())
            {
                invokerClassName.append(paramType.getComponentType().getSimpleName()).append("Array");
            }
            else
            {
                invokerClassName.append(paramType.getSimpleName());
            }
        }

        final CtClass ctClass = classPool.makeClass(invokerClassName.toString());
        addInterface(ctClass, Invoker.class);
        makePublicFinal(ctClass);
        final StringBuilder methodSrc = new StringBuilder("public void invoke(").append("Object").
                append(" implementation, Object[] args) {\n((").append(definition.getName().replace('$', '.')).
                append(")implementation).").append(method.getName()).append('(');

        appendParameters(parameterTypes, methodSrc);

        methodSrc.append(");\n}\n");

        if(DEBUG)
        {
            System.out.println(methodSrc);
        }

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

    private void makePublicFinal(final CtClass ctClass)
    {
        ctClass.setModifiers(Modifier.PUBLIC | Modifier.FINAL);
    }

    private void addInterface(final CtClass ctClass, final Class<?> interfaceClass)
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

    private <T> Map<Method, Invoker> createMethodToInvokerMap(final Class<T> definition)
    {
        final Map<Method, Invoker> methodToInvokerMap = new ConcurrentHashMap<Method, Invoker>();

        final Method[] declaredMethods = definition.getDeclaredMethods();

        for (Method declaredMethod : declaredMethods)
        {
            methodToInvokerMap.put(declaredMethod, generateInvoker(definition, declaredMethod));
        }

        return methodToInvokerMap;
    }

    private void appendParameters(final Class<?>[] parameterTypes, final StringBuilder methodSrc)
    {
        for(int i = 0; i < parameterTypes.length; i++)
        {
            final Class<?> parameterType = parameterTypes[i];
            final boolean primitive = parameterType.isPrimitive();

            if(primitive)
            {
                methodSrc.append("((").append(Primitives.getWrapperClassName(parameterType)).append(") args[").
                        append(i).append("]).").append(parameterType.getName()).append("Value()");
            }
            else if(parameterType.isArray())
            {
                final Class<?> arrayComponentType = parameterType.getComponentType();
                methodSrc.append('(').append(arrayComponentType.getName()).append("[]) args[").append(i).append(']');
            }
            else
            {
                methodSrc.append('(').append(parameterType.getName()).append(") args[").append(i).append(']');
            }

            if(i < parameterTypes.length  - 1)
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
        classPool.importPackage("com.epickrram.tool.disruptor");
        return classPool;
    }
}