package io.github.chunsinger.asmsauce.definitions;

import io.github.chunsinger.asmsauce.*;
import io.github.chunsinger.asmsauce.*;
import io.github.chunsinger.asmsauce.modifiers.AccessModifiers;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.ConstructorUtils;
import org.apache.commons.lang3.reflect.MethodUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static io.github.chunsinger.asmsauce.DefinitionBuilders.*;
import static io.github.chunsinger.asmsauce.modifiers.AccessModifiers.customAccess;

@Getter
public class MethodDefinition<O, R> {
    protected final TypeDefinition<O> owner;
    protected final AccessModifiers modifiers;
    protected final NameDefinition name;
    protected final ParametersDefinition parameters;
    protected final TypeDefinition<R> returnType;
    protected final ThrowsDefinition throwing;

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
                completedOpt = attemptFindMethod(
                    buildingContext.getClassContext(),
                    paramTypes
                ).map(MethodNode::getDefinition);
            }
        }
        else {
            if(name.isConstructorName()) {
                completedOpt = attemptFindConstructor(ownerType.getType(), paramTypes).map(constructor -> new CompleteMethodDefinition<>(
                    DefinitionBuilders.type(constructor.getDeclaringClass()),
                    customAccess(constructor.getModifiers()),
                    this.name,
                    DefinitionBuilders.voidType(),
                    parameters(constructor.getParameterTypes()),
                    DefinitionBuilders.throwing((Class[])constructor.getExceptionTypes())
                ));
            }
            else {
                completedOpt = attemptFindMethod(ownerType.getType(), paramTypes).map(method -> new CompleteMethodDefinition<>(
                    DefinitionBuilders.type(method.getDeclaringClass()),
                    customAccess(method.getModifiers()),
                    DefinitionBuilders.name(method.getName()),
                    DefinitionBuilders.type(method.getReturnType()),
                    parameters(method.getParameterTypes()),
                    DefinitionBuilders.throwing((Class[])method.getExceptionTypes())
                ));
            }
        }

        completedOpt = completedOpt.filter(m -> AccessModifiers.isAccessible(
            buildingContext.getClassContext(),
            ThisClass.class,
            m.getOwner().getType(),
            m.getModifiers())
        );

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

    protected Optional<Method> attemptFindMethod(Class<?> ownerClass, List<TypeDefinition<?>> params) {
        Class<?>[] paramClasses = params.stream().map(TypeDefinition::getType).toArray(Class[]::new);
        Method method = MethodUtils.getMatchingMethod(ownerClass, this.getName().getName(), paramClasses);
        return Optional.ofNullable(method);
    }

    protected Optional<Constructor<?>> attemptFindConstructor(Class<?> ownerClass, List<TypeDefinition<?>> params) {
        Class<?>[] paramClasses = params.stream().map(TypeDefinition::getType).toArray(Class[]::new);
        Constructor<?> constructor = ConstructorUtils.getMatchingAccessibleConstructor(ownerClass, paramClasses);
        return Optional.ofNullable(constructor);
    }

    /**
     * Attempts to find a method defined within the class being built that matches the required method by name,
     * static flag, parameter count, and parameter types.
     */
    protected Optional<MethodNode> attemptFindMethod(ClassBuildingContext classContext, List<TypeDefinition<?>> params) {
        return classContext.getMethods()
            .stream()
            .filter(node -> {
                //Name must match for method to potentially match
                MethodDefinition<?, ?> def = node.getDefinition();
                if(!def.getName().getName().equals(this.getName().getName()))
                    return false;

                //If number of parameters does not match then the method does not match
                List<TypeDefinition<?>> paramTypes = def.getParameters().getParamTypes();
                if(paramTypes.size() != params.size())
                    return false;

                //Check that the actual param types are all assignable to the required param types
                return doParametersMatch(classContext, paramTypes, params);
            })
            .findFirst();
    }

    protected Optional<ConstructorNode> attemptFindConstructor(ClassBuildingContext classContext, List<TypeDefinition<?>> params) {
        return classContext.getConstructors()
            .stream()
            .filter(node -> {
                MethodDefinition<?, ?> constructorDef = node.getDefinition();

                //If number of parameters does not match then the constructor does not match
                if(params.size() != constructorDef.getParameters().count())
                    return false;

                //Check that the actual param types are all assignable to the required param types
                List<TypeDefinition<?>> requiredTypes = constructorDef.getParameters().getParamTypes();

                return doParametersMatch(classContext, requiredTypes, params);
            })
            .findFirst();
    }

    private boolean doParametersMatch(ClassBuildingContext classContext, List<TypeDefinition<?>> requiredTypes, List<TypeDefinition<?>> actualTypes) {
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

    protected static void validateMethodOwner(TypeDefinition<?> ownerType) {
        String message = validateMethodOwnerType(ownerType);
        if(message != null)
            throw new IllegalStateException(message);
    }

    protected static void validateMethodOwnerArgument(TypeDefinition<?> ownerType) {
        String message = validateMethodOwnerType(ownerType);
        if(message != null)
            throw new IllegalArgumentException(message);
    }

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

    protected static String getMethodNotFoundMessage(String methodName, String methodOwnerName, List<TypeDefinition<?>> paramTypes) {
        String parametersText = createParametersString(paramTypes);
        return """
            Method '%s' not found in class '%s' with parameters:
            %s""".formatted(methodName, methodOwnerName, parametersText);
    }

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