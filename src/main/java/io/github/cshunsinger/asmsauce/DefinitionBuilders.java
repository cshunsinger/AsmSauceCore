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
    private static final TypeDefinition<?>[] NO_TYPES = new TypeDefinition<?>[] {};
    @SuppressWarnings("unchecked")
    private static final TypeDefinition<? extends Exception>[] NO_EXCEPTION_TYPES = new TypeDefinition[] {};

    /**
     * Creates a name definition with a given String as the name.
     * @param name The String to create a name definition for.
     * @return A new name definition containing the String name provided.
     * @see NameDefinition {@link NameDefinition}
     */
    public static NameDefinition name(String name) {
        return new NameDefinition(name);
    }

    /**
     * Creates a parameter definition which contains a parameter name and parameter type.
     * @param name The name of the parameter to define.
     * @param type The class type of the parameter to define.
     * @return A new parameter definition containing the parameter name and type.
     * @see ParamDefinition {@link ParamDefinition}
     * @see ParametersDefinition {@link ParametersDefinition}
     * @see #p(String, TypeDefinition) {@link #p(String, TypeDefinition)} Called by this method.
     */
    public static ParamDefinition p(String name, Class<?> type) {
        return p(name, type(type));
    }

    /**
     * Creates a parameter definition which contains a parameter name and parameter type.
     * @param name The name of the parameter to define.
     * @param type The type definition of the parameter to define.
     * @return A new parameter definition containing the parameter name and type.
     * @see ParamDefinition {@link ParamDefinition}
     * @see ParametersDefinition {@link ParametersDefinition}
     * @see #p(String, Class) {@link #p(String, Class)} Overload which takes a Class instead of a TypeDefinition.
     */
    public static ParamDefinition p(String name, TypeDefinition<?> type) {
        return new ParamDefinition(name, type);
    }

    /**
     * Creates a parameters definition, which defines the parameters for a method being generated or called explicitly.
     * @param params The parameter definitions of the parameters being defined.
     * @return A definition of all the listed parameters.
     * @see ParamDefinition {@link ParamDefinition}
     * @see ParametersDefinition {@link ParametersDefinition}
     * @see #parameters(Class...) {@link #parameters(Class...)} To define unnamed parameters from classes.
     * @see #parameters(TypeDefinition...) {@link #parameters(TypeDefinition...)} To define unnamed parameters from type definitions.
     * @see #noParameters() {@link #noParameters()} To define an empty parameter set. Useful for methods that contain no parameters.
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
     * @see ParamDefinition {@link ParamDefinition}
     * @see ParametersDefinition {@link ParametersDefinition}
     * @see #parameters(Class...) {@link #parameters(Class...)} To define unnamed parameters from classes.
     * @see #parameters(ParamDefinition...) {@link #parameters(ParamDefinition...)} To define named parameters.
     * @see #noParameters() {@link #noParameters()} To define an empty parameter set. Useful for methods that contain no parameters.
     */
    public static ParametersDefinition parameters(TypeDefinition<?>... paramTypes) {
        return new ParametersDefinition(paramTypes);
    }

    /**
     * Creates a parameters definition, which defines the parameters for a method being generated or called explicitly.
     * The parameters defined will be unnamed and will not be able to be referenced by name. Instead these parameters
     * will only be able to be referenced by their parameter index. This does not matter if the parameters are being
     * defined for a method being invoked in the bytecode, but can matter for a method being generated.
     * @param paramTypes The classes of the parameters being defined.
     * @return A definition defining a list of nameless parameters.
     * @see ParamDefinition {@link ParamDefinition}
     * @see ParametersDefinition {@link ParametersDefinition}
     * @see #parameters(TypeDefinition...) {@link #parameters(TypeDefinition...)} To define unnamed parameters from type definitions.
     * @see #parameters(ParamDefinition...) {@link #parameters(ParamDefinition...)} To define named parameters.
     * @see #noParameters() {@link #noParameters()} To define an empty parameter set. Useful for methods that contain no parameters.
     */
    public static ParametersDefinition parameters(Class<?>... paramTypes) {
        return parameters(TypeDefinition.typesFromClasses(paramTypes));
    }

    /**
     * Defines an empty parameter set. That is: a parameters definition containing no parameters.
     * @return An empty parameters definition.
     * @see ParamDefinition {@link ParamDefinition}
     * @see ParametersDefinition {@link ParametersDefinition}
     * @see #parameters(ParamDefinition...) {@link #parameters(ParamDefinition...)} To define named parameters.
     * @see #parameters(Class...) {@link #parameters(Class...)} To define unnamed parameters from classes.
     * @see #parameters(TypeDefinition...) {@link #parameters(TypeDefinition...)} To define unnamed parameters from type definitions.
     */
    public static ParametersDefinition noParameters() {
        return parameters(NO_TYPES);
    }

    /**
     * Defines a type out of a class. A {@link TypeDefinition} is used by this library when generating bytecode.
     * @param type The class to define as a type.
     * @param <T> The type being defined.
     * @return A type definition created from the given class.
     * @see TypeDefinition {@link TypeDefinition}
     * @see #voidType() {@link #voidType()} To define a void type.
     */
    public static <T> TypeDefinition<T> type(Class<T> type) {
        return new TypeDefinition<>(type);
    }

    /**
     * Defines a {@link TypeDefinition} representing 'void'.
     * @return A type definition representing 'void'.
     * @see TypeDefinition {@link TypeDefinition}
     * @see #type(Class) {@link #type(Class)} To define a type for a given class.
     */
    public static TypeDefinition<Void> voidType() {
        return type(void.class);
    }

    /**
     * Defines the exception types thrown by a method being created by the class builder. This definition is equivalent
     * to the 'throws' clause of a method header in Java source code.
     * @param types The types that could be thrown by the method being generated.
     * @return A definition of exception types representing the 'throws' clause of a generated method.
     * @see ThrowsDefinition {@link ThrowsDefinition}
     * @see #throwing(Class...) {@link #throwing(Class...)} To define the exception types being thrown from classes.
     * @see #noThrows() {@link #noThrows()} To define no exceptions being thrown.
     */
    @SafeVarargs
    public static ThrowsDefinition throwing(TypeDefinition<? extends Exception>... types) {
        return new ThrowsDefinition(types);
    }

    /**
     * Defines the exception types thrown by a method being created by the class builder. This definition is equivalent
     * to the 'throws' clause of a method header in Java source code.
     * @param classes The types that could be thrown by the method being generated.
     * @return A definition of exception types representing the 'throws' clause of a generated method.
     * @see ThrowsDefinition {@link ThrowsDefinition}
     * @see #throwing(TypeDefinition...) {@link #throwing(TypeDefinition...)} To define the exception types being thrown from type definitions.
     * @see #noThrows() {@link #noThrows()} To define no exceptions being thrown.
     */
    @SuppressWarnings("unchecked")
    public static ThrowsDefinition throwing(Class<? extends Throwable>... classes) {
        return throwing((TypeDefinition<? extends Exception>[])TypeDefinition.typesFromClasses(classes));
    }

    /**
     * Defines an empty 'throws' clause for a method. That means the method does not define itself as throwing any exceptions.
     * This is the equivalent to omitting a throws clause in a method header in Java source code.
     * @return A throws definition which defines no exceptions being thrown.
     * @see ThrowsDefinition {@link ThrowsDefinition}
     * @see #throwing(Class...) {@link #throwing(Class...)} To define the exception types being thrown from classes.
     * @see #throwing(TypeDefinition...) {@link #throwing(TypeDefinition...)} To define the exception types being thrown from type definitions.
     */
    public static ThrowsDefinition noThrows() {
        return throwing(NO_EXCEPTION_TYPES);
    }
}