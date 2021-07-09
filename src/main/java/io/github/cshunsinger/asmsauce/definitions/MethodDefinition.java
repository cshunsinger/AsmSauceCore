package io.github.cshunsinger.asmsauce.definitions;

import io.github.cshunsinger.asmsauce.ThisClass;
import io.github.cshunsinger.asmsauce.modifiers.AccessModifiers;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Optional;

import static io.github.cshunsinger.asmsauce.DefinitionBuilders.parameters;
import static io.github.cshunsinger.asmsauce.DefinitionBuilders.type;
import static io.github.cshunsinger.asmsauce.MethodBuildingContext.context;

/**
 * This class defines a method and contains all of its metadata. The data to define a method in this class may be
 * "incomplete" and more information might be required in order for bytecode to be generated.
 */
@Getter
public class MethodDefinition {
    /**
     * The type which owns this defined method.
     * @return The method owner type.
     */
    protected final TypeDefinition owner;
    /**
     * The access modifiers of this defined method.
     * @return The access modifiers.
     */
    protected final AccessModifiers modifiers;
    /**
     * The name of this defined method.
     * @return The name.
     */
    protected final NameDefinition name;
    /**
     * The set of parameters of this defined method.
     * @return The set of parameters.
     */
    protected final ParametersDefinition parameters;
    /**
     * The return type of this defined method.
     * @return The return type.
     */
    protected final TypeDefinition returnType;
    /**
     * The definition of exceptions thrown by this method.
     * @return This method's thrown exceptions.
     */
    protected final ThrowsDefinition throwing;

    /**
     * Creates a new method definition.
     * @param owner The owner of the method.
     * @param modifiers The method modifier flags.
     * @param name The method name.
     * @param parameters The parameters list of the method.
     * @param returnType The method return type.
     * @param throwing The types thrown by the method.
     * @throws IllegalArgumentException If owner is void, primitive, or an array type, since none of those types can have methods.
     * @throws IllegalArgumentException If name is null.
     * @throws IllegalArgumentException If owner is null and the modifiers indicate this method is a static method.
     */
    public MethodDefinition(TypeDefinition owner,
                            AccessModifiers modifiers,
                            NameDefinition name,
                            ParametersDefinition parameters,
                            TypeDefinition returnType,
                            ThrowsDefinition throwing) {
        if(owner != null)
            validateMethodOwnerArgument(owner);

        if(name == null)
            throw new IllegalArgumentException("Name cannot be null.");

        if(modifiers != null && modifiers.isStatic()) {
            if(owner == null)
                throw new IllegalArgumentException("Method owner type is mandatory for static methods.");
            else
                validateMethodOwnerArgument(owner);
        }

        this.owner = owner;
        this.modifiers = modifiers;
        this.name = name;
        this.parameters = parameters;
        this.returnType = returnType;
        this.throwing = throwing;
    }

    /**
     * Generates the jvm method signature of this method.
     * @return The jvm method signature of this method as a String.
     * @throws IllegalStateException If parameters or returnType have not been defined for this method.
     */
    public String jvmMethodSignature() {
        if(parameters == null)
            throw new IllegalStateException("Cannot build jvm method signature without defined parameters.");
        else if(returnType == null)
            throw new IllegalStateException("Cannot build jvm method signature without defined return type.");

        StringBuilder builder = new StringBuilder("(");
        parameters.getParamTypes().forEach(type -> builder.append(type.getJvmTypeDefinition()));
        builder.append(')');
        builder.append(returnType.getJvmTypeDefinition());
        return builder.toString();
    }

