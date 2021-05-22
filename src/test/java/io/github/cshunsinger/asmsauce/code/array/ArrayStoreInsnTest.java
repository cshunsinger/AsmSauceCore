package io.github.cshunsinger.asmsauce.code.array;

import io.github.cshunsinger.asmsauce.AsmClassBuilder;
import io.github.cshunsinger.asmsauce.BaseUnitTest;
import io.github.cshunsinger.asmsauce.MethodBuildingContext;
import io.github.cshunsinger.asmsauce.code.CodeInsnBuilderLike;
import io.github.cshunsinger.asmsauce.definitions.TypeDefinition;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.util.List;

import static io.github.cshunsinger.asmsauce.DefinitionBuilders.*;
import static io.github.cshunsinger.asmsauce.MethodNode.method;
import static io.github.cshunsinger.asmsauce.code.CodeBuilders.*;
import static io.github.cshunsinger.asmsauce.modifiers.AccessModifiers.publicOnly;
import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

public class ArrayStoreInsnTest extends BaseUnitTest {
    @Mock
    private CodeInsnBuilderLike mockIndexBuilder;
    @Mock
    private CodeInsnBuilderLike mockArrayValueBuilder;

    public static abstract class TestBaseType {
        public abstract void countUp(int[] array, int length);
    }

    @Test
    public void illegalArgumentException_nullArrayIndexCodeBuilder() {
        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class,
            () -> new ArrayStoreInsn(null, literal(1))
        );

        assertThat(ex, hasProperty("message", is("The array index code builder cannot be null.")));
    }

    @Test
    public void illegalArgumentException_nullArrayValueCodeBuilder() {
        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class,
            () -> new ArrayStoreInsn(literal(1), null)
        );

        assertThat(ex, hasProperty("message", is("Array value builder cannot be null.")));
    }

    @Test
    public void illegalStateException_arrayTypeNotOnStack() {
        MethodBuildingContext context = new MethodBuildingContext(null, null, null, emptyList());
        context.pushStack(TypeDefinition.INT);

        ArrayStoreInsn insn = new ArrayStoreInsn(literal(1), literal(1));
        assertThrows(IllegalStateException.class, insn::build);
    }

    @Test
    public void illegalStateException_moreThanOneValueForArrayIndexStacked() {
        MethodBuildingContext context = new MethodBuildingContext(null, null, null, emptyList());
        context.pushStack(TypeDefinition.INT.getArrayType());

        doAnswer(i -> { //Mock array index code builder pushes two elements to the stack - causing the IllegalStateException
            context.pushStack(TypeDefinition.INT);
            return context.pushStack(TypeDefinition.INT);
        }).when(mockIndexBuilder).build();
        when(mockIndexBuilder.getFirstInStack()).thenReturn(mockIndexBuilder);

        ArrayStoreInsn insn = new ArrayStoreInsn(mockIndexBuilder, literal(1));
        IllegalStateException ex = assertThrows(IllegalStateException.class, insn::build);

        assertThat(ex, hasProperty("message", is("Expected 1 element to be stacked. Got 2 instead.")));
    }

    @Test
    public void illegalStateException_stackedArrayIndexTypeIsNotIntegerType() {
        MethodBuildingContext context = new MethodBuildingContext(null, null, null, emptyList());
        context.pushStack(TypeDefinition.INT.getArrayType());

        //Mock the array index builder to push 1 value to the stack of type "double" to trigger the other IllegalStateException case
        doAnswer(i -> context.pushStack(TypeDefinition.DOUBLE)).when(mockIndexBuilder).build();
        when(mockIndexBuilder.getFirstInStack()).thenReturn(mockIndexBuilder);

        ArrayStoreInsn insn = new ArrayStoreInsn(mockIndexBuilder, literal(1));
        IllegalStateException ex = assertThrows(IllegalStateException.class, insn::build);

        assertThat(ex, hasProperty("message", is("Expected an int value to be stacked. Found double instead.")));
    }

    @Test
    public void illegalStateException_moreThanOneNewValuePushedToStack_toBeStoredInArray() {
        MethodBuildingContext context = new MethodBuildingContext(null, null, null, emptyList());
        context.pushStack(TypeDefinition.INT.getArrayType());

        //Mocking - exactly 1 int value stacked for the array index
        doAnswer(i -> context.pushStack(TypeDefinition.INT)).when(mockIndexBuilder).build();
        when(mockIndexBuilder.getFirstInStack()).thenReturn(mockIndexBuilder);
        //Mocking - place two values onto the stack to be stored into the array to trigger the IllegalStateException
        doAnswer(i -> {
            context.pushStack(TypeDefinition.INT);
            return context.pushStack(TypeDefinition.INT);
        }).when(mockArrayValueBuilder).build();
        when(mockArrayValueBuilder.getFirstInStack()).thenReturn(mockArrayValueBuilder);

        ArrayStoreInsn insn = new ArrayStoreInsn(mockIndexBuilder, mockArrayValueBuilder);
        IllegalStateException ex = assertThrows(IllegalStateException.class, insn::build);

        assertThat(ex, hasProperty("message", is("Expected 1 element to be stacked. Got 2 instead.")));
    }

    @Test
    public void successfullyStoreValuesIntoArray() {
        AsmClassBuilder<TestBaseType> builder = new AsmClassBuilder<>(TestBaseType.class)
            .withMethod(method(publicOnly(), name("countUp"), parameters(p("array", int[].class), p("length", int.class)),
                setVar("index", literal(0)), //int index = 0;
                while_(getVar("index").lt(getVar("length"))).do_( //while(index < length) {...}
                    getVar("array").set(getVar("index"), getVar("index").add(literal(1))), //array[index] = index+1;
                    setVar("index", getVar("index").add(literal(1))) //index = index + 1;
                ),
                returnVoid()
            ));

        TestBaseType instance = builder.buildInstance();
        int[] testArray = new int[10];
        instance.countUp(testArray, testArray.length);

        assertThat(testArray, is(new int[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10}));
    }
}