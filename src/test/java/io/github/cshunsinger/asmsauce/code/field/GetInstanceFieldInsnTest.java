package io.github.cshunsinger.asmsauce.code.field;

import io.github.cshunsinger.asmsauce.AsmClassBuilder;
import io.github.cshunsinger.asmsauce.MethodBuildingContext;
import io.github.cshunsinger.asmsauce.definitions.CompleteFieldDefinition;
import io.github.cshunsinger.asmsauce.BaseUnitTest;
import lombok.Getter;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import static io.github.cshunsinger.asmsauce.ConstructorNode.constructor;
import static io.github.cshunsinger.asmsauce.DefinitionBuilders.*;
import static io.github.cshunsinger.asmsauce.code.CodeBuilders.*;
import static io.github.cshunsinger.asmsauce.modifiers.AccessModifiers.publicOnly;
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
        new MethodBuildingContext(null, null, null, emptyList());

        GetInstanceFieldInsn insn = new GetInstanceFieldInsn(mockFieldDefinition);
        when(mockFieldDefinition.getFieldName()).thenReturn(name("testField"));

        IllegalStateException ex = assertThrows(IllegalStateException.class, insn::build);
        assertThat(ex, hasProperty("message", is("No instance on stack to access field 'testField' from.")));
    }

    @Test
    public void illegalStateException_attemptingToGetFieldFromPrimitiveTypeOnStack() {
        AsmClassBuilder<Object> builder = new AsmClassBuilder<>(Object.class)
            .withConstructor(constructor(publicOnly(), noParameters(),
                superConstructor(Object.class, noParameters()),
                invokeStatic(Math.class, name("abs"), parameters(int.class), type(int.class),
                    literal(123)
                ).getField(type(Object.class), name("fieldName"), type(int.class)),
                returnVoid()
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
            .withConstructor(constructor(publicOnly(), noParameters(),
                superConstructor(TestType.class, noParameters()),
                this_().getField("privateString"),
                returnVoid()
            ));

        IllegalStateException ex = assertThrows(IllegalStateException.class, builder::build);
        assertThat(ex, hasProperty("message", startsWith("No field named privateString found accessible from class %s".formatted(TestType.class.getName()))));
    }

    @Test
    public void illegalStateException_attemptingToAccessFieldThatDoesNotExist() {
        AsmClassBuilder<Object> builder = new AsmClassBuilder<>(Object.class)
            .withConstructor(constructor(publicOnly(), noParameters(),
                superConstructor(Object.class, noParameters()),
                literalObj("Test String").getField("SomeFieldThatDoesNotExist"),
                returnVoid()
            ));

        IllegalStateException ex = assertThrows(IllegalStateException.class, builder::build);
        assertThat(ex, hasProperty("message", startsWith("No field named SomeFieldThatDoesNotExist found accessible from class java.lang.Object")));
    }

    @Test
    public void successfullyAccessFieldWithImpliedOwnerAndImpliedType() {
        AsmClassBuilder<TestType> builder = new AsmClassBuilder<>(TestType.class)
            .withConstructor(constructor(publicOnly(), noParameters(), //constructor() {...}
                superConstructor(TestType.class, noParameters()), //super();
                this_().assignField("protectedString", literalObj("Some Test String Value")), //this.protectedString = "Some Test String Value";
                this_().assignField("otherProtectedString", this_().getField("protectedString")), //this.otherProtectedString = this.protectedString;
                returnVoid() //return;
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