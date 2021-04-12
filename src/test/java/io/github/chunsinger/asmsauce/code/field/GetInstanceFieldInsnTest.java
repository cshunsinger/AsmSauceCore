package io.github.chunsinger.asmsauce.code.field;

import io.github.chunsinger.asmsauce.AsmClassBuilder;
import io.github.chunsinger.asmsauce.MethodBuildingContext;
import io.github.chunsinger.asmsauce.code.CodeBuilders;
import io.github.chunsinger.asmsauce.definitions.CompleteFieldDefinition;
import io.github.chunsinger.asmsauce.testing.BaseUnitTest;
import io.github.chunsinger.asmsauce.DefinitionBuilders;
import lombok.Getter;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import static io.github.chunsinger.asmsauce.ConstructorNode.constructor;
import static io.github.chunsinger.asmsauce.modifiers.AccessModifiers.publicOnly;
import static java.util.Collections.emptyList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

class GetInstanceFieldInsnTest extends BaseUnitTest {
    @Mock
    private CompleteFieldDefinition mockFieldDefinition;

    @Test
    public void illegalArgumentException_nullFieldDefinition() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> new GetInstanceFieldInsn(null));
        assertThat(ex, hasProperty("message", is("Field definition cannot be null.")));
    }

    @Test
    public void illegalStateException_noInstanceOnStackToAccessFieldFrom() {
        MethodBuildingContext context = new MethodBuildingContext(null, null, null, emptyList());

        GetInstanceFieldInsn insn = new GetInstanceFieldInsn(mockFieldDefinition);
        when(mockFieldDefinition.getFieldName()).thenReturn(DefinitionBuilders.name("testField"));

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> insn.build(context));
        assertThat(ex, hasProperty("message", is("No instance on stack to access field 'testField' from.")));
    }

    @Test
    public void illegalStateException_attemptingToGetFieldFromPrimitiveTypeOnStack() {
        AsmClassBuilder<Object> builder = new AsmClassBuilder<>(Object.class)
            .withConstructor(constructor(publicOnly(), DefinitionBuilders.noParameters(),
                CodeBuilders.superConstructor(Object.class, DefinitionBuilders.noParameters()),
                CodeBuilders.invokeStatic(Math.class, DefinitionBuilders.name("abs"), DefinitionBuilders.parameters(int.class), DefinitionBuilders.type(int.class),
                    CodeBuilders.literal(123)
                ).getField(DefinitionBuilders.type(Object.class), DefinitionBuilders.name("fieldName"), DefinitionBuilders.type(int.class)),
                CodeBuilders.returnVoid()
            ));

        IllegalStateException ex = assertThrows(IllegalStateException.class, builder::build);
        assertThat(ex, hasProperty("message", is("Cannot access a field from primitive type 'int'.")));
    }

    public static class TestType {
        @SuppressWarnings("unused")
        private String privateString;

        @Getter
        protected String protectedString;
        @Getter
        protected String otherProtectedString;
    }

    @Test
    public void illegalStateException_attemptingToAccessInaccessibleField() {
        AsmClassBuilder<TestType> builder = new AsmClassBuilder<>(TestType.class)
            .withConstructor(constructor(publicOnly(), DefinitionBuilders.noParameters(),
                CodeBuilders.superConstructor(TestType.class, DefinitionBuilders.noParameters()),
                CodeBuilders.this_().getField("privateString"),
                CodeBuilders.returnVoid()
            ));

        IllegalStateException ex = assertThrows(IllegalStateException.class, builder::build);
        assertThat(ex, hasProperty("message", startsWith("No field named privateString found accessible from class %s".formatted(TestType.class.getName()))));
    }

    @Test
    public void illegalStateException_attemptingToAccessFieldThatDoesNotExist() {
        AsmClassBuilder<Object> builder = new AsmClassBuilder<>(Object.class)
            .withConstructor(constructor(publicOnly(), DefinitionBuilders.noParameters(),
                CodeBuilders.superConstructor(Object.class, DefinitionBuilders.noParameters()),
                CodeBuilders.literalObj("Test String").getField("SomeFieldThatDoesNotExist"),
                CodeBuilders.returnVoid()
            ));

        IllegalStateException ex = assertThrows(IllegalStateException.class, builder::build);
        assertThat(ex, hasProperty("message", startsWith("No field named SomeFieldThatDoesNotExist found accessible from class java.lang.Object")));
    }

    @Test
    public void successfullyAccessFieldWithImpliedOwnerAndImpliedType() {
        AsmClassBuilder<TestType> builder = new AsmClassBuilder<>(TestType.class)
            .withConstructor(constructor(publicOnly(), DefinitionBuilders.noParameters(), //constructor() {...}
                CodeBuilders.superConstructor(TestType.class, DefinitionBuilders.noParameters()), //super();
                CodeBuilders.this_().assignField("protectedString", CodeBuilders.literalObj("Some Test String Value")), //this.protectedString = "Some Test String Value";
                CodeBuilders.this_().assignField("otherProtectedString", CodeBuilders.this_().getField("protectedString")), //this.otherProtectedString = this.protectedString;
                CodeBuilders.returnVoid() //return;
            ));

        TestType instance = builder.buildInstance();
        assertThat(instance, allOf(
            hasProperty("protectedString", is("Some Test String Value")),
            hasProperty("otherProtectedString", is("Some Test String Value"))
        ));
    }

    //TODO: Add test coverage for successfully retrieving the .length field of an array type.

    /*
     * Test coverage for successfully getting and setting fields is located in the AssignInstanceFieldInsnTest
     * as well as in some other general test cases.
     */
}