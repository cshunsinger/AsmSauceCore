package com.chunsinger.asmsauce.code.field;

import com.chunsinger.asmsauce.AsmClassBuilder;
import com.chunsinger.asmsauce.MethodBuildingContext;
import com.chunsinger.asmsauce.definitions.CompleteFieldDefinition;
import com.chunsinger.asmsauce.definitions.TypeDefinition;
import com.chunsinger.asmsauce.testing.BaseUnitTest;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.util.ArrayList;

import static com.chunsinger.asmsauce.ConstructorNode.constructor;
import static com.chunsinger.asmsauce.DefinitionBuilders.*;
import static com.chunsinger.asmsauce.MethodNode.method;
import static com.chunsinger.asmsauce.code.CodeBuilders.*;
import static com.chunsinger.asmsauce.modifiers.AccessModifiers.publicOnly;
import static com.chunsinger.asmsauce.modifiers.AccessModifiers.publicStatic;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

class GetStaticFieldInsnTest extends BaseUnitTest {
    @Mock
    private CompleteFieldDefinition mockFieldDefinition;

    @Test
    public void illegalArgumentException_nullFieldDefinition() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> new GetStaticFieldInsn(null));
        assertThat(ex, hasProperty("message", is("Field definition cannot be null.")));
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void illegalArgumentException_nonStaticField() {
        when(mockFieldDefinition.getFieldName()).thenReturn(name("testField"));
        when(mockFieldDefinition.getAccessModifiers()).thenReturn(publicOnly());
        when(mockFieldDefinition.getFieldType()).thenReturn((TypeDefinition)type(String.class));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> new GetStaticFieldInsn(mockFieldDefinition));
        assertThat(ex, hasProperty("message", is("This instruction only handles getting static fields. Field 'java.lang.String.testField' is not static.")));
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void illegalStateException_attemptingToGetStaticFieldFromPrimitiveType() {
        when(mockFieldDefinition.getAccessModifiers()).thenReturn(publicStatic());
        when(mockFieldDefinition.getFieldOwner()).thenReturn((TypeDefinition)type(int.class));
        when(mockFieldDefinition.completeDefinition(null, type(int.class))).thenReturn(mockFieldDefinition);

        GetStaticFieldInsn insn = new GetStaticFieldInsn(mockFieldDefinition);
        IllegalStateException ex = assertThrows(
            IllegalStateException.class,
            () -> insn.build(new MethodBuildingContext(null, null, null, new ArrayList<>()))
        );
        assertThat(ex, hasProperty("message", is("Cannot access a field from primitive type 'int'.")));
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void illegalStateException_attemptingToGetStaticFieldFromArrayType() {
        when(mockFieldDefinition.getAccessModifiers()).thenReturn(publicStatic());
        when(mockFieldDefinition.getFieldOwner()).thenReturn((TypeDefinition)type(int[].class));

        GetStaticFieldInsn insn = new GetStaticFieldInsn(mockFieldDefinition);
        IllegalStateException ex = assertThrows(
            IllegalStateException.class,
            () -> insn.build(new MethodBuildingContext(null, null, null, new ArrayList<>()))
        );
        assertThat(ex, hasProperty("message", is("Cannot access static field from array type int[].")));
    }

    public static class TestStaticFieldClass {
        @SuppressWarnings("unused")
        private static final String INACCESSIBLE_STRING = "Inaccessible String";
        public static final String ACCESSIBLE_STRING = "Accessible String";
    }

    @SuppressWarnings("unused")
    public interface StaticFieldsTestInterface {
        String getStringValue();
    }

    public interface MoreStaticFieldsInterface {
        String OUTSIDE_STRING = "Static Field In Another Interface.";
    }

    @Test
    public void successfullyAccessStaticField() {
        AsmClassBuilder<StaticFieldsTestInterface> builder = new AsmClassBuilder<>(StaticFieldsTestInterface.class, Object.class, singletonList(StaticFieldsTestInterface.class), publicOnly())
            .withConstructor(constructor(publicOnly(), noParameters(),
                superConstructor(Object.class, noParameters()),
                returnVoid()
            ))
            .withMethod(method(publicOnly(), name("getStringValue"), noParameters(), type(String.class),
                returnValue(
                    getStatic(type(TestStaticFieldClass.class), name("ACCESSIBLE_STRING"), type(String.class))
                )
            ));

        StaticFieldsTestInterface instance = builder.buildInstance();
        assertThat(instance, hasProperty("stringValue", is(TestStaticFieldClass.ACCESSIBLE_STRING)));
    }

    @Test
    public void accessStaticFieldWithImplicitFieldInformation() {
        AsmClassBuilder<StaticFieldsTestInterface> builder = new AsmClassBuilder<>(StaticFieldsTestInterface.class, Object.class, singletonList(StaticFieldsTestInterface.class), publicOnly())
            .withConstructor(constructor(publicOnly(), noParameters(),
                superConstructor(Object.class, noParameters()),
                returnVoid()
            ))
            .withMethod(method(publicOnly(), name("getStringValue"), noParameters(), type(String.class),
                returnValue(
                    getStatic(MoreStaticFieldsInterface.class, "OUTSIDE_STRING")
                )
            ));

        StaticFieldsTestInterface instance = builder.buildInstance();
        assertThat(instance, hasProperty("stringValue", is(MoreStaticFieldsInterface.OUTSIDE_STRING)));
    }
}