package com.chunsinger.asmsauce.code.math;

import com.chunsinger.asmsauce.MethodBuildingContext;
import com.chunsinger.asmsauce.code.CodeInsnBuilder;
import com.chunsinger.asmsauce.code.CodeInsnBuilderLike;
import com.chunsinger.asmsauce.code.cast.ImplicitConversionInsn;
import com.chunsinger.asmsauce.definitions.TypeDefinition;
import org.apache.commons.lang3.ClassUtils;

import static com.chunsinger.asmsauce.DefinitionBuilders.type;

public abstract class MathOperationInsn extends CodeInsnBuilder {
    private final CodeInsnBuilderLike operandBuilder;

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
            new ImplicitConversionInsn(type(ClassUtils.wrapperToPrimitive(firstOperandType.getType()))).build(context);
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
            new ImplicitConversionInsn(type(ClassUtils.wrapperToPrimitive(secondOperand.getType()))).build(context);
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

    protected abstract int intOperator();
    protected abstract int longOperator();
    protected abstract int floatOperator();
    protected abstract int doubleOperator();
}