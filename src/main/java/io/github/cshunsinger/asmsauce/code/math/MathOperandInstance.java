package io.github.cshunsinger.asmsauce.code.math;

import io.github.cshunsinger.asmsauce.code.CodeInsnBuilderLike;

/**
 * Interface for code builders which are capable of stacking a value which can be considered a math operand.
 */
public interface MathOperandInstance extends CodeInsnBuilderLike {
    /**
     * Add a second operand to the first operand.
     * @param operandBuilder Code builder to stack the second operand.
     * @return A new addition code builder.
     */
    default AdditionMathOperationInsn add(CodeInsnBuilderLike operandBuilder) {
        AdditionMathOperationInsn op = new AdditionMathOperationInsn(operandBuilder);
        op.setPrev(this);
        this.setNext(op);
        return op;
    }

    /**
     * Subtract a second operand to the first operand.
     * @param operandBuilder Code builder to stack the second operand.
     * @return A new subtraction code builder.
     */
    default SubtractionMathOperationInsn sub(CodeInsnBuilderLike operandBuilder) {
        SubtractionMathOperationInsn op = new SubtractionMathOperationInsn(operandBuilder);
        op.setPrev(this);
        this.setNext(op);
        return op;
    }

    /**
     * Multiply the first operand by a second operand.
     * @param operandBuilder Code builder to stack the second operand.
     * @return A new multiplication code builder.
     */
    default MultiplicationMathOperationInsn mul(CodeInsnBuilderLike operandBuilder) {
        MultiplicationMathOperationInsn op = new MultiplicationMathOperationInsn(operandBuilder);
        op.setPrev(this);
        this.setNext(op);
        return op;
    }

    /**
     * Divide the first operand by a second operand.
     * @param operandBuilder Code builder to stack the second operand.
     * @return A new division code builder.
     */
    default DivisionMathOperationInsn div(CodeInsnBuilderLike operandBuilder) {
        DivisionMathOperationInsn op = new DivisionMathOperationInsn(operandBuilder);
        op.setPrev(this);
        this.setNext(op);
        return op;
    }

    /**
     * Get the remainder of dividing the first operand by a second operand (aka modulus!).
     * @param operandBuilder Code builder to stack the second operand.
     * @return A new modulus code builder.
     */
    default ModulusMathOperationInsn mod(CodeInsnBuilderLike operandBuilder) {
        ModulusMathOperationInsn op = new ModulusMathOperationInsn(operandBuilder);
        op.setPrev(this);
        this.setNext(op);
        return op;
    }
}