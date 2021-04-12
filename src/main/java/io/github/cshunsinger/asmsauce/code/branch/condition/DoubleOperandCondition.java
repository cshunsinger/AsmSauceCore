package io.github.cshunsinger.asmsauce.code.branch.condition;

import org.objectweb.asm.Label;
import io.github.cshunsinger.asmsauce.MethodBuildingContext;
import io.github.cshunsinger.asmsauce.code.CodeInsnBuilderLike;
import io.github.cshunsinger.asmsauce.code.branch.Op;
import io.github.cshunsinger.asmsauce.code.cast.ImplicitConversionInsn;
import io.github.cshunsinger.asmsauce.definitions.TypeDefinition;
import io.github.cshunsinger.asmsauce.DefinitionBuilders;
import lombok.Getter;
import org.apache.commons.lang3.ClassUtils;

import static org.objectweb.asm.Opcodes.*;

@Getter
public class DoubleOperandCondition extends Condition {
    private final CodeInsnBuilderLike operand1Builder;
    private final CodeInsnBuilderLike operand2Builder;

    public DoubleOperandCondition(CodeInsnBuilderLike operand1Builder,
                                  CodeInsnBuilderLike operand2Builder,
                                  Op comparisonOp) {
        super(comparisonOp);
        this.operand1Builder = operand1Builder.getFirstInStack();
        this.operand2Builder = operand2Builder.getFirstInStack();
    }

    @Override
    public DoubleOperandCondition invert() {
        Op invertedOp = switch(super.conditionOp) {
            case EQ -> Op.NOT_EQ;
            case NE -> Op.NOT_NE;
            case GE -> Op.NOT_GE;
            case LE -> Op.NOT_LE;
            case GT -> Op.NOT_GT;
            case LT -> Op.NOT_LT;
            case NOT_EQ -> Op.EQ;
            case NOT_NE -> Op.NE;
            case NOT_GE -> Op.GE;
            case NOT_LE -> Op.LE;
            case NOT_GT -> Op.GT;
            case NOT_LT -> Op.LT;
        };

        return new DoubleOperandCondition(this.operand1Builder, this.operand2Builder, invertedOp);
    }

    @Override
    public void build(MethodBuildingContext context, Label endLabel) {
        validateStackSingleValue(context, operand1Builder);
        TypeDefinition<?> firstType = context.peekStack();
        validateStackSingleValue(context, operand2Builder);

        if(firstType.isPrimitive()) {
            //Ensure the second operand type matches, or is implicitly converted to match, the first operand
            new ImplicitConversionInsn(firstType).build(context);

            Class<?> primitiveClass = firstType.getType();
            if(primitiveClass == long.class)
                context.getMethodVisitor().visitInsn(LCMP);
            else if(primitiveClass == double.class)
                context.getMethodVisitor().visitInsn(DCMPG);
            else if(primitiveClass == float.class)
                context.getMethodVisitor().visitInsn(FCMPG);

            if(primitiveClass == long.class || primitiveClass == double.class || primitiveClass == float.class)
                context.getMethodVisitor().visitJumpInsn(conditionOp.getSinglePrimitiveOpcode(), endLabel);
            else
                context.getMethodVisitor().visitJumpInsn(conditionOp.getDoublePrimitiveOpcode(), endLabel);
        }
        else {
            //Dealing with reference comparison - if second operand is primitive, convert it into a wrapper first
            TypeDefinition<?> secondType = context.peekStack();
            if(secondType.isPrimitive()) {
                TypeDefinition<?> secondTypeWrapper = DefinitionBuilders.type(ClassUtils.primitiveToWrapper(secondType.getType()));
                new ImplicitConversionInsn(secondTypeWrapper).build(context);
            }

            Integer referenceOpcode = conditionOp.getReferenceOpcode();
            if(referenceOpcode == null)
                throw new IllegalStateException("Comparison operation %s is invalid for reference comparisons.".formatted(conditionOp.name()));

            context.getMethodVisitor().visitJumpInsn(referenceOpcode, endLabel);
        }

        context.popStack(2);
    }
}