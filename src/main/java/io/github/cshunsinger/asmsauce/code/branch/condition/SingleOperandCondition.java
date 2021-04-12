package io.github.cshunsinger.asmsauce.code.branch.condition;

import aj.org.objectweb.asm.Label;
import io.github.cshunsinger.asmsauce.MethodBuildingContext;
import io.github.cshunsinger.asmsauce.code.CodeInsnBuilderLike;
import io.github.cshunsinger.asmsauce.code.branch.Op;

public abstract class SingleOperandCondition extends Condition {
    protected final CodeInsnBuilderLike operandBuilder;

    protected SingleOperandCondition(CodeInsnBuilderLike operandBuilder, Op conditionOp) {
        super(conditionOp);
        if(operandBuilder == null)
            throw new IllegalArgumentException("Operand builder cannot be null.");
        this.operandBuilder = operandBuilder.getFirstInStack();
    }

    @Override
    public void build(MethodBuildingContext context, Label endLabel) {
        validateStackSingleValue(context, operandBuilder);
    }
}