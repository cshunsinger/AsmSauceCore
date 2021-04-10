package com.chunsinger.asmsauce.code.method;

import com.chunsinger.asmsauce.AsmClassBuilder;
import com.chunsinger.asmsauce.testing.BaseUnitTest;
import lombok.Getter;
import org.junit.jupiter.api.Test;

import static com.chunsinger.asmsauce.ConstructorNode.constructor;
import static com.chunsinger.asmsauce.DefinitionBuilders.*;
import static com.chunsinger.asmsauce.MethodNode.method;
import static com.chunsinger.asmsauce.code.CodeBuilders.*;
import static com.chunsinger.asmsauce.modifiers.AccessModifiers.publicOnly;
import static java.util.Collections.singletonList;
import static org.apache.commons.lang3.RandomUtils.nextInt;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

class InstantiateObjectInsnTest extends BaseUnitTest {
    @Test
    public void illegalArgumentException_nullInstantiatedType() {
        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class,
            () -> new InstantiateObjectInsn(null)
        );
        assertThat(ex, hasProperty("message", is("Instantiated type cannot be null.")));
    }

    @SuppressWarnings("unused")
    public static class TestType {
        @Getter
        private final int testInt;

        public TestType() {
            testInt = 0;
        }

        private TestType(String s1) {
            this();
        }

        public TestType(int i) {
            this.testInt = i;
        }
    }

    public interface TestBase {
        TestType createTestType(int i);
    }

    @Test
    public void illegalStateException_implicitDefinition_tryingToAccessInaccessibleConstructor() {
        AsmClassBuilder<Object> builder = new AsmClassBuilder<>(Object.class)
            .withConstructor(constructor(publicOnly(), noParameters(),
                superConstructor(Object.class, noParameters()),
                instantiate(TestType.class, stackObject("Test String")),
                returnVoid()
            ));

        IllegalStateException ex = assertThrows(IllegalStateException.class, builder::build);
        assertThat(ex, hasProperty("message",
            is("Constructor not found in class '%s' with parameters:\n\t%s".formatted(TestType.class.getName(), String.class.getName()))
        ));
    }

    @Test
    public void illegalStateException_explicitDefinition_tryingToAccessInaccessibleConstructor() {
        AsmClassBuilder<Object> builder = new AsmClassBuilder<>(Object.class)
            .withConstructor(constructor(publicOnly(), noParameters(),
                superConstructor(Object.class, noParameters()),
                instantiate(TestType.class, parameters(String.class), stackObject("Test String")),
                returnVoid()
            ));

        IllegalStateException ex = assertThrows(IllegalStateException.class, builder::build);
        assertThat(ex, hasProperty("message",
            is("Constructor not found in class '%s' with parameters:\n\t%s".formatted(TestType.class.getName(), String.class.getName()))
        ));
    }

    @Test
    public void illegalStateException_implicitDefinition_tryingToAccessNonExistentConstructor() {
        AsmClassBuilder<Object> builder = new AsmClassBuilder<>(Object.class)
            .withConstructor(constructor(publicOnly(), noParameters(),
                superConstructor(Object.class, noParameters()),
                instantiate(TestType.class, stackObject("Test String"), stackObject("Test String 2")),
                returnVoid()
            ));

        IllegalStateException ex = assertThrows(IllegalStateException.class, builder::build);
        assertThat(ex, hasProperty("message",
            is("Constructor not found in class '%s' with parameters:\n\t%s\n\t%s".formatted(
                TestType.class.getName(), String.class.getName(), String.class.getName()
            ))
        ));
    }

    @Test
    public void illegalStateException_explicitDefinition_tryingToAccessNonExistentConstructor() {
        AsmClassBuilder<Object> builder = new AsmClassBuilder<>(Object.class)
            .withConstructor(constructor(publicOnly(), noParameters(),
                superConstructor(Object.class, noParameters()),
                instantiate(TestType.class, parameters(String.class, String.class), stackObject("Test String"), stackObject("Test String 2")),
                returnVoid()
            ));

        IllegalStateException ex = assertThrows(IllegalStateException.class, builder::build);
        assertThat(ex, hasProperty("message",
            is("Constructor not found in class '%s' with parameters:\n\t%s\n\t%s".formatted(
                TestType.class.getName(), String.class.getName(), String.class.getName()
            ))
        ));
    }

    @Test
    public void implicitDefinition_successfullyConstructNewObjectInstance() {
        AsmClassBuilder<TestBase> builder = new AsmClassBuilder<>(TestBase.class, Object.class, singletonList(TestBase.class), publicOnly())
            .withConstructor(constructor(publicOnly(), noParameters(),
                superConstructor(Object.class, noParameters()),
                returnVoid()
            ))
            .withMethod(method(publicOnly(), name("createTestType"), parameters(p("myInt", int.class)), type(TestType.class),
                storeLocal("testType", instantiate(TestType.class, localVar("myInt"))),
                returnValue(localVar("testType"))
            ));

        TestBase instance = builder.buildInstance();

        int testInt = nextInt();
        TestType testResult = instance.createTestType(testInt);
        assertThat(testResult, allOf(
            notNullValue(),
            hasProperty("testInt", is(testInt))
        ));
    }

    @Test
    public void explicitDefinition_successfullyConstructNewObjectInstance() {
        AsmClassBuilder<TestBase> builder = new AsmClassBuilder<>(TestBase.class, Object.class, singletonList(TestBase.class), publicOnly())
            .withConstructor(constructor(publicOnly(), noParameters(),
                superConstructor(Object.class, noParameters()),
                returnVoid()
            ))
            .withMethod(method(publicOnly(), name("createTestType"), parameters(p("myInt", int.class)), type(TestType.class),
                storeLocal("testType", instantiate(TestType.class, parameters(int.class), localVar("myInt"))),
                returnValue(localVar("testType"))
            ));

        TestBase instance = builder.buildInstance();

        int testInt = nextInt();
        TestType testResult = instance.createTestType(testInt);
        assertThat(testResult, allOf(
            notNullValue(),
            hasProperty("testInt", is(testInt))
        ));
    }
}