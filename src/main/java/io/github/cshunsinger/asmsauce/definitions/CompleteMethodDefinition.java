package io.github.cshunsinger.asmsauce.definitions;

import io.github.cshunsinger.asmsauce.modifiers.AccessModifiers;

import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

import static io.github.cshunsinger.asmsauce.DefinitionBuilders.*;
import static io.github.cshunsinger.asmsauce.modifiers.AccessModifiers.customAccess;

/**
 * This class defines a method and contains all of its metadata. This class represent a "complete" set of metadata
 * defining a method to call.
 */
public class CompleteMethodDefinition extends MethodDefinition {
    /**
     * Creates a completed method definition.
     * @param owner The owner of this method.
     * @param modifiers The access modifiers of this method being defined.
     * @param name The name of this method.
     * @param returnType The return type of this method.
     * @param parameters The parameter types of this method.
     * @param throwing The exceptions thrown by this method, if any.
     * @throws IllegalArgumentException If the "owner" type is null or represents a void type, primitive type, or array type.
     * @throws IllegalArgumentException If the "modifiers" are null.
     * @throws IllegalArgumentException If the "returnType" is null.
     * @throws IllegalArgumentException If the "parameters" definition is null.
     * @throws IllegalArgumentException If the "throwing" definition is null.
     */
    public CompleteMethodDefinition(TypeDefinition owner,
                                    AccessModifiers modifiers,
                                    NameDefinition name,
                                    TypeDefinition returnType,
                                    ParametersDefinition parameters,
                                    ThrowsDefinition throwing) {
        super(owner, modifiers, name, parameters, returnType, throwing);

        if(owner == null)
            throw new IllegalArgumentException("Method owner type cannot be null.");
        validateMethodOwnerArgument(owner);

        if(modifiers == null)
            throw new IllegalArgumentException("Modifiers cannot be null.");
        if(returnType == null)
            throw new IllegalArgumentException("Return type cannot be null.");
        if(parameters == null)
            throw new IllegalArgumentException("Parameters cannot be null.");
        if(throwing == null)
            throw new IllegalArgumentException("Throwing cannot be null.");
    }

    /**
     * Creates a method definition from an existing Java reflections Constructor/Method instance.
     * @param executable The Java reflections Constructor/Method instance.
     * @return A CompleteMethodDefinition instance created from details in the executable instance.
     */
    public static CompleteMethodDefinition fromExecutable(Executable executable) {
        NameDefinition methodName = NameDefinition.CONSTRUCTOR_NAME_DEFINITION;
        TypeDefinition returnType = type(void.class);

        if(executable instanceof Method) {
            Method method = (Method)executable;
            methodName = name(method.getName());
            returnType = type(method.getReturnType());
        }

        Parameter[] parameters = executable.getParameters();
        ParamDefinition[] paramDefs = new ParamDefinition[parameters.length];
        for(int i = 0; i < parameters.length; i++) {
            Parameter param = parameters[i];
            paramDefs[i] = p(param.getName(), param.getType());
        }

        return new CompleteMethodDefinition(
            type(executable.getDeclaringClass()),
            customAccess(executable.getModifiers()),
            methodName,
            returnType,
            parameters(paramDefs),
            throwing(executable.getExceptionTypes())
        );
    }

    /**
     * Validates that the complete set of method details provided in this "complete" method definition are all valid.
     * @param numParameters The number of parameters this method should have.
     * @return A new completed method definition for an existing method.
     * @throws IllegalStateException If no method or constructor can be found which has the details specified in this
     * method definition.
     */
    @Override
    public CompleteMethodDefinition completeDefinition(int numParameters) {
       return super.completeDefinition(numParameters);
    }
}