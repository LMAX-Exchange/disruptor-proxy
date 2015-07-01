disruptor-proxy       [![Build Status](https://travis-ci.org/LMAX-Exchange/disruptor-proxy.svg)](https://travis-ci.org/LMAX-Exchange/disruptor-proxy)
===============

Byte-code generator to create Disruptor-backed proxies

![implementation diagram](http://img.epickrram.com/projects/ringbuffer-proxy.png)

Maintainer
----------

[Mark Price](https://github.com/epickrram)

Example
-------

```java
final RingBufferProxyGeneratorFactory generatorFactory = new RingBufferProxyGeneratorFactory();

final T tImpl = new ConcreteT();

final RingBufferProxyGenerator generator = generatorFactory.newProxy(GeneratorType.BYTECODE_GENERATION);

final T proxy = generator.createRingBufferProxy(tImpl, T.class, disruptor, OverflowStrategy.DROP);

disruptor.start();
```

GeneratorType
-------------

* `GeneratorType.JDK_REFLECTION` - uses `java.lang.reflect.Proxy` to generate a dynamic proxy that will add events to the RingBuffer. Use this for minimal dependencies.
* `GeneratorType.BYTECODE_GENERATION` - uses Javassist to generate classes that will add events to the RingBuffer. Use this for maximum performance.


Dependencies
------------

Minimal dependency is the Disruptor JAR. 

If you are using byte-code generation for the proxy class (specified by `GeneratorType`), you'll also need the Javassist JAR.
