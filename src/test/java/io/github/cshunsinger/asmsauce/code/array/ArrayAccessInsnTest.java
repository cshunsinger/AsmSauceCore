package io.github.cshunsinger.asmsauce.code.array;

import io.github.cshunsinger.asmsauce.MethodBuildingContext;
import io.github.cshunsinger.asmsauce.definitions.TypeDefinition;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

import static java.util.Collections.emptyList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ArrayAccessInsnTest {
    @AfterAll
    public static void afterAll() {
        MethodBuildingContext.reset();
    }

    @Test
    public void validateArrayIsOnTopOfStack_noExceptionThrown() {
        MethodBuildingContext context = new MethodBuildingContext(null, null, null, emptyList());
        context.pushStack(TypeDefinition.INT.getArrayType());

        assertDoesNotThrow(ArrayAccessInsn::validateArrayTypeStacked);
    }

    @Test
    public void validateArrayIsOnTopOfStack_illegalStateException_becauseArrayIsNotOnTopOfStack() {
        MethodBuildingContext context = new MethodBuildingContext(null, null, null, emptyList());
        context.pushStack(TypeDefinition.INT);

        IllegalStateException ex = assertThrows(IllegalStateException.class, ArrayAccessInsn::validateArrayTypeStacked);
        assertThat(ex, hasProperty("message", is("Array type expected on stack. Got type int instead.")));
    }
}