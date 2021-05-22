package io.github.cshunsinger.asmsauce.code.stack;

import io.github.cshunsinger.asmsauce.BaseUnitTest;
import io.github.cshunsinger.asmsauce.MethodBuildingContext;
import org.junit.jupiter.api.Test;

import static java.util.Collections.emptyList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

class StackLocalVariableInsnTest extends BaseUnitTest {
    @Test
    public void illegalArgumentException_negativeLocalIndex() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> new StackLocalVariableInsn(-1));
        assertThat(ex, hasProperty("message", is("localIndex cannot be negative.")));
    }

    @Test
    public void illegalStateException_localIndexOutOfBounds() {
        new MethodBuildingContext(null, null, null, emptyList());
        IllegalStateException ex = assertThrows(
            IllegalStateException.class,
            () -> new StackLocalVariableInsn(1).build()
        );
        assertThat(ex, hasProperty("message", is("Trying to access local variable at index 1 when only 0 exists.")));
    }

    @Test
    public void illegalArgumentException_localVariableNameNullOrEmpty() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> new StackLocalVariableInsn(""));
        assertThat(ex, hasProperty("message", is("localName cannot be null or empty.")));
    }

    /*
     * Many other unit test cases and suites already cover all of the various successful cases of
     * StackLocalVariableInsn.
     */
}