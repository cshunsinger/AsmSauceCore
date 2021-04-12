package io.github.cshunsinger.asmsauce.definitions;

import io.github.cshunsinger.asmsauce.BaseUnitTest;
import io.github.cshunsinger.asmsauce.DefinitionBuilders;
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