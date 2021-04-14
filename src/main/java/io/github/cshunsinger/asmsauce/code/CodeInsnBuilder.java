package io.github.cshunsinger.asmsauce.code;

import io.github.cshunsinger.asmsauce.MethodBuildingContext;
import lombok.Getter;
import lombok.Setter;

/**
 * Base type for asm bytecode builder. All bytecode building types should inherit from this.
 */
public abstract class CodeInsnBuilder implements CodeInsnBuilderLike {
    @Setter
    private CodeInsnBuilderLike next;
    @Setter @Getter
    private CodeInsnBuilderLike prev;

    @Override
    public void build(MethodBuildingContext context) {
        if(next != null)
            next.build(context);
    }
}