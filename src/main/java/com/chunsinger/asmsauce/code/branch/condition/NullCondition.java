package com.chunsinger.asmsauce.code.branch.condition;

import aj.org.objectweb.asm.Label;
import com.chunsinger.asmsauce.MethodBuildingContext;
import com.chunsinger.asmsauce.code.CodeInsnBuilderLike;
import com.chunsinger.asmsauce.code.branch.Op;

public class NullCondition extends SingleOperandCondition {
    public NullCondition(CodeInsnBuilderLike operandBuilder) {
        super(operandBuilder, Op.EQ);
    }

    private NullCondition(CodeInsnBuilderLike operandBuilder, Op conditionOp) {
        super(operandBuilder, conditionOp);
    }

    @Override
    public NullCondition invert() {
        Op invertedOp = super.conditionOp == Op.NOT_EQ ? Op.EQ : Op.NOT_EQ;
        return new NullCondition(super.operandBuilder, invertedOp);
    }

    @Override
    public void build(MethodBuildingContext context, Label endLabel) {
        super.build(context, endLabel);

        if(context.peekStack().isPrimitive())
            throw new IllegalStateException("Cannot compare a primitive to null.");

        context.getMethodVisitor().visitJumpInsn(super.conditionOp.getNullCheckOpcode(), endLabel);
        context.popStack();
    }
}