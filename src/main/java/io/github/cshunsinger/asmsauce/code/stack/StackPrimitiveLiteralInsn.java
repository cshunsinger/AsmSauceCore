package io.github.cshunsinger.asmsauce.code.stack;

import io.github.cshunsinger.asmsauce.code.CodeInsnBuilder;
import io.github.cshunsinger.asmsauce.code.branch.condition.BooleanConditionBuilderLike;
import io.github.cshunsinger.asmsauce.code.branch.condition.ConditionBuilderLike;
import io.github.cshunsinger.asmsauce.code.math.MathOperandInstance;

import static io.github.cshunsinger.asmsauce.DefinitionBuilders.type;
import static io.github.cshunsinger.asmsauce.MethodBuildingContext.context;
import static org.objectweb.asm.Opcodes.ICONST_0;
import static org.objectweb.asm.Opcodes.ICONST_1;

/**
 * Asm bytecode builder instruction which builds bytecode to stack a literal primitive value onto the jvm stack.
 */
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

    /**
     * Stack a literal byte.
     * @param b The byte value to stack.
     */
    public StackPrimitiveLiteralInsn(byte b) {
        this.b = b;
        this.primitiveType = B_PRIM;
    }

    /**
     * Stack a literal short.
     * @param s The short value to stack.
     */
    public StackPrimitiveLiteralInsn(short s) {
        this.s = s;
        this.primitiveType = S_PRIM;
    }

    /**
     * Stack a literal char.
     * @param c The char value to stack.
     */
    public StackPrimitiveLiteralInsn(char c) {
        this.c = c;
        this.primitiveType = C_PRIM;
    }

    /**
     * Stack a literal int.
     * @param i The int value to stack.
     */
    public StackPrimitiveLiteralInsn(int i) {
        this.i = i;
        this.primitiveType = I_PRIM;
    }

    /**
     * Stack a literal long.
     * @param l The long value to stack.
     */
    public StackPrimitiveLiteralInsn(long l) {
        this.l = l;
        this.primitiveType = L_PRIM;
    }

    /**
     * Stack a literal float.
     * @param f The float value to stack.
     */
    public StackPrimitiveLiteralInsn(float f) {
        this.f = f;
        this.primitiveType = F_PRIM;
    }

    /**
     * Stack a literal double.
     * @param d The double value to stack.
     */
    public StackPrimitiveLiteralInsn(double d) {
        this.d = d;
        this.primitiveType = D_PRIM;
    }

    /**
     * Stack a literal boolean.
     * @param b The boolean value to stack.
     */
    public StackPrimitiveLiteralInsn(boolean b) {
        this.bool = b;
        this.primitiveType = BOOL_PRIM;
    }

    @Override
    public void build() {
        switch(primitiveType) {
            case B_PRIM -> {
                context().getMethodVisitor().visitLdcInsn(b);
                context().pushStack(type(byte.class));
            }
            case S_PRIM -> {
                context().getMethodVisitor().visitLdcInsn(s);
                context().pushStack(type(short.class));
            }
            case C_PRIM -> {
                context().getMethodVisitor().visitLdcInsn(c);
                context().pushStack(type(char.class));
            }
            case I_PRIM -> {
                context().getMethodVisitor().visitLdcInsn(i);
                context().pushStack(type(int.class));
            }
            case L_PRIM -> {
                context().getMethodVisitor().visitLdcInsn(l);
                context().pushStack(type(long.class));
            }
            case F_PRIM -> {
                context().getMethodVisitor().visitLdcInsn(f);
                context().pushStack(type(float.class));
            }
            case D_PRIM -> {
                context().getMethodVisitor().visitLdcInsn(d);
                context().pushStack(type(double.class));
            }
            case BOOL_PRIM -> {
                context().getMethodVisitor().visitInsn(bool ? ICONST_1 : ICONST_0);
                context().pushStack(type(boolean.class));
            }
        }

        super.build();
    }
}