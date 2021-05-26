package io.github.cshunsinger.asmsauce.code.branch;

import io.github.cshunsinger.asmsauce.AsmClassBuilder;
import io.github.cshunsinger.asmsauce.BaseUnitTest;
import io.github.cshunsinger.asmsauce.MethodBuildingContext;
import io.github.cshunsinger.asmsauce.code.CodeInsnBuilderLike;
import io.github.cshunsinger.asmsauce.code.branch.condition.Condition;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.objectweb.asm.MethodVisitor;

import static io.github.cshunsinger.asmsauce.DefinitionBuilders.*;
import static io.github.cshunsinger.asmsauce.MethodNode.method;
import static io.github.cshunsinger.asmsauce.code.CodeBuilders.*;
import static io.github.cshunsinger.asmsauce.definitions.TypeDefinition.BYTE;
import static io.github.cshunsinger.asmsauce.definitions.TypeDefinition.INT;
import static io.github.cshunsinger.asmsauce.modifiers.AccessModifiers.publicOnly;
import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

public class TernaryIfElseTest extends BaseUnitTest {
    @Mock
    private Condition mockCondition;
    @Mock
    private CodeInsnBuilderLike mockIfReturnInsn;
    @Mock
    private CodeInsnBuilderLike mockElseReturnInsn;
    @Mock
    private CodeInsnBuilderLike mockNothingInsn;
    @Mock
    private MethodVisitor mockMethodVisitor;

