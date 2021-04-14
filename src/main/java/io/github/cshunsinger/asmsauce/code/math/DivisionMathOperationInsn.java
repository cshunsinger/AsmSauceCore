package io.github.cshunsinger.asmsauce.code.math;

import io.github.cshunsinger.asmsauce.code.CodeInsnBuilderLike;
import io.github.cshunsinger.asmsauce.code.branch.condition.ConditionBuilderLike;

import static org.objectweb.asm.Opcodes.*;

/**
 * Code builder for dividing one operand by another.
 */
public class DivisionMathOperationInsn extends MathOperationInsn implements MathOperandInstance, ConditionBuilderLike {
    /**
     * New division operation.
     * @param operand The code builder to stack the second operand.
     */
    public DivisionMathOperationInsn(CodeInsnBuilderLike operand) {
        super(operand);
    }

    @Override
    public int intOperator() {
        return IDIV;
    }

    @Override
    public int longOperator() {
        return LDIV;
    }

    @Override
    public int floatOperator() {
        return FDIV;
    }

    @Override
    public int doubleOperator() {
        return DDIV;
    }
}