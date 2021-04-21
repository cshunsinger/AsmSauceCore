package io.github.cshunsinger.asmsauce;

import io.github.cshunsinger.asmsauce.code.CodeInsnBuilder;
import io.github.cshunsinger.asmsauce.code.CodeInsnBuilderLike;
import io.github.cshunsinger.asmsauce.code.method.InvokeBaseConstructorInsn;
import io.github.cshunsinger.asmsauce.definitions.CompleteMethodDefinition;
import io.github.cshunsinger.asmsauce.definitions.ParametersDefinition;
import io.github.cshunsinger.asmsauce.definitions.ThrowsDefinition;
import io.github.cshunsinger.asmsauce.modifiers.AccessModifiers;
import org.apache.commons.lang3.ArrayUtils;

/**
 * Represents a constructor being built on a new class.
 * This class defines the constructor parameters as well as the code body of the constructor in the class being generated.
 */
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

    /**
     * Creates a constructor to be generated in a new class.
     * @param modifiers The access modifiers of the constructor.
     * @param parameters The set of parameters of the constructor.
     * @param superInvoker A code builder to invoke another constructor, either in this class being generated or in the superclass.
     * @param constructorBody The code body of the new constructor.
     * @return A new constructor node to generate a constructor in a generated class.
     */
    public static ConstructorNode constructor(AccessModifiers modifiers,
                                              ParametersDefinition parameters,
                                              InvokeBaseConstructorInsn superInvoker,
                                              CodeInsnBuilder... constructorBody) {
        return constructor(modifiers, parameters, DefinitionBuilders.noThrows(), superInvoker, constructorBody);
    }

    /**
     * Creates a constructor to be generated in a new class.
     * @param modifiers The access modifiers of the constructor.
     * @param parameters The set of parameters of the constructor.
     * @param throwsDefinition The set of exceptions that can be thrown by the constructor.
     * @param superInvoker A code builder to invoke another constructor, either in this class being generated or in the superclass.
     * @param constructorBody The code body of the new constructor.
     * @return A new constructor node to generate a constructor in a generated class.
     */
    public static ConstructorNode constructor(AccessModifiers modifiers,
                                              ParametersDefinition parameters,
                                              ThrowsDefinition throwsDefinition,
                                              InvokeBaseConstructorInsn superInvoker,
                                              CodeInsnBuilderLike... constructorBody) {
        return new ConstructorNode(modifiers, parameters, throwsDefinition, superInvoker, constructorBody);
    }
}