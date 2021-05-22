package io.github.cshunsinger.asmsauce.code.array;

import io.github.cshunsinger.asmsauce.AsmClassBuilder;
import io.github.cshunsinger.asmsauce.BaseUnitTest;
import io.github.cshunsinger.asmsauce.MethodBuildingContext;
import io.github.cshunsinger.asmsauce.code.CodeInsnBuilderLike;
import io.github.cshunsinger.asmsauce.definitions.TypeDefinition;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import static io.github.cshunsinger.asmsauce.DefinitionBuilders.*;
import static io.github.cshunsinger.asmsauce.MethodNode.method;
import static io.github.cshunsinger.asmsauce.code.CodeBuilders.*;
import static io.github.cshunsinger.asmsauce.modifiers.AccessModifiers.publicOnly;
import static java.util.Collections.emptyList;
import static org.apache.commons.lang3.RandomUtils.nextInt;
import static org.junit.jupiter.api.Assertions.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.doAnswer;

public class InstantiateArrayInsnTest extends BaseUnitTest {
    @Mock
    private CodeInsnBuilderLike mockLengthBuilder;

    @AfterAll
    public static void afterAll() {
        MethodBuildingContext.reset();
    }

    /**
     * This test type is implemented to generate an array to test the bytecode being generated.
     */
    public static abstract class BaseTestType {
        public abstract Object[] createArray(int length);
    }

    @Test
    public void illegalArgumentException_nullArrayComponentType() {
        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class,
            () -> new InstantiateArrayInsn(null, literal(10))
        );

        assertThat(ex, hasProperty("message", is("Component type cannot be null.")));
    }

    @Test
    public void illegalArgumentException_nullArrayLengthCodeBuilder() {
        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class,
            () -> new InstantiateArrayInsn(TypeDefinition.BOOLEAN, null)
        );

        assertThat(ex, hasProperty("message", is("Array length instruction cannot be null.")));
    }

    @Test
    public void illegalStateException_moreThanOneElementStackedForArrayLengthValue() {
        //Mocking - mock code builder designed to "stack" 2 elements
        MethodBuildingContext context = new MethodBuildingContext(null, null, null, emptyList());
        doAnswer(i -> {
            context.pushStack(TypeDefinition.INT);
            context.pushStack(TypeDefinition.INT);
            return null;
        }).when(mockLengthBuilder).build();
        InstantiateArrayInsn testInsn = new InstantiateArrayInsn(TypeDefinition.INT, mockLengthBuilder);

        IllegalStateException ex = assertThrows(IllegalStateException.class, testInsn::build);
        assertThat(ex, hasProperty("message", is("Expected 1 element to be stacked. Got 2 instead.")));
    }

    @Test
    public void successfullyCreateNewArrayWithSpecifiedComponentTypeAndLength() {
        AsmClassBuilder<BaseTestType> builder = new AsmClassBuilder<>(BaseTestType.class)
            .withMethod(method(publicOnly(), name("createArray"), parameters(p("length", int.class)), type(Object[].class),
                returnValue(
                    newArray(Object.class, getVar("length"))
                )
            ));

        int randomLength = nextInt(1, 100);
        BaseTestType instance = builder.buildInstance();
        Object[] array = instance.createArray(randomLength);
        assertThat(array, allOf(
            notNullValue(),
            arrayWithSize(randomLength)
        ));
    }
}