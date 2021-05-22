package io.github.cshunsinger.asmsauce.code.math;

import io.github.cshunsinger.asmsauce.AsmClassBuilder;
import io.github.cshunsinger.asmsauce.BaseUnitTest;
import io.github.cshunsinger.asmsauce.MethodBuildingContext;
import io.github.cshunsinger.asmsauce.ThisClass;
import io.github.cshunsinger.asmsauce.code.CodeInsnBuilderLike;
import org.junit.jupiter.api.Test;

import static io.github.cshunsinger.asmsauce.ConstructorNode.constructor;
import static io.github.cshunsinger.asmsauce.DefinitionBuilders.*;
import static io.github.cshunsinger.asmsauce.MethodNode.method;
import static io.github.cshunsinger.asmsauce.code.CodeBuilders.*;
import static io.github.cshunsinger.asmsauce.modifiers.AccessModifiers.privateStatic;
import static io.github.cshunsinger.asmsauce.modifiers.AccessModifiers.publicOnly;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class MathOperationInsnTest extends BaseUnitTest {
    //This test class will be inherited by a generated class that implements all of these methods
    public static abstract class TestMaths {
        public abstract byte add(byte a, byte b);
        public abstract short add(short a, short b);
        public abstract int add(int a, int b);
        public abstract long add(long a, long b);
        public abstract float add(float a, float b);
        public abstract double add(double a, double d);

        public abstract byte sub(byte a, byte b);
        public abstract short sub(short a, short b);
        public abstract int sub(int a, int b);
        public abstract long sub(long a, long b);
        public abstract float sub(float a, float b);
        public abstract double sub(double a, double d);

        public abstract byte mul(byte a, byte b);
        public abstract short mul(short a, short b);
        public abstract int mul(int a, int b);
        public abstract long mul(long a, long b);
        public abstract float mul(float a, float b);
        public abstract double mul(double a, double d);

        public abstract byte div(byte a, byte b);
        public abstract short div(short a, short b);
        public abstract int div(int a, int b);
        public abstract long div(long a, long b);
        public abstract float div(float a, float b);
        public abstract double div(double a, double d);

        public abstract byte mod(byte a, byte b);
        public abstract short mod(short a, short b);
        public abstract int mod(int a, int b);
        public abstract long mod(long a, long b);
        public abstract float mod(float a, float b);
        public abstract double mod(double a, double d);
    }

    @Test
    public void testJavaByteCodeMathOperations() {
        AsmClassBuilder<TestMaths> mathsBuilder = new AsmClassBuilder<>(TestMaths.class)
            .withConstructor(constructor(publicOnly(), noParameters(), //public TestMathsImpl()
                superConstructor(TestMaths.class, noParameters()), //super();
                returnVoid() //return;
            ))
            .withMethod(method(publicOnly(), name("add"), parameters(byte.class, byte.class), type(byte.class), //public byte add(byte a, byte b)
                returnValue( //return a + b;
                    getVar(1).add(getVar(2))
                )
            ))
            .withMethod(method(publicOnly(), name("add"), parameters(short.class, short.class), type(short.class), //public short add(short a, short b)
                returnValue( //return a + b;
                    getVar(1).add(getVar(2))
                )
            ))
            .withMethod(method(publicOnly(), name("add"), parameters(int.class, int.class), type(int.class), //public int add(int a, int b)
                returnValue( //return a + b;
                    getVar(1).add(getVar(2))
                )
            ))
            .withMethod(method(publicOnly(), name("add"), parameters(long.class, long.class), type(long.class), //public long add(long a, long b)
                returnValue( //return a + b;
                    getVar(1).add(getVar(3))
                )
            ))
            .withMethod(method(publicOnly(), name("add"), parameters(float.class, float.class), type(float.class), //public float add(float a, float b)
                returnValue( //return a + b;
                    getVar(1).add(getVar(2))
                )
            ))
            .withMethod(method(publicOnly(), name("add"), parameters(double.class, double.class), type(double.class), //public double add(double a, double b)
                returnValue( //return a + b;
                    getVar(1).add(getVar(3))
                )
            ))
            .withMethod(method(publicOnly(), name("sub"), parameters(byte.class, byte.class), type(byte.class), //public byte sub(byte a, byte b)
                returnValue( //return a + b;
                    getVar(1).sub(getVar(2))
                )
            ))
            .withMethod(method(publicOnly(), name("sub"), parameters(short.class, short.class), type(short.class), //public short sub(short a, short b)
                returnValue( //return a + b;
                    getVar(1).sub(getVar(2))
                )
            ))
            .withMethod(method(publicOnly(), name("sub"), parameters(int.class, int.class), type(int.class), //public int sub(int a, int b)
                returnValue( //return a + b;
                    getVar(1).sub(getVar(2))
                )
            ))
            .withMethod(method(publicOnly(), name("sub"), parameters(long.class, long.class), type(long.class), //public long sub(long a, long b)
                returnValue( //return a + b;
                    getVar(1).sub(getVar(3))
                )
            ))
            .withMethod(method(publicOnly(), name("sub"), parameters(float.class, float.class), type(float.class), //public float sub(float a, float b)
                returnValue( //return a + b;
                    getVar(1).sub(getVar(2))
                )
            ))
            .withMethod(method(publicOnly(), name("sub"), parameters(double.class, double.class), type(double.class), //public double sub(double a, double b)
                returnValue( //return a + b;
                    getVar(1).sub(getVar(3))
                )
            ))
            .withMethod(method(publicOnly(), name("mul"), parameters(byte.class, byte.class), type(byte.class), //public byte mul(byte a, byte b)
                returnValue( //return a + b;
                    getVar(1).mul(getVar(2))
                )
            ))
            .withMethod(method(publicOnly(), name("mul"), parameters(short.class, short.class), type(short.class), //public short mul(short a, short b)
                returnValue( //return a + b;
                    getVar(1).mul(getVar(2))
                )
            ))
            .withMethod(method(publicOnly(), name("mul"), parameters(int.class, int.class), type(int.class), //public int mul(int a, int b)
                returnValue( //return a + b;
                    getVar(1).mul(getVar(2))
                )
            ))
            .withMethod(method(publicOnly(), name("mul"), parameters(long.class, long.class), type(long.class), //public long mul(long a, long b)
                returnValue( //return a + b;
                    getVar(1).mul(getVar(3))
                )
            ))
            .withMethod(method(publicOnly(), name("mul"), parameters(float.class, float.class), type(float.class), //public float mul(float a, float b)
                returnValue( //return a + b;
                    getVar(1).mul(getVar(2))
                )
            ))
            .withMethod(method(publicOnly(), name("mul"), parameters(double.class, double.class), type(double.class), //public double mul(double a, double b)
                returnValue( //return a + b;
                    getVar(1).mul(getVar(3))
                )
            ))
            .withMethod(method(publicOnly(), name("div"), parameters(byte.class, byte.class), type(byte.class), //public byte div(byte a, byte b)
                returnValue( //return a + b;
                    getVar(1).div(getVar(2))
                )
            ))
            .withMethod(method(publicOnly(), name("div"), parameters(short.class, short.class), type(short.class), //public short div(short a, short b)
                returnValue( //return a + b;
                    getVar(1).div(getVar(2))
                )
            ))
            .withMethod(method(publicOnly(), name("div"), parameters(int.class, int.class), type(int.class), //public int div(int a, int b)
                returnValue( //return a + b;
                    getVar(1).div(getVar(2))
                )
            ))
            .withMethod(method(publicOnly(), name("div"), parameters(long.class, long.class), type(long.class), //public long div(long a, long b)
                returnValue( //return a + b;
                    getVar(1).div(getVar(3))
                )
            ))
            .withMethod(method(publicOnly(), name("div"), parameters(float.class, float.class), type(float.class), //public float div(float a, float b)
                returnValue( //return a + b;
                    getVar(1).div(getVar(2))
                )
            ))
            .withMethod(method(publicOnly(), name("div"), parameters(double.class, double.class), type(double.class), //public double div(double a, double b)
                returnValue( //return a + b;
                    getVar(1).div(getVar(3))
                )
            ))
            .withMethod(method(publicOnly(), name("mod"), parameters(byte.class, byte.class), type(byte.class), //public byte mod(byte a, byte b)
                returnValue( //return a + b;
                    getVar(1).mod(getVar(2))
                )
            ))
            .withMethod(method(publicOnly(), name("mod"), parameters(short.class, short.class), type(short.class), //public short mod(short a, short b)
                returnValue( //return a + b;
                    getVar(1).mod(getVar(2))
                )
            ))
            .withMethod(method(publicOnly(), name("mod"), parameters(int.class, int.class), type(int.class), //public int mod(int a, int b)
                returnValue( //return a + b;
                    getVar(1).mod(getVar(2))
                )
            ))
            .withMethod(method(publicOnly(), name("mod"), parameters(long.class, long.class), type(long.class), //public long mod(long a, long b)
                returnValue( //return a + b;
                    getVar(1).mod(getVar(3))
                )
            ))
            .withMethod(method(publicOnly(), name("mod"), parameters(float.class, float.class), type(float.class), //public float mod(float a, float b)
                returnValue( //return a + b;
                    getVar(1).mod(getVar(2))
                )
            ))
            .withMethod(method(publicOnly(), name("mod"), parameters(double.class, double.class), type(double.class), //public double mod(double a, double b)
                returnValue( //return a + b;
                    getVar(1).mod(getVar(3))
                )
            ));
        TestMaths testMathsImpl = mathsBuilder.buildInstance();

        assertThat(testMathsImpl.add((byte)2, (byte)3), is((byte)5));
        assertThat(testMathsImpl.add((short)10, (short)9), is((short)19));
        assertThat(testMathsImpl.add(7, 3), is(10));
        assertThat(testMathsImpl.add(100L, 200L), is(300L));
        assertThat(testMathsImpl.add(15.4f, 21.1f), is(36.5f));
        assertThat(testMathsImpl.add(297.01, 49.94), is(346.95));

        assertThat(testMathsImpl.sub((byte)2, (byte)3), is((byte)-1));
        assertThat(testMathsImpl.sub((short)10, (short)9), is((short)1));
        assertThat(testMathsImpl.sub(7, 3), is(4));
        assertThat(testMathsImpl.sub(100L, 200L), is(-100L));
        assertThat(testMathsImpl.sub(15.0f, 5.0f), is(10.0f));
        assertThat(testMathsImpl.sub(297.01, 49.94), is(247.07));

        assertThat(testMathsImpl.mul((byte)2, (byte)3), is((byte)6));
        assertThat(testMathsImpl.mul((short)10, (short)9), is((short)90));
        assertThat(testMathsImpl.mul(7, 3), is(21));
        assertThat(testMathsImpl.mul(100L, 200L), is(20000L));
        assertThat(testMathsImpl.mul(4.0f, 5.0f), is(20.0f));
        assertThat(testMathsImpl.mul(15.0, 5.0), is(75.0));

        assertThat(testMathsImpl.div((byte)10, (byte)2), is((byte)5));
        assertThat(testMathsImpl.div((short)27, (short)9), is((short)3));
        assertThat(testMathsImpl.div(12, 3), is(4));
        assertThat(testMathsImpl.div(200L, 50L), is(4L));
        assertThat(testMathsImpl.div(4.0f, 5.0f), is(0.80f));
        assertThat(testMathsImpl.div(15.0, 5.0), is(3.0));

        assertThat(testMathsImpl.mod((byte)7, (byte)2), is((byte)1));
        assertThat(testMathsImpl.mod((short)27, (short)9), is((short)0));
        assertThat(testMathsImpl.mod(1, 3), is(1));
        assertThat(testMathsImpl.mod(20L, 50L), is(20L));
        assertThat(testMathsImpl.mod(4.0f, 5.0f), is(4.0f));
        assertThat(testMathsImpl.mod(15.0, 5.0), is(0.0));
    }

    //This class will be implemented via the asm code generator
    public static abstract class TestInterestingMaths {
        public abstract int wrapperAdd(Integer a, Integer b);
        public abstract int wrapperAdd(int a, Integer b);
        public abstract Float wrapperAdd(float a, float b);
        public abstract Double wrapperAdd(Double a, Double b);
    }

    @Test
    public void testMathOperationsWithWrapperClassesAndAutomaticImplicitBoxingAndUnboxing() {
        AsmClassBuilder<TestInterestingMaths> mathsBuilder = new AsmClassBuilder<>(TestInterestingMaths.class)
            .withConstructor(constructor(publicOnly(), noParameters(), //public TestInterestingMathsImpl()
                superConstructor(TestInterestingMaths.class, noParameters()), //super();
                returnVoid() //return;
            ))
            .withMethod(method(publicOnly(), name("wrapperAdd"), parameters(Integer.class, Integer.class), type(int.class),
                returnValue(
                    getVar(1).add(getVar(2))
                )
            ))
            .withMethod(method(publicOnly(), name("wrapperAdd"), parameters(int.class, Integer.class), type(int.class),
                returnValue(
                    getVar(1).add(getVar(2))
                )
            ))
            .withMethod(method(publicOnly(), name("wrapperAdd"), parameters(float.class, float.class), type(Float.class),
                returnValue(
                    getVar(1).add(getVar(2))
                )
            ))
            .withMethod(method(publicOnly(), name("wrapperAdd"), parameters(Double.class, Double.class), type(Double.class),
                returnValue(
                    getVar(1).add(getVar(2))
                )
            ));
        TestInterestingMaths mathsImpl = mathsBuilder.buildInstance();

        assertThat(mathsImpl.wrapperAdd((Integer)10, (Integer)15), is(25));
        assertThat(mathsImpl.wrapperAdd(5, (Integer)392), is(397));
        assertThat(mathsImpl.wrapperAdd(5.0f, 12.1f), is(17.1f));
        assertThat(mathsImpl.wrapperAdd(123.4, 15.2), is(138.6));
    }

    @Test
    public void createMathOperationInsn_illegalArgumentException() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> new AdditionMathOperationInsn(null));
        assertThat(ex.getMessage(), is("Operand code builder cannot be null."));
    }

    public interface MathsInterface {
        float add(float a, float b);
    }

    @Test
    public void illegalStateException_attemptingToBuildBytecodePerformingMathOperationOnObject() {
        AsmClassBuilder<MathsInterface> builder = new AsmClassBuilder<>(MathsInterface.class, Object.class, singletonList(MathsInterface.class), publicOnly())
            .withConstructor(constructor(publicOnly(), noParameters(),
                superConstructor(Object.class, noParameters()),
                returnVoid()
            ))
            .withMethod(method(publicOnly(), name("add"), parameters(float.class, float.class), type(float.class),
                returnValue(
                    literalObj("SomeString").add(literal(1234))
                )
            ));

        IllegalStateException ex = assertThrows(IllegalStateException.class, builder::build);
        assertThat(ex,
            hasProperty("message",
                is("Expected a math operand to exist on the stack as a primitive or wrapper type. Found type %s instead."
                    .formatted(String.class.getName())
                )
            )
        );
    }

    @Test
    public void illegalStateException_attemptingToBuildBytecodePerformingMathOnEmptyStack() {
        AsmClassBuilder<MathsInterface> builder = new AsmClassBuilder<>(MathsInterface.class, Object.class, singletonList(MathsInterface.class), publicOnly())
            .withConstructor(constructor(publicOnly(), noParameters(),
                superConstructor(Object.class, noParameters()),
                returnVoid()
            ))
            .withMethod(method(privateStatic(), name("consumeFloat"), parameters(float.class), returnVoid()))
            .withMethod(method(publicOnly(), name("add"), parameters(float.class, float.class), type(float.class),
                returnValue(
                    //return MathsInterfaceImpl.consumeFloat(123f) + 123f; //consumeFloat is a void method
                    invokeStatic(ThisClass.class, name("consumeFloat"), parameters(float.class), voidType(),
                        literal(123f)
                    ).add(literal(123f))
                )
            ));

        //The code generation above calls a void method and attempts to add a number to 'void'.
        //This results in a situation where the type stack is empty when trying to add
        //This should result in an IllegalStateException from the class builder system
        IllegalStateException ex = assertThrows(IllegalStateException.class, builder::build);
        assertThat(ex, hasProperty("message", is("Expected to find a math operand on the stack, but the stack was empty.")));
    }

    @Test
    public void illegalStateException_moreThanOneOperandPlacedOntoStack_whenOneOperandWasExpected() {
        CodeInsnBuilderLike mockOperandBuilder = mock(CodeInsnBuilderLike.class);
        MethodBuildingContext methodContext = new MethodBuildingContext(null, null, null, emptyList());
        methodContext.pushStack(type(float.class));

        doAnswer(i -> {
            methodContext.pushStack(type(int.class));
            methodContext.pushStack(type(int.class));
            return null;
        }).when(mockOperandBuilder).build();
        when(mockOperandBuilder.getFirstInStack()).thenReturn(mockOperandBuilder);

        AdditionMathOperationInsn op = new AdditionMathOperationInsn(mockOperandBuilder);
        IllegalStateException ex = assertThrows(IllegalStateException.class, op::build);
        assertThat(ex, hasProperty("message", is("Expected 1 element to be pushed to the stack. Instead 2 elements were pushed/removed.")));
    }
}