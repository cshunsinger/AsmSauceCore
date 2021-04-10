package com.chunsinger.asmsauce.code.method;

import com.chunsinger.asmsauce.AsmClassBuilder;
import com.chunsinger.asmsauce.definitions.NameDefinition;
import com.chunsinger.asmsauce.definitions.ParametersDefinition;
import com.chunsinger.asmsauce.definitions.ThrowsDefinition;
import com.chunsinger.asmsauce.definitions.TypeDefinition;
import com.chunsinger.asmsauce.testing.BaseUnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static com.chunsinger.asmsauce.ConstructorNode.constructor;
import static com.chunsinger.asmsauce.DefinitionBuilders.*;
import static com.chunsinger.asmsauce.MethodNode.method;
import static com.chunsinger.asmsauce.code.CodeBuilders.*;
import static com.chunsinger.asmsauce.modifiers.AccessModifiers.publicOnly;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

class InvokeStaticMethodInsnTest extends BaseUnitTest {
    @Test
    public void illegalArgumentException_nullMethod() {
        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class,
            () -> new InvokeStaticMethodInsn(type(Object.class), null)
        );

        assertThat(ex, hasProperty("message", is("Name cannot be null.")));
    }

    @ParameterizedTest
    @MethodSource("illegalArgumentException_constructingInsn_testArguments")
    public void illegalArgumentException_constructingInsn(TypeDefinition<?> testType,
                                                          NameDefinition testMethodName,
                                                          ParametersDefinition testMethodParameters,
                                                          TypeDefinition<?> testReturnType,
                                                          ThrowsDefinition testThrowsDefinition,
                                                          String expectedExceptionMessage) {
        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class,
            () -> new InvokeStaticMethodInsn(testType, testMethodName, testMethodParameters, testReturnType, testThrowsDefinition)
        );

        assertThat(ex, hasProperty("message", is(expectedExceptionMessage)));
    }

    private static Stream<Arguments> illegalArgumentException_constructingInsn_testArguments() {
        return Stream.of(
            Arguments.of(null, name("testMethod"), noParameters(), voidType(), noThrows(), "Method owner type is mandatory for static methods."),
            Arguments.of(voidType(), name("testMethod"), noParameters(), voidType(), noThrows(), "Method owner type cannot be void."),
            Arguments.of(type(Object.class), null, noParameters(), voidType(), noThrows(), "Name cannot be null."),
            Arguments.of(type(Object.class), name("testMethod"), null, voidType(), noThrows(), "Parameters cannot be null."),
            Arguments.of(type(Object.class), name("testMethod"), noParameters(), null, noThrows(), "Return type cannot be null."),
            Arguments.of(type(Object.class), name("testMethod"), noParameters(), voidType(), null, "Throwing cannot be null."),
            Arguments.of(type(Object.class), name("testMethod"), parameters(String.class), voidType(), noThrows(), "Expected 1 builders to satisfy the method parameters. Found 0 builders instead.")
        );
    }

    @Test
    public void illegalArgumentException_nullMethodOwnerForStaticMethod() {
        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class,
            () -> new InvokeStaticMethodInsn(null, name("testMethodName"))
        );

        assertThat(ex, hasProperty("message", is("Method owner type is mandatory for static methods.")));
    }

    public static class TestStatics {
        @SuppressWarnings("unused")
        private static void privateStaticMethod() {}
        @SuppressWarnings("unused")
        static void packageStaticMethod() {}
        @SuppressWarnings("unused")
        protected static void protectedStaticMethod() {}

        @SuppressWarnings("unused")
        public void nonStaticTestMethod() {}
    }

    public static abstract class TestBaseType extends TestStatics {
        public abstract int abs(int i);
    }

    @Test
    public void illegalStateException_implicitMethodInfo_staticMethodNotAccessible() {
        AsmClassBuilder<TestBaseType> builder = new AsmClassBuilder<>(TestBaseType.class)
            .withConstructor(constructor(publicOnly(), noParameters(),
                superConstructor(TestBaseType.class, noParameters()),
                invokeStatic(TestStatics.class, "privateStaticMethod"),
                returnVoid()
            ));

        IllegalStateException ex = assertThrows(IllegalStateException.class, builder::build);
        assertThat(ex, hasProperty("message",
            is("Method 'privateStaticMethod' not found in class '%s' with parameters:\n\t".formatted(TestStatics.class.getName()))
        ));
    }

    @Test
    public void illegalStateException_explicitMethodInfo_staticMethodNotAccessible() {
        AsmClassBuilder<TestBaseType> builder = new AsmClassBuilder<>(TestBaseType.class)
            .withConstructor(constructor(publicOnly(), noParameters(),
                superConstructor(TestBaseType.class, noParameters()),
                invokeStatic(TestStatics.class, name("privateStaticMethod"), noParameters(), voidType()),
                returnVoid()
            ));

        IllegalStateException ex = assertThrows(IllegalStateException.class, builder::build);
        assertThat(ex, hasProperty("message",
            is("Method 'privateStaticMethod' not found in class '%s' with parameters:\n\t".formatted(TestStatics.class.getName()))
        ));
    }

    @Test
    public void illegalStateException_implicitMethodInfo_staticMethodDoesNotExist() {
        AsmClassBuilder<TestBaseType> builder = new AsmClassBuilder<>(TestBaseType.class)
            .withConstructor(constructor(publicOnly(), noParameters(),
                superConstructor(TestBaseType.class, noParameters()),
                invokeStatic(TestStatics.class, "someMethodWhichNeverExists"),
                returnVoid()
            ));

        IllegalStateException ex = assertThrows(IllegalStateException.class, builder::build);
        assertThat(ex, hasProperty("message",
            is("Method 'someMethodWhichNeverExists' not found in class '%s' with parameters:\n\t".formatted(TestStatics.class.getName()))
        ));
    }

    @Test
    public void illegalStateException_explicitMethodInfo_staticMethodDoesNotExist() {
        AsmClassBuilder<TestBaseType> builder = new AsmClassBuilder<>(TestBaseType.class)
            .withConstructor(constructor(publicOnly(), noParameters(),
                superConstructor(TestBaseType.class, noParameters()),
                invokeStatic(TestStatics.class, name("someMethodWhichNeverExists"), noParameters(), voidType()),
                returnVoid()
            ));

        IllegalStateException ex = assertThrows(IllegalStateException.class, builder::build);
        assertThat(ex, hasProperty("message",
            is("Method 'someMethodWhichNeverExists' not found in class '%s' with parameters:\n\t".formatted(TestStatics.class.getName()))
        ));
    }

    @Test
    public void illegalStateException_implicitMethodInfo_methodIsNotStatic() {
        AsmClassBuilder<TestBaseType> builder = new AsmClassBuilder<>(TestBaseType.class)
            .withConstructor(constructor(publicOnly(), noParameters(),
                superConstructor(TestBaseType.class, noParameters()),
                invokeStatic(TestStatics.class, "nonStaticTestMethod"),
                returnVoid()
            ));

        IllegalStateException ex = assertThrows(IllegalStateException.class, builder::build);
        assertThat(ex, hasProperty("message",
            is("Method 'nonStaticTestMethod' in class '%s' is not static.".formatted(TestStatics.class.getName()))
        ));
    }

    @Test
    public void illegalStateException_explicitMethodInfo_methodIsNotStatic() {
        AsmClassBuilder<TestBaseType> builder = new AsmClassBuilder<>(TestBaseType.class)
            .withConstructor(constructor(publicOnly(), noParameters(),
                superConstructor(TestBaseType.class, noParameters()),
                invokeStatic(TestStatics.class, name("nonStaticTestMethod"), noParameters(), voidType()),
                returnVoid()
            ));

        IllegalStateException ex = assertThrows(IllegalStateException.class, builder::build);
        assertThat(ex, hasProperty("message",
            is("Method 'nonStaticTestMethod' in class '%s' is not static.".formatted(TestStatics.class.getName()))
        ));
    }

    @Test
    public void successfullyCallAnExplicitlyDefinedStaticMethod() {
        AsmClassBuilder<TestBaseType> builder = new AsmClassBuilder<>(TestBaseType.class)
            .withConstructor(constructor(publicOnly(), noParameters(),
                superConstructor(TestBaseType.class, noParameters()),
                returnVoid()
            ))
            .withMethod(method(publicOnly(), name("abs"), parameters(int.class), type(int.class),
                returnValue(
                    invokeStatic(type(Math.class), name("abs"), parameters(int.class), type(int.class),
                        localVar(1)
                    )
                )
            ));

        TestBaseType instance = builder.buildInstance();
        assertThat(instance.abs(-5000), is(5000));
        assertThat(instance.abs(5000), is(5000));
    }

    @Test
    public void successfullyCallAnImplicitlyDefinedStaticMethod() {
        AsmClassBuilder<TestBaseType> builder = new AsmClassBuilder<>(TestBaseType.class)
            .withConstructor(constructor(publicOnly(), noParameters(),
                superConstructor(TestBaseType.class, noParameters()),
                returnVoid()
            ))
            .withMethod(method(publicOnly(), name("abs"), parameters(int.class), type(int.class),
                returnValue(
                    invokeStatic(Math.class, "abs", localVar(1))
                )
            ));

        TestBaseType instance = builder.buildInstance();
        assertThat(instance.abs(-5000), is(5000));
        assertThat(instance.abs(5000), is(5000));
    }

    @Test
    public void illegalStateException_implicitMethodInfo_methodDoesNotExistInBuildingClass() {
        AsmClassBuilder<Object> builder = new AsmClassBuilder<>(Object.class)
            .withConstructor(constructor(publicOnly(), noParameters(),
                superConstructor(Object.class, noParameters()),
                thisInstance().invoke("someUnknownMethod"),
                returnVoid()
            ));

        IllegalStateException ex = assertThrows(IllegalStateException.class, builder::build);
        assertThat(ex, hasProperty("message",
            startsWith("Method 'someUnknownMethod' not found in class 'java.lang.Object")
        ));
    }

    @Test
    public void illegalStateException_parameterBuilderDoesNotAddExactlyOneElementToStack() {
        AsmClassBuilder<TestBaseType> builder = new AsmClassBuilder<>(TestBaseType.class)
            .withConstructor(constructor(publicOnly(), noParameters(),
                superConstructor(TestBaseType.class, noParameters()),
                returnVoid()
            ))
            .withMethod(method(publicOnly(), name("abs"), parameters(int.class), type(int.class),
                returnValue(
                    invokeStatic(Math.class, "abs",
                        invokeStatic(TestStatics.class, "protectedStaticMethod") //void method does not stack the expected 1 element
                    )
                )
            ));

        IllegalStateException ex = assertThrows(IllegalStateException.class, builder::build);
        assertThat(ex, hasProperty("message",
            is("Code builder expected to add 1 element to the stack. Instead 0 elements were added.")
        ));
    }
}