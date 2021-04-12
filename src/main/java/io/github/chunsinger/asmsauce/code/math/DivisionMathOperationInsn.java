package io.github.chunsinger.asmsauce.code.math;

import io.github.chunsinger.asmsauce.code.CodeInsnBuilderLike;
import io.github.chunsinger.asmsauce.code.branch.condition.ConditionBuilderLike;

import static aj.org.objectweb.asm.Opcodes.*;

public class DivisionMathOperationInsn extends MathOperationInsn implements MathOperandInstance, ConditionBuilderLike {
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