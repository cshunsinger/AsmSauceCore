package com.chunsinger.asmsauce.code.cast;

import aj.org.objectweb.asm.MethodVisitor;
import com.chunsinger.asmsauce.ClassBuildingContext;
import com.chunsinger.asmsauce.MethodBuildingContext;
import com.chunsinger.asmsauce.ThisClass;
import com.chunsinger.asmsauce.definitions.CompleteMethodDefinition;
import com.chunsinger.asmsauce.definitions.TypeDefinition;
import com.chunsinger.asmsauce.testing.BaseUnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;

import java.util.stream.Stream;

import static aj.org.objectweb.asm.Opcodes.*;
import static com.chunsinger.asmsauce.DefinitionBuilders.*;
import static com.chunsinger.asmsauce.modifiers.AccessModifiers.publicOnly;
import static java.util.Collections.emptyList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;

class ImplicitConversionInsnTest extends BaseUnitTest {
    @Mock
    private MethodVisitor mockMethodVisitor;

    private static final CompleteMethodDefinition<?, ?> methodDefinition = new CompleteMethodDefinition<>(
        type(ThisClass.class),
        publicOnly(),
        name("newMethodName"),
        voidType(),
        noParameters(),
        noThrows()
    );

    @Test
    public void illegalArgumentException_nullToType() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> new ImplicitConversionInsn(null));
        assertThat(ex, hasProperty("message", is("toType cannot be null.")));
    }

    @Test
    public void illegalStateException_nothingOnStackToImplicitlyConvert() {
        ImplicitConversionInsn insn = new ImplicitConversionInsn(type(String.class));
        IllegalStateException ex = assertThrows(
            IllegalStateException.class,
            () -> insn.build(new MethodBuildingContext(null, null, null, emptyList()))
        );

        assertThat(ex, hasProperty("message", is("There is no element expected on the stack to be cast.")));
    }

    @Test
    public void success_implicitConversionToAssignableType() {
        TypeDefinition<?> testToType = type(Object.class);
        ImplicitConversionInsn insn = new ImplicitConversionInsn(testToType);

        ClassBuildingContext classContext = new ClassBuildingContext(null, null, null, null, null, null, null);
        MethodBuildingContext methodContext = new MethodBuildingContext(mockMethodVisitor, methodDefinition, classContext, emptyList());
        methodContext.pushStack(type(String.class));

        insn.build(methodContext);

        assertThat(methodContext.stackSize(), is(1)); //Should still only have 1 item on stack
        assertThat(methodContext.peekStack(), is(testToType)); //Item on stack should now be Object instead of String

        verify(mockMethodVisitor).visitTypeInsn(CHECKCAST, testToType.getJvmTypeName());
    }

    @Test
    public void success_noCastRequired() {
        TypeDefinition<?> testToType = type(Object.class);
        ImplicitConversionInsn insn = new ImplicitConversionInsn(testToType);

        MethodBuildingContext methodContext = new MethodBuildingContext(mockMethodVisitor, null, null, emptyList());
        methodContext.pushStack(type(Object.class));

        insn.build(methodContext);

        assertThat(methodContext.stackSize(), is(1)); //Should still only have 1 item on stack
        assertThat(methodContext.peekStack(), is(testToType)); //Item on stack should now be Object instead of String
    }

    @ParameterizedTest
    @MethodSource("illegalStateException_cannotImplicitlyConvertReferenceType_testArguments")
    public void illegalStateException_cannotImplicitlyConvertReferenceTypes(TypeDefinition<?> testFromType, TypeDefinition<?> testToType) {
        ImplicitConversionInsn insn = new ImplicitConversionInsn(testToType);

        MethodBuildingContext methodContext = new MethodBuildingContext(mockMethodVisitor, null, null, emptyList());
        methodContext.pushStack(testFromType);

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> insn.build(methodContext));
        assertThat(ex, hasProperty("message", is(
            "Cannot convert from type %s into type %s.".formatted(testFromType.getType().getName(), testToType.getType().getName())
        )));
    }

    private static Stream<Arguments> illegalStateException_cannotImplicitlyConvertReferenceType_testArguments() {
        return Stream.of(
            Arguments.of(type(Object.class), type(String.class)),
            Arguments.of(type(int.class), type(Float.class)),
            Arguments.of(type(double.class), type(float.class)),
            Arguments.of(type(float.class), type(byte.class))
        );
    }

    @Test
    public void success_implicitAutoBoxingPrimitiveToWrapper() {
        TypeDefinition<?> testToType = type(Integer.class);
        ImplicitConversionInsn insn = new ImplicitConversionInsn(testToType);

        MethodBuildingContext methodContext = new MethodBuildingContext(mockMethodVisitor, methodDefinition, null, emptyList());
        methodContext.pushStack(type(int.class));

        insn.build(methodContext);

        verify(mockMethodVisitor).visitMethodInsn(
            INVOKESTATIC,
            testToType.getJvmTypeName(),
            "valueOf",
            "(I)Ljava/lang/Integer;",
            false
        );

        assertThat(methodContext.stackSize(), is(1)); //Stack size should remain unchanged
        assertThat(methodContext.peekStack(), is(testToType)); //Stack should contain the new type
    }

    @Test
    public void success_implicitAutoUnboxingWrapperToPrimitive() {
        TypeDefinition<?> testToType = type(int.class);
        TypeDefinition<?> testFromType = type(Integer.class);
        ImplicitConversionInsn insn = new ImplicitConversionInsn(testToType);

        MethodBuildingContext methodContext = new MethodBuildingContext(mockMethodVisitor, methodDefinition, null, emptyList());
        methodContext.pushStack(testFromType);

        insn.build(methodContext);

        verify(mockMethodVisitor).visitMethodInsn(
            INVOKEVIRTUAL,
            testFromType.getJvmTypeName(),
            "intValue",
            "()I",
            false
        );

        assertThat(methodContext.stackSize(), is(1)); //Stack size should remain unchanged
        assertThat(methodContext.peekStack(), is(testToType)); //Stack should contain the new type
    }

    @ParameterizedTest
    @MethodSource("success_implicitWideningCastOfPrimitiveToPrimitive_testArguments")
    public void success_implicitWideningCastOfPrimitiveToPrimitive(TypeDefinition<?> testFromType,
                                                                   TypeDefinition<?> testToType,
                                                                   Integer expectedInstruction) {
        ImplicitConversionInsn insn = new ImplicitConversionInsn(testToType);

        MethodBuildingContext methodContext = new MethodBuildingContext(mockMethodVisitor, methodDefinition, null, emptyList());
        methodContext.pushStack(testFromType);

        insn.build(methodContext);

        if(expectedInstruction != null)
            verify(mockMethodVisitor).visitInsn(expectedInstruction);

        assertThat(methodContext.stackSize(), is(1)); //Stack size should remain unchanged
        assertThat(methodContext.peekStack(), is(testToType)); //Stack should contain the new type
    }

    private static Stream<Arguments> success_implicitWideningCastOfPrimitiveToPrimitive_testArguments() {
        return Stream.of(
            //From byte
            Arguments.of(type(byte.class), type(byte.class), null),
            Arguments.of(type(byte.class), type(short.class), null),
            Arguments.of(type(byte.class), type(char.class), null),
            Arguments.of(type(byte.class), type(int.class), null),
            Arguments.of(type(byte.class), type(long.class), I2L),
            Arguments.of(type(byte.class), type(float.class), I2F),
            Arguments.of(type(byte.class), type(double.class), I2D),
            //From short
            Arguments.of(type(short.class), type(short.class), null),
            Arguments.of(type(short.class), type(char.class), null),
            Arguments.of(type(short.class), type(int.class), null),
            Arguments.of(type(short.class), type(long.class), I2L),
            Arguments.of(type(short.class), type(float.class), I2F),
            Arguments.of(type(short.class), type(double.class), I2D),
            //From char
            Arguments.of(type(char.class), type(short.class), null),
            Arguments.of(type(char.class), type(char.class), null),
            Arguments.of(type(char.class), type(int.class), null),
            Arguments.of(type(char.class), type(long.class), I2L),
            Arguments.of(type(char.class), type(float.class), I2F),
            Arguments.of(type(char.class), type(double.class), I2D),
            //From int
            Arguments.of(type(int.class), type(int.class), null),
            Arguments.of(type(int.class), type(long.class), I2L),
            Arguments.of(type(int.class), type(float.class), I2F),
            Arguments.of(type(int.class), type(double.class), I2D),
            //From long
            Arguments.of(type(long.class), type(long.class), null),
            Arguments.of(type(long.class), type(float.class), L2F),
            Arguments.of(type(long.class), type(double.class), L2D),
            //From float
            Arguments.of(type(float.class), type(float.class), null),
            Arguments.of(type(float.class), type(double.class), F2D),
            //From double
            Arguments.of(type(double.class), type(double.class), null)
        );
    }
}