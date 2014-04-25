disruptor-proxy
===============

Byte-code generator to create Disruptor-backed proxies

![implementation diagram](http://img.epickrram.com/projects/ringbuffer-proxy.png)

Example
-------

```java
final RingBufferProxyGeneratorFactory generatorFactory = new RingBufferProxyGeneratorFactory();

final T tImpl = new ConcreteT();

final RingBufferProxyGenerator generator = generatorFactory.create(GeneratorType.BYTECODE_GENERATION);

final T proxy = generator.createRingBufferProxy(tImpl, T.class, disruptor, OverflowStrategy.DROP);

disruptor.start();
```

GeneratorType
-------------

* `GeneratorType.JDK_REFLECTION` - uses `java.lang.reflect.Proxy` to generate a dynamic proxy that will add events to the RingBuffer. Use this for minimal dependencies.
* `GeneratorType.BYTECODE_GENERATION` - uses Javassist to generate classes that will add events to the RingBuffer. Use this for maximum performance.

Performance
-----------

Tests performed on `Intel(R) Core(TM) i7-2670QM CPU @ 2.20GHz`

`[JDK_REFLECTION]      - Ops per second: 14461091.00, avg latency 69ns`

`[BYTECODE_GENERATION] - Ops per second: 18313904.00, avg latency 54ns`



Dependencies
------------

Minimal dependency is the Disruptor JAR. 

If you are using byte-code generation for the proxy class (specified by `GeneratorType`), you'll also need the Javassist JAR. Both are included in the download.
