package io.github.cshunsinger.asmsauce.code;

import io.github.cshunsinger.asmsauce.MethodBuildingContext;
import lombok.Getter;
import lombok.Setter;

public abstract class CodeInsnBuilder implements CodeInsnBuilderLike {
    @Setter
    private CodeInsnBuilderLike next;
    @Setter @Getter
    private CodeInsnBuilderLike prev;

    /**
     * Tells the code builder to build it's bytecode using the provided MethodVisitor.
     * @param methodVisitor The method visitor to use.
     * @param typeStack Stack used to track which object types are left on the stack after the operations.
     * @param methodReturnType The type of value, or void.class, that should be returned by a method.
     */
    public void build(MethodBuildingContext context) {
        if(next != null)
            next.build(context);
    }
}