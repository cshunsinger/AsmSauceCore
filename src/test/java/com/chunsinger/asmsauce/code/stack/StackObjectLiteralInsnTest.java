package com.chunsinger.asmsauce.code.stack;

import aj.org.objectweb.asm.MethodVisitor;
import com.chunsinger.asmsauce.ClassBuildingContext;
import com.chunsinger.asmsauce.MethodBuildingContext;
import com.chunsinger.asmsauce.ThisClass;
import com.chunsinger.asmsauce.definitions.CompleteMethodDefinition;
import com.chunsinger.asmsauce.testing.BaseUnitTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.util.ArrayList;

import static aj.org.objectweb.asm.Opcodes.ACONST_NULL;
import static com.chunsinger.asmsauce.DefinitionBuilders.type;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class StackObjectLiteralInsnTest extends BaseUnitTest {
    @Mock
    private MethodVisitor mockMethodVisitor;
    @Mock
    private CompleteMethodDefinition<?, ?> mockMethodDefinition;
    @Mock
    private ClassBuildingContext mockClassContext;

    private MethodBuildingContext methodContext;

    @BeforeEach
    public void init() {
        methodContext = new MethodBuildingContext(mockMethodVisitor, mockMethodDefinition, mockClassContext, new ArrayList<>());
    }

    @Test
    public void placeNullReferenceOntoStack() {
        StackObjectLiteralInsn insn = new StackObjectLiteralInsn(String.class,null);
        insn.build(methodContext);

        verify(mockMethodVisitor).visitInsn(ACONST_NULL);
        verify(mockMethodVisitor, never()).visitLdcInsn(any());

        assertThat(methodContext.peekStack(), is(type(String.class)));
    }

    @Test
    public void placeNonNullReferenceOntoStack() {
        StackObjectLiteralInsn insn = new StackObjectLiteralInsn(new ThisClass());
        insn.build(methodContext);

        verify(mockMethodVisitor, never()).visitInsn(anyInt());
        verify(mockMethodVisitor).visitLdcInsn(any(ThisClass.class));

        assertThat(methodContext.peekStack(), is(type(ThisClass.class)));
    }

    @Test
    public void placeClassOntoStack() {
        StackObjectLiteralInsn insn = new StackObjectLiteralInsn(String.class);
        insn.build(methodContext);

        verify(mockMethodVisitor, never()).visitInsn(anyInt());
        verify(mockMethodVisitor).visitLdcInsn(type(String.class).getJvmTypeDefinition() + ".class");

        assertThat(methodContext.peekStack(), is(type(Class.class)));
    }

    @Test
    public void placeDefaultNullObjectOntoStack() {
        StackObjectLiteralInsn insn = new StackObjectLiteralInsn(null, null);
        insn.build(methodContext);

        verify(mockMethodVisitor).visitInsn(ACONST_NULL);
        verify(mockMethodVisitor, never()).visitLdcInsn(any());

        assertThat(methodContext.peekStack(), is(type(Object.class)));
    }
}