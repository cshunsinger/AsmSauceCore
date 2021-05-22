package io.github.cshunsinger.asmsauce.code.array;

import io.github.cshunsinger.asmsauce.AsmClassBuilder;
import io.github.cshunsinger.asmsauce.BaseUnitTest;
import io.github.cshunsinger.asmsauce.MethodBuildingContext;
import io.github.cshunsinger.asmsauce.code.CodeInsnBuilderLike;
import io.github.cshunsinger.asmsauce.definitions.TypeDefinition;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import static io.github.cshunsinger.asmsauce.DefinitionBuilders.*;
import static io.github.cshunsinger.asmsauce.MethodNode.method;
import static io.github.cshunsinger.asmsauce.code.CodeBuilders.*;
import static io.github.cshunsinger.asmsauce.modifiers.AccessModifiers.publicOnly;
import static java.util.Collections.emptyList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

public class ArrayLoadInsnTest extends BaseUnitTest {
    @Mock
    private CodeInsnBuilderLike mockIndexBuilder;

    public static abstract class TestBaseType {
        public abstract int addAll(int[] array, int length);
    }

    @Test
    public void illegalArgumentException_nullArrayIndexCodeBuilder() {
        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class,
            () -> new ArrayLoadInsn(null)
        );

        assertThat(ex, hasProperty("message", is("The array index code builder cannot be null.")));
    }

    @Test
    public void illegalStateException_arrayTypeNotOnStack() {
        MethodBuildingContext context = new MethodBuildingContext(null, null, null, emptyList());
        context.pushStack(TypeDefinition.INT);

        ArrayLoadInsn insn = new ArrayLoadInsn(literal(1));
        assertThrows(IllegalStateException.class, insn::build);
    }

    @Test
    public void illegalStateException_moreThanOneValueForArrayIndexStacked() {
        MethodBuildingContext context = new MethodBuildingContext(null, null, null, emptyList());
        context.pushStack(TypeDefinition.INT.getArrayType());

        doAnswer(i -> { //Mock array index code builder pushes two elements to the stack - causing the IllegalStateException
            context.pushStack(TypeDefinition.INT);
            context.pushStack(TypeDefinition.INT);
            return null;
        }).when(mockIndexBuilder).build();
        when(mockIndexBuilder.getFirstInStack()).thenReturn(mockIndexBuilder);

        ArrayLoadInsn insn = new ArrayLoadInsn(mockIndexBuilder);
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

        ArrayLoadInsn insn = new ArrayLoadInsn(mockIndexBuilder);
        IllegalStateException ex = assertThrows(IllegalStateException.class, insn::build);

        assertThat(ex, hasProperty("message", is("Expected an int value to be stacked. Found double instead.")));
    }

    @Test
    public void successfullyLoadValuesFromArray() {
        AsmClassBuilder<TestBaseType> builder = new AsmClassBuilder<>(TestBaseType.class)
            .withMethod(method(publicOnly(), name("addAll"), parameters(p("array", int[].class), p("length", int.class)), type(int.class),
                setVar("index", literal(0)), //The array index being accessed
                setVar("sum", literal(0)), //Sum of all array values
                while_(getVar("index").lt(getVar("length"))).do_(
                    setVar("value", getVar("array").get(getVar("index"))), //value = array[index];
                    setVar("sum", getVar("sum").add(getVar("value"))), //sum = sum + value;
                    setVar("index", getVar("index").add(literal(1))) //index = index + 1;
                ),
                returnValue(getVar("sum")) //return sum;
            ));

        TestBaseType instance = builder.buildInstance();
        int[] testArray = new int[] { 420, 69, 684, 487, 200, 50, 2387, 4704 };
        int testArraySum = instance.addAll(testArray, testArray.length);
        assertThat(testArraySum, is(9001));
    }
}