package io.github.cshunsinger.asmsauce.definitions;

import io.github.cshunsinger.asmsauce.*;
import io.github.cshunsinger.asmsauce.modifiers.AccessModifiers;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.MethodUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static io.github.cshunsinger.asmsauce.DefinitionBuilders.*;
import static io.github.cshunsinger.asmsauce.modifiers.AccessModifiers.customAccess;

/**
 * This class defines a method and contains all of its metadata. The data to define a method in this class may be
 * "incomplete" and more information might be required in order for bytecode to be generated.
 * @param <O> The type which owns this method being defined.
 * @param <R> The return type of the defined method.
 */
@Getter
public class MethodDefinition<O, R> {
    /**
     * @return The type which owns this defined method.
     */
    protected final TypeDefinition<O> owner;
    /**
     * @return The access modifiers of this defined method.
     */
    protected final AccessModifiers modifiers;
    /**
     * @return The name of this defined method.
     */
    protected final NameDefinition name;
    /**
     * @return The set of parameters of this defined method.
     */
    protected final ParametersDefinition parameters;
    /**
     * @return The return type of this defined method.
     */
    protected final TypeDefinition<R> returnType;
    /**
     * @return The definition of exceptions thrown by this method.
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
    public MethodDefinition(TypeDefinition<O> owner,
                            AccessModifiers modifiers,
                            NameDefinition name,
                            ParametersDefinition parameters,
                            TypeDefinition<R> returnType,
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
     * @param buildingContext The method building context.
     * @param numParameters The number of parameters this method should have.
     * @return A completed method definition which has all of the details filled in about this method.
     * @throws IllegalStateException If not method can be found which has the parameter types and owner type compatible
     *                               with the types currently loaded on the stack, and the same number of parameters.
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public CompleteMethodDefinition<?, ?> completeDefinition(MethodBuildingContext buildingContext, int numParameters) {
        //Get the parameter types based on what exists on the stack currently
        List<TypeDefinition<?>> paramTypes = new ArrayList<>();
        for(int i = numParameters-1; i >= 0; i--) {
            paramTypes.add(0, buildingContext.popStack());
        }

        //Get the type that is supposed to own this method
        boolean staticMethod = modifiers != null && modifiers.isStatic();
        TypeDefinition<?> ownerType = staticMethod ? this.owner : buildingContext.peekStack();

        //Re-stack the parameter types because this method should not be modifying the type stack
        for(int i = 0; i < numParameters; i++) {
            buildingContext.pushStack(paramTypes.get(i));
        }

        //Do some mid-flight validations on the method owner type if this is not a static method
        if(!staticMethod)
            validateMethodOwner(ownerType);

        Optional<CompleteMethodDefinition<?, ?>> completedOpt;
        if(ownerType.getType() == ThisClass.class) {
            if(name.isConstructorName()) {
                completedOpt = attemptFindConstructor(
                    buildingContext.getClassContext(),
                    paramTypes
                ).map(MethodNode::getDefinition);
            }
            else {
                //Attempt to find a method in the class being built right now
                completedOpt = attemptFindMethod(
                    buildingContext.getClassContext(),
                    paramTypes
                ).map(MethodNode::getDefinition);

                //If no method is found in the class being built right now, then attempt to find the method in the
                //super classes or implemented interfaces of the class being built right now.
                if(completedOpt.isEmpty()) {
                    //Attempt to locate method in superclass lineage
                    Optional<Method> methodOpt = attemptFindMethod(buildingContext, buildingContext.getClassContext().getSuperclass(), paramTypes);

                    //Check interfaces if nothing found
                    if(methodOpt.isEmpty()) {
                        //For every interface implemented by the class currently being generated, check it as well as
                        //all super-interfaces in the interface inheritance tree for the method existing.
                        methodOpt = buildingContext.getClassContext().getInterfaces().stream()
                            .map(rootInterfaceType -> Stream.concat(Stream.of(rootInterfaceType), Stream.of(rootInterfaceType.getInterfaces()))
                                .map(interfaceType -> attemptFindMethod(buildingContext, interfaceType, paramTypes))
                                .filter(Optional::isPresent)
                                .map(Optional::get)
                                .findFirst()
                            )
                            .filter(Optional::isPresent)
                            .map(Optional::get)
                            .findFirst();
                    }

                    //If a method was found in the class inheritance structure of the class being genrated, then create
                    //a completed method definition from the found method.
                    completedOpt = methodOpt.map(method -> new CompleteMethodDefinition<>(
                        DefinitionBuilders.type(method.getDeclaringClass()),
                        customAccess(method.getModifiers()),
                        DefinitionBuilders.name(method.getName()),
                        DefinitionBuilders.type(method.getReturnType()),
                        parameters(method.getParameterTypes()),
                        DefinitionBuilders.throwing((Class[])method.getExceptionTypes())
                    ));
                }
            }
        }
        else {
            if(name.isConstructorName()) {
                completedOpt = attemptFindConstructor(buildingContext, ownerType.getType(), paramTypes).map(constructor -> new CompleteMethodDefinition<>(
                    DefinitionBuilders.type(constructor.getDeclaringClass()),
                    customAccess(constructor.getModifiers()),
                    this.name,
                    DefinitionBuilders.voidType(),
                    parameters(constructor.getParameterTypes()),
                    DefinitionBuilders.throwing((Class[])constructor.getExceptionTypes())
                ));
            }
            else {
                completedOpt = attemptFindMethod(buildingContext, ownerType.getType(), paramTypes).map(method -> new CompleteMethodDefinition<>(
                    DefinitionBuilders.type(method.getDeclaringClass()),
                    customAccess(method.getModifiers()),
                    DefinitionBuilders.name(method.getName()),
                    DefinitionBuilders.type(method.getReturnType()),
                    parameters(method.getParameterTypes()),
                    DefinitionBuilders.throwing((Class[])method.getExceptionTypes())
                ));
            }
        }

        if(completedOpt.isEmpty()) {
            String ownerTypeName = ownerType.getType() == ThisClass.class ? buildingContext.getClassContext().getClassName() : ownerType.getClassName();
            String exceptionMessage;

            if(this.getName().isConstructorName())
                exceptionMessage = getConstructorNotFoundMessage(ownerTypeName, paramTypes);
            else
                exceptionMessage = getMethodNotFoundMessage(this.getName().getName(), ownerTypeName, paramTypes);

            throw new IllegalStateException(exceptionMessage);
        }
        else
            return completedOpt.get();
    }

    /**
     * Attempts to find a method in a class, which can be invoked using the specified parameter types.
     * @param ownerClass The class to search in for a method.
     * @param paramTypes The parameter types.
     * @return Returns a method from the specified ownerClass if one can be found whose parameter types are all assignable
     * from the given paramTypes and whose parameter count matches the number of provided paramTypes.
     * Returns an empty Optional if no method can be found matching the supplied param types.
     */
    protected Optional<Method> attemptFindMethod(MethodBuildingContext buildingContext, Class<?> ownerClass, List<TypeDefinition<?>> paramTypes) {
        Class<?>[] paramClasses = paramTypes.stream().map(TypeDefinition::getType).toArray(Class[]::new);
        Method method = MethodUtils.getMatchingMethod(ownerClass, this.getName().getName(), paramClasses);
        return Optional.ofNullable(method)
            .filter(m -> AccessModifiers.isAccessible(
                buildingContext.getClassContext(),
                ThisClass.class,
                m.getDeclaringClass(),
                customAccess(m.getModifiers())
            ));
    }

