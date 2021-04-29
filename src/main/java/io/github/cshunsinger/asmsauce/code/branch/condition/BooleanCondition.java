package io.github.cshunsinger.asmsauce.code.branch.condition;

import org.objectweb.asm.Label;
import io.github.cshunsinger.asmsauce.code.CodeInsnBuilderLike;
import io.github.cshunsinger.asmsauce.code.branch.Op;
import io.github.cshunsinger.asmsauce.code.cast.ImplicitConversionInsn;

import static io.github.cshunsinger.asmsauce.DefinitionBuilders.type;
import static io.github.cshunsinger.asmsauce.MethodBuildingContext.context;
import static org.objectweb.asm.Opcodes.IFEQ;
import static org.objectweb.asm.Opcodes.IFNE;

/**
 * This condition represents a condition in which a single boolean value is supplied. This condition checks if a single
 * value is true or false.
 */
public class BooleanCondition extends SingleOperandCondition {
    /**
     * Creates a Boolean truth condition from a single boolean operand. This condition will test for the operand
     * being true.
     * @param operandBuilder The operand builder which stacks the boolean value. This code builder should stack
     *                       a single boolean or Boolean value.
     */
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
    public void build(Label endLabel) {
        super.build(endLabel);

        //Make sure the stacked type is boolean in nature
        new ImplicitConversionInsn(type(boolean.class)).build();

        int opcode = super.conditionOp == Op.EQ ? IFEQ : IFNE;
        context().getMethodVisitor().visitJumpInsn(opcode, endLabel);
        context().popStack();
    }
}