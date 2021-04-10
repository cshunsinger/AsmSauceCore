package com.chunsinger.asmsauce.code.stack;

import aj.org.objectweb.asm.MethodVisitor;
import com.chunsinger.asmsauce.MethodBuildingContext;
import com.chunsinger.asmsauce.code.CodeInsnBuilderLike;
import com.chunsinger.asmsauce.testing.BaseUnitTest;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;

import java.util.ArrayList;
import java.util.stream.Stream;

import static aj.org.objectweb.asm.Opcodes.*;
import static com.chunsinger.asmsauce.DefinitionBuilders.type;
import static com.chunsinger.asmsauce.code.CodeBuilders.storeLocal;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class StoreLocalVariableInsnTest extends BaseUnitTest {
    @Mock
    private CodeInsnBuilderLike mockCodeBuilder;
    @Mock
    private MethodVisitor mockMethodVisitor;

    @ParameterizedTest
    @MethodSource("illegalArgumentException_badParameters_testCases")
    public void illegalArgumentException_badParameters(Integer localIndex,
                                                       CodeInsnBuilderLike valueBuilder,
                                                       String expectedExceptionMessage) {
        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class,
            () -> new StoreLocalVariableInsn(localIndex, valueBuilder)
        );
        assertThat(ex, hasProperty("message", is(expectedExceptionMessage)));
    }

    private static Stream<Arguments> illegalArgumentException_badParameters_testCases() {
        CodeInsnBuilderLike mockBuilder = mock(CodeInsnBuilderLike.class);

        return Stream.of(
            Arguments.of(-1, mockBuilder, "localIndex cannot be negative. Must be null or positive."),
            Arguments.of(1, null, "Value builder cannot be null.")
        );
    }

    @Test
    public void illegalArgumentException_badLocalVariableName() {
        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class,
            () -> new StoreLocalVariableInsn("   ", mockCodeBuilder)
        );
        assertThat(ex, hasProperty("message", is("localName cannot be null or empty.")));
    }

    @Test
    public void illegalStateException_moreThanOneValueStackedByCodeBuilder() {
        int localIndex = 1;
        MethodBuildingContext context = new MethodBuildingContext(mockMethodVisitor, null, null, new ArrayList<>());

        when(mockCodeBuilder.getFirstInStack()).thenReturn(mockCodeBuilder);
        doAnswer(i -> {
            context.pushStack(type(Object.class));
            context.pushStack(type(Object.class));
            return null;
        }).when(mockCodeBuilder).build(context);

        StoreLocalVariableInsn insn = new StoreLocalVariableInsn(localIndex, mockCodeBuilder);
        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> insn.build(context));

        assertThat(ex, hasProperty("message", is("Code builder expected to add 1 element to the stack. Instead 2 elements were added.")));
    }

    @ParameterizedTest
    @MethodSource("storeValueToLocalVariable_testCases")
    public void storeValueToLocalVariable(Integer localIndex, Class<?> valueClass, int opcode) {
        MethodBuildingContext context = new MethodBuildingContext(mockMethodVisitor, null, null, new ArrayList<>());

        when(mockCodeBuilder.getFirstInStack()).thenReturn(mockCodeBuilder);
        doAnswer(i -> context.pushStack(type(valueClass))).when(mockCodeBuilder).build(context);

        StoreLocalVariableInsn insn = new StoreLocalVariableInsn(localIndex, mockCodeBuilder);
        insn.build(context);

        verify(mockMethodVisitor).visitVarInsn(opcode, 0);

        if(valueClass == double.class || valueClass == long.class) {
            assertThat(context.getLocalTypes(), hasSize(2));
            assertThat(context.getLocalType(0), is(type(valueClass)));
            assertThat(context.getLocalType(1), is(type(valueClass)));
        }
        else {
            assertThat(context.getLocalTypes(), hasSize(1));
            assertThat(context.getLocalType(0), is(type(valueClass)));
        }
    }

    private static Stream<Arguments> storeValueToLocalVariable_testCases() {
        return Stream.of(
            Arguments.of(null, Object.class, ASTORE),
            Arguments.of(0,    Object.class, ASTORE),
            Arguments.of(0,    byte.class,   ISTORE),
            Arguments.of(0,    short.class,  ISTORE),
            Arguments.of(0,    int.class,    ISTORE),
            Arguments.of(0,    long.class,   LSTORE),
            Arguments.of(0,    float.class,  FSTORE),
            Arguments.of(0,    double.class, DSTORE)
        );
    }

    @ParameterizedTest
    @MethodSource("storeValueToNamedLocalVariable_testCases")
    public void storeValueToNamedLocalVariable(Class<?> valueClass, int opcode) {
        MethodBuildingContext context = new MethodBuildingContext(mockMethodVisitor, null, null, new ArrayList<>());
        String localName = RandomStringUtils.randomAlphanumeric(10);

        when(mockCodeBuilder.getFirstInStack()).thenReturn(mockCodeBuilder);
        doAnswer(i -> context.pushStack(type(valueClass))).when(mockCodeBuilder).build(context);

        StoreLocalVariableInsn insn = storeLocal(localName, mockCodeBuilder);
        insn.build(context);

        verify(mockMethodVisitor).visitVarInsn(opcode, 0);

        if(valueClass == double.class || valueClass == long.class) {
            assertThat(context.getLocalTypes(), hasSize(2));
            assertThat(context.getLocalType(0), is(type(valueClass)));
            assertThat(context.getLocalType(1), is(type(valueClass)));
        }
        else {
            assertThat(context.getLocalTypes(), hasSize(1));
            assertThat(context.getLocalType(0), is(type(valueClass)));
        }

        assertThat(context.getLocalIndex(localName), is(0));
    }

    private static Stream<Arguments> storeValueToNamedLocalVariable_testCases() {
        return Stream.of(
            Arguments.of(Object.class, ASTORE),
            Arguments.of(byte.class,   ISTORE),
            Arguments.of(short.class,  ISTORE),
            Arguments.of(int.class,    ISTORE),
            Arguments.of(long.class,   LSTORE),
            Arguments.of(float.class,  FSTORE),
            Arguments.of(double.class, DSTORE)
        );
    }
}