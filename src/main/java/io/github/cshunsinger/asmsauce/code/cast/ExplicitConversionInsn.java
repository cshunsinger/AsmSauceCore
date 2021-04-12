package io.github.cshunsinger.asmsauce.code.cast;

import io.github.cshunsinger.asmsauce.MethodBuildingContext;
import io.github.cshunsinger.asmsauce.code.CodeInsnBuilder;
import io.github.cshunsinger.asmsauce.code.CodeInsnBuilderLike;
import io.github.cshunsinger.asmsauce.code.branch.condition.BooleanConditionBuilderLike;
import io.github.cshunsinger.asmsauce.code.branch.condition.ConditionBuilderLike;
import io.github.cshunsinger.asmsauce.code.branch.condition.NullConditionBuilderLike;
import io.github.cshunsinger.asmsauce.code.math.MathOperandInstance;
import io.github.cshunsinger.asmsauce.code.method.InvokableInstance;
import io.github.cshunsinger.asmsauce.definitions.TypeDefinition;

import static aj.org.objectweb.asm.Opcodes.*;

public class ExplicitConversionInsn extends CodeInsnBuilder implements
    InvokableInstance, MathOperandInstance, ConditionBuilderLike, NullConditionBuilderLike, BooleanConditionBuilderLike {
    private final TypeDefinition<?> toType;
    private final CodeInsnBuilderLike valueBuilder;

    public ExplicitConversionInsn(TypeDefinition<?> toType, CodeInsnBuilderLike valueBuilder) {
        if(toType == null)
            throw new IllegalArgumentException("toType cannot be null.");
        if(toType.isVoid())
            throw new IllegalArgumentException("toType cannot be void.");
        if(valueBuilder == null)
            throw new IllegalArgumentException("Code builder cannot be null.");

        this.toType = toType;
        this.valueBuilder = valueBuilder.getFirstInStack();
    }

    @Override
    public void build(MethodBuildingContext context) {
        int stackSize = context.stackSize();
        valueBuilder.build(context);

        if(context.stackSize() != stackSize+1)
            throw new IllegalStateException(
                "Expected 1 value to be placed onto the stack. Instead %d items were placed/removed to the stack."
                    .formatted(context.stackSize() - stackSize)
            );

        TypeDefinition<?> fromType = context.peekStack();

        if(fromType.getType() != toType.getType()) {
            if(ImplicitConversionInsn.implicitCastAllowed(fromType.getType(), toType.getType())) {
                //Utilize the implicit conversion insn to do the conversion
                new ImplicitConversionInsn(toType).build(context);
            }
            else {
                //An implicit cast cannot be done - must be explicit cast
                context.popStack(); //Pull the type being cast off the stack

                if(fromType.isPrimitive() && toType.isPrimitive()) {
                    //Conversion of one primitive to another which cannot be done implicitly
                    //The below conversions are only going to be conversions that are not found in the ImplicitConversionInsn class
                    Class<?> toClass = toType.getType();

                    if(fromType.getType() == double.class) {
                        if(toClass == float.class)
                            context.getMethodVisitor().visitInsn(D2F);
                        else if(toClass == long.class)
                            context.getMethodVisitor().visitInsn(D2L);
                        else
                            context.getMethodVisitor().visitInsn(D2I);
                    }
                    else if(fromType.getType() == float.class) {
                        if(toClass == long.class)
                            context.getMethodVisitor().visitInsn(F2L);
                        else
                            context.getMethodVisitor().visitInsn(F2I);
                    }
                    else if(fromType.getType() == long.class) {
                        context.getMethodVisitor().visitInsn(L2I);
                    }

                    if(toClass == byte.class)
                        context.getMethodVisitor().visitInsn(I2B);
                    else if(toClass == char.class)
                        context.getMethodVisitor().visitInsn(I2C);
                    else if(toClass == short.class)
                        context.getMethodVisitor().visitInsn(I2S);
                }
                else if(fromType.isPrimitive() || toType.isPrimitive()) {
                    //Casting not possible because one type is primitive and another type is reference type
                    //And fromType cannot be implicitly converted into toType
                    throw new IllegalStateException("Illegal casting attempt. Cannot cast %s to %s.".formatted(fromType.getClassName(), toType.getClassName()));
                }
                else {
                    //Both values are reference types
                    context.getMethodVisitor().visitTypeInsn(CHECKCAST, toType.getJvmTypeName(context.getClassContext().getJvmTypeName()));
                }

                context.pushStack(toType); //Push the new type from the cast onto the stack
            }
        }

        super.build(context);
    }
}