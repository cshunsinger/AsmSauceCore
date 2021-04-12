package io.github.cshunsinger.asmsauce.code;

import io.github.cshunsinger.asmsauce.MethodBuildingContext;

import static aj.org.objectweb.asm.Opcodes.POP;
import static aj.org.objectweb.asm.Opcodes.POP2;

public interface CodeInsnBuilderLike {
    void setNext(CodeInsnBuilderLike next);
    void setPrev(CodeInsnBuilderLike prev);
    CodeInsnBuilderLike getPrev();

    void build(MethodBuildingContext context);

    default void buildBytecode(MethodBuildingContext context) {
        int initialStackSize = context.stackSize();
        this.build(context);

        if(getPrev() != null)
            return;

        while(context.stackSize() > initialStackSize) {
            Class<?> stackType = context.popStack().getType();
            if(stackType == long.class || stackType == double.class)
                context.getMethodVisitor().visitInsn(POP2);
            else
                context.getMethodVisitor().visitInsn(POP);
        }
    }

    /**
     * Since code instruction builders can be stacked together, it may be necessary to traverse back to the top of that stack.
     * @return The first CodeInsnBuilderLike instance to have getPrev() == `null`
     */
    default CodeInsnBuilderLike getFirstInStack() {
        CodeInsnBuilderLike current = this;
        while(current.getPrev() != null) {
            current = current.getPrev();
        }
        return current;
    }
}