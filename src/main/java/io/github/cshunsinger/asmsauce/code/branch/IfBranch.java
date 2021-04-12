package io.github.cshunsinger.asmsauce.code.branch;

import org.objectweb.asm.Label;
import io.github.cshunsinger.asmsauce.MethodBuildingContext;
import io.github.cshunsinger.asmsauce.code.CodeInsnBuilderLike;
import io.github.cshunsinger.asmsauce.code.branch.condition.Condition;
import lombok.RequiredArgsConstructor;

public class IfBranch extends IfElseBranch {
    private final Condition condition;

    IfBranch(IfBranch original, CodeInsnBuilderLike... body) {
        this(original.condition, original.endLabel, body);
    }

    public IfBranch(Condition condition, CodeInsnBuilderLike... body) {
        this(condition, new Label(), body);
    }

    public IfBranch(Condition condition, Label endLabel, CodeInsnBuilderLike... body) {
        super(endLabel, body);

        if(condition == null)
            throw new IllegalArgumentException("Condition cannot be null.");

        this.condition = condition;
    }

    @Override
    public void build(MethodBuildingContext context) {
        //Generate the bytecode for the branching
        condition.build(context, endLabel);

        //Build the if body
        super.build(context);
    }

    public ElseBranch elseDo(CodeInsnBuilderLike... elseBody) {
        return new ElseBranch(this, elseBody);
    }

    @RequiredArgsConstructor
    public static class IfBuilder {
        private final Condition condition;

        public IfBranch then(CodeInsnBuilderLike... body) {
            return new IfBranch(condition, body);
        }
    }
}