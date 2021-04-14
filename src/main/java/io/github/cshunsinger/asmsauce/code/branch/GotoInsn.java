package io.github.cshunsinger.asmsauce.code.branch;

import org.objectweb.asm.Label;
import io.github.cshunsinger.asmsauce.MethodBuildingContext;
import io.github.cshunsinger.asmsauce.code.CodeInsnBuilder;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import static org.objectweb.asm.Opcodes.GOTO;

/**
 * Simple code builder which creates bytecode to jump to any arbitrary label.
 */
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class GotoInsn extends CodeInsnBuilder {
    private final Label label;

    @Override
    public void build(MethodBuildingContext context) {
        context.getMethodVisitor().visitJumpInsn(GOTO, label);
    }
}