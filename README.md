# disruptor-proxy

![Java CI with Gradle](https://github.com/LMAX-Exchange/disruptor-proxy/workflows/Java%20CI%20with%20Gradle/badge.svg)
[![License](https://img.shields.io/github/license/LMAX-Exchange/disruptor-proxy)](https://github.com/LMAX-Exchange/disruptor-proxy/blob/master/LICENCE.txt)

The disruptor-proxy is a tool for creating thread-safe proxies to your existing business code.

Utilising the power of the [Disruptor](https://github.com/LMAX-Exchange/disruptor),
disruptor-proxy will provide a high-performance, low-latency multi-threaded interface
to your single-threaded components.

This in turn allows users to exploit the
[single-writer principle](http://mechanical-sympathy.blogspot.co.uk/2011/09/single-writer-principle.html)
for maximum straight-line performance.

![implementation diagram](https://raw.githubusercontent.com/LMAX-Exchange/disruptor-proxy/master/doc/DisruptorProxy.jpg)

## Maintainer


LMAX Development Team

## Examples

```java

// Basic usage

final RingBufferProxyGeneratorFactory generatorFactory = new RingBufferProxyGeneratorFactory();

final T tImpl = new ConcreteT();

final RingBufferProxyGenerator generator = generatorFactory.newProxy(GeneratorType.BYTECODE_GENERATION);

final T proxy = generator.createRingBufferProxy(T.class, disruptor, OverflowStrategy.DROP, tImpl);

disruptor.start();
```



```java

// Get notified of end-of-batch events

final RingBufferProxyGeneratorFactory generatorFactory = new RingBufferProxyGeneratorFactory();

final T tImpl = new ConcreteT();
final BatchListener batchListener = (BatchListener) tImpl; // implement BatchListener in your component

final RingBufferProxyGenerator generator = generatorFactory.newProxy(GeneratorType.BYTECODE_GENERATION);

final T proxy = generator.createRingBufferProxy(T.class, disruptor, OverflowStrategy.DROP, tImpl);

disruptor.start();
```



```java

// Get notified of buffer-overflow events

final RingBufferProxyGeneratorFactory generatorFactory = new RingBufferProxyGeneratorFactory();

final T tImpl = new ConcreteT();
final DropListener dropListener = new MyDropListener(); // handle drop events

final RingBufferProxyGenerator generator =
        generatorFactory.newProxy(GeneratorType.BYTECODE_GENERATION,
        new ConfigurableValidator(true, true),
        dropListener);

final T proxy = generator.createRingBufferProxy(T.class, disruptor, OverflowStrategy.DROP, tImpl);

disruptor.start();
```


## GeneratorType

* `GeneratorType.JDK_REFLECTION` - uses `java.lang.reflect.Proxy` to generate a dynamic proxy that will add events to the RingBuffer. Use this for minimal dependencies.
* `GeneratorType.BYTECODE_GENERATION` - uses Javassist to generate classes that will add events to the RingBuffer. Use this for maximum performance.


## Dependencies

Minimal dependency is the Disruptor JAR. 

If you are using byte-code generation for the proxy class (specified by `GeneratorType`), you'll also need the Javassist JAR.
