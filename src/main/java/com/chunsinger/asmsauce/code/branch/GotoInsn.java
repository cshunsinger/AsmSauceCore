package com.chunsinger.asmsauce.code.branch;

import aj.org.objectweb.asm.Label;
import com.chunsinger.asmsauce.MethodBuildingContext;
import com.chunsinger.asmsauce.code.CodeInsnBuilder;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import static aj.org.objectweb.asm.Opcodes.GOTO;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class GotoInsn extends CodeInsnBuilder {
    private final Label label;

    @Override
    public void build(MethodBuildingContext context) {
        context.getMethodVisitor().visitJumpInsn(GOTO, label);
    }
}