package io.github.cshunsinger.asmsauce.code.stack;

import org.objectweb.asm.MethodVisitor;
import io.github.cshunsinger.asmsauce.MethodBuildingContext;
import io.github.cshunsinger.asmsauce.BaseUnitTest;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import static io.github.cshunsinger.asmsauce.code.CodeBuilders.*;
import static org.objectweb.asm.Opcodes.ICONST_0;
import static org.objectweb.asm.Opcodes.ICONST_1;
import static java.util.Collections.emptyList;
import static org.apache.commons.lang3.RandomUtils.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.verify;

class StackPrimitiveLiteralInsnTest extends BaseUnitTest {
    @Mock
    private MethodVisitor mockMethodVisitor;

    @Test
    public void stackPrimitiveByte() {
        StackPrimitiveLiteralInsn insn = literal((byte)nextInt());
        testBuild(insn, byte.class);
    }

    @Test
    public void stackPrimitiveShort() {
        StackPrimitiveLiteralInsn insn = literal((short)nextInt());
        testBuild(insn, short.class);
    }

    @Test
    public void stackPrimitiveChar() {
        StackPrimitiveLiteralInsn insn = literal((char)nextInt());
        testBuild(insn, char.class);
    }

    @Test
    public void stackPrimitiveInt() {
        StackPrimitiveLiteralInsn insn = literal(nextInt());
        testBuild(insn, int.class);
    }

    @Test
    public void stackPrimitiveLong() {
        StackPrimitiveLiteralInsn insn = literal(nextLong());
        testBuild(insn, long.class);
    }

    @Test
    public void stackPrimitiveFloat() {
        StackPrimitiveLiteralInsn insn = literal(nextFloat());
        testBuild(insn, float.class);
    }

    @Test
    public void stackPrimitiveDouble() {
        StackPrimitiveLiteralInsn insn = literal(nextDouble());
        testBuild(insn, double.class);
    }

    @Test
    public void stackPrimitiveBooleanTrue() {
        StackPrimitiveLiteralInsn insn = true_();
        testBooleanBuild(insn, true);
    }

    @Test
    public void stackPrimitiveBooleanFalse() {
        StackPrimitiveLiteralInsn insn = false_();
        testBooleanBuild(insn, false);
    }

    private void testBooleanBuild(StackPrimitiveLiteralInsn insn, boolean value) {
        testBuildStack(insn, boolean.class);
        verify(mockMethodVisitor).visitInsn(value ? ICONST_1 : ICONST_0);
    }

    private void testBuild(StackPrimitiveLiteralInsn insn, Class<?> expectedType) {
        testBuildStack(insn, expectedType);

        //method visitor should be called
        verify(mockMethodVisitor).visitLdcInsn(any());
    }

    private void testBuildStack(StackPrimitiveLiteralInsn insn, Class<?> expectedType) {
        MethodBuildingContext methodContext = new MethodBuildingContext(mockMethodVisitor, null, null, emptyList());
        insn.build();

        //type stack should have exactly 1 element and that element should be the expected type
        assertThat(methodContext.stackSize(), is(1));
        assertThat(methodContext.popStack(), hasProperty("type", is(expectedType)));
    }
}