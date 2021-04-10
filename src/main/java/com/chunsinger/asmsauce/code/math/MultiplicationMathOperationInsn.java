package com.chunsinger.asmsauce.code.math;

import com.chunsinger.asmsauce.code.CodeInsnBuilderLike;
import com.chunsinger.asmsauce.code.branch.condition.ConditionBuilderLike;

import static aj.org.objectweb.asm.Opcodes.*;

public class MultiplicationMathOperationInsn extends MathOperationInsn implements MathOperandInstance, ConditionBuilderLike {
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