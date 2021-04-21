package io.github.cshunsinger.asmsauce.code.branch.condition;

import org.objectweb.asm.Label;
import io.github.cshunsinger.asmsauce.MethodBuildingContext;
import io.github.cshunsinger.asmsauce.code.CodeInsnBuilderLike;
import io.github.cshunsinger.asmsauce.code.branch.Op;
import lombok.Getter;

/**
 * This class represents a single condition, which can be built as bytecode by the asm package.
 * Different types of conditions exist which will inherit this class.
 */
@Getter
public abstract class Condition {
    /**
     * @return The operation used for this condition.
     */
    protected final Op conditionOp;

    /**
     * Creates a new condition using the specified conditional operation.
     * @param conditionOp The conditional operation.
     */
    protected Condition(Op conditionOp) {
        this.conditionOp = conditionOp;
    }

    /**
     * Creates the inverse of this condition.
     * @return Returns a new Condition instance which is the inverse of this instance.
     */
    public abstract Condition invert();

    /**
     * Builds the bytecode that makes up this condition.
     * @param context Method building context.
     * @param endLabel Label to jump to if the condition evaluates to false.
     */
    public abstract void build(MethodBuildingContext context, Label endLabel);

    /**
     * Combines this condition and another condition with an AND operation.
     * @param otherCondition The condition to AND with this one.
     * @return Returns a new CompoundCondition instance which contains the AND of this condition and otherCondition.
     * @see #or(Condition) For OR conditions.
     */
    public CompoundCondition and(Condition otherCondition) {
        return new CompoundCondition(this, otherCondition, true);
    }

    /**
     * Combines this condition and another condition with an OR operation.
     * @param otherCondition The condition to OR with this one.
     * @return Returns a new CompoundCondition instance which contains the OR of this condition with otherCondition.
     */
    public CompoundCondition or(Condition otherCondition) {
        return new CompoundCondition(this, otherCondition, false);
    }

    /**
     * Executes a provided code builder, and verifies that exactly 1 element was stacked by that code builder.
     * @param context The method building context.
     * @param insn The code builder to execute.
     */
    protected static void validateStackSingleValue(MethodBuildingContext context, CodeInsnBuilderLike insn) {
        int stackSize = context.stackSize();
        insn.build(context);
        int numStacked = context.stackSize() - stackSize;
        if(numStacked != 1)
            throw new IllegalStateException("Expected 1 element to be stacked. Found " + numStacked + " instead.");
    }
}