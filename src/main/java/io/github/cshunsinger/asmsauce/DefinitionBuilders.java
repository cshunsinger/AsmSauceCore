package io.github.cshunsinger.asmsauce;

import io.github.cshunsinger.asmsauce.definitions.*;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DefinitionBuilders {
    private static final TypeDefinition<?>[] NO_TYPES = new TypeDefinition<?>[] {};
    @SuppressWarnings("unchecked")
    private static final TypeDefinition<? extends Exception>[] NO_EXCEPTION_TYPES = new TypeDefinition[] {};

    public static NameDefinition name(String name) {
        return new NameDefinition(name);
    }

    public static ParamDefinition p(String name, Class<?> type) {
        return p(name, type(type));
    }

    public static ParamDefinition p(String name, TypeDefinition<?> type) {
        return new ParamDefinition(name, type);
    }

    public static ParametersDefinition parameters(ParamDefinition... params) {
        return new ParametersDefinition(params);
    }

    public static ParametersDefinition parameters(TypeDefinition<?>... paramTypes) {
        return new ParametersDefinition(paramTypes);
    }

    public static ParametersDefinition parameters(Class<?>... paramTypes) {
        return parameters(TypeDefinition.typesFromClasses(paramTypes));
    }

    public static ParametersDefinition noParameters() {
        return parameters(NO_TYPES);
    }

    public static <T> TypeDefinition<T> type(Class<T> type) {
        return new TypeDefinition<>(type);
    }

    public static TypeDefinition<Void> voidType() {
        return type(void.class);
    }

    @SafeVarargs
    public static ThrowsDefinition throwing(TypeDefinition<? extends Exception>... types) {
        return new ThrowsDefinition(types);
    }

    @SuppressWarnings("unchecked")
    public static ThrowsDefinition throwing(Class<? extends Throwable>... classes) {
        return throwing((TypeDefinition<? extends Exception>[])TypeDefinition.typesFromClasses(classes));
    }

    public static ThrowsDefinition noThrows() {
        return throwing(NO_EXCEPTION_TYPES);
    }
}