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

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ReflectionsUtils {
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
     * Generates the JVM bytecode representation of a method signature for a given method.
     */
    public static String generateJvmMethodSignature(Method method) {
        return generateJvmMethodSignature(method.getParameters(), method.getReturnType());
    }

    /**
     * Generates a classname favored by the JVM.
     * Basically gets the fully qualified classname of the given class, and replaces all periods with forward slashes.
     */
    public static String jvmClassname(final Class<?> clazz) {
        return clazz.getName().replace('.', '/');
    }

    /**
     * Produces a JVM type definition for a given class type.
     * Primitive types have type references that are one letter long.
     * For example, byte and int are B and I respectively.
     * For non-primitive classes, the definition is LjvmClassname;.
     * For array types, the definition is the same EXCEPT you add a [ to the front.
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