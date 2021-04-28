package io.github.cshunsinger.asmsauce.code.cast;

import org.objectweb.asm.MethodVisitor;
import io.github.cshunsinger.asmsauce.ClassBuildingContext;
import io.github.cshunsinger.asmsauce.MethodBuildingContext;
import io.github.cshunsinger.asmsauce.ThisClass;
import io.github.cshunsinger.asmsauce.definitions.CompleteMethodDefinition;
import io.github.cshunsinger.asmsauce.definitions.TypeDefinition;
import io.github.cshunsinger.asmsauce.BaseUnitTest;
import io.github.cshunsinger.asmsauce.DefinitionBuilders;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;

import java.util.stream.Stream;

import static org.objectweb.asm.Opcodes.*;
import static io.github.cshunsinger.asmsauce.modifiers.AccessModifiers.publicOnly;
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
        DefinitionBuilders.type(ThisClass.class),
        publicOnly(),
        DefinitionBuilders.name("newMethodName"),
        DefinitionBuilders.voidType(),
        DefinitionBuilders.noParameters(),
        DefinitionBuilders.noThrows()
    );

    @Test
    public void illegalArgumentException_nullToType() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> new ImplicitConversionInsn(null));
        assertThat(ex, hasProperty("message", is("toType cannot be null.")));
    }

    @Test
    public void illegalStateException_nothingOnStackToImplicitlyConvert() {
        ImplicitConversionInsn insn = new ImplicitConversionInsn(DefinitionBuilders.type(String.class));

        new MethodBuildingContext(null, null, null, emptyList());

        IllegalStateException ex = assertThrows(IllegalStateException.class, insn::build);

        assertThat(ex, hasProperty("message", is("There is no element expected on the stack to be cast.")));
    }

    @Test
    public void success_implicitConversionToAssignableType() {
        TypeDefinition<?> testToType = DefinitionBuilders.type(Object.class);
        ImplicitConversionInsn insn = new ImplicitConversionInsn(testToType);

        ClassBuildingContext classContext = new ClassBuildingContext(null, null, null, null, null, null, null);
        MethodBuildingContext methodContext = new MethodBuildingContext(mockMethodVisitor, methodDefinition, classContext, emptyList());
        methodContext.pushStack(DefinitionBuilders.type(String.class));

        insn.build();

        assertThat(methodContext.stackSize(), is(1)); //Should still only have 1 item on stack
        assertThat(methodContext.peekStack(), is(testToType)); //Item on stack should now be Object instead of String

        verify(mockMethodVisitor).visitTypeInsn(CHECKCAST, testToType.getJvmTypeName());
    }

    @Test
    public void success_noCastRequired() {
        TypeDefinition<?> testToType = DefinitionBuilders.type(Object.class);
        ImplicitConversionInsn insn = new ImplicitConversionInsn(testToType);

        MethodBuildingContext methodContext = new MethodBuildingContext(mockMethodVisitor, null, null, emptyList());
        methodContext.pushStack(DefinitionBuilders.type(Object.class));

        insn.build();

        assertThat(methodContext.stackSize(), is(1)); //Should still only have 1 item on stack
        assertThat(methodContext.peekStack(), is(testToType)); //Item on stack should now be Object instead of String
    }

    @ParameterizedTest
    @MethodSource("illegalStateException_cannotImplicitlyConvertReferenceType_testArguments")
    public void illegalStateException_cannotImplicitlyConvertReferenceTypes(TypeDefinition<?> testFromType, TypeDefinition<?> testToType) {
        ImplicitConversionInsn insn = new ImplicitConversionInsn(testToType);

        MethodBuildingContext methodContext = new MethodBuildingContext(mockMethodVisitor, null, null, emptyList());
        methodContext.pushStack(testFromType);

        IllegalStateException ex = assertThrows(IllegalStateException.class, insn::build);
        assertThat(ex, hasProperty("message", is(
            "Cannot convert from type %s into type %s.".formatted(testFromType.getType().getName(), testToType.getType().getName())
        )));
    }

    private static Stream<Arguments> illegalStateException_cannotImplicitlyConvertReferenceType_testArguments() {
        return Stream.of(
            Arguments.of(DefinitionBuilders.type(Object.class), DefinitionBuilders.type(String.class)),
            Arguments.of(DefinitionBuilders.type(int.class), DefinitionBuilders.type(Float.class)),
            Arguments.of(DefinitionBuilders.type(double.class), DefinitionBuilders.type(float.class)),
            Arguments.of(DefinitionBuilders.type(float.class), DefinitionBuilders.type(byte.class))
        );
    }

    @Test
    public void success_implicitAutoBoxingPrimitiveToWrapper() {
        TypeDefinition<?> testToType = DefinitionBuilders.type(Integer.class);
        ImplicitConversionInsn insn = new ImplicitConversionInsn(testToType);

        MethodBuildingContext methodContext = new MethodBuildingContext(mockMethodVisitor, methodDefinition, null, emptyList());
        methodContext.pushStack(DefinitionBuilders.type(int.class));

        insn.build();

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
        TypeDefinition<?> testToType = DefinitionBuilders.type(int.class);
        TypeDefinition<?> testFromType = DefinitionBuilders.type(Integer.class);
        ImplicitConversionInsn insn = new ImplicitConversionInsn(testToType);

        MethodBuildingContext methodContext = new MethodBuildingContext(mockMethodVisitor, methodDefinition, null, emptyList());
        methodContext.pushStack(testFromType);

        insn.build();

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

        insn.build();

        if(expectedInstruction != null)
            verify(mockMethodVisitor).visitInsn(expectedInstruction);

        assertThat(methodContext.stackSize(), is(1)); //Stack size should remain unchanged
        assertThat(methodContext.peekStack(), is(testToType)); //Stack should contain the new type
    }

    private static Stream<Arguments> success_implicitWideningCastOfPrimitiveToPrimitive_testArguments() {
        return Stream.of(
            //From byte
            Arguments.of(DefinitionBuilders.type(byte.class), DefinitionBuilders.type(byte.class), null),
            Arguments.of(DefinitionBuilders.type(byte.class), DefinitionBuilders.type(short.class), null),
            Arguments.of(DefinitionBuilders.type(byte.class), DefinitionBuilders.type(char.class), null),
            Arguments.of(DefinitionBuilders.type(byte.class), DefinitionBuilders.type(int.class), null),
            Arguments.of(DefinitionBuilders.type(byte.class), DefinitionBuilders.type(long.class), I2L),
            Arguments.of(DefinitionBuilders.type(byte.class), DefinitionBuilders.type(float.class), I2F),
            Arguments.of(DefinitionBuilders.type(byte.class), DefinitionBuilders.type(double.class), I2D),
            //From short
            Arguments.of(DefinitionBuilders.type(short.class), DefinitionBuilders.type(short.class), null),
            Arguments.of(DefinitionBuilders.type(short.class), DefinitionBuilders.type(char.class), null),
            Arguments.of(DefinitionBuilders.type(short.class), DefinitionBuilders.type(int.class), null),
            Arguments.of(DefinitionBuilders.type(short.class), DefinitionBuilders.type(long.class), I2L),
            Arguments.of(DefinitionBuilders.type(short.class), DefinitionBuilders.type(float.class), I2F),
            Arguments.of(DefinitionBuilders.type(short.class), DefinitionBuilders.type(double.class), I2D),
            //From char
            Arguments.of(DefinitionBuilders.type(char.class), DefinitionBuilders.type(short.class), null),
            Arguments.of(DefinitionBuilders.type(char.class), DefinitionBuilders.type(char.class), null),
            Arguments.of(DefinitionBuilders.type(char.class), DefinitionBuilders.type(int.class), null),
            Arguments.of(DefinitionBuilders.type(char.class), DefinitionBuilders.type(long.class), I2L),
            Arguments.of(DefinitionBuilders.type(char.class), DefinitionBuilders.type(float.class), I2F),
            Arguments.of(DefinitionBuilders.type(char.class), DefinitionBuilders.type(double.class), I2D),
            //From int
            Arguments.of(DefinitionBuilders.type(int.class), DefinitionBuilders.type(int.class), null),
            Arguments.of(DefinitionBuilders.type(int.class), DefinitionBuilders.type(long.class), I2L),
            Arguments.of(DefinitionBuilders.type(int.class), DefinitionBuilders.type(float.class), I2F),
            Arguments.of(DefinitionBuilders.type(int.class), DefinitionBuilders.type(double.class), I2D),
            //From long
            Arguments.of(DefinitionBuilders.type(long.class), DefinitionBuilders.type(long.class), null),
            Arguments.of(DefinitionBuilders.type(long.class), DefinitionBuilders.type(float.class), L2F),
            Arguments.of(DefinitionBuilders.type(long.class), DefinitionBuilders.type(double.class), L2D),
            //From float
            Arguments.of(DefinitionBuilders.type(float.class), DefinitionBuilders.type(float.class), null),
            Arguments.of(DefinitionBuilders.type(float.class), DefinitionBuilders.type(double.class), F2D),
            //From double
            Arguments.of(DefinitionBuilders.type(double.class), DefinitionBuilders.type(double.class), null)
        );
    }
}