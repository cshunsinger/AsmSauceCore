package io.github.cshunsinger.asmsauce;

import io.github.cshunsinger.asmsauce.definitions.*;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * This class contains all of the builder methods for creating different definition objects.
 * These definitions are used to provide details about methods, fields, types, and names being used or defined.
 *
 * When building a new class, it is recommended to import all of the methods in this class with a static import.
 *
 * @see AsmClassBuilder {@link AsmClassBuilder} Uses these definitions when building classes.
 * @see io.github.cshunsinger.asmsauce.code.CodeBuilders {@link io.github.cshunsinger.asmsauce.code.CodeBuilders}
 * A class similar to this one, but for creating method body instructions instead of definitions.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DefinitionBuilders {
    private static final TypeDefinition[] NO_TYPES = new TypeDefinition[] {};
    private static final TypeDefinition[] NO_EXCEPTION_TYPES = new TypeDefinition[] {};

    /**
     * Creates a name definition with a given String as the name.
     * @param name The String to create a name definition for.
     * @return A new name definition containing the String name provided.
     * @see NameDefinition
     */
    public static NameDefinition name(String name) {
        return new NameDefinition(name);
    }

    /**
     * Creates a parameter definition which contains a parameter name and parameter type.
     * @param name The name of the parameter to define.
     * @param type The class type of the parameter to define.
     * @return A new parameter definition containing the parameter name and type.
     * @see ParamDefinition
     * @see ParametersDefinition
     * @see #p(String, TypeDefinition)
     */
    public static ParamDefinition p(String name, Class<?> type) {
        return p(name, type(type));
    }

    /**
     * Creates a parameter definition which contains a parameter name and parameter type.
     * @param name The name of the parameter to define.
     * @param type The type definition of the parameter to define.
     * @return A new parameter definition containing the parameter name and type.
     * @see ParamDefinition
     * @see ParametersDefinition
     * @see #p(String, Class)
     */
    public static ParamDefinition p(String name, TypeDefinition type) {
        return new ParamDefinition(name, type);
    }

    /**
     * Creates a parameters definition, which defines the parameters for a method being generated or called explicitly.
     * @param params The parameter definitions of the parameters being defined.
     * @return A definition of all the listed parameters.
     * @see ParamDefinition
     * @see ParametersDefinition
     * @see #parameters(Class...)
     * @see #parameters(TypeDefinition...)
     * @see #noParameters()
     */
    public static ParametersDefinition parameters(ParamDefinition... params) {
        return new ParametersDefinition(params);
    }

    /**
     * Creates a parameters definition, which defines the parameters for a method being generated or called explicitly.
     * The parameters defined will be unnamed and will not be able to be referenced by name. Instead these parameters
     * will only be able to be referenced by their parameter index. This does not matter if the parameters are being
     * defined for a method being invoked in the bytecode, but can matter for a method being generated.
     * @param paramTypes The type definitions of the parameters being defined.
     * @return A definition defining a list of nameless parameters.
     * @see ParamDefinition
     * @see ParametersDefinition
     * @see #parameters(Class...)
     * @see #parameters(ParamDefinition...)
     * @see #noParameters()
     */
    public static ParametersDefinition parameters(TypeDefinition... paramTypes) {
        return new ParametersDefinition(paramTypes);
    }

    /**
     * Creates a parameters definition, which defines the parameters for a method being generated or called explicitly.
     * The parameters defined will be unnamed and will not be able to be referenced by name. Instead these parameters
     * will only be able to be referenced by their parameter index. This does not matter if the parameters are being
     * defined for a method being invoked in the bytecode, but can matter for a method being generated.
     * @param paramTypes The classes of the parameters being defined.
     * @return A definition defining a list of nameless parameters.
     * @see ParamDefinition
     * @see ParametersDefinition
     * @see #parameters(TypeDefinition...)
     * @see #parameters(ParamDefinition...)
     * @see #noParameters()
     */
    public static ParametersDefinition parameters(Class<?>... paramTypes) {
        return parameters(TypeDefinition.typesFromClasses(paramTypes));
    }

    /**
     * Defines an empty parameter set. That is: a parameters definition containing no parameters.
     * @return An empty parameters definition.
     * @see ParamDefinition
     * @see ParametersDefinition
     * @see #parameters(ParamDefinition...)
     * @see #parameters(Class...)
     * @see #parameters(TypeDefinition...)
     */
    public static ParametersDefinition noParameters() {
        return parameters(NO_TYPES);
    }

    /**
     * Defines a type out of a class. A {@link TypeDefinition} is used by this library when generating bytecode.
     * @param type The class to define as a type.
     * @param <T> The type being defined.
     * @return A type definition created from the given class.
     * @see TypeDefinition
     * @see #voidType()
     */
    public static <T> TypeDefinition type(Class<T> type) {
        return TypeDefinition.fromClass(type);
    }

    /**
     * Defines a {@link TypeDefinition} representing 'void'.
     * @return A type definition representing 'void'.
     * @see TypeDefinition
     * @see #type(Class)
     */
    public static TypeDefinition voidType() {
        return type(void.class);
    }

    /**
     * Defines the exception types thrown by a method being created by the class builder. This definition is equivalent
     * to the 'throws' clause of a method header in Java source code.
     * @param types The types that could be thrown by the method being generated.
     * @return A definition of exception types representing the 'throws' clause of a generated method.
     * @see ThrowsDefinition
     * @see #throwing(Class...)
     * @see #noThrows()
     */
    public static ThrowsDefinition throwing(TypeDefinition... types) {
        return new ThrowsDefinition(types);
    }

    /**
     * Defines the exception types thrown by a method being created by the class builder. This definition is equivalent
     * to the 'throws' clause of a method header in Java source code.
     * @param classes The types that could be thrown by the method being generated.
     * @return A definition of exception types representing the 'throws' clause of a generated method.
     * @see ThrowsDefinition
     * @see #throwing(TypeDefinition...)
     * @see #noThrows()
     */
    public static ThrowsDefinition throwing(Class<?>... classes) {
        return throwing(TypeDefinition.typesFromClasses(classes));
    }

    /**
     * Defines an empty 'throws' clause for a method. That means the method does not define itself as throwing any exceptions.
     * This is the equivalent to omitting a throws clause in a method header in Java source code.
     * @return A throws definition which defines no exceptions being thrown.
     * @see ThrowsDefinition
     * @see #throwing(Class...)
     * @see #throwing(TypeDefinition...)
     */
    public static ThrowsDefinition noThrows() {
        return throwing(NO_EXCEPTION_TYPES);
    }
}