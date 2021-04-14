package io.github.cshunsinger.asmsauce.code.stack;

import io.github.cshunsinger.asmsauce.MethodBuildingContext;
import io.github.cshunsinger.asmsauce.code.CodeInsnBuilder;
import io.github.cshunsinger.asmsauce.code.branch.condition.BooleanConditionBuilderLike;
import io.github.cshunsinger.asmsauce.code.branch.condition.ConditionBuilderLike;
import io.github.cshunsinger.asmsauce.code.branch.condition.NullConditionBuilderLike;
import io.github.cshunsinger.asmsauce.code.field.FieldAccessibleInstance;
import io.github.cshunsinger.asmsauce.code.field.FieldAssignableInstance;
import io.github.cshunsinger.asmsauce.code.math.MathOperandInstance;
import io.github.cshunsinger.asmsauce.code.method.InvokableInstance;
import io.github.cshunsinger.asmsauce.definitions.TypeDefinition;
import org.apache.commons.lang3.StringUtils;

import static org.objectweb.asm.Opcodes.*;
import static java.util.Arrays.asList;

/**
 * Asm bytecode builder instruction which generates bytecode to place a local variable onto the jvm stack.
 */
public class StackLocalVariableInsn extends CodeInsnBuilder implements
    InvokableInstance, FieldAccessibleInstance, FieldAssignableInstance, MathOperandInstance,
    ConditionBuilderLike, NullConditionBuilderLike, BooleanConditionBuilderLike {
    private final int localIndex;
    private final String localName;

    /**
     * Stack a local variable by it's index. Useful for unnamed local variables.
     * @param localIndex The zero-based index of the local variable to stack.
     * @throws IllegalArgumentException If localIndex is negative.
     */
    public StackLocalVariableInsn(int localIndex) {
        if(localIndex < 0)
            throw new IllegalArgumentException("localIndex cannot be negative.");
        this.localIndex = localIndex;
        this.localName = null;
    }

    /**
     * Stack a named local variable. Useful for easily referencing named local variables.
     * @param localName The name of the local variable to stack.
     * @throws IllegalArgumentException If localName is null or empty/blank.
     */
    public StackLocalVariableInsn(String localName) {
        if(StringUtils.trimToNull(localName) == null)
            throw new IllegalArgumentException("localName cannot be null or empty.");
        this.localIndex = -1;
        this.localName = localName;
    }

    @Override
    public void build(MethodBuildingContext context) {
        TypeDefinition<?> typeDefinition;

        int index;
        if(localName != null) {
            index = context.getLocalIndex(localName);
            typeDefinition = context.getLocalType(localName);
        }
        else {
            if(localIndex >= context.numLocals())
                throw new IllegalStateException("Trying to access local variable at index " + localIndex + " when only " + context.numLocals() + " exists.");

            typeDefinition = context.getLocalType(localIndex);
            index = localIndex;
        }

        Class<?> typeClass = typeDefinition.getType();

        int opcode;
        if(asList(byte.class, char.class, short.class, int.class, boolean.class).contains(typeClass))
            opcode = ILOAD;
        else if(typeClass == float.class)
            opcode = FLOAD;
        else if(typeClass == double.class)
            opcode = DLOAD;
        else if(typeClass == long.class)
            opcode = LLOAD;
        else
            opcode = ALOAD;

        context.getMethodVisitor().visitVarInsn(opcode, index);
        context.pushStack(typeDefinition);

        //Build the next series of bytecode instructions
        super.build(context);
    }
}