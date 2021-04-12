package com.chunsinger.asmsauce.code.branch;

import aj.org.objectweb.asm.Label;
import com.chunsinger.asmsauce.AsmClassBuilder;
import com.chunsinger.asmsauce.code.CodeInsnBuilderLike;
import com.chunsinger.asmsauce.code.branch.condition.Condition;
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
import static org.apache.commons.lang3.RandomUtils.nextInt;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class IfBranchTest extends BaseUnitTest {
    @ParameterizedTest
    @MethodSource("illegalArgumentException_badConstructorParameters_testCases")
    public void illegalArgumentException_badConstructorParameters(Condition testCondition,
                                                                  Label testLabel,
                                                                  CodeInsnBuilderLike[] testBody,
                                                                  String expectedExceptionMessage) {
        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class,
            () -> new IfBranch(testCondition, testLabel, testBody)
        );

        assertThat(ex, hasProperty("message", is(expectedExceptionMessage)));
    }

    private static Stream<Arguments> illegalArgumentException_badConstructorParameters_testCases() {
        Condition testCondition = literal(1).eq(literal(1));
        CodeInsnBuilderLike[] testBody = new CodeInsnBuilderLike[1];
        testBody[0] = literal(1);

        return Stream.of(
            Arguments.of(null, new Label(), testBody, "Condition cannot be null."),
            Arguments.of(testCondition, null, testBody, "End Label cannot be null."),
            Arguments.of(testCondition, new Label(), new CodeInsnBuilderLike[0], "Body cannot be empty.")
        );
    }

    public static abstract class TestType {
        public abstract int abs(int value);
    }

    @Test
    public void useBasicIfStatementToImplementAbsoluteValueMethod() {
        AsmClassBuilder<TestType> builder = new AsmClassBuilder<>(TestType.class)
            .withConstructor(constructor(publicOnly(), noParameters(),
                superConstructor(TestType.class, noParameters()),
                returnVoid()
            ))
            .withMethod(method(publicOnly(), name("abs"), parameters(p("value", int.class)), type(int.class),
                if_(getVar("value").lt(literal(0))).then(
                    returnValue(getVar("value").mul(literal(-1)))
                ),
                returnValue(getVar("value"))
            ));

        TestType instance = builder.buildInstance();

        int positive = nextInt(1, Integer.MAX_VALUE);
        int negative = -positive;

        assertThat(instance.abs(positive), is(positive));
        assertThat(instance.abs(negative), is(positive));
    }
}