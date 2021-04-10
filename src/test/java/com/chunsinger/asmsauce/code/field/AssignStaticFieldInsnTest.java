package com.chunsinger.asmsauce.code.field;

import com.chunsinger.asmsauce.AsmClassBuilder;
import com.chunsinger.asmsauce.MethodBuildingContext;
import com.chunsinger.asmsauce.code.CodeInsnBuilderLike;
import com.chunsinger.asmsauce.definitions.CompleteFieldDefinition;
import com.chunsinger.asmsauce.definitions.TypeDefinition;
import com.chunsinger.asmsauce.testing.BaseUnitTest;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import static com.chunsinger.asmsauce.ConstructorNode.constructor;
import static com.chunsinger.asmsauce.DefinitionBuilders.*;
import static com.chunsinger.asmsauce.code.CodeBuilders.*;
import static com.chunsinger.asmsauce.modifiers.AccessModifiers.publicOnly;
import static com.chunsinger.asmsauce.modifiers.AccessModifiers.publicStatic;
import static java.util.Collections.emptyList;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

class AssignStaticFieldInsnTest extends BaseUnitTest {
    @Mock
    private CompleteFieldDefinition mockFieldDefinition;

    @Mock
    private CodeInsnBuilderLike mockCodeBuilder;

    @Test
    public void illegalArgumentException_nullFieldDefinition() {
        test_illegalArgumentException_base(null, mockCodeBuilder, "Field definition cannot be null.");
    }

    @Test
    public void illegalArgumentException_nullCodeBuilder() {
        test_illegalArgumentException_base(mockFieldDefinition, null, "Value builder cannot be null.");
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void illegalArgumentException_instanceField() {
        when(mockFieldDefinition.getAccessModifiers()).thenReturn(publicOnly());
        when(mockFieldDefinition.getFieldType()).thenReturn((TypeDefinition)type(String.class));
        when(mockFieldDefinition.getFieldName()).thenReturn(name("FieldName"));

        test_illegalArgumentException_base(mockFieldDefinition, mockCodeBuilder,
            "This instruction only handles assigning static fields. Field 'java.lang.String.FieldName' is not static."
        );
    }

    public void test_illegalArgumentException_base(CompleteFieldDefinition fieldDefinition,
                                                   CodeInsnBuilderLike codeBuilder,
                                                   String exceptionMessage) {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> new AssignStaticFieldInsn(fieldDefinition, codeBuilder));
        assertThat(ex, hasProperty("message", is(exceptionMessage)));
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void illegalStateException_codeBuilderDoesNotPlaceExactlyOneElementOntoTheStack() {
        MethodBuildingContext methodContext = new MethodBuildingContext(null, null, null, emptyList());
        methodContext.pushStack(type(Object.class));

        when(mockFieldDefinition.getAccessModifiers()).thenReturn(publicStatic());
        when(mockFieldDefinition.getFieldOwner()).thenReturn((TypeDefinition)type(String.class));
        when(mockCodeBuilder.getFirstInStack()).thenReturn(mockCodeBuilder);
        doAnswer(i -> null).when(mockCodeBuilder).build(methodContext);

        AssignStaticFieldInsn op = new AssignStaticFieldInsn(mockFieldDefinition, mockCodeBuilder);
        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> op.build(methodContext));
        assertThat(ex, hasProperty("message", is("Expected 1 element placed onto the stack. Instead 0 elements were added/removed.")));
    }

    @Test
    public void illegalStateException_attemptingToAccessStaticFieldOfAnArray() {
        AsmClassBuilder<Object> builder = new AsmClassBuilder<>(Object.class)
            .withConstructor(constructor(publicOnly(), noParameters(),
                superConstructor(type(Object.class), noParameters()),
                setStaticField(type(int[].class), name("length"), type(int.class),
                    stackValue(123)
                ),
                returnVoid()
            ));

        IllegalStateException ex = assertThrows(IllegalStateException.class, builder::build);
        assertThat(ex, hasProperty("message", is("Cannot access static field from array type int[].")));
    }

    public static class TestStaticFieldsModifiable {
        public static String MODIFIABLE_STRING = null;
    }

    @Test
    public void successfullyAssignStaticField() {
        AsmClassBuilder<TestStaticFieldsModifiable> builder = new AsmClassBuilder<>(TestStaticFieldsModifiable.class)
            .withConstructor(constructor(publicOnly(), parameters(String.class),
                superConstructor(type(TestStaticFieldsModifiable.class), noParameters()),
                setStaticField(type(TestStaticFieldsModifiable.class), name("MODIFIABLE_STRING"), type(String.class),
                    localVar(1)
                ),
                returnVoid()
            ));

        String testString = randomAlphanumeric(25);
        builder.buildInstance(testString); //the constructor sets the static field
        assertThat(TestStaticFieldsModifiable.MODIFIABLE_STRING, is(testString));
    }

    @Test
    public void successfullyAssignStaticField_implicitFieldData() {
        AsmClassBuilder<TestStaticFieldsModifiable> builder = new AsmClassBuilder<>(TestStaticFieldsModifiable.class)
            .withConstructor(constructor(publicOnly(), parameters(String.class),
                superConstructor(type(TestStaticFieldsModifiable.class), noParameters()),
                setStaticField(TestStaticFieldsModifiable.class, "MODIFIABLE_STRING",
                    localVar(1)
                ),
                returnVoid()
            ));

        String testString = randomAlphanumeric(25);
        builder.buildInstance(testString); //the constructor sets the static field
        assertThat(TestStaticFieldsModifiable.MODIFIABLE_STRING, is(testString));
    }
}