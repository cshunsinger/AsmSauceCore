package io.github.cshunsinger.asmsauce.code.branch;

import org.objectweb.asm.Label;
import io.github.cshunsinger.asmsauce.MethodBuildingContext;
import io.github.cshunsinger.asmsauce.code.CodeInsnBuilder;
import io.github.cshunsinger.asmsauce.code.CodeInsnBuilderLike;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Code builder for producing the bytecode for a branch. Builds the bytecode which builds the body of a branch before
 * jumping to an end label.
 */
public class IfElseBranch extends CodeInsnBuilder {
    /**
     * The code builders making up the body if this branch.
     */
    protected final List<CodeInsnBuilderLike> body;
    /**
     * The label at the end of this branch body.
     */
    protected final Label endLabel;

    /**
     * Creates the branch from an end label and branch code body.
     * @param endLabel The label to jump to after the end of the branch body bytecode.
     * @param body The body of the branch.
     * @throws IllegalArgumentException If endLabel is null.
     * @throws IllegalArgumentException If branch body is empty.
     */
    public IfElseBranch(Label endLabel, CodeInsnBuilderLike... body) {
        if(endLabel == null)
            throw new IllegalArgumentException("End Label cannot be null.");
        if(body.length == 0)
            throw new IllegalArgumentException("Body cannot be empty.");

        this.endLabel = endLabel;
        this.body = Stream.of(body)
            .map(CodeInsnBuilderLike::getFirstInStack)
            .collect(Collectors.toList());
    }

    @Override
    public void build(MethodBuildingContext context) {
        //Build the branch body inside of its own scope
        context.beginScope();
        body.forEach(builder -> builder.buildClean(context));
        context.endScope();

        //Visit label at end of block
        context.getMethodVisitor().visitLabel(endLabel);

        super.build(context);
    }
}