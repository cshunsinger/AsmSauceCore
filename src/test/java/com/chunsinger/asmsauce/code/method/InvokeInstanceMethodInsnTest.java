package com.chunsinger.asmsauce.code.method;

import aj.org.objectweb.asm.MethodVisitor;
import com.chunsinger.asmsauce.AsmClassBuilder;
import com.chunsinger.asmsauce.MethodBuildingContext;
import com.chunsinger.asmsauce.ThisClass;
import com.chunsinger.asmsauce.code.CodeInsnBuilderLike;
import com.chunsinger.asmsauce.definitions.TypeDefinition;
import com.chunsinger.asmsauce.testing.BaseUnitTest;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;

import java.lang.reflect.Method;
import java.util.stream.Stream;

import static com.chunsinger.asmsauce.ConstructorNode.constructor;
import static com.chunsinger.asmsauce.DefinitionBuilders.*;
import static com.chunsinger.asmsauce.MethodNode.method;
import static com.chunsinger.asmsauce.code.CodeBuilders.*;
import static com.chunsinger.asmsauce.modifiers.AccessModifiers.*;
import static java.util.Collections.emptyList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

class InvokeInstanceMethodInsnTest extends BaseUnitTest {
    @Mock
    private MethodVisitor mockMethodVisitor;
    @Mock
    private CodeInsnBuilderLike mockParameterBuilder;

    @SuppressWarnings("unused")
    public String testMethod(String param) { return param; }

    private Method mockMethod;

    @SneakyThrows
    @BeforeEach
    public void init() {
        mockMethod = InvokeInstanceMethodInsnTest.class.getMethod("testMethod", String.class);
    }

    @ParameterizedTest
    @MethodSource("illegalArgumentException_whenConstructing_testArguments")
    public void illegalArgumentException_whenConstructing(TypeDefinition<?> testType,
                                                          Method testMethod,
                                                          String expectedExceptionMessage) {
        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class,
            () -> new InvokeInstanceMethodInsn(testType, testMethod)
        );

        assertThat(ex, hasProperty("message", is(expectedExceptionMessage)));
    }

    @SneakyThrows
    private static Stream<Arguments> illegalArgumentException_whenConstructing_testArguments() {
        Method mockMethod = InvokeInstanceMethodInsnTest.class.getMethod("testMethod", String.class);

        return Stream.of(
            Arguments.of(null, mockMethod, "Method owner type cannot be null."),
            Arguments.of(voidType(), mockMethod, "Method owner type cannot be void."),
            Arguments.of(type(int.class), mockMethod, "Method owner type cannot be a primitive type."),
            Arguments.of(type(ThisClass.class), null, "Method cannot be null."),
            Arguments.of(type(ThisClass.class), mockMethod, "Expected 1 builders to satisfy the method parameters. Found 0 builders instead.")
        );
    }

    @Test
    @DisplayName("Throw IllegalStateException when there is no value on the stack to act as the 'this' value.")
    public void illegalStateException_noElementOnStackForMethodOwnerInstance() {
        MethodBuildingContext methodContext = new MethodBuildingContext(mockMethodVisitor, null, null, emptyList());

        InvokeInstanceMethodInsn insn = new InvokeInstanceMethodInsn(type(ThisClass.class), mockMethod, mockParameterBuilder);
        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> insn.build(methodContext));

        assertThat(ex, hasProperty("message", is("There is no element expected on the stack to be cast.")));
    }

    @Test
    public void illegalStateException_wrongNumberOfValuesStackedByParamBuilder() {
        MethodBuildingContext methodContext = new MethodBuildingContext(mockMethodVisitor, null, null, emptyList());
        methodContext.pushStack(type(ThisClass.class));

        when(mockParameterBuilder.getFirstInStack()).thenReturn(mockParameterBuilder);

        InvokeInstanceMethodInsn insn = new InvokeInstanceMethodInsn(type(ThisClass.class), mockMethod, mockParameterBuilder);
        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> insn.build(methodContext));

        assertThat(ex, hasProperty("message",
            is("Code builder expected to add 1 element to the stack. Instead 0 elements were added.")
        ));
    }

    public static abstract class TestBaseClass {
        public abstract double distance(double aX, double aY, double bX, double bY);
    }

    @Test
    public void createPrivateMethodInNewClassAndCallIt() {
        AsmClassBuilder<TestBaseClass> builder = new AsmClassBuilder<>(TestBaseClass.class)
            //public TestBaseClassImpl() {...}
            .withConstructor(constructor(publicOnly(), noParameters(),
                superConstructor(type(TestBaseClass.class), noParameters()),
                returnVoid()
            ))
            //private static double squared(double d) {...}
            .withMethod(method(privateStatic(), name("squared"), parameters(double.class), type(double.class),
                returnValue(localVar(0).mul(localVar(0))) //return d * d;
            ))
            //public double distance(double aX, double aY, double bX, double bY) {...}
            .withMethod(method(publicOnly(), name("distance"), parameters(double.class, double.class, double.class, double.class), type(double.class),
                //double bXMinusAX = bX - aX; (local var 9)
                storeLocal(localVar(5).sub(localVar(1))),
                //double bYMinusAY = bY - aY; (local var 11)
                storeLocal(localVar(7).sub(localVar(3))),
                //return Math.sqrt(TestBaseClassImpl.squared(bXMinusAX) + TestBaseClassImpl.squared(bYMinusAY))
                returnValue(invokeStatic(Math.class, name("sqrt"), parameters(double.class), type(double.class),
                    invokeStatic(ThisClass.class, name("squared"), parameters(double.class), type(double.class),
                        localVar(9)
                    ).add(invokeStatic(ThisClass.class, name("squared"), parameters(double.class), type(double.class),
                        localVar(11))
                    )
                ))
            ));

        TestBaseClass instance = builder.buildInstance();
        double result = instance.distance(10.0, 5.0, 20.0, 5.0);
        assertThat(result, is(10.0));
    }

    public interface TestInterfaceType {
        String getTestString();
        void setTestString(String testString);
    }

    @Getter @Setter
    @AllArgsConstructor
    public static class TestInterfaceModel implements TestInterfaceType {
        private String testString;
    }

    public static abstract class TestInterfaceReceiver {
        @Getter
        protected String testString;

        public abstract void receive(TestInterfaceType value);
    }

    @Test
    public void successfullyInvokeAnInstanceMethod_whichIsAlsoAnInterfaceMethod() {
        AsmClassBuilder<TestInterfaceReceiver> builder = new AsmClassBuilder<>(TestInterfaceReceiver.class)
            .withConstructor(constructor(publicOnly(), noParameters(),
                superConstructor(TestInterfaceReceiver.class, noParameters()),
                returnVoid()
            ))
            .withMethod(method(publicOnly(), name("receive"), parameters(TestInterfaceType.class),
                thisInstance().assignField("testString", //super.testString = value.getTestString();
                    localVar(1).invoke("getTestString")
                ),
                returnVoid()
            ));

        //Test data
        String testString = RandomStringUtils.randomAlphanumeric(25);
        TestInterfaceModel model = new TestInterfaceModel(testString);

        //Instance of the built class
        TestInterfaceReceiver instance = builder.buildInstance();
        instance.receive(model);

        assertThat(instance, hasProperty("testString", is(testString)));
    }
}