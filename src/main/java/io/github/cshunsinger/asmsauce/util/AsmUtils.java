package io.github.cshunsinger.asmsauce.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * This class contains some utility methods commonly used throughout the AsmSauce library.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AsmUtils {
    private static final Map<Class<?>, String> TYPE_MAPPINGS = new HashMap<>();
    static {
        TYPE_MAPPINGS.put(byte.class, "B");
        TYPE_MAPPINGS.put(short.class, "S");
        TYPE_MAPPINGS.put(int.class, "I");
        TYPE_MAPPINGS.put(long.class, "J");
        TYPE_MAPPINGS.put(float.class, "F");
        TYPE_MAPPINGS.put(double.class, "D");
        TYPE_MAPPINGS.put(char.class, "C");
        TYPE_MAPPINGS.put(boolean.class, "Z");
        TYPE_MAPPINGS.put(void.class, "V");
    }

    /**
     * Generates the JVM runtime representation of a method signature for a method.
     *
     * @param method The method to generate a JVM signature for.
     * @return A string representation of the JVM signature for the given method.
     * @see #generateJvmMethodSignature(List, Class)
     */
    public static String generateJvmMethodSignature(Method method) {
        return generateJvmMethodSignature(method.getParameters(), method.getReturnType());
    }

    /**
     * Generates a classname favored by the JVM.
     * The JVM uses class names, which are the fully qualified name of a class, but with forward slashes instead of
     * periods.
     * For example:
     * Object (java.lang.Object) is represented as java/lang/Object in the JVM.
     *
     * @param clazz The class to create a jvm class name from.
     * @return A string, which is the fully-qualified jvm class name of the class.
     */
    public static String jvmClassname(final Class<?> clazz) {
        return clazz.getName().replace('.', '/');
    }

    /**
     * Produces a JVM type definition for a given class type.
     * Primitive types have type references that are one letter long.
     * For example, byte and int are B and I respectively.
     * For non-primitive classes, the definition is LjvmClassname;
     * For array types, the definition is the same EXCEPT you add a [ to the front. Such as [LjvmClassname;
     * @param clazz The class to produce a jvm type definition for.
     * @return A string, which is the JVM type representation of the given class.
     * @see #jvmClassname(Class)
     */
    public static String jvmTypeDefinition(final Class<?> clazz) {
        if(clazz.isArray()) {
            final Class<?> componentClass = clazz.getComponentType();
            return "[" + jvmTypeDefinition(componentClass);
        }

        return TYPE_MAPPINGS.getOrDefault(clazz, "L" + jvmClassname(clazz) + ";");
    }

    private static String generateJvmMethodSignature(final Parameter[] parameters, final Class<?> returnType) {
        List<Class<?>> parameterTypes = Arrays.stream(parameters)
            .map(Parameter::getType)
            .collect(Collectors.toList());
        return generateJvmMethodSignature(parameterTypes, returnType);
    }

    /**
     * Generates the JVM runtime representation of a method signature for a method having a given list of parameter types
     * and a given return type.
     * A JVM method signature is two parenthesis '()' containing all of the parameter types of the method, followed by
     * the return type of the method. A void method that takes zero parameters looks like this: ()V
     *
     * Math.min(int a, int b) looks like this: (II)I
     *
     * String.valueOf(Object obj) looks like this: (Ljava/lang/Object;)Ljava/lang/String;
     *
     * @param parameterTypes A list of classes representing the list of parameter types for a method.
     * @param returnType A class representing the return type of a method.
     * @return A string, which is the signature of a method with the given parameter and return types.
     */
    public static String generateJvmMethodSignature(final List<Class<?>> parameterTypes, final Class<?> returnType) {
        final StringBuilder builder = new StringBuilder("(");
        for(final Class<?> paramType: parameterTypes) {
            builder.append(jvmTypeDefinition(paramType));
        }
        builder.append(')');
        builder.append(jvmTypeDefinition(returnType));
        return builder.toString();
    }
}