package io.github.chunsinger.asmsauce.definitions;

import io.github.chunsinger.asmsauce.ConstructorNode;
import io.github.chunsinger.asmsauce.MethodBuildingContext;
import io.github.chunsinger.asmsauce.MethodNode;
import io.github.chunsinger.asmsauce.ThisClass;
import io.github.chunsinger.asmsauce.modifiers.AccessModifiers;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Optional;

import static io.github.chunsinger.asmsauce.modifiers.AccessModifiers.customAccess;

public class CompleteMethodDefinition<O, R> extends MethodDefinition<O, R> {
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

    @Override
    public CompleteMethodDefinition<?, ?> completeDefinition(MethodBuildingContext buildingContext, int numParameters) {
        AccessModifiers newModifiers;

        if(name.isConstructorName()) {
            if(owner.getType() == ThisClass.class) {
                Optional<ConstructorNode> foundConstructor = attemptFindConstructor(buildingContext.getClassContext(), parameters.getParamTypes());
                if(foundConstructor.isEmpty()) {
                    String exceptionMessage = getConstructorNotFoundMessage(buildingContext.getClassContext().getClassName(), parameters.getParamTypes());
                    throw new IllegalStateException(exceptionMessage);
                }

                newModifiers = foundConstructor.get().getDefinition().modifiers;
            }
            else {
                Optional<Constructor<?>> foundConstructor = attemptFindConstructor(owner.getType(), parameters.getParamTypes());
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
                Optional<MethodNode> foundMethod = attemptFindMethod(buildingContext.getClassContext(), parameters.getParamTypes());
                if(foundMethod.isEmpty()) {
                    String exceptionMessage = getMethodNotFoundMessage(name.getName(), buildingContext.getClassContext().getClassName(), parameters.getParamTypes());
                    throw new IllegalStateException(exceptionMessage);
                }
                newModifiers = foundMethod.get().getDefinition().modifiers;
            }
            else {
                //Make sure method exists and is accessible inside the owner class
                Optional<Method> foundMethod = attemptFindMethod(owner.getType(), parameters.getParamTypes());
                if (foundMethod.isEmpty()) {
                    String exceptionMessage = getMethodNotFoundMessage(name.getName(), owner.getClassName(), parameters.getParamTypes());
                    throw new IllegalStateException(exceptionMessage);
                }
                else {
                    newModifiers = customAccess(foundMethod.get().getModifiers());
                    if(!AccessModifiers.isAccessible(buildingContext.getClassContext(), ThisClass.class, owner.getType(), newModifiers)) {
                        String exceptionMessage = getMethodNotFoundMessage(name.getName(), owner.getClassName(), parameters.getParamTypes());
                        throw new IllegalStateException(exceptionMessage);
                    }
                }
            }
        }

        return new CompleteMethodDefinition<>(owner, newModifiers, name, returnType, parameters, throwing);
    }
}