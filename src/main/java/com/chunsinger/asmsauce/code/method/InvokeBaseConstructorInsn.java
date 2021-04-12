package com.chunsinger.asmsauce.code.method;

import com.chunsinger.asmsauce.DefinitionBuilders;
import com.chunsinger.asmsauce.MethodBuildingContext;
import com.chunsinger.asmsauce.code.CodeInsnBuilderLike;
import com.chunsinger.asmsauce.definitions.*;

import java.lang.reflect.Constructor;

import static com.chunsinger.asmsauce.DefinitionBuilders.*;
import static com.chunsinger.asmsauce.code.CodeBuilders.this_;
import static com.chunsinger.asmsauce.modifiers.AccessModifiers.customAccess;

public class InvokeBaseConstructorInsn extends InvocationInsn {
    @SuppressWarnings("unchecked")
    public InvokeBaseConstructorInsn(Constructor<?> constructor, CodeInsnBuilderLike... paramStackBuilders) {
        //Only need to use throwIfNull for first parameter. After that, constructor is verified as non-null if no exception was thrown
        this(
            type(throwIfNull(constructor).getDeclaringClass()),
            DefinitionBuilders.parameters(constructor.getParameterTypes()),
            throwing((Class<? extends Exception>[])constructor.getExceptionTypes()),
            paramStackBuilders
        );
    }

    private static Constructor<?> throwIfNull(Constructor<?> c) {
        if(c == null)
            throw new IllegalArgumentException("Constructor cannot be null.");
        else
            return c;
    }

    public InvokeBaseConstructorInsn(TypeDefinition<?> owner, ParametersDefinition parameters, ThrowsDefinition throwsDefinition, CodeInsnBuilderLike... paramStackBuilders) {
        super(new CompleteMethodDefinition<>(
            owner,
            customAccess(0), //access level is irrelevant here
            NameDefinition.CONSTRUCTOR_NAME_DEFINITION,
            voidType(),
            parameters,
            throwsDefinition
        ), paramStackBuilders);
    }

    @Override
    public void build(MethodBuildingContext context) {
        //Stack "this" onto the stack
        this_().build(context);

        //Generate bytecode to load the parameters, and then generate bytecode to invoke the constructor
        super.build(context);
    }
}