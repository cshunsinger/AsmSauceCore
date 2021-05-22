package io.github.cshunsinger.asmsauce.code.branch;

import io.github.cshunsinger.asmsauce.AsmClassBuilder;
import io.github.cshunsinger.asmsauce.BaseUnitTest;
import io.github.cshunsinger.asmsauce.code.CodeInsnBuilderLike;
import io.github.cshunsinger.asmsauce.code.branch.condition.Condition;
import lombok.Getter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.ThrowingSupplier;
import org.mockito.Mock;

import static io.github.cshunsinger.asmsauce.ConstructorNode.constructor;
import static io.github.cshunsinger.asmsauce.DefinitionBuilders.*;
import static io.github.cshunsinger.asmsauce.MethodNode.method;
import static io.github.cshunsinger.asmsauce.code.CodeBuilders.*;
import static io.github.cshunsinger.asmsauce.modifiers.AccessModifiers.publicOnly;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

public class ElseBranchTest extends BaseUnitTest {
    @Mock
    private Condition mockCondition;
    @Mock
    private CodeInsnBuilderLike mockCodeInsnBuilder;

    @Test
    public void illegalArgumentException_emptyElseBody() {
        when(mockCodeInsnBuilder.getFirstInStack()).thenReturn(mockCodeInsnBuilder);
        IfBranch ifBranch = new IfBranch(mockCondition, mockCodeInsnBuilder);

        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class,
            () -> new ElseBranch(ifBranch)
        );

        assertThat(ex, hasProperty("message", is("Body cannot be empty.")));
    }

    /**
     * This base class is for testing if the if/else system works or not.
     */
    @Getter
    public static abstract class IfElseTestType {
        protected boolean ifExecuted;
        protected boolean elseIfExecuted;

        public abstract void testIfElse(String branchName);
    }

    @Test
    public void successfullyCreateBytecodeForIfElseBlock() {
        AsmClassBuilder<IfElseTestType> builder = new AsmClassBuilder<>(IfElseTestType.class)
            .withConstructor(constructor(publicOnly(), noParameters(),
                superConstructor(IfElseTestType.class, noParameters()),
                returnVoid()
            ))
            .withMethod(method(publicOnly(), name("testIfElse"), parameters(p("branchName", String.class)),
                if_(literalObj("if").invoke("equals", getVar("branchName")).isTrue()).then(
                    this_().assignField("ifExecuted", true_()),
                    this_().assignField("elseIfExecuted", false_())
                )
                .elseDo(
                    if_(literalObj("elseIf").invoke("equals", getVar("branchName")).isTrue()).then(
                        this_().assignField("ifExecuted", false_()),
                        this_().assignField("elseIfExecuted", true_())
                    )
                    .elseDo(
                        this_().assignField("ifExecuted", false_()),
                        this_().assignField("elseIfExecuted", false_())
                    )
                ),

                returnVoid()
            ));

        IfElseTestType instance = assertDoesNotThrow((ThrowingSupplier<IfElseTestType>)builder::buildInstance);

        instance.testIfElse("if");
        assertThat(instance, allOf(
            hasProperty("ifExecuted", is(true)),
            hasProperty("elseIfExecuted", is(false))
        ));

        instance.testIfElse("elseIf");
        assertThat(instance, allOf(
            hasProperty("ifExecuted", is(false)),
            hasProperty("elseIfExecuted", is(true))
        ));

        instance.testIfElse("else");
        assertThat(instance, allOf(
            hasProperty("ifExecuted", is(false)),
            hasProperty("elseIfExecuted", is(false))
        ));
    }
}