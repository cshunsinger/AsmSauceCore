package io.github.cshunsinger.asmsauce.code.array;

import io.github.cshunsinger.asmsauce.code.CodeInsnBuilder;
import io.github.cshunsinger.asmsauce.code.math.MathOperandInstance;

import static io.github.cshunsinger.asmsauce.MethodBuildingContext.context;
import static io.github.cshunsinger.asmsauce.definitions.TypeDefinition.INT;
import static org.objectweb.asm.Opcodes.ARRAYLENGTH;

/**
 * This class represents an instruction to get the length of an array. This instruction is intended to generate the
 * bytecode which will consume an array reference off of the JVM stack, then place an integer representing the length
 * of that array onto the stack.
 */
public class ArrayLengthInsn extends CodeInsnBuilder implements MathOperandInstance {
    /**
     * Creates a new code builder for generating bytecode which will get the length of an array reference.
     */
    public ArrayLengthInsn() {}

    @Override
    public void build() {
        ArrayAccessInsn.validateArrayTypeStacked();

        context().getMethodVisitor().visitInsn(ARRAYLENGTH);
        context().popStack(); //Pop the array off of the stack
        context().pushStack(INT); //Push int (array.length) onto the stack
    }
}