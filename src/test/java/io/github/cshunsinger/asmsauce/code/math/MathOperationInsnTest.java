package io.github.cshunsinger.asmsauce.code.math;

import io.github.cshunsinger.asmsauce.AsmClassBuilder;
import io.github.cshunsinger.asmsauce.MethodBuildingContext;
import io.github.cshunsinger.asmsauce.ThisClass;
import io.github.cshunsinger.asmsauce.code.CodeBuilders;
import io.github.cshunsinger.asmsauce.code.CodeInsnBuilderLike;
import io.github.cshunsinger.asmsauce.BaseUnitTest;
import io.github.cshunsinger.asmsauce.DefinitionBuilders;
import org.junit.jupiter.api.Test;

import static io.github.cshunsinger.asmsauce.ConstructorNode.constructor;
import static io.github.cshunsinger.asmsauce.MethodNode.method;
import static io.github.cshunsinger.asmsauce.modifiers.AccessModifiers.*;
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
            .withConstructor(constructor(publicOnly(), DefinitionBuilders.noParameters(), //public TestMathsImpl()
                CodeBuilders.superConstructor(TestMaths.class, DefinitionBuilders.noParameters()), //super();
                CodeBuilders.returnVoid() //return;
            ))
            .withMethod(method(publicOnly(), DefinitionBuilders.name("add"), DefinitionBuilders.parameters(byte.class, byte.class), DefinitionBuilders.type(byte.class), //public byte add(byte a, byte b)
                CodeBuilders.returnValue( //return a + b;
                    CodeBuilders.getVar(1).add(CodeBuilders.getVar(2))
                )
            ))
            .withMethod(method(publicOnly(), DefinitionBuilders.name("add"), DefinitionBuilders.parameters(short.class, short.class), DefinitionBuilders.type(short.class), //public short add(short a, short b)
                CodeBuilders.returnValue( //return a + b;
                    CodeBuilders.getVar(1).add(CodeBuilders.getVar(2))
                )
            ))
            .withMethod(method(publicOnly(), DefinitionBuilders.name("add"), DefinitionBuilders.parameters(int.class, int.class), DefinitionBuilders.type(int.class), //public int add(int a, int b)
                CodeBuilders.returnValue( //return a + b;
                    CodeBuilders.getVar(1).add(CodeBuilders.getVar(2))
                )
            ))
            .withMethod(method(publicOnly(), DefinitionBuilders.name("add"), DefinitionBuilders.parameters(long.class, long.class), DefinitionBuilders.type(long.class), //public long add(long a, long b)
                CodeBuilders.returnValue( //return a + b;
                    CodeBuilders.getVar(1).add(CodeBuilders.getVar(3))
                )
            ))
            .withMethod(method(publicOnly(), DefinitionBuilders.name("add"), DefinitionBuilders.parameters(float.class, float.class), DefinitionBuilders.type(float.class), //public float add(float a, float b)
                CodeBuilders.returnValue( //return a + b;
                    CodeBuilders.getVar(1).add(CodeBuilders.getVar(2))
                )
            ))
            .withMethod(method(publicOnly(), DefinitionBuilders.name("add"), DefinitionBuilders.parameters(double.class, double.class), DefinitionBuilders.type(double.class), //public double add(double a, double b)
                CodeBuilders.returnValue( //return a + b;
                    CodeBuilders.getVar(1).add(CodeBuilders.getVar(3))
                )
            ))
            .withMethod(method(publicOnly(), DefinitionBuilders.name("sub"), DefinitionBuilders.parameters(byte.class, byte.class), DefinitionBuilders.type(byte.class), //public byte sub(byte a, byte b)
                CodeBuilders.returnValue( //return a + b;
                    CodeBuilders.getVar(1).sub(CodeBuilders.getVar(2))
                )
            ))
            .withMethod(method(publicOnly(), DefinitionBuilders.name("sub"), DefinitionBuilders.parameters(short.class, short.class), DefinitionBuilders.type(short.class), //public short sub(short a, short b)
                CodeBuilders.returnValue( //return a + b;
                    CodeBuilders.getVar(1).sub(CodeBuilders.getVar(2))
                )
            ))
            .withMethod(method(publicOnly(), DefinitionBuilders.name("sub"), DefinitionBuilders.parameters(int.class, int.class), DefinitionBuilders.type(int.class), //public int sub(int a, int b)
                CodeBuilders.returnValue( //return a + b;
                    CodeBuilders.getVar(1).sub(CodeBuilders.getVar(2))
                )
            ))
            .withMethod(method(publicOnly(), DefinitionBuilders.name("sub"), DefinitionBuilders.parameters(long.class, long.class), DefinitionBuilders.type(long.class), //public long sub(long a, long b)
                CodeBuilders.returnValue( //return a + b;
                    CodeBuilders.getVar(1).sub(CodeBuilders.getVar(3))
                )
            ))
            .withMethod(method(publicOnly(), DefinitionBuilders.name("sub"), DefinitionBuilders.parameters(float.class, float.class), DefinitionBuilders.type(float.class), //public float sub(float a, float b)
                CodeBuilders.returnValue( //return a + b;
                    CodeBuilders.getVar(1).sub(CodeBuilders.getVar(2))
                )
            ))
            .withMethod(method(publicOnly(), DefinitionBuilders.name("sub"), DefinitionBuilders.parameters(double.class, double.class), DefinitionBuilders.type(double.class), //public double sub(double a, double b)
                CodeBuilders.returnValue( //return a + b;
                    CodeBuilders.getVar(1).sub(CodeBuilders.getVar(3))
                )
            ))
            .withMethod(method(publicOnly(), DefinitionBuilders.name("mul"), DefinitionBuilders.parameters(byte.class, byte.class), DefinitionBuilders.type(byte.class), //public byte mul(byte a, byte b)
                CodeBuilders.returnValue( //return a + b;
                    CodeBuilders.getVar(1).mul(CodeBuilders.getVar(2))
                )
            ))
            .withMethod(method(publicOnly(), DefinitionBuilders.name("mul"), DefinitionBuilders.parameters(short.class, short.class), DefinitionBuilders.type(short.class), //public short mul(short a, short b)
                CodeBuilders.returnValue( //return a + b;
                    CodeBuilders.getVar(1).mul(CodeBuilders.getVar(2))
                )
            ))
            .withMethod(method(publicOnly(), DefinitionBuilders.name("mul"), DefinitionBuilders.parameters(int.class, int.class), DefinitionBuilders.type(int.class), //public int mul(int a, int b)
                CodeBuilders.returnValue( //return a + b;
                    CodeBuilders.getVar(1).mul(CodeBuilders.getVar(2))
                )
            ))
            .withMethod(method(publicOnly(), DefinitionBuilders.name("mul"), DefinitionBuilders.parameters(long.class, long.class), DefinitionBuilders.type(long.class), //public long mul(long a, long b)
                CodeBuilders.returnValue( //return a + b;
                    CodeBuilders.getVar(1).mul(CodeBuilders.getVar(3))
                )
            ))
            .withMethod(method(publicOnly(), DefinitionBuilders.name("mul"), DefinitionBuilders.parameters(float.class, float.class), DefinitionBuilders.type(float.class), //public float mul(float a, float b)
                CodeBuilders.returnValue( //return a + b;
                    CodeBuilders.getVar(1).mul(CodeBuilders.getVar(2))
                )
            ))
            .withMethod(method(publicOnly(), DefinitionBuilders.name("mul"), DefinitionBuilders.parameters(double.class, double.class), DefinitionBuilders.type(double.class), //public double mul(double a, double b)
                CodeBuilders.returnValue( //return a + b;
                    CodeBuilders.getVar(1).mul(CodeBuilders.getVar(3))
                )
            ))
            .withMethod(method(publicOnly(), DefinitionBuilders.name("div"), DefinitionBuilders.parameters(byte.class, byte.class), DefinitionBuilders.type(byte.class), //public byte div(byte a, byte b)
                CodeBuilders.returnValue( //return a + b;
                    CodeBuilders.getVar(1).div(CodeBuilders.getVar(2))
                )
            ))
            .withMethod(method(publicOnly(), DefinitionBuilders.name("div"), DefinitionBuilders.parameters(short.class, short.class), DefinitionBuilders.type(short.class), //public short div(short a, short b)
                CodeBuilders.returnValue( //return a + b;
                    CodeBuilders.getVar(1).div(CodeBuilders.getVar(2))
                )
            ))
            .withMethod(method(publicOnly(), DefinitionBuilders.name("div"), DefinitionBuilders.parameters(int.class, int.class), DefinitionBuilders.type(int.class), //public int div(int a, int b)
                CodeBuilders.returnValue( //return a + b;
                    CodeBuilders.getVar(1).div(CodeBuilders.getVar(2))
                )
            ))
            .withMethod(method(publicOnly(), DefinitionBuilders.name("div"), DefinitionBuilders.parameters(long.class, long.class), DefinitionBuilders.type(long.class), //public long div(long a, long b)
                CodeBuilders.returnValue( //return a + b;
                    CodeBuilders.getVar(1).div(CodeBuilders.getVar(3))
                )
            ))
            .withMethod(method(publicOnly(), DefinitionBuilders.name("div"), DefinitionBuilders.parameters(float.class, float.class), DefinitionBuilders.type(float.class), //public float div(float a, float b)
                CodeBuilders.returnValue( //return a + b;
                    CodeBuilders.getVar(1).div(CodeBuilders.getVar(2))
                )
            ))
            .withMethod(method(publicOnly(), DefinitionBuilders.name("div"), DefinitionBuilders.parameters(double.class, double.class), DefinitionBuilders.type(double.class), //public double div(double a, double b)
                CodeBuilders.returnValue( //return a + b;
                    CodeBuilders.getVar(1).div(CodeBuilders.getVar(3))
                )
            ))
            .withMethod(method(publicOnly(), DefinitionBuilders.name("mod"), DefinitionBuilders.parameters(byte.class, byte.class), DefinitionBuilders.type(byte.class), //public byte mod(byte a, byte b)
                CodeBuilders.returnValue( //return a + b;
                    CodeBuilders.getVar(1).mod(CodeBuilders.getVar(2))
                )
            ))
            .withMethod(method(publicOnly(), DefinitionBuilders.name("mod"), DefinitionBuilders.parameters(short.class, short.class), DefinitionBuilders.type(short.class), //public short mod(short a, short b)
                CodeBuilders.returnValue( //return a + b;
                    CodeBuilders.getVar(1).mod(CodeBuilders.getVar(2))
                )
            ))
            .withMethod(method(publicOnly(), DefinitionBuilders.name("mod"), DefinitionBuilders.parameters(int.class, int.class), DefinitionBuilders.type(int.class), //public int mod(int a, int b)
                CodeBuilders.returnValue( //return a + b;
                    CodeBuilders.getVar(1).mod(CodeBuilders.getVar(2))
                )
            ))
            .withMethod(method(publicOnly(), DefinitionBuilders.name("mod"), DefinitionBuilders.parameters(long.class, long.class), DefinitionBuilders.type(long.class), //public long mod(long a, long b)
                CodeBuilders.returnValue( //return a + b;
                    CodeBuilders.getVar(1).mod(CodeBuilders.getVar(3))
                )
            ))
            .withMethod(method(publicOnly(), DefinitionBuilders.name("mod"), DefinitionBuilders.parameters(float.class, float.class), DefinitionBuilders.type(float.class), //public float mod(float a, float b)
                CodeBuilders.returnValue( //return a + b;
                    CodeBuilders.getVar(1).mod(CodeBuilders.getVar(2))
                )
            ))
            .withMethod(method(publicOnly(), DefinitionBuilders.name("mod"), DefinitionBuilders.parameters(double.class, double.class), DefinitionBuilders.type(double.class), //public double mod(double a, double b)
                CodeBuilders.returnValue( //return a + b;
                    CodeBuilders.getVar(1).mod(CodeBuilders.getVar(3))
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
            .withConstructor(constructor(publicOnly(), DefinitionBuilders.noParameters(), //public TestInterestingMathsImpl()
                CodeBuilders.superConstructor(TestInterestingMaths.class, DefinitionBuilders.noParameters()), //super();
                CodeBuilders.returnVoid() //return;
            ))
            .withMethod(method(publicOnly(), DefinitionBuilders.name("wrapperAdd"), DefinitionBuilders.parameters(Integer.class, Integer.class), DefinitionBuilders.type(int.class),
                CodeBuilders.returnValue(
                    CodeBuilders.getVar(1).add(CodeBuilders.getVar(2))
                )
            ))
            .withMethod(method(publicOnly(), DefinitionBuilders.name("wrapperAdd"), DefinitionBuilders.parameters(int.class, Integer.class), DefinitionBuilders.type(int.class),
                CodeBuilders.returnValue(
                    CodeBuilders.getVar(1).add(CodeBuilders.getVar(2))
                )
            ))
            .withMethod(method(publicOnly(), DefinitionBuilders.name("wrapperAdd"), DefinitionBuilders.parameters(float.class, float.class), DefinitionBuilders.type(Float.class),
                CodeBuilders.returnValue(
                    CodeBuilders.getVar(1).add(CodeBuilders.getVar(2))
                )
            ))
            .withMethod(method(publicOnly(), DefinitionBuilders.name("wrapperAdd"), DefinitionBuilders.parameters(Double.class, Double.class), DefinitionBuilders.type(Double.class),
                CodeBuilders.returnValue(
                    CodeBuilders.getVar(1).add(CodeBuilders.getVar(2))
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
            .withConstructor(constructor(publicOnly(), DefinitionBuilders.noParameters(),
                CodeBuilders.superConstructor(Object.class, DefinitionBuilders.noParameters()),
                CodeBuilders.returnVoid()
            ))
            .withMethod(method(publicOnly(), DefinitionBuilders.name("add"), DefinitionBuilders.parameters(float.class, float.class), DefinitionBuilders.type(float.class),
                CodeBuilders.returnValue(
                    CodeBuilders.literalObj("SomeString").add(CodeBuilders.literal(1234))
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
            .withConstructor(constructor(publicOnly(), DefinitionBuilders.noParameters(),
                CodeBuilders.superConstructor(Object.class, DefinitionBuilders.noParameters()),
                CodeBuilders.returnVoid()
            ))
            .withMethod(method(privateStatic(), DefinitionBuilders.name("consumeFloat"), DefinitionBuilders.parameters(float.class), CodeBuilders.returnVoid()))
            .withMethod(method(publicOnly(), DefinitionBuilders.name("add"), DefinitionBuilders.parameters(float.class, float.class), DefinitionBuilders.type(float.class),
                CodeBuilders.returnValue(
                    //return MathsInterfaceImpl.consumeFloat(123f) + 123f; //consumeFloat is a void method
                    CodeBuilders.invokeStatic(ThisClass.class, DefinitionBuilders.name("consumeFloat"), DefinitionBuilders.parameters(float.class), DefinitionBuilders.voidType(),
                        CodeBuilders.literal(123f)
                    ).add(CodeBuilders.literal(123f))
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
        methodContext.pushStack(DefinitionBuilders.type(float.class));

        doAnswer(i -> {
            methodContext.pushStack(DefinitionBuilders.type(int.class));
            methodContext.pushStack(DefinitionBuilders.type(int.class));
            return null;
        }).when(mockOperandBuilder).build();
        when(mockOperandBuilder.getFirstInStack()).thenReturn(mockOperandBuilder);

        AdditionMathOperationInsn op = new AdditionMathOperationInsn(mockOperandBuilder);
        IllegalStateException ex = assertThrows(IllegalStateException.class, op::build);
        assertThat(ex, hasProperty("message", is("Expected 1 element to be pushed to the stack. Instead 2 elements were pushed/removed.")));
    }
}