    /**
     * Attempts to find a constructor in a class, which can be invoked using the specified parameter types.
     * @param ownerClass The class to search in for a constructor.
     * @param paramTypes The parameter types.
     * @return Returns a constructor from the specified ownerClass if one can be found whose parameter types are all
     * assignable from the given paramTypes and whose parameter count matches the number of provided paramTypes.
     * Returns an empty Optional if no constructor can be found matching the supplied param types.
     */
    protected Optional<Constructor<?>> attemptFindConstructor(MethodBuildingContext buildingContext, Class<?> ownerClass, List<TypeDefinition<?>> paramTypes) {
        Class<?>[] paramClasses = paramTypes.stream().map(TypeDefinition::getType).toArray(Class[]::new);

        //First attempt to find a perfect match
        try {
            return filterByAccessModifiers(buildingContext, Optional.of(ownerClass.getDeclaredConstructor(paramClasses)));
        }
        catch(Exception ignored) {}

        //Then attempt to find an assignment-compatible match
        List<TypeDefinition<?>> actualTypes = parameters(paramClasses).getParamTypes();
        return filterByAccessModifiers(buildingContext,
            Stream.of(ownerClass.getDeclaredConstructors())
                .filter(constructor -> {
                    List<TypeDefinition<?>> requiredTypes = parameters(constructor.getParameterTypes()).getParamTypes();
                    return doParametersMatch(null, requiredTypes, actualTypes);
                })
                .findFirst()
        );
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private <T extends Executable> Optional<T> filterByAccessModifiers(MethodBuildingContext context, Optional<T> optional) {
        return optional.filter(e -> AccessModifiers.isAccessible(
            context.getClassContext(),
            ThisClass.class,
            e.getDeclaringClass(),
            customAccess(e.getModifiers())
        ));
    }

    /**
     * Attempts to find a method defined within the class being built that matches the required method by name,
     * static flag, parameter count, and parameter types.
     * @param classContext Class building context, which contains information about the other methods that exist in this class being built.
     * @param paramTypes The parameter types.
     * @return Returns a MethodNode from the class currently being built if one is found whose parameters match the supplied
     * paramTypes. Otherwise an empty Optional is returned.
     */
    protected Optional<MethodNode> attemptFindMethod(ClassBuildingContext classContext, List<TypeDefinition<?>> paramTypes) {
        return classContext.getMethods()
            .stream()
            .filter(node -> {
                //Name must match for method to potentially match
                MethodDefinition<?, ?> def = node.getDefinition();
                if(!def.getName().getName().equals(this.getName().getName()))
                    return false;

                List<TypeDefinition<?>> types = def.getParameters().getParamTypes();

                //Check that the actual param types are all assignable to the required param types
                return doParametersMatch(classContext, types, paramTypes);
            })
            .findFirst();
    }

    /**
     * Attempts to find a constructor defined within the class being built that matches by parameter types.
     * @param classContext The class building context.
     * @param paramTypes The parameter types.
     * @return Returns a ConstructorNode from the class currently being built if one is found whose parameters match the
     * supplied paramTypes. Otherwise an empty Optional is returned.
     */
    protected Optional<ConstructorNode> attemptFindConstructor(ClassBuildingContext classContext, List<TypeDefinition<?>> paramTypes) {
        return classContext.getConstructors()
            .stream()
            .filter(node -> {
                MethodDefinition<?, ?> constructorDef = node.getDefinition();

                //Check that the actual param types are all assignable to the required param types
                List<TypeDefinition<?>> requiredTypes = constructorDef.getParameters().getParamTypes();

                return doParametersMatch(classContext, requiredTypes, paramTypes);
            })
            .findFirst();
    }

    private boolean doParametersMatch(ClassBuildingContext classContext, List<TypeDefinition<?>> requiredTypes, List<TypeDefinition<?>> actualTypes) {
        if(requiredTypes.size() != actualTypes.size())
            return false;

        for(int i = 0; i < actualTypes.size(); i++) {
            TypeDefinition<?> requiredType = requiredTypes.get(i);
            TypeDefinition<?> actualType = actualTypes.get(i);

            if(actualType.equals(requiredType))
                continue;

            //If types do not match then check if actual type is assignable to required type
            Class<?> requiredClass = requiredType.getType();
            Class<?> actualClass = actualType.getType();

            if(actualClass == ThisClass.class) {
                //If one of ThisClass's interfaces or supertype is assignable to the required parameter
                //then ThisClass will also be assignable to the required parameter
                List<Class<?>> otherActualTypes = new ArrayList<>();
                otherActualTypes.add(classContext.getSuperclass());
                otherActualTypes.addAll(classContext.getInterfaces());
                boolean matching = otherActualTypes.stream().anyMatch(requiredClass::isAssignableFrom);
                if(!matching)
                    return false;
            }
            else if(!requiredClass.isAssignableFrom(actualClass)) {
                //If the actual parameter value cannot be assigned to the required parameter type
                //then there's no match.
                return false;
            }
        }

        return true;
    }

    /**
     * Validates a method owner by throwing an exception if that method owner type is invalid.
     * This validation method is intended to validate a resolved method owner during the class building process.
     * @param ownerType The method owner type to validate.
     * @throws IllegalStateException If the specified owner type is void, primitive, or an array.
     */
    protected static void validateMethodOwner(TypeDefinition<?> ownerType) {
        String message = validateMethodOwnerType(ownerType);
        if(message != null)
            throw new IllegalStateException(message);
    }

    /**
     * Validates a method owner type argument by throwing an exception if that method owner type is invalid.
     * This validation method is intended to be used to validate arguments.
     * @param ownerType The method owner type to validate.
     * @throws IllegalArgumentException If the specified owner type is void, primitive, or an array.
     */
    protected static void validateMethodOwnerArgument(TypeDefinition<?> ownerType) {
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
    protected static String validateMethodOwnerType(TypeDefinition<?> ownerType) {
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
     * @param paramTypes The list of parameter types that may have failed to match on a method.
     * @return A string message which details the method that was not found and the list of expected parameter types.
     */
    protected static String getMethodNotFoundMessage(String methodName, String methodOwnerName, List<TypeDefinition<?>> paramTypes) {
        String parametersText = createParametersString(paramTypes);
        return """
            Method '%s' not found in class '%s' with parameters:
            %s""".formatted(methodName, methodOwnerName, parametersText);
    }

    /**
     * Produces a message when a constructor is not found in a class matching the given parameter types.
     * @param constructorOwnerName The name of the constructor owner that did not contain the desired constructor.
     * @param paramTypes The list of parameter types that may have failed to match on a constructor.
     * @return A string message which details the constructor that was not found and the list of expected parameter types.
     */
    protected static String getConstructorNotFoundMessage(String constructorOwnerName, List<TypeDefinition<?>> paramTypes) {
        String parametersText = createParametersString(paramTypes);
        return """
            Constructor not found in class '%s' with parameters:
            %s""".formatted(constructorOwnerName, parametersText);
    }

    private static String createParametersString(List<TypeDefinition<?>> paramTypes) {
        String[] paramClassNames = paramTypes.stream().map(TypeDefinition::getClassName).toArray(String[]::new);
        return '\t' + StringUtils.joinWith("\n\t", (Object[])paramClassNames);
    }
}