package com.chunsinger.asmsauce.definitions;

import com.chunsinger.asmsauce.AsmClassBuilder;
import com.chunsinger.asmsauce.ThisClass;
import com.chunsinger.asmsauce.code.CodeBuilders;
import com.chunsinger.asmsauce.testing.BaseUnitTest;
import org.junit.jupiter.api.Test;

import static com.chunsinger.asmsauce.ConstructorNode.constructor;
import static com.chunsinger.asmsauce.DefinitionBuilders.*;
import static com.chunsinger.asmsauce.FieldNode.field;
import static com.chunsinger.asmsauce.MethodNode.method;
import static com.chunsinger.asmsauce.code.CodeBuilders.*;
import static com.chunsinger.asmsauce.modifiers.AccessModifiers.privateOnly;
import static com.chunsinger.asmsauce.modifiers.AccessModifiers.publicOnly;
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
                setStatic(ThisClass.class, "myImaginaryField", CodeBuilders.literalObj("My Test String")),
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
                this_().assignField("stringValue", CodeBuilders.literalObj("My Test String")),
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
}