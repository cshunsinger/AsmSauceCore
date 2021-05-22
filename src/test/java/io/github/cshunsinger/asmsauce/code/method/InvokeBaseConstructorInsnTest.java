package io.github.cshunsinger.asmsauce.code.method;

import io.github.cshunsinger.asmsauce.BaseUnitTest;
import io.github.cshunsinger.asmsauce.MethodBuildingContext;
import io.github.cshunsinger.asmsauce.ThisClass;
import io.github.cshunsinger.asmsauce.code.CodeInsnBuilderLike;
import io.github.cshunsinger.asmsauce.definitions.ParametersDefinition;
import io.github.cshunsinger.asmsauce.definitions.ThrowsDefinition;
import io.github.cshunsinger.asmsauce.definitions.TypeDefinition;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.objectweb.asm.MethodVisitor;

import java.util.stream.Stream;

import static io.github.cshunsinger.asmsauce.DefinitionBuilders.*;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

class InvokeBaseConstructorInsnTest extends BaseUnitTest {
    @Mock
    private MethodVisitor mockMethodVisitor;
    @Mock
    private CodeInsnBuilderLike mockParamBuilder;

    @Test
    public void illegalArgumentException_nullConstructorObject() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> new InvokeBaseConstructorInsn(null));
        assertThat(ex, hasProperty("message", is("Constructor cannot be null.")));
    }

    @ParameterizedTest
    @MethodSource("illegalArgumentException_constructingNewInsn_testParameters")
    public void illegalArgumentException_constructingNewInsn(TypeDefinition testOwnerType,
                                                             ParametersDefinition testParametersDefinition,
                                                             ThrowsDefinition testThrowsDefinition,
                                                             String expectedExceptionMessage) {
        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class,
            () -> new InvokeBaseConstructorInsn(testOwnerType, testParametersDefinition, testThrowsDefinition)
        );

        assertThat(ex, hasProperty("message", is(expectedExceptionMessage)));
    }

    private static Stream<Arguments> illegalArgumentException_constructingNewInsn_testParameters() {
        return Stream.of(
            Arguments.of(null, noParameters(), noThrows(), "Method owner type cannot be null."),
            Arguments.of(voidType(), noParameters(), noThrows(), "Method owner type cannot be void."),
            Arguments.of(type(int.class), noParameters(), noThrows(), "Method owner type cannot be a primitive type."),
            Arguments.of(type(ThisClass.class), null, noThrows(), "Parameters cannot be null."),
            Arguments.of(type(ThisClass.class), noParameters(), null, "Throwing cannot be null."),
            Arguments.of(type(ThisClass.class), parameters(String.class), noThrows(), "Expected 1 builders to satisfy the method parameters. Found 0 builders instead.")
        );
    }

    @Test
    public void illegalStateException_parameterBuilderPlacesMoreThanOneItemOntoTheStack() {
        new MethodBuildingContext(mockMethodVisitor, null, null, singletonList(p("this", ThisClass.class)));

        when(mockParamBuilder.getFirstInStack()).thenReturn(mockParamBuilder);
        doAnswer(i -> null).when(mockParamBuilder).build();

        InvokeBaseConstructorInsn insn = new InvokeBaseConstructorInsn(type(ThisClass.class), parameters(String.class), noThrows(), mockParamBuilder);

        IllegalStateException ex = assertThrows(IllegalStateException.class, insn::build);
        assertThat(ex, hasProperty("message", is("Code builder expected to add 1 element to the stack. Instead 0 elements were added.")));
    }

    @Test
    public void illegalStateException_parameterBuilderPlacesWrongTypeOntoTheStack() {
        MethodBuildingContext methodContext = new MethodBuildingContext(mockMethodVisitor, null, null, singletonList(p("this", ThisClass.class)));

        //the mocking below simulates an int being pushed to the stack as a parameter, when a String is expected
        //Because int cannot implicitly convert to a String, an exception should be thrown.
        when(mockParamBuilder.getFirstInStack()).thenReturn(mockParamBuilder);
        doAnswer(i -> methodContext.pushStack(type(int.class))).when(mockParamBuilder).build();

        InvokeBaseConstructorInsn insn = new InvokeBaseConstructorInsn(type(ThisClass.class), parameters(String.class), noThrows(), mockParamBuilder);

        IllegalStateException ex = assertThrows(IllegalStateException.class, insn::build);
        assertThat(ex, hasProperty("message", is("Cannot convert from type int into type java.lang.String.")));
    }

    /*
     * Other test cases, especially positive test cases, are covered in the many other test suites in this package.
     */
}