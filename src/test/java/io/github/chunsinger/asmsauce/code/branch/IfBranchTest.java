package io.github.chunsinger.asmsauce.code.branch;

import aj.org.objectweb.asm.Label;
import io.github.chunsinger.asmsauce.AsmClassBuilder;
import io.github.chunsinger.asmsauce.code.CodeInsnBuilderLike;
import io.github.chunsinger.asmsauce.code.branch.condition.Condition;
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
        Condition testCondition = CodeBuilders.literal(1).eq(CodeBuilders.literal(1));
        CodeInsnBuilderLike[] testBody = new CodeInsnBuilderLike[1];
        testBody[0] = CodeBuilders.literal(1);

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
            .withConstructor(constructor(publicOnly(), DefinitionBuilders.noParameters(),
                CodeBuilders.superConstructor(TestType.class, DefinitionBuilders.noParameters()),
                CodeBuilders.returnVoid()
            ))
            .withMethod(method(publicOnly(), DefinitionBuilders.name("abs"), DefinitionBuilders.parameters(DefinitionBuilders.p("value", int.class)), DefinitionBuilders.type(int.class),
                CodeBuilders.if_(CodeBuilders.getVar("value").lt(CodeBuilders.literal(0))).then(
                    CodeBuilders.returnValue(CodeBuilders.getVar("value").mul(CodeBuilders.literal(-1)))
                ),
                CodeBuilders.returnValue(CodeBuilders.getVar("value"))
            ));

        TestType instance = builder.buildInstance();

        int positive = nextInt(1, Integer.MAX_VALUE);
        int negative = -positive;

        assertThat(instance.abs(positive), is(positive));
        assertThat(instance.abs(negative), is(positive));
    }
}