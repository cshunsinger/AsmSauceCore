package io.github.cshunsinger.asmsauce.definitions;

import io.github.cshunsinger.asmsauce.AsmClassBuilder;
import io.github.cshunsinger.asmsauce.BaseUnitTest;
import io.github.cshunsinger.asmsauce.ThisClass;
import org.junit.jupiter.api.Test;

import static io.github.cshunsinger.asmsauce.ConstructorNode.constructor;
import static io.github.cshunsinger.asmsauce.DefinitionBuilders.*;
import static io.github.cshunsinger.asmsauce.FieldNode.field;
import static io.github.cshunsinger.asmsauce.MethodNode.method;
import static io.github.cshunsinger.asmsauce.code.CodeBuilders.*;
import static io.github.cshunsinger.asmsauce.modifiers.AccessModifiers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class FieldDefinitionTest extends BaseUnitTest {
    @SuppressWarnings("unused")
    public static abstract class FieldTestType {
        public abstract String getStringValue();
    }

    @Test
    public void illegalStateException_attemptingToAccessFieldInThisClassWhichDoesNotExist() {
        AsmClassBuilder<Object> builder = new AsmClassBuilder<>(Object.class)
            .withConstructor(constructor(publicOnly(), noParameters(),
                superConstructor(Object.class, noParameters()),
                setStatic(ThisClass.class, "myImaginaryField", literalObj("My Test String")),
                returnVoid()
            ));

        IllegalStateException ex = assertThrows(IllegalStateException.class, builder::build);
        assertThat(ex, hasProperty("message", startsWith("No field named myImaginaryField found accessible from class")));
    }

    @Test
    public void successfullyAccessOwnFieldImplicitly() {
        AsmClassBuilder<FieldTestType> builder = new AsmClassBuilder<>(FieldTestType.class)
            .withField(field(privateOnly(), type(String.class), name("stringValue")))
            .withConstructor(constructor(publicOnly(), noParameters(),
                superConstructor(FieldTestType.class, noParameters()),
                this_().assignField("stringValue", literalObj("My Test String")),
                returnVoid()
            ))
            .withMethod(method(publicOnly(), name("getStringValue"), noParameters(), type(String.class),
                returnValue(
                    this_().getField("stringValue")
                )
            ));

        FieldTestType instance = builder.buildInstance();
        assertThat(instance, hasProperty("stringValue", is("My Test String")));
    }

    @Test
    public void illegalArgumentException_nullFieldName() {
        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class,
            () -> new FieldDefinition(null, null, null, null)
        );

        assertThat(ex, hasProperty("message", is("Field name cannot be null.")));
    }

    @Test
    public void illegalArgumentException_nullFieldOwnerForStaticField() {
        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class,
            () -> new FieldDefinition(publicStatic(), null, name("SomeName"), null)
        );

        assertThat(ex, hasProperty("message", is("Field owner cannot be null when referring to a static field.")));
    }

    @Test
    public void illegalArgumentException_fieldOwnerIsIllegal() {
        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class,
            () -> new FieldDefinition(null, type(int.class), name("SomeName"), null)
        );

        assertThat(ex, hasProperty("message", is("Field owner cannot be void, primitive, or an array type.")));
    }
}