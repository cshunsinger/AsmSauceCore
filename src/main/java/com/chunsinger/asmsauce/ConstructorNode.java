package com.chunsinger.asmsauce;

import com.chunsinger.asmsauce.code.CodeInsnBuilder;
import com.chunsinger.asmsauce.code.CodeInsnBuilderLike;
import com.chunsinger.asmsauce.code.method.InvokeBaseConstructorInsn;
import com.chunsinger.asmsauce.definitions.CompleteMethodDefinition;
import com.chunsinger.asmsauce.definitions.ParametersDefinition;
import com.chunsinger.asmsauce.definitions.ThrowsDefinition;
import com.chunsinger.asmsauce.modifiers.AccessModifiers;
import org.apache.commons.lang3.ArrayUtils;

import static com.chunsinger.asmsauce.DefinitionBuilders.*;

public class ConstructorNode extends MethodNode {
    private ConstructorNode(AccessModifiers modifiers,
                           ParametersDefinition parameters,
                           ThrowsDefinition throwsDefinition,
                           InvokeBaseConstructorInsn superInvoker,
                           CodeInsnBuilderLike... constructorBody) {
        super(
            new CompleteMethodDefinition<>(
                type(ThisClass.class),
                validateModifiers(modifiers),
                name("<init>"),
                voidType(),
                parameters,
                throwsDefinition
            ),
            produceMethodBody(superInvoker, constructorBody)
        );
    }

    private static AccessModifiers validateModifiers(AccessModifiers modifiers) {
        //if modifiers is null then MethodNode constructor will throw an exception
        if(modifiers != null) {
            if(modifiers.isFinal())
                throw new IllegalArgumentException("A constructor cannot have the 'final' modifier.");
            if(modifiers.isStatic())
                throw new IllegalArgumentException("A constructor cannot have the 'static' modifier.");
        }

        return modifiers;
    }

    private static CodeInsnBuilderLike[] produceMethodBody(InvokeBaseConstructorInsn baseInvoker, CodeInsnBuilderLike[] constructorBody) {
        return ArrayUtils.addAll(new CodeInsnBuilderLike[] {baseInvoker}, constructorBody);
    }

    public static ConstructorNode constructor(AccessModifiers modifiers,
                                              ParametersDefinition parameters,
                                              InvokeBaseConstructorInsn superInvoker,
                                              CodeInsnBuilder... constructorBody) {
        return constructor(modifiers, parameters, noThrows(), superInvoker, constructorBody);
    }

    public static ConstructorNode constructor(AccessModifiers modifiers,
                                              ParametersDefinition parameters,
                                              ThrowsDefinition throwsDefinition,
                                              InvokeBaseConstructorInsn superInvoker,
                                              CodeInsnBuilderLike... constructorBody) {
        return new ConstructorNode(modifiers, parameters, throwsDefinition, superInvoker, constructorBody);
    }
}