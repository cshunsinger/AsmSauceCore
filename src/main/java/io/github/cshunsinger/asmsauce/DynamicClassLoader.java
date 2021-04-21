package io.github.cshunsinger.asmsauce;

/**
 * Internal asmsauce class which inherits from Java's ClassLoader {@link ClassLoader} class to expose access to
 * define a class from a byte array at runtime.
 */
public class DynamicClassLoader extends ClassLoader {
    /**
     * Creates a new dynamic class loader which can load new classes from a byte array. This class loader
     * requires a parent class loader for proper class visibility.
     * @param parent A parent class loader.
     */
    public DynamicClassLoader(ClassLoader parent) {
        super(parent);
    }

    /**
     * Publicly exposes the ability to define and load a Java class from a byte array.
     * @param name The jvm classname of the class being loaded from a byte array.
     * @param data A byte array containing all of the data making up a class.
     * @return A class instance representing the class which was just loaded from the provided byte array.
     */
    public Class<?> defineClass(String name, byte[] data) {
        return defineClass(name, data, 0, data.length);
    }
}