package io.github.cshunsinger.asmsauce;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

class ClassBuildingContextTest {
    @Test
    public void illegalStateException_attemptingToAccessClassBuildingContext_whenNotBuilding() {
        ClassBuildingContext.reset();
        IllegalStateException ex = assertThrows(IllegalStateException.class, ClassBuildingContext::context);
        assertThat(ex, hasProperty("message",
            is("Context must be accessed from within a method building scope.")
        ));
    }

    @Test
    public void accessClassBuildingContext_whenOneExistsAndIsActive() {
        ClassBuildingContext context = new ClassBuildingContext(null, null, null, null, null, null, null);
        assertThat(ClassBuildingContext.context(), is(context));
        ClassBuildingContext.reset();
    }
}