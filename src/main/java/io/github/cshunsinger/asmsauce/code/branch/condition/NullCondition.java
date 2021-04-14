package io.github.cshunsinger.asmsauce.code.branch.condition;

import org.objectweb.asm.Label;
import io.github.cshunsinger.asmsauce.MethodBuildingContext;
import io.github.cshunsinger.asmsauce.code.CodeInsnBuilderLike;
import io.github.cshunsinger.asmsauce.code.branch.Op;

/**
 * A condition which tests for a null or non-null reference.
 */
public class NullCondition extends SingleOperandCondition {
    /**
     * Code builder which stacks the reference operand to test for null/nonnull.
     * @param operandBuilder The code builder to stack the reference operand.
     */
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