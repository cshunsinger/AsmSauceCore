package io.github.cshunsinger.asmsauce.code.method;

import org.objectweb.asm.MethodVisitor;
import io.github.cshunsinger.asmsauce.MethodBuildingContext;
import io.github.cshunsinger.asmsauce.definitions.CompleteMethodDefinition;
import io.github.cshunsinger.asmsauce.BaseUnitTest;
import io.github.cshunsinger.asmsauce.DefinitionBuilders;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import static java.util.Collections.emptyList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

class ReturnInsnTest extends BaseUnitTest {
    @Mock
    private CompleteMethodDefinition<?, String> mockMethodDefinition;

    @Mock
    private MethodVisitor mockMethodVisitor;

    @Test
    public void illegalStateException_methodHasReturnTypeButNoValueOnStackToReturn() {
        ReturnInsn insn = new ReturnInsn();
        MethodBuildingContext methodContext = new MethodBuildingContext(mockMethodVisitor, mockMethodDefinition, null, emptyList());

        when(mockMethodDefinition.getReturnType()).thenReturn(DefinitionBuilders.type(String.class));

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> insn.build(methodContext));

        assertThat(ex, hasProperty("message", is(
            "Method being implemented has a return type of java.lang.String but no value on the stack left to return."
        )));
    }

    /*
     * Positive test cases are handled in other unit tests for this package.
     */
}