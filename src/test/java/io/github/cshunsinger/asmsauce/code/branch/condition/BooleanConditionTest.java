package io.github.cshunsinger.asmsauce.code.branch.condition;

import io.github.cshunsinger.asmsauce.AsmClassBuilder;
import io.github.cshunsinger.asmsauce.BaseUnitTest;
import org.junit.jupiter.api.Test;

import static io.github.cshunsinger.asmsauce.ConstructorNode.constructor;
import static io.github.cshunsinger.asmsauce.DefinitionBuilders.*;
import static io.github.cshunsinger.asmsauce.MethodNode.method;
import static io.github.cshunsinger.asmsauce.code.CodeBuilders.*;
import static io.github.cshunsinger.asmsauce.modifiers.AccessModifiers.publicOnly;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class BooleanConditionTest extends BaseUnitTest {
    public static abstract class TestWithBooleansType {
        public abstract String caseString(String inputString, boolean b);
    }

    @Test
    public void booleanIfTrueCondition() {
        AsmClassBuilder<TestWithBooleansType> builder = new AsmClassBuilder<>(TestWithBooleansType.class)
            .withConstructor(constructor(publicOnly(), noParameters(),
                superConstructor(TestWithBooleansType.class, noParameters()),
                returnVoid()
            ))
            .withMethod(method(publicOnly(), name("caseString"), parameters(p("inputString", String.class), p("lowercase", boolean.class)), type(String.class),
                if_(getVar("lowercase").isTrue()).then(
                    returnValue(getVar("inputString").invoke("toLowerCase"))
                ),
                returnValue(getVar("inputString"))
            ));

        TestWithBooleansType instance = builder.buildInstance();
        assertThat(instance.caseString("TEST STRING", true), is("test string"));
        assertThat(instance.caseString("TEST STRING", false), is("TEST STRING"));
    }

    @Test
    public void booleanIfFalseCondition() {
        AsmClassBuilder<TestWithBooleansType> builder = new AsmClassBuilder<>(TestWithBooleansType.class)
            .withConstructor(constructor(publicOnly(), noParameters(),
                superConstructor(TestWithBooleansType.class, noParameters()),
                returnVoid()
            ))
            .withMethod(method(publicOnly(), name("caseString"), parameters(p("inputString", String.class), p("uppercase", boolean.class)), type(String.class),
                if_(getVar("uppercase").isFalse()).then(
                    returnValue(getVar("inputString"))
                ),
                returnValue(getVar("inputString").invoke("toUpperCase"))
            ));

        TestWithBooleansType instance = builder.buildInstance();
        assertThat(instance.caseString("test string", true), is("TEST STRING"));
        assertThat(instance.caseString("test string", false), is("test string"));
    }

    @Test
    public void illegalArgumentException_nullOperandBuilder() {
        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class,
            () -> new BooleanCondition(null)
        );

        assertThat(ex, hasProperty("message", is("Operand builder cannot be null.")));
    }
}