    @Test
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void illegalArgumentException_attemptingToBuildTernaryWithNullCondition() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
            () -> ternary(null)
        );

        assertThat(ex, hasProperty("message", is("Ternary condition may not be null.")));
    }

    @Test
    public void illegalArgumentException_attemptingToBuildTernary_trueBodyIsNull() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
            () -> ternary(mockCondition).thenCalculate((CodeInsnBuilderLike[])null)
        );

        assertThat(ex, hasProperty("message", is("Ternary if-body cannot be null.")));
    }

    @Test
    public void illegalArgumentException_attemptingToBuildTernary_trueBodyIsEmpty() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
            () -> ternary(mockCondition).thenCalculate()
        );

        assertThat(ex, hasProperty("message", is("Ternary if-body cannot be empty.")));
    }

    @Test
    public void illegalArgumentException_attemptingToBuildTernary_lastStatementOfTrueBodyIsNull() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
            () -> ternary(mockCondition).thenCalculate(mockIfReturnInsn, null)
        );

        assertThat(ex, hasProperty("message", is("The last statement of the ternary if-body cannot be null.")));
    }

    @Test
    public void illegalArgumentException_attemptingToBuildTernary_falseBodyIsNull() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
            () -> ternary(mockCondition).thenCalculate(mockIfReturnInsn).elseCalculate((CodeInsnBuilderLike[])null)
        );

        assertThat(ex, hasProperty("message", is("Ternary else-body cannot be null.")));
    }

    @Test
    public void illegalArgumentException_attemptingToBuildTernary_falseBodyIsEmpty() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
            () -> ternary(mockCondition).thenCalculate(mockIfReturnInsn).elseCalculate()
        );

        assertThat(ex, hasProperty("message", is("Ternary else-body cannot be empty.")));
    }

    @Test
    public void illegalArgumentException_attemptingToBuildTernary_lastStatementOfFalseBodyIsNull() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
            () -> ternary(mockCondition).thenCalculate(mockIfReturnInsn).elseCalculate(mockElseReturnInsn, null)
        );

        assertThat(ex, hasProperty("message", is("The last statement of the ternary else-body cannot be null.")));
    }

    @Test
    public void illegalStateException_ternaryStatementTrueBranchDoesNotStackOneValue() {
        MethodBuildingContext context = new MethodBuildingContext(null, null, null, emptyList());

        when(mockNothingInsn.getFirstInStack()).thenReturn(mockNothingInsn);
        when(mockIfReturnInsn.getFirstInStack()).thenReturn(mockIfReturnInsn);
        when(mockElseReturnInsn.getFirstInStack()).thenReturn(mockElseReturnInsn);

        doAnswer(i -> null).when(mockNothingInsn).buildClean();

        doAnswer(i -> {
            context.pushStack(INT);
            return context.pushStack(INT);
        }).when(mockIfReturnInsn).build();

        TernaryIfElse ternaryInsn = ternary(mockCondition)
            .thenCalculate(mockNothingInsn, mockIfReturnInsn)
            .elseCalculate(mockNothingInsn, mockElseReturnInsn);

        IllegalStateException ex = assertThrows(IllegalStateException.class, ternaryInsn::build);
        assertThat(ex, hasProperty("message", is("Ternary if-else expected 1 element to be stacked. Got 2 instead.")));
    }

    @Test
    public void illegalStateException_ternaryStatementFalseBranchDoesNotStackOneValue() {
        MethodBuildingContext context = new MethodBuildingContext(mockMethodVisitor, null, null, emptyList());

        when(mockNothingInsn.getFirstInStack()).thenReturn(mockNothingInsn);
        when(mockIfReturnInsn.getFirstInStack()).thenReturn(mockIfReturnInsn);
        when(mockElseReturnInsn.getFirstInStack()).thenReturn(mockElseReturnInsn);

        doAnswer(i -> null).when(mockNothingInsn).buildClean();
        doAnswer(i -> context.pushStack(INT)).when(mockIfReturnInsn).build();

        doAnswer(i -> {
            context.pushStack(INT);
            return context.pushStack(INT);
        }).when(mockElseReturnInsn).build();

        TernaryIfElse ternaryInsn = ternary(mockCondition)
            .thenCalculate(mockNothingInsn, mockIfReturnInsn)
            .elseCalculate(mockNothingInsn, mockElseReturnInsn);

        IllegalStateException ex = assertThrows(IllegalStateException.class, ternaryInsn::build);
        assertThat(ex, hasProperty("message", is("Ternary if-else expected 1 element to be stacked. Got 2 instead.")));
    }

    @Test
    public void illegalStateException_ternaryElseValueTypeIsNotCompatibleWithTheTernaryIfValueType() {
        MethodBuildingContext context = new MethodBuildingContext(mockMethodVisitor, null, null, emptyList());

        when(mockNothingInsn.getFirstInStack()).thenReturn(mockNothingInsn);
        when(mockIfReturnInsn.getFirstInStack()).thenReturn(mockIfReturnInsn);
        when(mockElseReturnInsn.getFirstInStack()).thenReturn(mockElseReturnInsn);

        //Mocking the if-return to be a byte, and the else-return to be an int. An int cannot be implicitly converted into a byte.
        doAnswer(i -> context.pushStack(BYTE)).when(mockIfReturnInsn).build();
        doAnswer(i -> context.pushStack(INT)).when(mockElseReturnInsn).build();
        doAnswer(i -> null).when(mockNothingInsn).buildClean();

        TernaryIfElse ternaryInsn = ternary(mockCondition)
            .thenCalculate(mockNothingInsn, mockIfReturnInsn)
            .elseCalculate(mockNothingInsn, mockElseReturnInsn);

        IllegalStateException ex = assertThrows(IllegalStateException.class, ternaryInsn::build);
        assertThat(ex, hasProperty("message", is("Ternary else-value of type int is not compatible with the if-value type byte.")));
    }

    public static abstract class TestTernaryBaseType {
        public abstract int getMin(int a, int b);
    }

    @Test
    public void successfullyReturnValueFromTernaryStatement() {
        AsmClassBuilder<TestTernaryBaseType> builder = new AsmClassBuilder<>(TestTernaryBaseType.class)
            .withMethod(method(publicOnly(), name("getMin"), parameters(p("a", INT), p("b", INT)), INT,
                returnValue(
                    ternary(getVar("a").lt(getVar("b")))
                        .thenCalculate(getVar("a"))
                        .elseCalculate(getVar("b"))
                )
            ));

        TestTernaryBaseType instance = builder.buildInstance();
        assertThat(instance.getMin(10, 5), is(5));
        assertThat(instance.getMin(5, 10), is(5));
        assertThat(instance.getMin(-1, -2), is(-2));
    }
}