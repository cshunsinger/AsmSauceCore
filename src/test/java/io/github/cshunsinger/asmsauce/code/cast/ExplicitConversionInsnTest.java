package io.github.cshunsinger.asmsauce.code.cast;

import io.github.cshunsinger.asmsauce.AsmClassBuilder;
import io.github.cshunsinger.asmsauce.code.CodeBuilders;
import io.github.cshunsinger.asmsauce.code.CodeInsnBuilderLike;
import io.github.cshunsinger.asmsauce.definitions.TypeDefinition;
import io.github.cshunsinger.asmsauce.BaseUnitTest;
import io.github.cshunsinger.asmsauce.DefinitionBuilders;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ClassUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.lang.reflect.InvocationTargetException;
import java.util.stream.Stream;

import static io.github.cshunsinger.asmsauce.ConstructorNode.constructor;
import static io.github.cshunsinger.asmsauce.code.CodeBuilders.*;
import static io.github.cshunsinger.asmsauce.modifiers.AccessModifiers.publicOnly;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.apache.commons.lang3.RandomUtils.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

public class ExplicitConversionInsnTest extends BaseUnitTest {
    @Getter
    @RequiredArgsConstructor
    public static class TestExplicitConversion {
        private final String str;

        //Used to eat the stack to nothing when testing for IllegalStateExceptions throwing
        @SuppressWarnings("unused")
        protected static void wasteMethod(Object obj) {}
    }

