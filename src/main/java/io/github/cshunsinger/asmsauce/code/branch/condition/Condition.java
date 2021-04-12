package io.github.cshunsinger.asmsauce.code.branch.condition;

import org.objectweb.asm.Label;
import io.github.cshunsinger.asmsauce.MethodBuildingContext;
import io.github.cshunsinger.asmsauce.code.CodeInsnBuilderLike;
import io.github.cshunsinger.asmsauce.code.branch.Op;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * This class represents a single condition, which can be built as bytecode by the asm package.
 * Different types of conditions exist which will inherit this class.
 */
@Getter
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class Condition {
    protected final Op conditionOp;

    public abstract Condition invert();
    public abstract void build(MethodBuildingContext context, Label endLabel);

    public CompoundCondition and(Condition otherCondition) {
        return new CompoundCondition(this, otherCondition, true);
    }

    public CompoundCondition or(Condition otherCondition) {
        return new CompoundCondition(this, otherCondition, false);
    }

    protected static void validateStackSingleValue(MethodBuildingContext context, CodeInsnBuilderLike insn) {
        int stackSize = context.stackSize();
        insn.build(context);
        int numStacked = context.stackSize() - stackSize;
        if(numStacked != 1)
            throw new IllegalStateException("Expected 1 element to be stacked. Found " + numStacked + " instead.");
    }
}