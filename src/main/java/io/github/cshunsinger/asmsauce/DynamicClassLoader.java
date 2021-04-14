package io.github.cshunsinger.asmsauce;

/**
 * Internal asmsauce class which inherits from Java's ClassLoader {@link ClassLoader} class to expose access to
 * define a class from a byte array at runtime.
 */
public class DynamicClassLoader extends ClassLoader {
    public Class<?> defineClass(String name, byte[] data) {
        return defineClass(name, data, 0, data.length);
    }
}