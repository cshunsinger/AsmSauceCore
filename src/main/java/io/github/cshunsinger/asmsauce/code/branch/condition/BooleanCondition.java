package io.github.cshunsinger.asmsauce.code.branch.condition;

import aj.org.objectweb.asm.Label;
import io.github.cshunsinger.asmsauce.MethodBuildingContext;
import io.github.cshunsinger.asmsauce.code.CodeInsnBuilderLike;
import io.github.cshunsinger.asmsauce.code.branch.Op;
import io.github.cshunsinger.asmsauce.code.cast.ImplicitConversionInsn;
import io.github.cshunsinger.asmsauce.DefinitionBuilders;

import static aj.org.objectweb.asm.Opcodes.IFEQ;
import static aj.org.objectweb.asm.Opcodes.IFNE;

/**
 * This condition represents a condition in which a single boolean value is supplied. This condition checks if a single
 * value is true or false.
 */
public class BooleanCondition extends SingleOperandCondition {
    public BooleanCondition(CodeInsnBuilderLike operandBuilder) {
        this(operandBuilder, Op.EQ);
    }

    private BooleanCondition(CodeInsnBuilderLike operandBuilder, Op booleanOp) {
        super(operandBuilder, booleanOp);
    }

    @Override
    public BooleanCondition invert() {
        Op invertedOp = super.conditionOp == Op.NOT_EQ ? Op.EQ : Op.NOT_EQ;
        return new BooleanCondition(super.operandBuilder, invertedOp);
    }

    @Override
    public void build(MethodBuildingContext context, Label endLabel) {
        super.build(context, endLabel);

        //Make sure the stacked type is boolean in nature
        new ImplicitConversionInsn(DefinitionBuilders.type(boolean.class)).build(context);

        int opcode = super.conditionOp == Op.EQ ? IFEQ : IFNE;
        context.getMethodVisitor().visitJumpInsn(opcode, endLabel);
        context.popStack();
    }
}