package io.github.chunsinger.asmsauce.definitions;

import io.github.chunsinger.asmsauce.AsmClassBuilder;
import io.github.chunsinger.asmsauce.ThisClass;
import io.github.chunsinger.asmsauce.code.CodeBuilders;
import io.github.chunsinger.asmsauce.testing.BaseUnitTest;
import io.github.chunsinger.asmsauce.DefinitionBuilders;
import org.junit.jupiter.api.Test;

import static io.github.chunsinger.asmsauce.ConstructorNode.constructor;
import static io.github.chunsinger.asmsauce.FieldNode.field;
import static io.github.chunsinger.asmsauce.MethodNode.method;
import static io.github.chunsinger.asmsauce.code.CodeBuilders.*;
import static io.github.chunsinger.asmsauce.modifiers.AccessModifiers.privateOnly;
import static io.github.chunsinger.asmsauce.modifiers.AccessModifiers.publicOnly;
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
            .withConstructor(constructor(publicOnly(), DefinitionBuilders.noParameters(),
                superConstructor(Object.class, DefinitionBuilders.noParameters()),
                setStatic(ThisClass.class, "myImaginaryField", CodeBuilders.literalObj("My Test String")),
                returnVoid()
            ));

        IllegalStateException ex = assertThrows(IllegalStateException.class, builder::build);
        assertThat(ex, hasProperty("message", startsWith("No field named myImaginaryField found accessible from class")));
    }

    @Test
    public void successfullyAccessOwnFieldImplicitly() {
        AsmClassBuilder<FieldTestType> builder = new AsmClassBuilder<>(FieldTestType.class)
            .withField(field(privateOnly(), DefinitionBuilders.type(String.class), DefinitionBuilders.name("stringValue")))
            .withConstructor(constructor(publicOnly(), DefinitionBuilders.noParameters(),
                superConstructor(FieldTestType.class, DefinitionBuilders.noParameters()),
                this_().assignField("stringValue", CodeBuilders.literalObj("My Test String")),
                returnVoid()
            ))
            .withMethod(method(publicOnly(), DefinitionBuilders.name("getStringValue"), DefinitionBuilders.noParameters(), DefinitionBuilders.type(String.class),
                returnValue(
                    this_().getField("stringValue")
                )
            ));

        FieldTestType instance = builder.buildInstance();
        assertThat(instance, hasProperty("stringValue", is("My Test String")));
    }
}