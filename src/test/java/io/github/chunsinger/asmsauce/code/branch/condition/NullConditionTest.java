package io.github.chunsinger.asmsauce.code.branch.condition;

import io.github.chunsinger.asmsauce.AsmClassBuilder;
import io.github.chunsinger.asmsauce.code.CodeBuilders;
import io.github.chunsinger.asmsauce.testing.BaseUnitTest;
import io.github.chunsinger.asmsauce.DefinitionBuilders;
import org.junit.jupiter.api.Test;

import static io.github.chunsinger.asmsauce.ConstructorNode.constructor;
import static io.github.chunsinger.asmsauce.MethodNode.method;
import static io.github.chunsinger.asmsauce.modifiers.AccessModifiers.publicOnly;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class NullConditionTest extends BaseUnitTest {
    public static abstract class TestType {
        public abstract String generateString(Object value, String defaultString);
    }

    @Test
    public void testUseOfNullAndNonNullConditions() {
        AsmClassBuilder<TestType> builder = new AsmClassBuilder<>(TestType.class)
            .withConstructor(constructor(publicOnly(), DefinitionBuilders.noParameters(),
                CodeBuilders.superConstructor(TestType.class, DefinitionBuilders.noParameters()),
                CodeBuilders.returnVoid()
            ))
            .withMethod(method(publicOnly(), DefinitionBuilders.name("generateString"), DefinitionBuilders.parameters(DefinitionBuilders.p("value", Object.class), DefinitionBuilders.p("defaultString", String.class)), DefinitionBuilders.type(String.class),
                CodeBuilders.if_(CodeBuilders.getVar("value").isNotNull()).then(
                    CodeBuilders.returnValue(CodeBuilders.getVar("value").invoke("toString"))
                ),
                CodeBuilders.if_(CodeBuilders.not(CodeBuilders.getVar("defaultString").isNull())).then(
                    CodeBuilders.returnValue(CodeBuilders.getVar("defaultString"))
                ),
                CodeBuilders.returnValue(CodeBuilders.literalObj("null"))
            ));

        TestType instance = builder.buildInstance();
        Object testObject = new Object();
        String testDefaultString = randomAlphanumeric(15);

        assertThat(instance.generateString(null, null), is("null"));
        assertThat(instance.generateString(testObject, null), is(testObject.toString()));
        assertThat(instance.generateString(testObject, testDefaultString), is(testObject.toString()));
        assertThat(instance.generateString(null, testDefaultString), is(testDefaultString));
    }

    @Test
    public void illegalStateException_attemptingToComparePrimitiveTypeToNull() {
        AsmClassBuilder<TestType> builder = new AsmClassBuilder<>(TestType.class)
            .withConstructor(constructor(publicOnly(), DefinitionBuilders.noParameters(),
                CodeBuilders.superConstructor(TestType.class, DefinitionBuilders.noParameters()),
                CodeBuilders.returnVoid()
            ))
            .withMethod(method(publicOnly(), DefinitionBuilders.name("generateString"), DefinitionBuilders.parameters(DefinitionBuilders.p("value", Object.class), DefinitionBuilders.p("defaultString", String.class)), DefinitionBuilders.type(String.class),
                CodeBuilders.if_(new NullCondition(CodeBuilders.literal(101))).then(
                    CodeBuilders.returnValue(CodeBuilders.getVar("value").invoke("toString"))
                ),
                CodeBuilders.returnValue(CodeBuilders.stackNull())
            ));

        IllegalStateException ex = assertThrows(IllegalStateException.class, builder::build);
        assertThat(ex, hasProperty("message", is("Cannot compare a primitive to null.")));
    }

    @Test
    public void illegalArgumentException_nullCodeBuilder() {
        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class,
            () -> new NullCondition(null)
        );

        assertThat(ex, hasProperty("message", is("Operand builder cannot be null.")));
    }
}