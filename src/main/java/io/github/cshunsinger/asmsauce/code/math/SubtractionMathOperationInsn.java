package io.github.cshunsinger.asmsauce.code.math;

import io.github.cshunsinger.asmsauce.code.CodeInsnBuilderLike;
import io.github.cshunsinger.asmsauce.code.branch.condition.ConditionBuilderLike;

import static org.objectweb.asm.Opcodes.*;

public class SubtractionMathOperationInsn extends MathOperationInsn implements MathOperandInstance, ConditionBuilderLike {
    public SubtractionMathOperationInsn(CodeInsnBuilderLike operand) {
        super(operand);
    }

    @Override
    public int intOperator() {
        return ISUB;
    }

    @Override
    public int longOperator() {
        return LSUB;
    }

    @Override
    public int floatOperator() {
        return FSUB;
    }

    @Override
    public int doubleOperator() {
        return DSUB;
    }
}