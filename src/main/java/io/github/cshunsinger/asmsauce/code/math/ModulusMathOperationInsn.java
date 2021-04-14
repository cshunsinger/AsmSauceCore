package io.github.cshunsinger.asmsauce.code.math;

import io.github.cshunsinger.asmsauce.code.CodeInsnBuilderLike;
import io.github.cshunsinger.asmsauce.code.branch.condition.ConditionBuilderLike;

import static org.objectweb.asm.Opcodes.*;

/**
 * Code builder for getting the modulus of one operand with another.
 */
public class ModulusMathOperationInsn extends MathOperationInsn implements MathOperandInstance, ConditionBuilderLike {
    /**
     * New modulus operation.
     * @param operand The code builder to stack the second operand.
     */
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