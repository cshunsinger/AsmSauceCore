package com.chunsinger.asmsauce.definitions;

import com.chunsinger.asmsauce.testing.BaseUnitTest;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static com.chunsinger.asmsauce.DefinitionBuilders.type;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

class ThrowsDefinitionTest extends BaseUnitTest {
    @Test
    public void returnNull_whenThereAreZeroExceptionTypesDefined() {
        assertThat(new ThrowsDefinition(), hasProperty("jvmExceptions", nullValue()));
    }

    @Test
    public void returnStringArrayOfJvmTypesForDefinedExceptionTypes() {
        ThrowsDefinition throwsDefinition = new ThrowsDefinition(type(IOException.class), type(Exception.class));
        assertThat(throwsDefinition, hasProperty("jvmExceptions", arrayContaining(
            type(IOException.class).getJvmTypeName(),
            type(Exception.class).getJvmTypeName()
        )));
    }
}