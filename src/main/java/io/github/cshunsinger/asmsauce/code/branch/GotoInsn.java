package io.github.cshunsinger.asmsauce.code.branch;

import aj.org.objectweb.asm.Label;
import io.github.cshunsinger.asmsauce.MethodBuildingContext;
import io.github.cshunsinger.asmsauce.code.CodeInsnBuilder;
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