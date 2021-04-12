package io.github.chunsinger.asmsauce.code.branch;

import aj.org.objectweb.asm.Label;
import io.github.chunsinger.asmsauce.code.CodeInsnBuilderLike;

import static io.github.chunsinger.asmsauce.code.CodeBuilders.block;

public class ElseBranch extends IfElseBranch {
    ElseBranch(IfBranch ifBranch, CodeInsnBuilderLike... elseBody) {
        this(ifBranch, new Label(), elseBody);
    }

    private ElseBranch(IfBranch ifBranch, Label endElseLabel, CodeInsnBuilderLike... elseBody) {
        super(endElseLabel, elseBody);

        IfBranch newIfBranch = new IfBranch(ifBranch,
            block(
                block(ifBranch.body.toArray(CodeInsnBuilderLike[]::new)),
                new GotoInsn(endElseLabel)
            )
        );

        this.setPrev(newIfBranch);
        newIfBranch.setNext(this);
    }
}