package com.chunsinger.asmsauce.code.method;

import com.chunsinger.asmsauce.MethodBuildingContext;
import com.chunsinger.asmsauce.code.CodeInsnBuilderLike;
import com.chunsinger.asmsauce.code.branch.condition.BooleanConditionBuilderLike;
import com.chunsinger.asmsauce.code.branch.condition.ConditionBuilderLike;
import com.chunsinger.asmsauce.code.branch.condition.NullConditionBuilderLike;
import com.chunsinger.asmsauce.code.cast.ImplicitConversionInsn;
import com.chunsinger.asmsauce.code.field.FieldAccessibleInstance;
import com.chunsinger.asmsauce.code.field.FieldAssignableInstance;
import com.chunsinger.asmsauce.definitions.CompleteMethodDefinition;
import com.chunsinger.asmsauce.definitions.MethodDefinition;
import com.chunsinger.asmsauce.definitions.NameDefinition;
import com.chunsinger.asmsauce.definitions.TypeDefinition;

import java.lang.reflect.Method;

import static com.chunsinger.asmsauce.DefinitionBuilders.*;
import static com.chunsinger.asmsauce.modifiers.AccessModifiers.customAccess;

public class InvokeInstanceMethodInsn extends InvocationInsn implements
    InvokableInstance, FieldAccessibleInstance, FieldAssignableInstance,
    ConditionBuilderLike, BooleanConditionBuilderLike, NullConditionBuilderLike {
    @SuppressWarnings("unchecked")
    public InvokeInstanceMethodInsn(TypeDefinition<?> thisType, Method method, CodeInsnBuilderLike... codeBuilders) {
        super(new CompleteMethodDefinition<>(
            thisType,
            customAccess(validateMethod(method).getModifiers()),
            name(method.getName()),
            type(method.getReturnType()),
            parameters(method.getParameterTypes()),
            throwing((Class<? extends Exception>[])method.getExceptionTypes())
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