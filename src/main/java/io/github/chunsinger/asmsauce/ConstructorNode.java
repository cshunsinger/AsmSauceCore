package io.github.chunsinger.asmsauce;

import io.github.chunsinger.asmsauce.code.CodeInsnBuilder;
import io.github.chunsinger.asmsauce.code.CodeInsnBuilderLike;
import io.github.chunsinger.asmsauce.code.method.InvokeBaseConstructorInsn;
import io.github.chunsinger.asmsauce.definitions.CompleteMethodDefinition;
import io.github.chunsinger.asmsauce.definitions.ParametersDefinition;
import io.github.chunsinger.asmsauce.definitions.ThrowsDefinition;
import io.github.chunsinger.asmsauce.modifiers.AccessModifiers;
import org.apache.commons.lang3.ArrayUtils;

public class ConstructorNode extends MethodNode {
    private ConstructorNode(AccessModifiers modifiers,
                           ParametersDefinition parameters,
                           ThrowsDefinition throwsDefinition,
                           InvokeBaseConstructorInsn superInvoker,
                           CodeInsnBuilderLike... constructorBody) {
        super(
            new CompleteMethodDefinition<>(
                DefinitionBuilders.type(ThisClass.class),
                validateModifiers(modifiers),
                DefinitionBuilders.name("<init>"),
                DefinitionBuilders.voidType(),
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
        return constructor(modifiers, parameters, DefinitionBuilders.noThrows(), superInvoker, constructorBody);
    }

    public static ConstructorNode constructor(AccessModifiers modifiers,
                                              ParametersDefinition parameters,
                                              ThrowsDefinition throwsDefinition,
                                              InvokeBaseConstructorInsn superInvoker,
                                              CodeInsnBuilderLike... constructorBody) {
        return new ConstructorNode(modifiers, parameters, throwsDefinition, superInvoker, constructorBody);
    }
}