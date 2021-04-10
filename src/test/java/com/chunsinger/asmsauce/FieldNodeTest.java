package com.chunsinger.asmsauce;

import aj.org.objectweb.asm.ClassWriter;
import aj.org.objectweb.asm.FieldVisitor;
import com.chunsinger.asmsauce.definitions.CompleteFieldDefinition;
import com.chunsinger.asmsauce.testing.BaseUnitTest;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.util.List;

import static com.chunsinger.asmsauce.ConstructorNode.constructor;
import static com.chunsinger.asmsauce.DefinitionBuilders.*;
import static com.chunsinger.asmsauce.FieldNode.field;
import static com.chunsinger.asmsauce.MethodNode.method;
import static com.chunsinger.asmsauce.code.CodeBuilders.*;
import static com.chunsinger.asmsauce.modifiers.AccessModifiers.*;
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
            type(ThisClass.class),
            name("fieldName"),
            type(String.class)
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
            type(ThisClass.class),
            name("fieldName"),
            type(Object.class)
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
            type(ThisClass.class),
            name("fieldName"),
            type(String.class)
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
            type(ThisClass.class),
            name("fieldName"),
            type(int.class)
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
        field(publicStatic(), type(String.class), name("myField"));
        field(publicStatic(), name("intField"), 123);
        field(publicStatic(), name("longField"), 123L);
        field(publicStatic(), name("floatField"), 123f);
        field(publicStatic(), name("doubleField"), 123.0);
        field(publicStatic(), name("stringField"), "My String");
    }

    public interface SelfContainerInterface {
        SelfContainerInterface getSelf();
    }

    @Test
    public void allowGeneratedClassToStoreInstanceOfItself() {
        AsmClassBuilder<SelfContainerInterface> builder = new AsmClassBuilder<>(SelfContainerInterface.class, Object.class, List.of(SelfContainerInterface.class), publicOnly())
            .withField(field(privateOnly().withFinal(), type(ThisClass.class), name("self")))
            .withConstructor(constructor(publicOnly(), noParameters(),
                superConstructor(Object.class, noParameters()),
                thisInstance().assignField("self", thisInstance()), //this.self = this;
                returnVoid()
            ))
            .withMethod(method(publicOnly(), name("getSelf"), noParameters(), type(SelfContainerInterface.class),
                returnValue(
                    thisInstance().getField("self")
                )
            ));

        SelfContainerInterface instance = builder.buildInstance();
        assertThat(instance.getSelf(), is(instance));
    }
}