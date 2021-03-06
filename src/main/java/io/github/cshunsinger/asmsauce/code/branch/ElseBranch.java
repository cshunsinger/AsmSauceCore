package io.github.cshunsinger.asmsauce.code.branch;

import io.github.cshunsinger.asmsauce.code.CodeInsnBuilderLike;
import org.objectweb.asm.Label;

import static io.github.cshunsinger.asmsauce.code.CodeBuilders.block;

/**
 * This code builder represents the "else" branch of an if-statement. If the condition of an if-statement evaluates to
 * false, then the bytecode generated for the else-body will be executed. The bytecode for the else-body will not be
 * executed if the if-condition evaluates to true.
 */
public class ElseBranch extends IfElseBranch {
    /**
     * Constructs a new ElseBranch from an if-branch and body.
     * @param ifBranch The original if-branch to which this else-block is being attached to.
     * @param elseBody Code builders which will make up the else-body. The bytecode generated by these code builders is
     *                 what will be executed if the if-condition evaluates to false.
     */
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