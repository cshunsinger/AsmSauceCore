package io.github.cshunsinger.asmsauce;

import io.github.cshunsinger.asmsauce.definitions.ThrowsDefinition;
import org.junit.jupiter.api.Test;

import static io.github.cshunsinger.asmsauce.DefinitionBuilders.*;
import static io.github.cshunsinger.asmsauce.MethodNode.method;
import static io.github.cshunsinger.asmsauce.modifiers.AccessModifiers.publicOnly;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MethodNodeTest extends BaseUnitTest {
    @Test
    public void illegalArgumentException_nullMethodDefinition() {
        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class,
            () -> new MethodNode(null)
        );
        assertThat(ex, hasProperty("message", is("Method definition cannot be null.")));
    }

    @Test
    public void defineMethodNodeWithThrowingClause() {
        ThrowsDefinition throwsDefinition = throwing(Exception.class);
        MethodNode node = method(publicOnly(), name("myMagicalMethod"), noParameters(), throwsDefinition);
        assertThat(node, hasProperty("definition",
            hasProperty("throwing", is(throwsDefinition))
        ));
    }
}