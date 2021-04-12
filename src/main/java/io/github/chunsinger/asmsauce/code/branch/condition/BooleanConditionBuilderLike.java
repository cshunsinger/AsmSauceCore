package io.github.chunsinger.asmsauce.code.branch.condition;

import io.github.chunsinger.asmsauce.code.CodeInsnBuilderLike;

public interface BooleanConditionBuilderLike extends CodeInsnBuilderLike {
    default Condition isTrue() {
        return new BooleanCondition(this);
    }

    default Condition isFalse() {
        return isTrue().invert();
    }
}