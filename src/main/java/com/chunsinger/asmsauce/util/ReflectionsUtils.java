package com.chunsinger.asmsauce.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.slf4j.MDC;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
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

    /**
     * Given a field, attempts to find an accessor method meeting the following criteria:
     *   - is/get method (isFieldName or getFieldName where the name of the field is fieldName or FieldName)
     *   - Zero parameters
     *   - Return type is not void
     *   - Method is not static
     *   - Method is public
     */
    public static Method findGetterMethodForField(Class<?> type, Field field) {
        String fieldName = field.getName();
        String methodName = (field.getType() == boolean.class ? "is" : "get") + StringUtils.capitalize(fieldName);
        MDC.put("field", fieldName);

        log.info("Attempting to find getter method named " + methodName);
        Method getterMethod = MethodUtils.getMatchingAccessibleMethod(type, methodName);

        boolean valid = isValidGetterMethod(getterMethod, methodName);
        MDC.remove("field");
        return valid ? getterMethod : null;
    }

    public static boolean isValidGetterMethod(Method getterMethod, String methodName) {
        String reason = getInvalidGetterMethodReason(getterMethod, methodName);
        if(reason == null) {
            log.info("Found getter method named " + getterMethod.getName());
            return true;
        }
        else {
            log.info(reason);
            return false;
        }
    }

    /**
     * Given a field, attempts to find a setter method meeting the following criteria:
     *   - set method (setFieldName where the name of the field is fieldName or FieldName)
     *   - Exactly 1 parameter matching the type of tje foe;d
     *   - No return value (void method)
     *   - Method is public
     *   - Method is not static
     */
    public static Method findSetterMethodForField(Class<?> type, Field field) {
        Class<?> fieldType = field.getType();
        String fieldName = field.getName();
        String methodName = "set" + StringUtils.capitalize(fieldName);
        MDC.put("field", fieldName);

        log.info("Attempting to find setter method named " + methodName);
        Method setterMethod = MethodUtils.getMatchingAccessibleMethod(type, methodName, fieldType);

        boolean valid = isValidSetterMethod(setterMethod, methodName);
        MDC.remove("field");

        return valid ? setterMethod : null;
    }

    public static boolean isValidSetterMethod(Method setterMethod) {
        return setterMethod != null && isValidSetterMethod(setterMethod, setterMethod.getName());
    }

    public static boolean isValidSetterMethod(Method setterMethod, String setterName) {
        String reason = getInvalidSetterMethodReason(setterMethod, setterName);
        if(reason == null) {
            log.info("Found setter method named " + setterName);
            return true;
        }
        else {
            log.info(reason);
            return false;
        }
    }

    public static String getInvalidSetterMethodReason(Method setterMethod) {
        if(setterMethod == null)
            return "Method is null.";
        else
            return getInvalidSetterMethodReason(setterMethod, setterMethod.getName());
    }

    public static String getInvalidSetterMethodReason(Method setterMethod, String setterName) {
        if(setterMethod == null)
            return "No setter method named " + setterName + " found.";
        else if(setterMethod.getReturnType() != void.class)
            return "Setter method " + setterName + " must have a void return type.";
        else if(Modifier.isStatic(setterMethod.getModifiers())) {
            return "Setter method " + setterName + " cannot be static.";
        }
        else if(!Modifier.isPublic(setterMethod.getModifiers()))
            return "Setter method " + setterName + " must be public.";
        else
            return null;
    }

    public static String getInvalidGetterMethodReason(Method getterMethod) {
        if(getterMethod == null)
            return "Method is null.";
        else
            return getInvalidGetterMethodReason(getterMethod, getterMethod.getName());
    }

    public static String getInvalidGetterMethodReason(Method getterMethod, String methodName) {
        if(getterMethod == null)
            return "No getter method named " + methodName + " found.";
        else if(getterMethod.getReturnType() == void.class)
            return "Getter method " + methodName + " cannot be void.";
        else if(Modifier.isStatic(getterMethod.getModifiers()))
            return "Getter method " + methodName + " cannot be static.";
        else if(!Modifier.isPublic(getterMethod.getModifiers()))
            return "Getter method " + methodName + " must be public.";
        else if(getterMethod.getParameters().length > 0)
            return "Getter method " + methodName + " must not contain any parameters.";
        else
            return null;
    }
}