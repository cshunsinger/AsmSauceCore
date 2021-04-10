package com.chunsinger.asmsauce.code.method;

import aj.org.objectweb.asm.MethodVisitor;
import com.chunsinger.asmsauce.MethodBuildingContext;
import com.chunsinger.asmsauce.definitions.CompleteMethodDefinition;
import com.chunsinger.asmsauce.testing.BaseUnitTest;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import static com.chunsinger.asmsauce.DefinitionBuilders.type;
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

        when(mockMethodDefinition.getReturnType()).thenReturn(type(String.class));

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> insn.build(methodContext));

        assertThat(ex, hasProperty("message", is(
            "Method being implemented has a return type of java.lang.String but no value on the stack left to return."
        )));
    }

    /*
     * Positive test cases are handled in other unit tests for this package.
     */
}