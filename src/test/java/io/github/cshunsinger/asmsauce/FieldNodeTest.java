package io.github.cshunsinger.asmsauce;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import io.github.cshunsinger.asmsauce.definitions.CompleteFieldDefinition;
import io.github.cshunsinger.asmsauce.code.CodeBuilders;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.util.List;

import static io.github.cshunsinger.asmsauce.ConstructorNode.constructor;
import static io.github.cshunsinger.asmsauce.FieldNode.field;
import static io.github.cshunsinger.asmsauce.MethodNode.method;
import static io.github.cshunsinger.asmsauce.modifiers.AccessModifiers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FieldNodeTest extends BaseUnitTest {
    @Mock
    private ClassBuildingContext mockClassBuildingContext;
    @Mock
    private ClassWriter mockClassWriter;
    @Mock
    private FieldVisitor mockFieldVisitor;

    @Test
    public void illegalArgumentException_nullFieldDefinition() {
        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class,
            () -> new FieldNode(null, null)
        );
        assertThat(ex, hasProperty("message", is("Field definition cannot be null.")));
    }

    @Test
    public void illegalArgumentException_attemptingToSupplyInitialValueForInstanceField() {
        CompleteFieldDefinition fieldDefinition = new CompleteFieldDefinition(
            publicOnly(),
            DefinitionBuilders.type(ThisClass.class),
            DefinitionBuilders.name("fieldName"),
            DefinitionBuilders.type(String.class)
        );

        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class,
            () -> new FieldNode(fieldDefinition, "Initial Value")
        );
        assertThat(ex, hasProperty("message", is("Only static fields can be given an initial value.")));
    }

    @Test
    public void illegalArgumentException_attemptingToUseForbiddenTypeForInitialStaticFieldValue() {
        CompleteFieldDefinition fieldDefinition = new CompleteFieldDefinition(
            publicStaticFinal(),
            DefinitionBuilders.type(ThisClass.class),
            DefinitionBuilders.name("fieldName"),
            DefinitionBuilders.type(Object.class)
        );

        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class,
            () -> new FieldNode(fieldDefinition, new Object())
        );
        assertThat(ex, hasProperty("message", is(
            "An initial value of type %s was provided. The initial value of a static field must be one of: Integer, Float, Long, Double, String"
                .formatted(Object.class.getName())
        )));
    }

    @Test
    public void illegalArgumentException_attemptingToInitializeStaticFieldWithValueOfWrongType() {
        CompleteFieldDefinition fieldDefinition = new CompleteFieldDefinition(
            publicStaticFinal(),
            DefinitionBuilders.type(ThisClass.class),
            DefinitionBuilders.name("fieldName"),
            DefinitionBuilders.type(String.class)
        );

        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class,
            () -> new FieldNode(fieldDefinition, 12345)
        );
        assertThat(ex, hasProperty("message", is(
            "Initial value 12345 of type java.lang.Integer is not assignable to this field type: java.lang.String"
        )));
    }

    @Test
    public void illegalArgumentException_attemptingToInitializeStaticPrimitiveFieldWithNull() {
        CompleteFieldDefinition fieldDefinition = new CompleteFieldDefinition(
            publicStaticFinal(),
            DefinitionBuilders.type(ThisClass.class),
            DefinitionBuilders.name("fieldName"),
            DefinitionBuilders.type(int.class)
        );

        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class,
            () -> new FieldNode(fieldDefinition, null)
        );
        assertThat(ex, hasProperty("message", is(
            "The initial value of a static primitive field cannot be null."
        )));
    }

    @Test
    public void successfullyInstantiateNewFieldNodeWithInitialValues() {
        //The only requirements here are that no exceptions are thrown
        field(publicStatic(), DefinitionBuilders.type(String.class), DefinitionBuilders.name("myField"));
        field(publicStatic(), DefinitionBuilders.name("intField"), 123);
        field(publicStatic(), DefinitionBuilders.name("longField"), 123L);
        field(publicStatic(), DefinitionBuilders.name("floatField"), 123f);
        field(publicStatic(), DefinitionBuilders.name("doubleField"), 123.0);
        field(publicStatic(), DefinitionBuilders.name("stringField"), "My String");
    }

    public interface SelfContainerInterface {
        SelfContainerInterface getSelf();
    }

    @Test
    public void allowGeneratedClassToStoreInstanceOfItself() {
        AsmClassBuilder<SelfContainerInterface> builder = new AsmClassBuilder<>(SelfContainerInterface.class, Object.class, List.of(SelfContainerInterface.class), publicOnly())
            .withField(field(privateOnly().withFinal(), DefinitionBuilders.type(ThisClass.class), DefinitionBuilders.name("self")))
            .withConstructor(constructor(publicOnly(), DefinitionBuilders.noParameters(),
                CodeBuilders.superConstructor(Object.class, DefinitionBuilders.noParameters()),
                CodeBuilders.this_().assignField("self", CodeBuilders.this_()), //this.self = this;
                CodeBuilders.returnVoid()
            ))
            .withMethod(method(publicOnly(), DefinitionBuilders.name("getSelf"), DefinitionBuilders.noParameters(), DefinitionBuilders.type(SelfContainerInterface.class),
                CodeBuilders.returnValue(
                    CodeBuilders.this_().getField("self")
                )
            ));

        SelfContainerInterface instance = builder.buildInstance();
        assertThat(instance.getSelf(), is(instance));
    }
}