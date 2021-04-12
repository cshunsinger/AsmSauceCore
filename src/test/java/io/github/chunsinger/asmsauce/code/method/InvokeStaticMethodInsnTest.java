package io.github.chunsinger.asmsauce.code.method;

import io.github.chunsinger.asmsauce.AsmClassBuilder;
import io.github.chunsinger.asmsauce.definitions.NameDefinition;
import io.github.chunsinger.asmsauce.definitions.ParametersDefinition;
import io.github.chunsinger.asmsauce.definitions.ThrowsDefinition;
import io.github.chunsinger.asmsauce.definitions.TypeDefinition;
import io.github.chunsinger.asmsauce.testing.BaseUnitTest;
import io.github.chunsinger.asmsauce.DefinitionBuilders;
import io.github.chunsinger.asmsauce.code.CodeBuilders;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static io.github.chunsinger.asmsauce.ConstructorNode.constructor;
import static io.github.chunsinger.asmsauce.MethodNode.method;
import static io.github.chunsinger.asmsauce.modifiers.AccessModifiers.publicOnly;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

class InvokeStaticMethodInsnTest extends BaseUnitTest {
    @Test
    public void illegalArgumentException_nullMethod() {
        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class,
            () -> new InvokeStaticMethodInsn(DefinitionBuilders.type(Object.class), null)
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
            Arguments.of(null, DefinitionBuilders.name("testMethod"), DefinitionBuilders.noParameters(), DefinitionBuilders.voidType(), DefinitionBuilders.noThrows(), "Method owner type is mandatory for static methods."),
            Arguments.of(DefinitionBuilders.voidType(), DefinitionBuilders.name("testMethod"), DefinitionBuilders.noParameters(), DefinitionBuilders.voidType(), DefinitionBuilders.noThrows(), "Method owner type cannot be void."),
            Arguments.of(DefinitionBuilders.type(Object.class), null, DefinitionBuilders.noParameters(), DefinitionBuilders.voidType(), DefinitionBuilders.noThrows(), "Name cannot be null."),
            Arguments.of(DefinitionBuilders.type(Object.class), DefinitionBuilders.name("testMethod"), null, DefinitionBuilders.voidType(), DefinitionBuilders.noThrows(), "Parameters cannot be null."),
            Arguments.of(DefinitionBuilders.type(Object.class), DefinitionBuilders.name("testMethod"), DefinitionBuilders.noParameters(), null, DefinitionBuilders.noThrows(), "Return type cannot be null."),
            Arguments.of(DefinitionBuilders.type(Object.class), DefinitionBuilders.name("testMethod"), DefinitionBuilders.noParameters(), DefinitionBuilders.voidType(), null, "Throwing cannot be null."),
            Arguments.of(DefinitionBuilders.type(Object.class), DefinitionBuilders.name("testMethod"), DefinitionBuilders.parameters(String.class), DefinitionBuilders.voidType(), DefinitionBuilders.noThrows(), "Expected 1 builders to satisfy the method parameters. Found 0 builders instead.")
        );
    }

    @Test
    public void illegalArgumentException_nullMethodOwnerForStaticMethod() {
        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class,
            () -> new InvokeStaticMethodInsn(null, DefinitionBuilders.name("testMethodName"))
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
            .withConstructor(constructor(publicOnly(), DefinitionBuilders.noParameters(),
                CodeBuilders.superConstructor(TestBaseType.class, DefinitionBuilders.noParameters()),
                CodeBuilders.invokeStatic(TestStatics.class, "privateStaticMethod"),
                CodeBuilders.returnVoid()
            ));

        IllegalStateException ex = assertThrows(IllegalStateException.class, builder::build);
        assertThat(ex, hasProperty("message",
            is("Method 'privateStaticMethod' not found in class '%s' with parameters:\n\t".formatted(TestStatics.class.getName()))
        ));
    }

    @Test
    public void illegalStateException_explicitMethodInfo_staticMethodNotAccessible() {
        AsmClassBuilder<TestBaseType> builder = new AsmClassBuilder<>(TestBaseType.class)
            .withConstructor(constructor(publicOnly(), DefinitionBuilders.noParameters(),
                CodeBuilders.superConstructor(TestBaseType.class, DefinitionBuilders.noParameters()),
                CodeBuilders.invokeStatic(TestStatics.class, DefinitionBuilders.name("privateStaticMethod"), DefinitionBuilders.noParameters(), DefinitionBuilders.voidType()),
                CodeBuilders.returnVoid()
            ));

        IllegalStateException ex = assertThrows(IllegalStateException.class, builder::build);
        assertThat(ex, hasProperty("message",
            is("Method 'privateStaticMethod' not found in class '%s' with parameters:\n\t".formatted(TestStatics.class.getName()))
        ));
    }

    @Test
    public void illegalStateException_implicitMethodInfo_staticMethodDoesNotExist() {
        AsmClassBuilder<TestBaseType> builder = new AsmClassBuilder<>(TestBaseType.class)
            .withConstructor(constructor(publicOnly(), DefinitionBuilders.noParameters(),
                CodeBuilders.superConstructor(TestBaseType.class, DefinitionBuilders.noParameters()),
                CodeBuilders.invokeStatic(TestStatics.class, "someMethodWhichNeverExists"),
                CodeBuilders.returnVoid()
            ));

        IllegalStateException ex = assertThrows(IllegalStateException.class, builder::build);
        assertThat(ex, hasProperty("message",
            is("Method 'someMethodWhichNeverExists' not found in class '%s' with parameters:\n\t".formatted(TestStatics.class.getName()))
        ));
    }

    @Test
    public void illegalStateException_explicitMethodInfo_staticMethodDoesNotExist() {
        AsmClassBuilder<TestBaseType> builder = new AsmClassBuilder<>(TestBaseType.class)
            .withConstructor(constructor(publicOnly(), DefinitionBuilders.noParameters(),
                CodeBuilders.superConstructor(TestBaseType.class, DefinitionBuilders.noParameters()),
                CodeBuilders.invokeStatic(TestStatics.class, DefinitionBuilders.name("someMethodWhichNeverExists"), DefinitionBuilders.noParameters(), DefinitionBuilders.voidType()),
                CodeBuilders.returnVoid()
            ));

        IllegalStateException ex = assertThrows(IllegalStateException.class, builder::build);
        assertThat(ex, hasProperty("message",
            is("Method 'someMethodWhichNeverExists' not found in class '%s' with parameters:\n\t".formatted(TestStatics.class.getName()))
        ));
    }

    @Test
    public void illegalStateException_implicitMethodInfo_methodIsNotStatic() {
        AsmClassBuilder<TestBaseType> builder = new AsmClassBuilder<>(TestBaseType.class)
            .withConstructor(constructor(publicOnly(), DefinitionBuilders.noParameters(),
                CodeBuilders.superConstructor(TestBaseType.class, DefinitionBuilders.noParameters()),
                CodeBuilders.invokeStatic(TestStatics.class, "nonStaticTestMethod"),
                CodeBuilders.returnVoid()
            ));

        IllegalStateException ex = assertThrows(IllegalStateException.class, builder::build);
        assertThat(ex, hasProperty("message",
            is("Method 'nonStaticTestMethod' in class '%s' is not static.".formatted(TestStatics.class.getName()))
        ));
    }

    @Test
    public void illegalStateException_explicitMethodInfo_methodIsNotStatic() {
        AsmClassBuilder<TestBaseType> builder = new AsmClassBuilder<>(TestBaseType.class)
            .withConstructor(constructor(publicOnly(), DefinitionBuilders.noParameters(),
                CodeBuilders.superConstructor(TestBaseType.class, DefinitionBuilders.noParameters()),
                CodeBuilders.invokeStatic(TestStatics.class, DefinitionBuilders.name("nonStaticTestMethod"), DefinitionBuilders.noParameters(), DefinitionBuilders.voidType()),
                CodeBuilders.returnVoid()
            ));

        IllegalStateException ex = assertThrows(IllegalStateException.class, builder::build);
        assertThat(ex, hasProperty("message",
            is("Method 'nonStaticTestMethod' in class '%s' is not static.".formatted(TestStatics.class.getName()))
        ));
    }

    @Test
    public void successfullyCallAnExplicitlyDefinedStaticMethod() {
        AsmClassBuilder<TestBaseType> builder = new AsmClassBuilder<>(TestBaseType.class)
            .withConstructor(constructor(publicOnly(), DefinitionBuilders.noParameters(),
                CodeBuilders.superConstructor(TestBaseType.class, DefinitionBuilders.noParameters()),
                CodeBuilders.returnVoid()
            ))
            .withMethod(method(publicOnly(), DefinitionBuilders.name("abs"), DefinitionBuilders.parameters(int.class), DefinitionBuilders.type(int.class),
                CodeBuilders.returnValue(
                    CodeBuilders.invokeStatic(DefinitionBuilders.type(Math.class), DefinitionBuilders.name("abs"), DefinitionBuilders.parameters(int.class), DefinitionBuilders.type(int.class),
                        CodeBuilders.getVar(1)
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
            .withConstructor(constructor(publicOnly(), DefinitionBuilders.noParameters(),
                CodeBuilders.superConstructor(TestBaseType.class, DefinitionBuilders.noParameters()),
                CodeBuilders.returnVoid()
            ))
            .withMethod(method(publicOnly(), DefinitionBuilders.name("abs"), DefinitionBuilders.parameters(int.class), DefinitionBuilders.type(int.class),
                CodeBuilders.returnValue(
                    CodeBuilders.invokeStatic(Math.class, "abs", CodeBuilders.getVar(1))
                )
            ));

        TestBaseType instance = builder.buildInstance();
        assertThat(instance.abs(-5000), is(5000));
        assertThat(instance.abs(5000), is(5000));
    }

    @Test
    public void illegalStateException_implicitMethodInfo_methodDoesNotExistInBuildingClass() {
        AsmClassBuilder<Object> builder = new AsmClassBuilder<>(Object.class)
            .withConstructor(constructor(publicOnly(), DefinitionBuilders.noParameters(),
                CodeBuilders.superConstructor(Object.class, DefinitionBuilders.noParameters()),
                CodeBuilders.this_().invoke("someUnknownMethod"),
                CodeBuilders.returnVoid()
            ));

        IllegalStateException ex = assertThrows(IllegalStateException.class, builder::build);
        assertThat(ex, hasProperty("message",
            startsWith("Method 'someUnknownMethod' not found in class 'java.lang.Object")
        ));
    }

    @Test
    public void illegalStateException_parameterBuilderDoesNotAddExactlyOneElementToStack() {
        AsmClassBuilder<TestBaseType> builder = new AsmClassBuilder<>(TestBaseType.class)
            .withConstructor(constructor(publicOnly(), DefinitionBuilders.noParameters(),
                CodeBuilders.superConstructor(TestBaseType.class, DefinitionBuilders.noParameters()),
                CodeBuilders.returnVoid()
            ))
            .withMethod(method(publicOnly(), DefinitionBuilders.name("abs"), DefinitionBuilders.parameters(int.class), DefinitionBuilders.type(int.class),
                CodeBuilders.returnValue(
                    CodeBuilders.invokeStatic(Math.class, "abs",
                        CodeBuilders.invokeStatic(TestStatics.class, "protectedStaticMethod") //void method does not stack the expected 1 element
                    )
                )
            ));

        IllegalStateException ex = assertThrows(IllegalStateException.class, builder::build);
        assertThat(ex, hasProperty("message",
            is("Code builder expected to add 1 element to the stack. Instead 0 elements were added.")
        ));
    }
}