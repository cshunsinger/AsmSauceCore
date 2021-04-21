package io.github.cshunsinger.asmsauce.definitions;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

class ParamDefinitionTest {
    @Test
    public void illegalArgumentException_nullParameterType() {
        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class,
            () -> new ParamDefinition(null, null)
        );

        assertThat(ex, hasProperty("message", is("Param type cannot be null.")));
    }
}