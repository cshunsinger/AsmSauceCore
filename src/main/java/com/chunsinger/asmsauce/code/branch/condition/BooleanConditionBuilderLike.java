package com.chunsinger.asmsauce.code.branch.condition;

import com.chunsinger.asmsauce.code.CodeInsnBuilderLike;

public interface BooleanConditionBuilderLike extends CodeInsnBuilderLike {
    default Condition isTrue() {
        return new BooleanCondition(this);
    }

    default Condition isFalse() {
        return isTrue().invert();
    }
}