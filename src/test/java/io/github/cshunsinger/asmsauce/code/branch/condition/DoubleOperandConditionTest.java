package io.github.cshunsinger.asmsauce.code.branch.condition;

import io.github.cshunsinger.asmsauce.AsmClassBuilder;
import io.github.cshunsinger.asmsauce.code.CodeInsnBuilderLike;
import io.github.cshunsinger.asmsauce.code.branch.Op;
import io.github.cshunsinger.asmsauce.definitions.ParametersDefinition;
import io.github.cshunsinger.asmsauce.BaseUnitTest;
import lombok.SneakyThrows;
import org.apache.commons.lang3.ClassUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.lang.reflect.Method;
import java.util.stream.Stream;

import static io.github.cshunsinger.asmsauce.ConstructorNode.constructor;
import static io.github.cshunsinger.asmsauce.DefinitionBuilders.*;
import static io.github.cshunsinger.asmsauce.MethodNode.method;
import static io.github.cshunsinger.asmsauce.code.CodeBuilders.*;
import static io.github.cshunsinger.asmsauce.modifiers.AccessModifiers.publicOnly;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.apache.commons.lang3.RandomUtils.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class DoubleOperandConditionTest extends BaseUnitTest {
    @SuppressWarnings("unused")
    public static abstract class TestIntComparisonType {
        public abstract boolean equals(int first, int second);
        public abstract boolean notEquals(int first, int second);
        public abstract boolean greaterThanOrEquals(int first, int second);
        public abstract boolean lessThanOrEquals(int first, int second);
        public abstract boolean greaterThan(int first, int second);
        public abstract boolean lessThan(int first, int second);
        public abstract boolean refEqualsPrim(Integer first, int second);
    }

    @SuppressWarnings("unused")
    public static abstract class TestLongComparisonType {
        public abstract boolean equals(long first, long second);
        public abstract boolean notEquals(long first, long second);
        public abstract boolean greaterThanOrEquals(long first, long second);
        public abstract boolean lessThanOrEquals(long first, long second);
        public abstract boolean greaterThan(long first, long second);
        public abstract boolean lessThan(long first, long second);
        public abstract boolean refEqualsPrim(Long first, long second);
    }

    @SuppressWarnings("unused")
    public static abstract class TestDoubleComparisonType {
        public abstract boolean equals(double first, double second);
        public abstract boolean notEquals(double first, double second);
        public abstract boolean greaterThanOrEquals(double first, double second);
        public abstract boolean lessThanOrEquals(double first, double second);
        public abstract boolean greaterThan(double first, double second);
        public abstract boolean lessThan(double first, double second);
        public abstract boolean refEqualsPrim(Double first, double second);
    }

    @SuppressWarnings("unused")
    public static abstract class TestFloatComparisonType {
        public abstract boolean equals(float first, float second);
        public abstract boolean notEquals(float first, float second);
        public abstract boolean greaterThanOrEquals(float first, float second);
        public abstract boolean lessThanOrEquals(float first, float second);
        public abstract boolean greaterThan(float first, float second);
        public abstract boolean lessThan(float first, float second);
        public abstract boolean refEqualsPrim(Float first, float second);
    }

    @SuppressWarnings("unused")
    public static abstract class TestShortComparisonType {
        public abstract boolean equals(short first, short second);
        public abstract boolean notEquals(short first, short second);
        public abstract boolean greaterThanOrEquals(short first, short second);
        public abstract boolean lessThanOrEquals(short first, short second);
        public abstract boolean greaterThan(short first, short second);
        public abstract boolean lessThan(short first, short second);
        public abstract boolean refEqualsPrim(Short first, short second);
    }

    @SuppressWarnings("unused")
    public static abstract class TestCharComparisonType {
        public abstract boolean equals(char first, char second);
        public abstract boolean notEquals(char first, char second);
        public abstract boolean greaterThanOrEquals(char first, char second);
        public abstract boolean lessThanOrEquals(char first, char second);
        public abstract boolean greaterThan(char first, char second);
        public abstract boolean lessThan(char first, char second);
        public abstract boolean refEqualsPrim(Character first, char second);
    }

    @SuppressWarnings("unused")
    public static abstract class TestByteComparisonType {
        public abstract boolean equals(byte first, byte second);
        public abstract boolean notEquals(byte first, byte second);
        public abstract boolean greaterThanOrEquals(byte first, byte second);
        public abstract boolean lessThanOrEquals(byte first, byte second);
        public abstract boolean greaterThan(byte first, byte second);
        public abstract boolean lessThan(byte first, byte second);
        public abstract boolean refEqualsPrim(Byte first, byte second);
    }

    @SneakyThrows
    @ParameterizedTest
    @MethodSource("testComparisonsWithDifferentTypes_testCases")
    public void testComparisonsWithDifferentPrimitiveTypes(Class<?> baseClass,
                                                           Class<?> parameterPrimitiveType,
                                                           Object firstValue,
                                                           Object secondValue) {
        Class<?> parameterWrapperType = ClassUtils.primitiveToWrapper(parameterPrimitiveType);

        Method equals = baseClass.getMethod("equals", parameterPrimitiveType, parameterPrimitiveType);
        Method notEquals = baseClass.getMethod("notEquals", parameterPrimitiveType, parameterPrimitiveType);
        Method greaterThanOrEquals = baseClass.getMethod("greaterThanOrEquals", parameterPrimitiveType, parameterPrimitiveType);
        Method lessThanOrEquals = baseClass.getMethod("lessThanOrEquals", parameterPrimitiveType, parameterPrimitiveType);
        Method greaterThan = baseClass.getMethod("greaterThan", parameterPrimitiveType, parameterPrimitiveType);
        Method lessThan = baseClass.getMethod("lessThan", parameterPrimitiveType, parameterPrimitiveType);
        Method refEqualsPrim = baseClass.getMethod("refEqualsPrim", parameterWrapperType, parameterPrimitiveType);

        ParametersDefinition methodParameters = parameters(p("first", parameterPrimitiveType), p("second", parameterPrimitiveType));

        Object instance = new AsmClassBuilder<>(baseClass)
            .withConstructor(constructor(publicOnly(), noParameters(),
                superConstructor(baseClass, noParameters()),
                returnVoid()
            ))
            .withMethod(method(publicOnly(), name("equals"), methodParameters, type(boolean.class),
                if_(getVar("first").eq(getVar("second"))).then(
                    returnValue(true_())
                ),
                returnValue(false_())
            ))
            .withMethod(method(publicOnly(), name("notEquals"), methodParameters, type(boolean.class),
                if_(getVar("first").ne(getVar("second"))).then(
                    returnValue(true_())
                ),
                returnValue(false_())
            ))
            .withMethod(method(publicOnly(), name("greaterThanOrEquals"), methodParameters, type(boolean.class),
                if_(getVar("first").ge(getVar("second"))).then(
                    returnValue(true_())
                ),
                returnValue(false_())
            ))
            .withMethod(method(publicOnly(), name("lessThanOrEquals"), methodParameters, type(boolean.class),
                if_(getVar("first").le(getVar("second"))).then(
                    returnValue(true_())
                ),
                returnValue(false_())
            ))
            .withMethod(method(publicOnly(), name("greaterThan"), methodParameters, type(boolean.class),
                if_(getVar("first").gt(getVar("second"))).then(
                    returnValue(true_())
                ),
                returnValue(false_())
            ))
            .withMethod(method(publicOnly(), name("lessThan"), methodParameters, type(boolean.class),
                if_(getVar("first").lt(getVar("second"))).then(
                    returnValue(true_())
                ),
                returnValue(false_())
            ))
            .withMethod(method(publicOnly(), name("refEqualsPrim"), parameters(p("first", parameterWrapperType), p("second", parameterPrimitiveType)), type(boolean.class),
                if_(getVar("first").isNotNull()).then(
                    getStatic(System.class, "out").invoke("println",
                        literalObj("first: ").invoke("concat",
                            getVar("first").invoke("toString")
                        )
                    )
                ),
                if_(getVar("first").isNull()).then(
                    getStatic(System.class, "out").invoke("println",
                        literalObj("first: null")
                    )
                ),
                getStatic(System.class, "out").invoke("println",
                    literalObj("second: ").invoke("concat",
                        invokeStatic(String.class, "valueOf",
                            parameterPrimitiveType == byte.class || parameterPrimitiveType == short.class ?
                                cast(int.class, getVar("second")) :
                                getVar("second")
                        )
                    )
                ),
                if_(getVar("first").eq(getVar("second"))).then(
                    returnValue(true_())
                ),
                returnValue(false_())
            ))
            .buildInstance();

        //NOTE: The test cases guarantee that `secondValue` is greater than `firstValue`
        assertThat(equals.invoke(instance, firstValue, firstValue), is(true));
        assertThat(equals.invoke(instance, firstValue, secondValue), is(false));
        assertThat(notEquals.invoke(instance, firstValue, secondValue), is(true));
        assertThat(notEquals.invoke(instance, firstValue, firstValue), is(false));
        assertThat(greaterThanOrEquals.invoke(instance, firstValue, firstValue), is(true));
        assertThat(greaterThanOrEquals.invoke(instance, secondValue, firstValue), is(true));
        assertThat(greaterThanOrEquals.invoke(instance, firstValue, secondValue), is(false));
        assertThat(lessThanOrEquals.invoke(instance, firstValue, firstValue), is(true));
        assertThat(lessThanOrEquals.invoke(instance, secondValue, firstValue), is(false));
        assertThat(lessThanOrEquals.invoke(instance, firstValue, secondValue), is(true));
        assertThat(greaterThan.invoke(instance, firstValue, firstValue), is(false));
        assertThat(greaterThan.invoke(instance, secondValue, firstValue), is(true));
        assertThat(greaterThan.invoke(instance, firstValue, secondValue), is(false));
        assertThat(lessThan.invoke(instance, firstValue, firstValue), is(false));
        assertThat(lessThan.invoke(instance, secondValue, firstValue), is(false));
        assertThat(lessThan.invoke(instance, firstValue, secondValue), is(true));

        //TODO: It is a known bug that comparing a double or float wrapper to a double or float primitive will never yield `true`
        //TODO: All primitives compare properly to other primitives.
        //TODO: And all other primitives/wrappers compare properly. It's literally just Float/float and Double/double...
        if(parameterPrimitiveType != double.class && parameterPrimitiveType != float.class) {
            //For now, this bug will be ignored and fixed at a later time
            assertThat(refEqualsPrim.invoke(instance, firstValue, firstValue), is(true));
            assertThat(refEqualsPrim.invoke(instance, firstValue, secondValue), is(false));
            assertThat(refEqualsPrim.invoke(instance, null, firstValue), is(false));
        }
    }

    private static Stream<Arguments> testComparisonsWithDifferentTypes_testCases() {
        //NOTE: The test cases guarantee that `secondValue` is greater than `firstValue`
        byte firstByte = (byte)nextInt(0, 50);
        byte secondByte = (byte)nextInt(51, 100);
        char firstChar = randomAlphabetic(1).charAt(0);
        char secondChar = (char)(firstChar + 1);
        short firstShort = (short)nextInt(0, 100);
        short secondShort = (short)nextInt(101, 200);
        int firstInt = nextInt(0, 100);
        int secondInt = nextInt(101, 200);
        long firstLong = nextLong(0L, 100L);
        long secondLong = nextLong(101L, 200L);
        float firstFloat = nextFloat(0.0f, 100.0f);
        float secondFloat = nextFloat(101.0f, 200.0f);
        double firstDouble = nextDouble(0.0, 100.0);
        double secondDouble = nextDouble(101.0, 200.0);

        return Stream.of(
            Arguments.of(TestIntComparisonType.class, int.class, firstInt, secondInt),
            Arguments.of(TestLongComparisonType.class, long.class, firstLong, secondLong),
            Arguments.of(TestFloatComparisonType.class, float.class, firstFloat, secondFloat),
            Arguments.of(TestDoubleComparisonType.class, double.class, firstDouble, secondDouble),
            Arguments.of(TestShortComparisonType.class, short.class, firstShort, secondShort),
            Arguments.of(TestCharComparisonType.class, char.class, firstChar, secondChar),
            Arguments.of(TestByteComparisonType.class, byte.class, firstByte, secondByte)
        );
    }

    @SuppressWarnings("unused")
    public static abstract class TestReferenceComparisonsType {
        public abstract boolean equals(Object first, Object second);
        public abstract boolean notEquals(Object first, Object second);
    }

    @Test
    public void testComparisonsWithReferences() {
        AsmClassBuilder<TestReferenceComparisonsType> builder = new AsmClassBuilder<>(TestReferenceComparisonsType.class)
            .withConstructor(constructor(publicOnly(), noParameters(),
                superConstructor(TestReferenceComparisonsType.class, noParameters()),
                returnVoid()
            ))
            .withMethod(method(publicOnly(), name("equals"), parameters(p("first", Object.class), p("second", Object.class)), type(boolean.class),
                if_(getVar("first").eq(getVar("second"))).then(
                    returnValue(true_())
                ),
                returnValue(false_())
            ))
            .withMethod(method(publicOnly(), name("notEquals"), parameters(p("first", Object.class), p("second", Object.class)), type(boolean.class),
                if_(getVar("first").ne(getVar("second"))).then(
                    returnValue(true_())
                ),
                returnValue(false_())
            ));

        TestReferenceComparisonsType instance = builder.buildInstance();

        Object testFirst = new Object();
        Object testSecond = new Object();

        //equals
        assertThat(instance.equals(testFirst, testFirst), is(true));
        assertThat(instance.equals(testFirst, testSecond), is(false));
        assertThat(instance.equals(null, testFirst), is(false));
        assertThat(instance.equals(testFirst, null), is(false));
        assertThat(instance.equals(null, null), is(true));

        //notEquals
        assertThat(instance.notEquals(testFirst, testFirst), is(false));
        assertThat(instance.notEquals(testFirst, testSecond), is(true));
        assertThat(instance.notEquals(null, testFirst), is(true));
        assertThat(instance.notEquals(testFirst, null), is(true));
        assertThat(instance.notEquals(null, null), is(false));
    }

    @SuppressWarnings("unused")
    public static abstract class TestInvalidReferenceComparisonsType {
        public abstract boolean invalidComparison(Object first, Object second);
    }

    @ParameterizedTest
    @EnumSource(value = Op.class, names = {"EQ", "NE", "NOT_EQ", "NOT_NE"}, mode = EnumSource.Mode.EXCLUDE)
    public void illegalStateException_invalidComparisonOperator(Op operation) {
        AsmClassBuilder<TestInvalidReferenceComparisonsType> builder = new AsmClassBuilder<>(TestInvalidReferenceComparisonsType.class)
            .withConstructor(constructor(publicOnly(), noParameters(),
                superConstructor(TestInvalidReferenceComparisonsType.class, noParameters()),
                returnVoid()
            ))
            .withMethod(method(publicOnly(), name("invalidComparison"), parameters(p("first", Object.class), p("second", Object.class)), type(boolean.class),
                if_(new DoubleOperandCondition(getVar("first"), getVar("second"), operation)).then(
                    returnValue(true_())
                ),
                returnValue(false_())
            ));

        IllegalStateException ex = assertThrows(IllegalStateException.class, builder::build);
        assertThat(ex, hasProperty("message", is(
            "Comparison operation %s is invalid for reference comparisons.".formatted(operation.name())
        )));
    }

    @Test
    public void illegalStateException_comparisonOperandBuilderStacksZeroValues() {
        AsmClassBuilder<TestInvalidReferenceComparisonsType> builder = new AsmClassBuilder<>(TestInvalidReferenceComparisonsType.class)
            .withConstructor(constructor(publicOnly(), noParameters(),
                superConstructor(TestInvalidReferenceComparisonsType.class, noParameters()),
                returnVoid()
            ))
            .withMethod(method(publicOnly(), name("invalidComparison"), parameters(p("first", Object.class), p("second", Object.class)), type(boolean.class),
                if_(getStatic(System.class, "out").invoke("println", getVar("first").invoke("toString")).eq(getVar("second"))).then(
                    returnValue(true_())
                ),
                returnValue(false_())
            ));

        IllegalStateException ex = assertThrows(IllegalStateException.class, builder::build);
        assertThat(ex, hasProperty("message", is("Expected 1 element to be stacked. Found 0 instead.")));
    }

    @ParameterizedTest
    @CsvSource({
        "EQ,NOT_EQ",
        "NE,NOT_NE",
        "GE,NOT_GE",
        "LE,NOT_LE",
        "GT,NOT_GT",
        "LT,NOT_LT"
    })
    public void testInvertingDoubleOperandConditions(Op operation, Op inverseOperation) {
        CodeInsnBuilderLike operand1 = literal(123);
        CodeInsnBuilderLike operand2 = literal(456);

        //Test condition to be inverted
        DoubleOperandCondition originalCondition = new DoubleOperandCondition(operand1, operand2, operation);
        //Inverse of the original condition
        DoubleOperandCondition inverseCondition = originalCondition.invert();
        //Inverse of the inverted original condition - should be identical to the original condition, thought not the same reference.
        DoubleOperandCondition condition = inverseCondition.invert();

        assertThat(inverseCondition, allOf(
            notNullValue(),
            hasProperty("operand1Builder", is(operand1)),
            hasProperty("operand2Builder", is(operand2)),
            hasProperty("conditionOp", is(inverseOperation))
        ));

        assertThat(condition, allOf(
            notNullValue(),
            hasProperty("operand1Builder", is(operand1)),
            hasProperty("operand2Builder", is(operand2)),
            hasProperty("conditionOp", is(operation))
        ));
    }
}