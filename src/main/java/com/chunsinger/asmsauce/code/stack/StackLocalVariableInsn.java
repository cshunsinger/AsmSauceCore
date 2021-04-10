package com.chunsinger.asmsauce.code.stack;

import com.chunsinger.asmsauce.MethodBuildingContext;
import com.chunsinger.asmsauce.code.CodeInsnBuilder;
import com.chunsinger.asmsauce.code.branch.condition.BooleanConditionBuilderLike;
import com.chunsinger.asmsauce.code.branch.condition.ConditionBuilderLike;
import com.chunsinger.asmsauce.code.branch.condition.NullConditionBuilderLike;
import com.chunsinger.asmsauce.code.field.FieldAccessibleInstance;
import com.chunsinger.asmsauce.code.field.FieldAssignableInstance;
import com.chunsinger.asmsauce.code.math.MathOperandInstance;
import com.chunsinger.asmsauce.code.method.InvokableInstance;
import com.chunsinger.asmsauce.definitions.TypeDefinition;
import org.apache.commons.lang3.StringUtils;

import static aj.org.objectweb.asm.Opcodes.*;
import static java.util.Arrays.asList;

public class StackLocalVariableInsn extends CodeInsnBuilder implements
    InvokableInstance, FieldAccessibleInstance, FieldAssignableInstance, MathOperandInstance,
    ConditionBuilderLike, NullConditionBuilderLike, BooleanConditionBuilderLike {
    private final int localIndex;
    private final String localName;

    public StackLocalVariableInsn(int localIndex) {
        if(localIndex < 0)
            throw new IllegalArgumentException("localIndex cannot be negative.");
        this.localIndex = localIndex;
        this.localName = null;
    }

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