package io.github.cshunsinger.asmsauce.code;

import io.github.cshunsinger.asmsauce.MethodBuildingContext;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Simple asm code builder representing zero or more code builders.
 */
public class CodeBlock extends CodeInsnBuilder {
    private final List<CodeInsnBuilderLike> builders;

    /**
     * Constructs a new code block from zero or more code builders.
     * An empty code block is acceptable.
     * @param builders The code builders.
     */
    public CodeBlock(CodeInsnBuilderLike... builders) {
        this.builders = List.of(builders)
            .stream()
            .map(CodeInsnBuilderLike::getFirstInStack)
            .collect(Collectors.toList());
    }

    @Override
    public void build(MethodBuildingContext context) {
        builders.forEach(builder -> builder.buildClean(context));

        super.build(context);
    }
}