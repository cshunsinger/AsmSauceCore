package io.github.chunsinger.asmsauce.definitions;

import io.github.chunsinger.asmsauce.testing.BaseUnitTest;
import io.github.chunsinger.asmsauce.DefinitionBuilders;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

class ThrowsDefinitionTest extends BaseUnitTest {
    @Test
    public void returnNull_whenThereAreZeroExceptionTypesDefined() {
        assertThat(new ThrowsDefinition(), hasProperty("jvmExceptions", nullValue()));
    }

    @Test
    public void returnStringArrayOfJvmTypesForDefinedExceptionTypes() {
        ThrowsDefinition throwsDefinition = new ThrowsDefinition(DefinitionBuilders.type(IOException.class), DefinitionBuilders.type(Exception.class));
        assertThat(throwsDefinition, hasProperty("jvmExceptions", arrayContaining(
            DefinitionBuilders.type(IOException.class).getJvmTypeName(),
            DefinitionBuilders.type(Exception.class).getJvmTypeName()
        )));
    }
}