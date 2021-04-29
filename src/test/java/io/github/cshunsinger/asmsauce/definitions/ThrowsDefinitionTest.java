package io.github.cshunsinger.asmsauce.definitions;

import io.github.cshunsinger.asmsauce.BaseUnitTest;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static io.github.cshunsinger.asmsauce.DefinitionBuilders.type;
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