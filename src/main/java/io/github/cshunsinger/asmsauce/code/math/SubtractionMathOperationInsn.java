package io.github.cshunsinger.asmsauce.code.math;

import io.github.cshunsinger.asmsauce.code.CodeInsnBuilderLike;
import io.github.cshunsinger.asmsauce.code.branch.condition.ConditionBuilderLike;

import static org.objectweb.asm.Opcodes.*;

/**
 * Code builder for subtracting one operand from another.
 */
public class SubtractionMathOperationInsn extends MathOperationInsn implements MathOperandInstance, ConditionBuilderLike {
    /**
     * New subtraction operation.
     * @param operand The code builder to stack the second operand.
     */
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