package com.chunsinger.asmsauce.code.branch.condition;

import com.chunsinger.asmsauce.AsmClassBuilder;
import com.chunsinger.asmsauce.code.CodeBuilders;
import com.chunsinger.asmsauce.testing.BaseUnitTest;
import org.junit.jupiter.api.Test;

import static com.chunsinger.asmsauce.ConstructorNode.constructor;
import static com.chunsinger.asmsauce.DefinitionBuilders.*;
import static com.chunsinger.asmsauce.MethodNode.method;
import static com.chunsinger.asmsauce.code.CodeBuilders.*;
import static com.chunsinger.asmsauce.modifiers.AccessModifiers.publicOnly;
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
            .withConstructor(constructor(publicOnly(), noParameters(),
                superConstructor(TestType.class, noParameters()),
                returnVoid()
            ))
            .withMethod(method(publicOnly(), name("generateString"), parameters(p("value", Object.class), p("defaultString", String.class)), type(String.class),
                if_(getVar("value").isNotNull()).then(
                    returnValue(getVar("value").invoke("toString"))
                ),
                if_(not(getVar("defaultString").isNull())).then(
                    returnValue(getVar("defaultString"))
                ),
                returnValue(CodeBuilders.literalObj("null"))
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
            .withConstructor(constructor(publicOnly(), noParameters(),
                superConstructor(TestType.class, noParameters()),
                returnVoid()
            ))
            .withMethod(method(publicOnly(), name("generateString"), parameters(p("value", Object.class), p("defaultString", String.class)), type(String.class),
                if_(new NullCondition(literal(101))).then(
                    returnValue(getVar("value").invoke("toString"))
                ),
                returnValue(stackNull())
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