package io.github.cshunsinger.asmsauce.definitions;

import io.github.cshunsinger.asmsauce.ConstructorNode;
import io.github.cshunsinger.asmsauce.MethodNode;
import io.github.cshunsinger.asmsauce.ThisClass;
import io.github.cshunsinger.asmsauce.modifiers.AccessModifiers;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Optional;

import static io.github.cshunsinger.asmsauce.MethodBuildingContext.context;
import static io.github.cshunsinger.asmsauce.modifiers.AccessModifiers.customAccess;

/**
 * This class defines a method and contains all of its metadata. This class represent a "complete" set of metadata
 * defining a method to call.
 * @param <O> The type owning the method.
 * @param <R> The return type of the method.
 */
public class CompleteMethodDefinition<O, R> extends MethodDefinition<O, R> {
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
    public CompleteMethodDefinition(TypeDefinition<O> owner,
                                    AccessModifiers modifiers,
                                    NameDefinition name,
                                    TypeDefinition<R> returnType,
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
     * Validates that the complete set of method details provided in this "complete" method definition are all valid.
     * @param numParameters The number of parameters this method should have.
     * @return Returns a copy of this method definition which is guaranteed to have valid method or constructor details.
     * @throws IllegalStateException If the method or constructor represented by this definition does not exist, is not
     * accessible from the class being generated, does not have the required number of parameters "numParameters", or
     * whose set of parameters are not assignable from the parameter types of this "complete" definition.
     */
    @Override
    public CompleteMethodDefinition<?, ?> completeDefinition(int numParameters) {
        AccessModifiers newModifiers;

        if(name.isConstructorName()) {
            if(owner.getType() == ThisClass.class) {
                Optional<ConstructorNode> foundConstructor = attemptFindConstructor(context().getClassContext(), parameters.getParamTypes());
                if(foundConstructor.isEmpty()) {
                    String exceptionMessage = getConstructorNotFoundMessage(context().getClassContext().getClassName(), parameters.getParamTypes());
                    throw new IllegalStateException(exceptionMessage);
                }

                newModifiers = foundConstructor.get().getDefinition().modifiers;
            }
            else {
                Optional<Constructor<?>> foundConstructor = attemptFindConstructor(context(), owner.getType(), parameters.getParamTypes());
                if(foundConstructor.isEmpty()) {
                    String exceptionMessage = getConstructorNotFoundMessage(owner.getClassName(), parameters.getParamTypes());
                    throw new IllegalStateException(exceptionMessage);
                }
                newModifiers = customAccess(foundConstructor.get().getModifiers());
            }
        }
        else {
            if(owner.getType() == ThisClass.class) {
                //Make sure the method exists in the class being built
                //The method is guaranteed accessible if it does exist
                Optional<MethodNode> foundMethod = attemptFindMethod(context().getClassContext(), parameters.getParamTypes());
                if(foundMethod.isEmpty()) {
                    String exceptionMessage = getMethodNotFoundMessage(name.getName(), context().getClassContext().getClassName(), parameters.getParamTypes());
                    throw new IllegalStateException(exceptionMessage);
                }
                newModifiers = foundMethod.get().getDefinition().modifiers;
            }
            else {
                //Make sure method exists and is accessible inside the owner class
                Optional<Method> foundMethod = attemptFindMethod(context(), owner.getType(), parameters.getParamTypes());
                if (foundMethod.isEmpty()) {
                    String exceptionMessage = getMethodNotFoundMessage(name.getName(), owner.getClassName(), parameters.getParamTypes());
                    throw new IllegalStateException(exceptionMessage);
                }
                else {
                    newModifiers = customAccess(foundMethod.get().getModifiers());
                    if(!AccessModifiers.isAccessible(context().getClassContext(), ThisClass.class, owner.getType(), newModifiers)) {
                        String exceptionMessage = getMethodNotFoundMessage(name.getName(), owner.getClassName(), parameters.getParamTypes());
                        throw new IllegalStateException(exceptionMessage);
                    }
                }
            }
        }

        return new CompleteMethodDefinition<>(owner, newModifiers, name, returnType, parameters, throwing);
    }
}