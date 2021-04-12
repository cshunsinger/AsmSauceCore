package io.github.chunsinger.asmsauce.code.math;

import io.github.chunsinger.asmsauce.code.CodeInsnBuilderLike;
import io.github.chunsinger.asmsauce.code.branch.condition.ConditionBuilderLike;

import static aj.org.objectweb.asm.Opcodes.*;

public class AdditionMathOperationInsn extends MathOperationInsn implements MathOperandInstance, ConditionBuilderLike {
    public AdditionMathOperationInsn(CodeInsnBuilderLike operand) {
        super(operand);
    }

    @Override
    public int intOperator() {
        return IADD;
    }

    @Override
    public int longOperator() {
        return LADD;
    }

    @Override
    public int floatOperator() {
        return FADD;
    }

    @Override
    public int doubleOperator() {
        return DADD;
    }
}