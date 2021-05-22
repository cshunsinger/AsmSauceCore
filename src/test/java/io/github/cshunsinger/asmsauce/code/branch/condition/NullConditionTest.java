package io.github.cshunsinger.asmsauce.code.branch.condition;

import io.github.cshunsinger.asmsauce.AsmClassBuilder;
import io.github.cshunsinger.asmsauce.BaseUnitTest;
import io.github.cshunsinger.asmsauce.code.CodeBuilders;
import org.junit.jupiter.api.Test;

import static io.github.cshunsinger.asmsauce.ConstructorNode.constructor;
import static io.github.cshunsinger.asmsauce.DefinitionBuilders.*;
import static io.github.cshunsinger.asmsauce.MethodNode.method;
import static io.github.cshunsinger.asmsauce.code.CodeBuilders.*;
import static io.github.cshunsinger.asmsauce.modifiers.AccessModifiers.publicOnly;
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
                if_(CodeBuilders.not(getVar("defaultString").isNull())).then(
                    returnValue(getVar("defaultString"))
                ),
                returnValue(literalObj("null"))
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
                returnValue(CodeBuilders.stackNull())
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