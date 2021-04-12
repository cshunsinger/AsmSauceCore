package io.github.cshunsinger.asmsauce;

public class DynamicClassLoader extends ClassLoader {
    public Class<?> defineClass(String name, byte[] data) {
        return defineClass(name, data, 0, data.length);
    }
}