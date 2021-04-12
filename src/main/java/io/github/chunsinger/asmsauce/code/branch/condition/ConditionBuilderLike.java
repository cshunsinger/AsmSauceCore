package io.github.chunsinger.asmsauce.code.branch.condition;

import io.github.chunsinger.asmsauce.code.CodeInsnBuilderLike;
import io.github.chunsinger.asmsauce.code.branch.Op;

public interface ConditionBuilderLike extends CodeInsnBuilderLike {
    default Condition eq(CodeInsnBuilderLike secondOperand) {
        return condition(secondOperand, Op.EQ);
    }

    default Condition ne(CodeInsnBuilderLike secondOperand) {
        return condition(secondOperand, Op.NE);
    }

    default Condition ge(CodeInsnBuilderLike secondOperand) {
        return condition(secondOperand, Op.GE);
    }

    default Condition le(CodeInsnBuilderLike secondOperand) {
        return condition(secondOperand, Op.LE);
    }

    default Condition gt(CodeInsnBuilderLike secondOperand) {
        return condition(secondOperand, Op.GT);
    }

    default Condition lt(CodeInsnBuilderLike secondOperand) {
        return condition(secondOperand, Op.LT);
    }

    private Condition condition(CodeInsnBuilderLike secondOperand, Op operation) {
        return new DoubleOperandCondition(this, secondOperand, operation);
    }
}