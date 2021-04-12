package io.github.cshunsinger.asmsauce.code.math;

import io.github.cshunsinger.asmsauce.code.CodeInsnBuilderLike;

public interface MathOperandInstance extends CodeInsnBuilderLike {
    default AdditionMathOperationInsn add(CodeInsnBuilderLike operandBuilder) {
        AdditionMathOperationInsn op = new AdditionMathOperationInsn(operandBuilder);
        op.setPrev(this);
        this.setNext(op);
        return op;
    }

    default SubtractionMathOperationInsn sub(CodeInsnBuilderLike operandBuilder) {
        SubtractionMathOperationInsn op = new SubtractionMathOperationInsn(operandBuilder);
        op.setPrev(this);
        this.setNext(op);
        return op;
    }

    default MultiplicationMathOperationInsn mul(CodeInsnBuilderLike operandBuilder) {
        MultiplicationMathOperationInsn op = new MultiplicationMathOperationInsn(operandBuilder);
        op.setPrev(this);
        this.setNext(op);
        return op;
    }

    default DivisionMathOperationInsn div(CodeInsnBuilderLike operandBuilder) {
        DivisionMathOperationInsn op = new DivisionMathOperationInsn(operandBuilder);
        op.setPrev(this);
        this.setNext(op);
        return op;
    }

    default ModulusMathOperationInsn mod(CodeInsnBuilderLike operandBuilder) {
        ModulusMathOperationInsn op = new ModulusMathOperationInsn(operandBuilder);
        op.setPrev(this);
        this.setNext(op);
        return op;
    }
}