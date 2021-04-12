package io.github.cshunsinger.asmsauce.definitions;

import io.github.cshunsinger.asmsauce.BaseUnitTest;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

class NameDefinitionTest extends BaseUnitTest {
    @Test
    public void illegalArgumentException_nullNameString() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> new NameDefinition(null));
        assertThat(ex, hasProperty("message", is("Method name cannot be null.")));
    }

    @Test
    public void successfullyCreateNewNameDefinition() {
        NameDefinition nameDefinition = new NameDefinition("My New Name");
        assertThat(nameDefinition, hasProperty("name", is("My New Name")));
    }
}