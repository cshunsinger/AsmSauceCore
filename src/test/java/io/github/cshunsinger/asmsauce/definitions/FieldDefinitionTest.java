package io.github.cshunsinger.asmsauce.definitions;

import io.github.cshunsinger.asmsauce.AsmClassBuilder;
import io.github.cshunsinger.asmsauce.ThisClass;
import io.github.cshunsinger.asmsauce.code.CodeBuilders;
import io.github.cshunsinger.asmsauce.BaseUnitTest;
import io.github.cshunsinger.asmsauce.DefinitionBuilders;
import org.junit.jupiter.api.Test;

import static io.github.cshunsinger.asmsauce.ConstructorNode.constructor;
import static io.github.cshunsinger.asmsauce.FieldNode.field;
import static io.github.cshunsinger.asmsauce.MethodNode.method;
import static io.github.cshunsinger.asmsauce.code.CodeBuilders.*;
import static io.github.cshunsinger.asmsauce.modifiers.AccessModifiers.privateOnly;
import static io.github.cshunsinger.asmsauce.modifiers.AccessModifiers.publicOnly;
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