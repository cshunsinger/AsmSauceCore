package io.github.chunsinger.asmsauce.code.stack;

import io.github.chunsinger.asmsauce.MethodBuildingContext;
import io.github.chunsinger.asmsauce.code.CodeInsnBuilder;
import io.github.chunsinger.asmsauce.code.branch.condition.BooleanConditionBuilderLike;
import io.github.chunsinger.asmsauce.code.branch.condition.ConditionBuilderLike;
import io.github.chunsinger.asmsauce.code.math.MathOperandInstance;
import io.github.chunsinger.asmsauce.DefinitionBuilders;

import static aj.org.objectweb.asm.Opcodes.ICONST_0;
import static aj.org.objectweb.asm.Opcodes.ICONST_1;

public class StackPrimitiveLiteralInsn extends CodeInsnBuilder implements
    MathOperandInstance, ConditionBuilderLike, BooleanConditionBuilderLike {
    private static final int B_PRIM = 0;
    private static final int S_PRIM = 1;
    private static final int C_PRIM = 2;
    private static final int I_PRIM = 3;
    private static final int L_PRIM = 4;
    private static final int F_PRIM = 5;
    private static final int D_PRIM = 6;
    private static final int BOOL_PRIM = 7;

    private byte b;
    private short s;
    private char c;
    private int i;
    private long l;
    private float f;
    private double d;
    private boolean bool;

    private final int primitiveType;

    public StackPrimitiveLiteralInsn(byte b) {
        this.b = b;
        this.primitiveType = B_PRIM;
    }

    public StackPrimitiveLiteralInsn(short s) {
        this.s = s;
        this.primitiveType = S_PRIM;
    }

    public StackPrimitiveLiteralInsn(char c) {
        this.c = c;
        this.primitiveType = C_PRIM;
    }

    public StackPrimitiveLiteralInsn(int i) {
        this.i = i;
        this.primitiveType = I_PRIM;
    }

    public StackPrimitiveLiteralInsn(long l) {
        this.l = l;
        this.primitiveType = L_PRIM;
    }

    public StackPrimitiveLiteralInsn(float f) {
        this.f = f;
        this.primitiveType = F_PRIM;
    }

    public StackPrimitiveLiteralInsn(double d) {
        this.d = d;
        this.primitiveType = D_PRIM;
    }

    public StackPrimitiveLiteralInsn(boolean b) {
        this.bool = b;
        this.primitiveType = BOOL_PRIM;
    }

    @Override
    public void build(MethodBuildingContext context) {
        switch(primitiveType) {
            case B_PRIM -> {
                context.getMethodVisitor().visitLdcInsn(b);
                context.pushStack(DefinitionBuilders.type(byte.class));
            }
            case S_PRIM -> {
                context.getMethodVisitor().visitLdcInsn(s);
                context.pushStack(DefinitionBuilders.type(short.class));
            }
            case C_PRIM -> {
                context.getMethodVisitor().visitLdcInsn(c);
                context.pushStack(DefinitionBuilders.type(char.class));
            }
            case I_PRIM -> {
                context.getMethodVisitor().visitLdcInsn(i);
                context.pushStack(DefinitionBuilders.type(int.class));
            }
            case L_PRIM -> {
                context.getMethodVisitor().visitLdcInsn(l);
                context.pushStack(DefinitionBuilders.type(long.class));
            }
            case F_PRIM -> {
                context.getMethodVisitor().visitLdcInsn(f);
                context.pushStack(DefinitionBuilders.type(float.class));
            }
            case D_PRIM -> {
                context.getMethodVisitor().visitLdcInsn(d);
                context.pushStack(DefinitionBuilders.type(double.class));
            }
            case BOOL_PRIM -> {
                context.getMethodVisitor().visitInsn(bool ? ICONST_1 : ICONST_0);
                context.pushStack(DefinitionBuilders.type(boolean.class));
            }
        }

        super.build(context);
    }
}