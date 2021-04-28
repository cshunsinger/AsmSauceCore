package io.github.cshunsinger.asmsauce.code;

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
    public void build() {
        if(next != null)
            next.build();
    }
}