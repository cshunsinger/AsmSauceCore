package io.github.cshunsinger.asmsauce.code.math;

import io.github.cshunsinger.asmsauce.code.CodeInsnBuilderLike;
import io.github.cshunsinger.asmsauce.code.branch.condition.ConditionBuilderLike;

import static org.objectweb.asm.Opcodes.*;

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