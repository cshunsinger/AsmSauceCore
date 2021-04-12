package io.github.cshunsinger.asmsauce.code.branch.condition;

import io.github.cshunsinger.asmsauce.code.CodeInsnBuilderLike;

public interface NullConditionBuilderLike extends CodeInsnBuilderLike {
    default Condition isNull() {
        return new NullCondition(this);
    }

    default Condition isNotNull() {
        return isNull().invert();
    }
}