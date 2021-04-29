package io.github.cshunsinger.asmsauce.definitions;

import io.github.cshunsinger.asmsauce.ThisClass;
import io.github.cshunsinger.asmsauce.BaseUnitTest;
import org.junit.jupiter.api.Test;

import static io.github.cshunsinger.asmsauce.DefinitionBuilders.type;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TypeDefinitionTest extends BaseUnitTest {
    @Test
    public void illegalArgumentException_nullClass() {
        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class,
            () -> new TypeDefinition(null)
        );
        assertThat(ex, hasProperty("message", is("Field type cannot be null.")));
    }

    @Test
    public void getTheJvmNameOfTheNewClassWhenTypeIsThisClassType() {
        TypeDefinition typeDefinition = type(ThisClass.class);

        assertThat(typeDefinition.getJvmTypeDefinition("MyNewTypeDefinition"), is("LMyNewTypeDefinition;"));
    }

    @Test
    @SuppressWarnings("ConstantConditions")
    public void testEqualityBetweenTypeDefinitions() {
        TypeDefinition typeDefinition = type(ThisClass.class);
        assertThat(typeDefinition.equals(type(Object.class)), is(false));
        assertThat(typeDefinition.equals(type(ThisClass.class)), is(true));
        assertThat(typeDefinition.equals(null), is(false));
    }
}