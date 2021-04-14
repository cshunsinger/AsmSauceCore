package io.github.cshunsinger.asmsauce.code;

import io.github.cshunsinger.asmsauce.MethodBuildingContext;

import static org.objectweb.asm.Opcodes.POP;
import static org.objectweb.asm.Opcodes.POP2;

/**
 * The interface-type for asm bytecode builders.
 */
public interface CodeInsnBuilderLike {
    /**
     * Sets the instruction to build after this one.
     * @param next The next instruction to build after this one.
     */
    void setNext(CodeInsnBuilderLike next);

    /**
     * Sets the instruction being built before this one.
     * @param prev The instruction to build before this one.
     */
    void setPrev(CodeInsnBuilderLike prev);

    /**
     * Gets the instruction being built before this one.
     * @return The instruction to build before this one, or null if no instructions precede this one.
     */
    CodeInsnBuilderLike getPrev();

    /**
     * Called by the class builder {@link io.github.cshunsinger.asmsauce.AsmClassBuilder} when building a class.
     * Builds the bytecode for this instruction.
     * @param context The method building context containing all of the information around the method body being built.
     * @see MethodBuildingContext {@link MethodBuildingContext}
     * @see #buildClean(MethodBuildingContext) {@link #buildClean(MethodBuildingContext)}
     */
    void build(MethodBuildingContext context);

    /**
     * Called by the class builder {@link io.github.cshunsinger.asmsauce.AsmClassBuilder} when building a class.
     * Builds the bytecode for this instruction, and cleans the stack automatically by adding pop and pop2 instructions.
     * This methods handles cases in which the stack may have values on it when it should not, such as when the return
     * value of a method is ignored.
     * @param context The method building context containing all of the information around the method body being built.
     * @see MethodBuildingContext {@link MethodBuildingContext}
     * @see #build(MethodBuildingContext) {@link #build(MethodBuildingContext)}
     */
    default void buildClean(MethodBuildingContext context) {
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