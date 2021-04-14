package io.github.cshunsinger.asmsauce.code.branch.condition;

import io.github.cshunsinger.asmsauce.code.CodeInsnBuilderLike;

/**
 * Interface type inherited by any code builder which is capable of producing a value which can be compared to true or false.
 */
public interface BooleanConditionBuilderLike extends CodeInsnBuilderLike {
    /**
     * Creates a boolean condition testing for boolean-true. For example, in an if statement: if(myBoolean) { ... }
     * @return A new simple condition testing for boolean true.
     */
    default Condition isTrue() {
        return new BooleanCondition(this);
    }

    /**
     * Creates a boolean condition testing for boolean-false. For example, in an if statement: if(!myBoolean) { ... }
     * @return A new simple condition testing for boolean not-true.
     */
    default Condition isFalse() {
        return isTrue().invert();
    }
}