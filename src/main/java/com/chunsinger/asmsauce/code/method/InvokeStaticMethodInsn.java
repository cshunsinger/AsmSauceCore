package com.chunsinger.asmsauce.code.method;

import com.chunsinger.asmsauce.MethodBuildingContext;
import com.chunsinger.asmsauce.code.CodeInsnBuilderLike;
import com.chunsinger.asmsauce.code.branch.condition.BooleanConditionBuilderLike;
import com.chunsinger.asmsauce.code.branch.condition.ConditionBuilderLike;
import com.chunsinger.asmsauce.code.branch.condition.NullConditionBuilderLike;
import com.chunsinger.asmsauce.code.field.FieldAccessibleInstance;
import com.chunsinger.asmsauce.code.field.FieldAssignableInstance;
import com.chunsinger.asmsauce.code.math.MathOperandInstance;
import com.chunsinger.asmsauce.definitions.*;

import static com.chunsinger.asmsauce.modifiers.AccessModifiers.customAccess;

public class InvokeStaticMethodInsn extends InvocationInsn implements
    InvokableInstance, FieldAccessibleInstance, FieldAssignableInstance, MathOperandInstance,
    ConditionBuilderLike, BooleanConditionBuilderLike, NullConditionBuilderLike {
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