package io.github.cshunsinger.asmsauce.code.method;

import aj.org.objectweb.asm.MethodVisitor;
import io.github.cshunsinger.asmsauce.AsmClassBuilder;
import io.github.cshunsinger.asmsauce.MethodBuildingContext;
import io.github.cshunsinger.asmsauce.ThisClass;
import io.github.cshunsinger.asmsauce.code.CodeInsnBuilderLike;
import io.github.cshunsinger.asmsauce.definitions.TypeDefinition;
import io.github.cshunsinger.asmsauce.testing.BaseUnitTest;
import io.github.cshunsinger.asmsauce.DefinitionBuilders;
import io.github.cshunsinger.asmsauce.code.CodeBuilders;
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

import static io.github.cshunsinger.asmsauce.ConstructorNode.constructor;
import static io.github.cshunsinger.asmsauce.MethodNode.method;
import static io.github.cshunsinger.asmsauce.code.CodeBuilders.*;
import static io.github.cshunsinger.asmsauce.modifiers.AccessModifiers.*;
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
            Arguments.of(DefinitionBuilders.voidType(), mockMethod, "Method owner type cannot be void."),
            Arguments.of(DefinitionBuilders.type(int.class), mockMethod, "Method owner type cannot be a primitive type."),
            Arguments.of(DefinitionBuilders.type(ThisClass.class), null, "Method cannot be null."),
            Arguments.of(DefinitionBuilders.type(ThisClass.class), mockMethod, "Expected 1 builders to satisfy the method parameters. Found 0 builders instead.")
        );
    }

    @Test
    @DisplayName("Throw IllegalStateException when there is no value on the stack to act as the 'this' value.")
    public void illegalStateException_noElementOnStackForMethodOwnerInstance() {
        MethodBuildingContext methodContext = new MethodBuildingContext(mockMethodVisitor, null, null, emptyList());

        InvokeInstanceMethodInsn insn = new InvokeInstanceMethodInsn(DefinitionBuilders.type(ThisClass.class), mockMethod, mockParameterBuilder);
        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> insn.build(methodContext));

        assertThat(ex, hasProperty("message", is("There is no element expected on the stack to be cast.")));
    }

    @Test
    public void illegalStateException_wrongNumberOfValuesStackedByParamBuilder() {
        MethodBuildingContext methodContext = new MethodBuildingContext(mockMethodVisitor, null, null, emptyList());
        methodContext.pushStack(DefinitionBuilders.type(ThisClass.class));

        when(mockParameterBuilder.getFirstInStack()).thenReturn(mockParameterBuilder);

        InvokeInstanceMethodInsn insn = new InvokeInstanceMethodInsn(DefinitionBuilders.type(ThisClass.class), mockMethod, mockParameterBuilder);
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
            .withConstructor(constructor(publicOnly(), DefinitionBuilders.noParameters(),
                CodeBuilders.superConstructor(DefinitionBuilders.type(TestBaseClass.class), DefinitionBuilders.noParameters()),
                CodeBuilders.returnVoid()
            ))
            //private static double squared(double d) {...}
            .withMethod(method(privateStatic(), DefinitionBuilders.name("squared"), DefinitionBuilders.parameters(double.class), DefinitionBuilders.type(double.class),
                CodeBuilders.returnValue(CodeBuilders.getVar(0).mul(CodeBuilders.getVar(0))) //return d * d;
            ))
            //public double distance(double aX, double aY, double bX, double bY) {...}
            .withMethod(method(publicOnly(), DefinitionBuilders.name("distance"), DefinitionBuilders.parameters(double.class, double.class, double.class, double.class), DefinitionBuilders.type(double.class),
                //double bXMinusAX = bX - aX; (local var 9)
                CodeBuilders.setVar(CodeBuilders.getVar(5).sub(CodeBuilders.getVar(1))),
                //double bYMinusAY = bY - aY; (local var 11)
                CodeBuilders.setVar(CodeBuilders.getVar(7).sub(CodeBuilders.getVar(3))),
                //return Math.sqrt(TestBaseClassImpl.squared(bXMinusAX) + TestBaseClassImpl.squared(bYMinusAY))
                CodeBuilders.returnValue(invokeStatic(Math.class, DefinitionBuilders.name("sqrt"), DefinitionBuilders.parameters(double.class), DefinitionBuilders.type(double.class),
                    invokeStatic(ThisClass.class, DefinitionBuilders.name("squared"), DefinitionBuilders.parameters(double.class), DefinitionBuilders.type(double.class),
                        CodeBuilders.getVar(9)
                    ).add(invokeStatic(ThisClass.class, DefinitionBuilders.name("squared"), DefinitionBuilders.parameters(double.class), DefinitionBuilders.type(double.class),
                        CodeBuilders.getVar(11))
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
            .withConstructor(constructor(publicOnly(), DefinitionBuilders.noParameters(),
                CodeBuilders.superConstructor(TestInterfaceReceiver.class, DefinitionBuilders.noParameters()),
                CodeBuilders.returnVoid()
            ))
            .withMethod(method(publicOnly(), DefinitionBuilders.name("receive"), DefinitionBuilders.parameters(TestInterfaceType.class),
                CodeBuilders.this_().assignField("testString", //super.testString = value.getTestString();
                    CodeBuilders.getVar(1).invoke("getTestString")
                ),
                CodeBuilders.returnVoid()
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