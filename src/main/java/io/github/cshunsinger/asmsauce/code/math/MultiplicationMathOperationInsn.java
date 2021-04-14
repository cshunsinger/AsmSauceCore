package io.github.cshunsinger.asmsauce.code.math;

import io.github.cshunsinger.asmsauce.code.CodeInsnBuilderLike;
import io.github.cshunsinger.asmsauce.code.branch.condition.ConditionBuilderLike;

import static org.objectweb.asm.Opcodes.*;

/**
 * Code builder for multiplying two operands.
 */
public class MultiplicationMathOperationInsn extends MathOperationInsn implements MathOperandInstance, ConditionBuilderLike {
    /**
     * New multiplication operation.
     * @param operand The code builder to stack the second operand.
     */
    public MultiplicationMathOperationInsn(CodeInsnBuilderLike operand) {
        super(operand);
    }

    @Override
    public int intOperator() {
        return IMUL;
    }

    @Override
    public int longOperator() {
        return LMUL;
    }

    @Override
    public int floatOperator() {
        return FMUL;
    }

    @Override
    public int doubleOperator() {
        return DMUL;
    }
}