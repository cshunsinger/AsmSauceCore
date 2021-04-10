package com.chunsinger.asmsauce.code.branch.condition;

import com.chunsinger.asmsauce.AsmClassBuilder;
import com.chunsinger.asmsauce.testing.BaseUnitTest;
import org.junit.jupiter.api.Test;

import static com.chunsinger.asmsauce.ConstructorNode.constructor;
import static com.chunsinger.asmsauce.DefinitionBuilders.*;
import static com.chunsinger.asmsauce.MethodNode.method;
import static com.chunsinger.asmsauce.code.CodeBuilders.*;
import static com.chunsinger.asmsauce.modifiers.AccessModifiers.publicOnly;
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
                iff(localVar("lowercase").isTrue()).then(
                    returnValue(localVar("inputString").invoke("toLowerCase"))
                ),
                returnValue(localVar("inputString"))
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
                iff(localVar("uppercase").isFalse()).then(
                    returnValue(localVar("inputString"))
                ),
                returnValue(localVar("inputString").invoke("toUpperCase"))
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