    /**
     * A method definition is considered "incomplete" by default, and is usually created when some method details are
     * meant to be implied during bytecode generation in the code builders.
     * This method reads from the method building context and other info to provide a completed method definition.
     * @param numParameters The number of parameters this method should have.
     * @return A completed method definition which has all of the details filled in about this method.
     * @throws IllegalStateException If no method or constructor can be found which has the parameter types and owner
     * type compatible with the types currently loaded on the stack, and the same number of parameters.
     * @throws IllegalStateException If this method definition describes an instance method, and if the method owner is
     * implied from the type stack, and if the method owner from the type stack is invalid.
     */
    public CompleteMethodDefinition completeDefinition(int numParameters) {
        TypeDefinition methodOwner = this.owner;
        ParametersDefinition parameters = this.parameters;

        int stackSize = context().stackSize();

        //Resolve the method owner if it's an instance method and the method owner has not already been specified
        if(methodOwner == null) {
            //Guaranteed to never be null if the method is static
            methodOwner = context().getTypeStack().get(stackSize - (numParameters+1));

            //If the implied method owner from the stack cannot contain methods then throw an exception
            if(!methodOwner.canHaveMembers())
                throw new IllegalStateException(validateMethodOwnerType(methodOwner));
        }

        //Resolve the method parameter types if the parameter types weren't specified
        if(parameters == null) {
            TypeDefinition[] paramTypes = new TypeDefinition[numParameters];
            for(int i = 0, j = stackSize - numParameters; i < numParameters; i++, j++) {
                paramTypes[i] = context().getTypeStack().get(j);
            }
            parameters = parameters(paramTypes);
        }

        boolean isConstructor = this.name.isConstructorName();
        Optional<? extends CompleteMethodDefinition> foundDefinitionOpt = Optional.empty();
        if(isConstructor) {
            foundDefinitionOpt = methodOwner.findDeclaredMatchingConstructors(parameters)
                .stream()
                .filter(c -> AccessModifiers.isAccessible(type(ThisClass.class), c.owner, c.modifiers))
                .findFirst();
        }
        else if(this.modifiers != null && this.modifiers.isStatic()) {
            foundDefinitionOpt = methodOwner.findDeclaredMatchingMethods(this.name, parameters)
                .stream()
                .filter(m -> AccessModifiers.isAccessible(type(ThisClass.class), m.owner, m.modifiers))
                .findFirst();
        }
        else {
            List<TypeDefinition> hierarchy = methodOwner.flatHierarchy();
            for(TypeDefinition current: hierarchy) {
                foundDefinitionOpt = current.findDeclaredMatchingMethods(this.name, parameters)
                    .stream()
                    .filter(m -> AccessModifiers.isAccessible(type(ThisClass.class), m.owner, m.modifiers))
                    .findFirst();

                if(foundDefinitionOpt.isPresent())
                    break;
            }
        }

        if(foundDefinitionOpt.isEmpty()) {
            if(isConstructor)
                throw new IllegalStateException(getConstructorNotFoundMessage(methodOwner.getClassName(), parameters));
            else
                throw new IllegalStateException(getMethodNotFoundMessage(this.name.getName(), methodOwner.getClassName(), parameters));
        }

        return foundDefinitionOpt.get();
    }

    /**
     * Validates a method owner type argument by throwing an exception if that method owner type is invalid.
     * This validation method is intended to be used to validate arguments.
     * @param ownerType The method owner type to validate.
     * @throws IllegalArgumentException If the specified owner type is void, primitive, or an array.
     */
    protected static void validateMethodOwnerArgument(TypeDefinition ownerType) {
        String message = validateMethodOwnerType(ownerType);
        if(message != null)
            throw new IllegalArgumentException(message);
    }

    /**
     * Determines if a given type definition is capable of being a method owner, and provides a reason why the given
     * type cannot be a method owner.
     * @param ownerType The type to validate as a method owner.
     * @return Returns null if ownerType is capable of owning a method. Otherwise, a String is returned detailing the
     * reason why the ownerType cannot contain a method.
     */
    protected static String validateMethodOwnerType(TypeDefinition ownerType) {
        if(ownerType.isVoid())
            return "Method owner type cannot be void.";
        else if(ownerType.getType().isPrimitive())
            return "Method owner type cannot be a primitive type.";
        else if(ownerType.getType().isArray())
            return "Method owner type cannot be an array type.";
        else
            return null;
    }

    /**
     * Produces a message when a method is not found in a class matching the given parameter types.
     * @param methodName The name of the method that could not be found.
     * @param methodOwnerName The name of the method owner that did not contain the desired method.
     * @param parameters The parameters that may have failed to match on a method.
     * @return A string message which details the method that was not found and the list of expected parameter types.
     */
    protected static String getMethodNotFoundMessage(String methodName, String methodOwnerName, ParametersDefinition parameters) {
        String parametersText = createParametersString(parameters);
        return """
            Method '%s' not found in class '%s' with parameters:
            %s""".formatted(methodName, methodOwnerName, parametersText);
    }

    /**
     * Produces a message when a constructor is not found in a class matching the given parameter types.
     * @param constructorOwnerName The name of the constructor owner that did not contain the desired constructor.
     * @param parameters The parameters that may have failed to match on a constructor.
     * @return A string message which details the constructor that was not found and the list of expected parameter types.
     */
    protected static String getConstructorNotFoundMessage(String constructorOwnerName, ParametersDefinition parameters) {
        String parametersText = createParametersString(parameters);
        return """
            Constructor not found in class '%s' with parameters:
            %s""".formatted(constructorOwnerName, parametersText);
    }

    private static String createParametersString(ParametersDefinition parameters) {
        String[] paramClassNames = parameters.getParamTypes()
            .stream()
            .map(TypeDefinition::getClassName)
            .toArray(String[]::new);
        return '\t' + StringUtils.joinWith("\n\t", (Object[])paramClassNames);
    }
}