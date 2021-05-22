package io.github.cshunsinger.asmsauce.code;

import io.github.cshunsinger.asmsauce.BaseUnitTest;
import io.github.cshunsinger.asmsauce.MethodBuildingContext;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.util.ArrayList;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CodeBlockTest extends BaseUnitTest {
    @Mock
    private CodeInsnBuilderLike mockCodeBuilder1;
    @Mock
    private CodeInsnBuilderLike mockCodeBuilder2;

    @Test
    public void wrapMultipleCodeBuilders() {
        when(mockCodeBuilder1.getFirstInStack()).thenReturn(mockCodeBuilder1);
        when(mockCodeBuilder2.getFirstInStack()).thenReturn(mockCodeBuilder2);

        new MethodBuildingContext(null, null, null, new ArrayList<>());
        CodeBlock codeBlock = CodeBuilders.block(mockCodeBuilder1, mockCodeBuilder2);
        codeBlock.build();

        verify(mockCodeBuilder1).getFirstInStack();
        verify(mockCodeBuilder1).buildClean();
        verify(mockCodeBuilder2).getFirstInStack();
        verify(mockCodeBuilder2).buildClean();
    }
}