    @ParameterizedTest
    @MethodSource("illegalArgumentException_nullOrInvalidConstructorParameters_testCases")
    public void illegalArgumentException_nullOrInvalidConstructorParameters(TypeDefinition<?> testType, CodeInsnBuilderLike testCodeBuilder, String exceptionMessage) {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> new ExplicitConversionInsn(testType, testCodeBuilder));
        assertThat(ex, hasProperty("message", is(exceptionMessage)));
    }

    private static Stream<Arguments> illegalArgumentException_nullOrInvalidConstructorParameters_testCases() {
        CodeInsnBuilderLike mockCodeBuilder = mock(CodeInsnBuilderLike.class);

        return Stream.of(
            Arguments.of(DefinitionBuilders.voidType(), mockCodeBuilder, "toType cannot be void."),
            Arguments.of(null, mockCodeBuilder, "toType cannot be null."),
            Arguments.of(DefinitionBuilders.type(Object.class), null, "Code builder cannot be null.")
        );
    }

    @Test
    public void illegalStateException_noStackElementsFromCodeBuilder() {
        AsmClassBuilder<TestExplicitConversion> builder = new AsmClassBuilder<>(TestExplicitConversion.class)
            .withConstructor(constructor(publicOnly(), DefinitionBuilders.parameters(Object.class), //public TestExplicitConversionImpl(Object obj)
                CodeBuilders.superConstructor(TestExplicitConversion.class, DefinitionBuilders.parameters(String.class),
                    CodeBuilders.cast(
                        String.class,
                        invokeStatic( //TestExplicitConversion.wasteMethod(obj)
                            TestExplicitConversion.class,
                            DefinitionBuilders.name("wasteMethod"),
                            DefinitionBuilders.parameters(Object.class),
                            CodeBuilders.getVar(1)
                        )
                    ) //super((String)TestExplicitConversion.wasteMethod(obj));
                ),
                CodeBuilders.returnVoid()
            ));

        IllegalStateException ex = assertThrows(IllegalStateException.class, builder::build);
        assertThat(ex, hasProperty("message", is("Expected 1 value to be placed onto the stack. Instead 0 items were placed/removed to the stack.")));
    }

    @Test
    public void illegalStateException_cannotCastObjectToPrimitive() {
        AsmClassBuilder<TestExplicitConversion> builder = new AsmClassBuilder<>(TestExplicitConversion.class)
            .withConstructor(constructor(publicOnly(), DefinitionBuilders.noParameters(), //public TestExplicitConversionImpl()
                superConstructor(TestExplicitConversion.class, DefinitionBuilders.parameters(String.class),
                    CodeBuilders.literalObj("SomeString")
                ),
                CodeBuilders.setVar(cast(int.class, CodeBuilders.instantiate(Object.class))), //attempt: (int)(new Object()) which is an invalid cast
                CodeBuilders.returnVoid()
            ));

        IllegalStateException ex = assertThrows(IllegalStateException.class, builder::build);
        assertThat(ex, hasProperty("message",
            is("Illegal casting attempt. Cannot cast %s to %s.".formatted(Object.class.getName(), int.class.getName()))
        ));
    }

    @Test
    public void successfulUseOfImplicitCastingSystemWhenImplicitCastingIsPossible() {
        //Using the explicit casting system to cast String to an Object
        //String can be implicitly cast, so the explicit casting system will fall back on the implicit casting system
        AsmClassBuilder<TestExplicitConversion> builder = new AsmClassBuilder<>(TestExplicitConversion.class)
            .withConstructor(constructor(publicOnly(), DefinitionBuilders.parameters(String.class),
                superConstructor(TestExplicitConversion.class, DefinitionBuilders.parameters(String.class), CodeBuilders.getVar(1)),
                CodeBuilders.getStatic(System.class, "out").invoke("println",
                    cast(Object.class, CodeBuilders.getVar(1)
                )),
                CodeBuilders.returnVoid()
            ));

        String testString = "Uncle Ben Dies in Spiderman: Far From Home";
        TestExplicitConversion instance = builder.buildInstance(testString);
        assertThat(instance, hasProperty("str", is(testString)));
    }

    @Test
    public void successfulCastFromObjectToString_noErrorsThrown() {
        Object testInput = "Test String As Object";

        AsmClassBuilder<TestExplicitConversion> builder = new AsmClassBuilder<>(TestExplicitConversion.class)
            .withConstructor(constructor(publicOnly(), DefinitionBuilders.parameters(Object.class),
                CodeBuilders.superConstructor(TestExplicitConversion.class, DefinitionBuilders.parameters(String.class),
                    cast(String.class, CodeBuilders.getVar(1))
                ),
                CodeBuilders.returnVoid()
            ));

        //Successfully instantiate when passing a String as an Object parameter.
        TestExplicitConversion instance = builder.buildInstance(testInput);
        assertThat(instance, hasProperty("str", is("Test String As Object")));

        //Class cast exception when passing in an object that cannot be cast to a String
        assertThrows(InvocationTargetException.class, () -> builder.buildInstance(new Object()));
    }

    @Getter
    @AllArgsConstructor
    public static abstract class TestPrimitivesType {
        private final double aDouble;
        private final float aFloat;
        private final long aLong;
        private final int anInt;
        private final short aShort;
        private final char aChar;
        private final byte aByte;
    }

    @ParameterizedTest
    @MethodSource("successfullyCastingPrimitivesExplicitly_testCases")
    public void successfullyCastingPrimitivesExplicitly(Number value, CodeInsnBuilderLike valueStacker) {
        AsmClassBuilder<TestPrimitivesType> builder = new AsmClassBuilder<>(TestPrimitivesType.class)
            .withConstructor(constructor(publicOnly(), DefinitionBuilders.noParameters(),
                CodeBuilders.superConstructor(TestPrimitivesType.class, DefinitionBuilders.parameters(double.class, float.class, long.class, int.class, short.class, char.class, byte.class),
                    CodeBuilders.cast(double.class, valueStacker),
                    CodeBuilders.cast(float.class, valueStacker),
                    CodeBuilders.cast(long.class, valueStacker),
                    CodeBuilders.cast(int.class, valueStacker),
                    CodeBuilders.cast(short.class, valueStacker),
                    CodeBuilders.cast(char.class, valueStacker),
                    CodeBuilders.cast(byte.class, valueStacker)
                ),
                CodeBuilders.returnVoid()
            ));

        TestPrimitivesType instance = builder.buildInstance();

        //Logging all of the values to make this test case a little easier to read and understand
        System.out.printf("""
            Casting test value of type %s to the different primitive types: %s
            Expected/Actual Values:
                double: %s  /   %s
                float:  %s  /   %s
                long:   %s  /   %s
                short:  %s  /   %s
                char:   %s  /   %s
                byte:   %s  /   %s
            """,
            ClassUtils.wrapperToPrimitive(value.getClass()), value,
            value.doubleValue(), instance.getADouble(),
            value.floatValue(), instance.getAFloat(),
            value.longValue(), instance.getALong(),
            value.shortValue(), instance.getAShort(),
            (char)value.intValue(), instance.getAChar(),
            value.byteValue(), instance.getAByte());

        assertThat(instance, allOf(
            notNullValue(),
            hasProperty("ADouble", is(value.doubleValue())),
            hasProperty("AFloat", is(value.floatValue())),
            hasProperty("ALong", is(value.longValue())),
            hasProperty("anInt", is(value.intValue())),
            hasProperty("AShort", is(value.shortValue())),
            hasProperty("AChar", is((char)value.intValue())),
            hasProperty("AByte", is(value.byteValue()))
        ));
    }

    private static Stream<Arguments> successfullyCastingPrimitivesExplicitly_testCases() {
        double d = nextDouble();
        float f = nextFloat();
        long l = nextLong();
        int i = nextInt();
        short s = (short)nextInt();
        char c = randomAlphabetic(1).charAt(0);
        byte b = (byte)nextInt();

        return Stream.of(
            Arguments.of(d, CodeBuilders.literal(d)),
            Arguments.of(f, CodeBuilders.literal(f)),
            Arguments.of(l, CodeBuilders.literal(l)),
            Arguments.of(i, CodeBuilders.literal(i)),
            Arguments.of(s, CodeBuilders.literal(s)),
            Arguments.of((int)c, CodeBuilders.literal(c)), //Character does not inherit from Number, so cast it to int to be passed as test parameter
            Arguments.of(b, CodeBuilders.literal(b))
        );
    }
}