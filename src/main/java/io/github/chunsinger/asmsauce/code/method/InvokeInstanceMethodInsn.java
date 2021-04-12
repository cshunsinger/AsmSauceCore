package io.github.chunsinger.asmsauce.code.method;

import io.github.chunsinger.asmsauce.MethodBuildingContext;
import io.github.chunsinger.asmsauce.code.CodeInsnBuilderLike;
import io.github.chunsinger.asmsauce.code.branch.condition.BooleanConditionBuilderLike;
import io.github.chunsinger.asmsauce.code.branch.condition.ConditionBuilderLike;
import io.github.chunsinger.asmsauce.code.branch.condition.NullConditionBuilderLike;
import io.github.chunsinger.asmsauce.code.cast.ImplicitConversionInsn;
import io.github.chunsinger.asmsauce.code.field.FieldAccessibleInstance;
import io.github.chunsinger.asmsauce.code.field.FieldAssignableInstance;
import io.github.chunsinger.asmsauce.definitions.CompleteMethodDefinition;
import io.github.chunsinger.asmsauce.definitions.MethodDefinition;
import io.github.chunsinger.asmsauce.definitions.NameDefinition;
import io.github.chunsinger.asmsauce.definitions.TypeDefinition;
import io.github.chunsinger.asmsauce.DefinitionBuilders;

import java.lang.reflect.Method;

import static io.github.chunsinger.asmsauce.modifiers.AccessModifiers.customAccess;

public class InvokeInstanceMethodInsn extends InvocationInsn implements
    InvokableInstance, FieldAccessibleInstance, FieldAssignableInstance,
    ConditionBuilderLike, BooleanConditionBuilderLike, NullConditionBuilderLike {
    @SuppressWarnings("unchecked")
    public InvokeInstanceMethodInsn(TypeDefinition<?> thisType, Method method, CodeInsnBuilderLike... codeBuilders) {
        super(new CompleteMethodDefinition<>(
            thisType,
            customAccess(validateMethod(method).getModifiers()),
            DefinitionBuilders.name(method.getName()),
            DefinitionBuilders.type(method.getReturnType()),
            DefinitionBuilders.parameters(method.getParameterTypes()),
            DefinitionBuilders.throwing((Class<? extends Exception>[])method.getExceptionTypes())
        ), codeBuilders);
    }

    public InvokeInstanceMethodInsn(NameDefinition methodName, CodeInsnBuilderLike... codeBuilders) {
        super(new MethodDefinition<>(null, null, methodName, null, null, null), codeBuilders);
    }

    private static Method validateMethod(Method method) {
        if(method == null)
            throw new IllegalArgumentException("Method cannot be null.");
        return method;
    }

    @Override
    public void build(MethodBuildingContext context) {
        //The element at the top of the stack is what is the element that the instance method will be invoked against
        //Perform any necessary implicit casting first
        if(method.getOwner() != null) //Ignore implicit cast if method owner type will be implied
            new ImplicitConversionInsn(super.method.getOwner()).build(context);

        //Perform the method call
        super.build(context);
    }
}