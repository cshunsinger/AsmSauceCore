package io.github.cshunsinger.asmsauce.code.method;

import io.github.cshunsinger.asmsauce.MethodBuildingContext;
import io.github.cshunsinger.asmsauce.code.CodeInsnBuilderLike;
import io.github.cshunsinger.asmsauce.code.branch.condition.BooleanConditionBuilderLike;
import io.github.cshunsinger.asmsauce.code.branch.condition.ConditionBuilderLike;
import io.github.cshunsinger.asmsauce.code.branch.condition.NullConditionBuilderLike;
import io.github.cshunsinger.asmsauce.code.field.FieldAccessibleInstance;
import io.github.cshunsinger.asmsauce.code.field.FieldAssignableInstance;
import io.github.cshunsinger.asmsauce.code.math.MathOperandInstance;
import io.github.cshunsinger.asmsauce.definitions.*;

import static io.github.cshunsinger.asmsauce.modifiers.AccessModifiers.customAccess;

/**
 * Code builder for invoking a static method.
 */
public class InvokeStaticMethodInsn extends InvocationInsn implements
    InvokableInstance, FieldAccessibleInstance, FieldAssignableInstance, MathOperandInstance,
    ConditionBuilderLike, BooleanConditionBuilderLike, NullConditionBuilderLike {

    /**
     * New code builder to invoke a static method.
     * @param type The type that contains the static method.
     * @param methodName The name of the static method.
     * @param methodParameters The parameters of the static method.
     * @param returnType The return type of the static method.
     * @param throwsDefinition The throws clause of the static method.
     * @param paramBuilders The code builders to stack the parameters to pass to the static method.
     */
    public InvokeStaticMethodInsn(TypeDefinition<?> type, NameDefinition methodName, ParametersDefinition methodParameters, TypeDefinition<?> returnType, ThrowsDefinition throwsDefinition, CodeInsnBuilderLike... paramBuilders) {
        super(new CompleteMethodDefinition<>(
            type,
            customAccess(0).withStatic(),
            methodName,
            returnType,
            methodParameters,
            throwsDefinition
        ), paramBuilders);
    }

    /**
     * New code builder to invoke a static method created from minimal details.
     * @param ownerType The type that contains the static method.
     * @param name The name of the static method.
     * @param paramBuilders The code builders which will stack the parameter values to pass to the static method.
     */
    public InvokeStaticMethodInsn(TypeDefinition<?> ownerType, NameDefinition name, CodeInsnBuilderLike... paramBuilders) {
        super(new MethodDefinition<>(
            ownerType,
            customAccess(0).withStatic(),
            name,
            null,
            null,
            null), paramBuilders);
    }

    @Override
    protected void invokeMethodVisitor(MethodBuildingContext context) {
        validateMethodIsStatic(super.method);
        super.invokeMethodVisitor(context);
    }

    private static void validateMethodIsStatic(MethodDefinition<?, ?> methodDefinition) {
        if(methodDefinition.getModifiers().isStatic())
            return;

        String exceptionMessage = "Method '%s' in class '%s' is not static.".formatted(
            methodDefinition.getName().getName(),
            methodDefinition.getOwner().getClassName()
        );
        throw new IllegalStateException(exceptionMessage);
    }
}