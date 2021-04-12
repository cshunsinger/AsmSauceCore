package io.github.cshunsinger.asmsauce.code.branch;

import aj.org.objectweb.asm.Label;
import io.github.cshunsinger.asmsauce.MethodBuildingContext;
import io.github.cshunsinger.asmsauce.code.CodeInsnBuilder;
import io.github.cshunsinger.asmsauce.code.CodeInsnBuilderLike;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class IfElseBranch extends CodeInsnBuilder {
    protected final List<CodeInsnBuilderLike> body;
    protected final Label endLabel;

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
        body.forEach(builder -> builder.buildBytecode(context));
        context.endScope();

        //Visit label at end of block
        context.getMethodVisitor().visitLabel(endLabel);

        super.build(context);
    }
}