package io.github.chunsinger.asmsauce.code.math;

import io.github.chunsinger.asmsauce.code.CodeInsnBuilderLike;
import io.github.chunsinger.asmsauce.code.branch.condition.ConditionBuilderLike;

import static aj.org.objectweb.asm.Opcodes.*;

public class ModulusMathOperationInsn extends MathOperationInsn implements MathOperandInstance, ConditionBuilderLike {
    public ModulusMathOperationInsn(CodeInsnBuilderLike operand) {
        super(operand);
    }

    @Override
    public int intOperator() {
        return IREM;
    }

    @Override
    public int longOperator() {
        return LREM;
    }

    @Override
    public int floatOperator() {
        return FREM;
    }

    @Override
    public int doubleOperator() {
        return DREM;
    }
}