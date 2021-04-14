package io.github.cshunsinger.asmsauce.code.math;

import io.github.cshunsinger.asmsauce.MethodBuildingContext;
import io.github.cshunsinger.asmsauce.code.CodeInsnBuilder;
import io.github.cshunsinger.asmsauce.code.CodeInsnBuilderLike;
import io.github.cshunsinger.asmsauce.code.cast.ImplicitConversionInsn;
import io.github.cshunsinger.asmsauce.definitions.TypeDefinition;
import io.github.cshunsinger.asmsauce.DefinitionBuilders;
import org.apache.commons.lang3.ClassUtils;

/**
 * Code builder for math operations.
 */
public abstract class MathOperationInsn extends CodeInsnBuilder {
    private final CodeInsnBuilderLike operandBuilder;

    /**
     * Creates a new math operation code builder which will perform a math operation.
     * The element already on the top of the jvm stack will be the first operand. The value stacked by the provided
     * operand code builder will be the second operand.
     * @param operand The code builder to stack the second operand to use for the math operation.
     */
    public MathOperationInsn(CodeInsnBuilderLike operand) {
        if(operand == null)
            throw new IllegalArgumentException("Operand code builder cannot be null.");

        //This instruction assumes the first operand is on the stack and the second operand will be built
        //by the code builder passed in through the parameter
        this.operandBuilder = operand.getFirstInStack();
    }

    @Override
    public void build(MethodBuildingContext context) {
        //Make sure there is actually the first operand already on the stack
        if(context.isStackEmpty())
            throw new IllegalStateException("Expected to find a math operand on the stack, but the stack was empty.");

        //Make sure the first operand on the stack is actually an operand
        TypeDefinition<?> firstOperandType = context.peekStack();
        if(!ClassUtils.isPrimitiveOrWrapper(firstOperandType.getType())) {
            throw new IllegalStateException(
                "Expected a math operand to exist on the stack as a primitive or wrapper type. Found type %s instead."
                .formatted(firstOperandType.getType().getName())
            );
        }

        if(ClassUtils.isPrimitiveWrapper(firstOperandType.getType())) {
            //Auto-unbox if the type is a wrapper type
            new ImplicitConversionInsn(DefinitionBuilders.type(ClassUtils.wrapperToPrimitive(firstOperandType.getType()))).build(context);
            firstOperandType = context.peekStack();
        }

        //Call the operand builder and then make sure that the stack size is correct
        int stackSize = context.stackSize();
        operandBuilder.build(context);
        if(context.stackSize() != stackSize+1) {
            throw new IllegalStateException(
                "Expected 1 element to be pushed to the stack. Instead %d elements were pushed/removed."
                    .formatted(context.stackSize() - stackSize)
            );
        }

        //Implicit cast if necessary
        TypeDefinition<?> secondOperand = context.peekStack();
        if(ClassUtils.isPrimitiveWrapper(secondOperand.getType()))
            new ImplicitConversionInsn(DefinitionBuilders.type(ClassUtils.wrapperToPrimitive(secondOperand.getType()))).build(context);
        new ImplicitConversionInsn(firstOperandType).build(context);

        //Pop the two operands
        context.popStack(2);

        //Perform operation in bytecode
        context.getMethodVisitor().visitInsn(mathOperator(firstOperandType));

        //Push the result type to the stack
        context.pushStack(firstOperandType);
    }

    private int mathOperator(TypeDefinition<?> operandType) {
        Class<?> operandClass = operandType.getType();
        if(operandClass == double.class)
            return doubleOperator();
        else if(operandClass == float.class)
            return floatOperator();
        else if(operandClass == long.class)
            return longOperator();
        else
            return intOperator();
    }

    /**
     * Gets the opcode to use for an operation involving ints.
     * @return The opcode for operating on int operands.
     */
    protected abstract int intOperator();

    /**
     * Gets the opcode to use for an operation involving longs.
     * @return The opcode for operating on long operands.
     */
    protected abstract int longOperator();

    /**
     * Gets the opcode to use for an operation involving floats.
     * @return The opcode for operating on float operands.
     */
    protected abstract int floatOperator();

    /**
     * Gets the opcode to use for an operation involving doubles.
     * @return The opcode for operating on double operands.
     */
    protected abstract int doubleOperator();
}