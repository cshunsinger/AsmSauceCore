package io.github.cshunsinger.asmsauce.code.branch.condition;

import io.github.cshunsinger.asmsauce.code.CodeInsnBuilderLike;
import io.github.cshunsinger.asmsauce.code.branch.Op;

/**
 * Interface representing a type designed for building a condition.
 */
public interface ConditionBuilderLike extends CodeInsnBuilderLike {
    /**
     * Creates a new condition comparing one operand to a second operand using equals.
     * @param secondOperand Code builder which stacks the second operand of the condition.
     * @return A simple condition comparing two operands.
     */
    default Condition eq(CodeInsnBuilderLike secondOperand) {
        return condition(secondOperand, Op.EQ);
    }

    /**
     * Creates a new condition comparing one operand to a second operand using not-equals.
     * @param secondOperand Code builder which stacks the second operand of the condition.
     * @return A simple condition comparing two operands.
     */
    default Condition ne(CodeInsnBuilderLike secondOperand) {
        return condition(secondOperand, Op.NE);
    }

    /**
     * Creates a new condition comparing one operand to a second operand using greater-than-equal.
     * @param secondOperand Code builder which stacks the second operand of the condition.
     * @return A simple condition comparing two operands.
     */
    default Condition ge(CodeInsnBuilderLike secondOperand) {
        return condition(secondOperand, Op.GE);
    }

    /**
     * Creates a new condition comparing one operand to a second operand using less-than-equal.
     * @param secondOperand Code builder which stacks the second operand of the condition.
     * @return A simple condition comparing two operands.
     */
    default Condition le(CodeInsnBuilderLike secondOperand) {
        return condition(secondOperand, Op.LE);
    }

    /**
     * Creates a new condition comparing one operand to a second operand using greater-than.
     * @param secondOperand Code builder which stacks the second operand of the condition.
     * @return A simple condition comparing two operands.
     */
    default Condition gt(CodeInsnBuilderLike secondOperand) {
        return condition(secondOperand, Op.GT);
    }

    /**
     * Creates a new condition comparing one operand to a second operand using less-than.
     * @param secondOperand Code builder which stacks the second operand of the condition.
     * @return A simple condition comparing two operands.
     */
    default Condition lt(CodeInsnBuilderLike secondOperand) {
        return condition(secondOperand, Op.LT);
    }

    private Condition condition(CodeInsnBuilderLike secondOperand, Op operation) {
        return new DoubleOperandCondition(this, secondOperand, operation);
    }
}