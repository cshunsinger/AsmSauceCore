package com.chunsinger.asmsauce.code;

import com.chunsinger.asmsauce.MethodBuildingContext;

import java.util.List;
import java.util.stream.Collectors;

public class CodeBlock extends CodeInsnBuilder {
    private final List<CodeInsnBuilderLike> builders;

    public CodeBlock(CodeInsnBuilderLike... builders) {
        this.builders = List.of(builders)
            .stream()
            .map(CodeInsnBuilderLike::getFirstInStack)
            .collect(Collectors.toList());
    }

    @Override
    public void build(MethodBuildingContext context) {
        builders.forEach(builder -> builder.buildBytecode(context));

        super.build(context);
    }
}