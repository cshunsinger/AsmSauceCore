package io.github.cshunsinger.asmsauce.definitions;

import io.github.cshunsinger.asmsauce.BaseUnitTest;
import org.junit.jupiter.api.Test;

import static io.github.cshunsinger.asmsauce.DefinitionBuilders.name;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.*;

class NameDefinitionTest extends BaseUnitTest {
    @Test
    public void equals_shouldBeEqualToItself() {
        NameDefinition nameDefinition = name("Test Name");
        assertEquals(nameDefinition, nameDefinition);
    }

    @Test
    public void equals_shouldNotBeEqualToDifferentObjectType() {
        NameDefinition nameDefinition = name("Test Name");
        assertNotEquals(nameDefinition, new Object());
    }

    @Test
    public void equals_shouldNotBeEqualToAnotherNameDefinition_withDifferentNames() {
        NameDefinition first = name("first");
        NameDefinition second = name("second");
        assertNotEquals(first, second);
    }

    @Test
    public void equals_shouldBeEqualToAnotherNameDefinition_withSameName() {
        NameDefinition first = name("name");
        NameDefinition second = name(first.getName());
        assertEquals(first, second);
    }

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