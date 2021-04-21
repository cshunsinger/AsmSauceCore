package io.github.cshunsinger.asmsauce.code.branch.condition;

import org.objectweb.asm.Label;
import io.github.cshunsinger.asmsauce.MethodBuildingContext;
import io.github.cshunsinger.asmsauce.code.CodeInsnBuilderLike;
import io.github.cshunsinger.asmsauce.code.branch.Op;

/**
 * Represents a condition involving only one operand. For example: null checks, boolean checks.
 */
public abstract class SingleOperandCondition extends Condition {
    /**
     * The code builder which will place a value on the JVM stack. This stacked value is the single operand
     * used for this condition.
     */
    protected final CodeInsnBuilderLike operandBuilder;

    /**
     * Creates a new single operand condition from an operand builder and a conditional operation.
     * @param operandBuilder The code builder to stack the operand for this condition.
     * @param conditionOp The operation to use for this condition.
     */
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