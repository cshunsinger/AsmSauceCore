package io.github.cshunsinger.asmsauce.code.branch.condition;

import io.github.cshunsinger.asmsauce.code.CodeInsnBuilderLike;

/**
 * Interface type inherited by any code builders which can stack a reference value which can be checked for null/nonnull.
 */
public interface NullConditionBuilderLike extends CodeInsnBuilderLike {
    /**
     * Creates a condition which tests for the stacked operand being null.
     * @return A new simple null-check condition.
     */
    default Condition isNull() {
        return new NullCondition(this);
    }

    /**
     * Creates a condition which tests for the stacked operand being nonnull.
     * @return A new simple nonnull-check condition.
     */
    default Condition isNotNull() {
        return isNull().invert();
    }
}