package io.github.cshunsinger.asmsauce.code.stack;

import io.github.cshunsinger.asmsauce.MethodBuildingContext;
import io.github.cshunsinger.asmsauce.code.CodeInsnBuilder;
import io.github.cshunsinger.asmsauce.code.CodeInsnBuilderLike;
import io.github.cshunsinger.asmsauce.definitions.TypeDefinition;
import org.apache.commons.lang3.StringUtils;

import static aj.org.objectweb.asm.Opcodes.*;

public class StoreLocalVariableInsn extends CodeInsnBuilder {
    private final Integer localIndex;
    private final String localName;
    private final CodeInsnBuilderLike valueBuilder;

    public StoreLocalVariableInsn(Integer localIndex, CodeInsnBuilderLike valueBuilder) {
        this(null, localIndex, valueBuilder);

        if(localIndex != null && localIndex < 0)
            throw new IllegalArgumentException("localIndex cannot be negative. Must be null or positive.");
    }

    public StoreLocalVariableInsn(String localName, CodeInsnBuilderLike valueBuilder) {
        this(localName, null, valueBuilder);

        if(StringUtils.trimToNull(localName) == null)
            throw new IllegalArgumentException("localName cannot be null or empty.");
    }

    private StoreLocalVariableInsn(String localName, Integer localIndex, CodeInsnBuilderLike valueBuilder) {
        if(valueBuilder == null)
            throw new IllegalArgumentException("Value builder cannot be null.");

        this.localIndex = localIndex;
        this.localName = localName;
        this.valueBuilder = valueBuilder.getFirstInStack();
    }

    @Override
    public void build(MethodBuildingContext context) {
        int initialStackSize = context.stackSize();
        valueBuilder.build(context);
        int finalStackSize = context.stackSize();

        if(finalStackSize != initialStackSize+1)
            throw new IllegalStateException("Code builder expected to add 1 element to the stack. Instead %d elements were added.".formatted(finalStackSize-initialStackSize));

        //Pop the result of the value builder from the stack to store it in local
        TypeDefinition<?> type = context.popStack();

        //Need to know the index of the local variable for the jvm
        int index;

        if(localName == null) {
            //Store local variable by index
            if(localIndex == null)
                index = context.addLocalType(type);
            else {
                index = localIndex;
                context.setLocalType(index, type);
            }
        }
        else {
            //Store local variable by name
            context.setLocalType(localName, type);
            index = context.getLocalIndex(localName);
        }

        //Determine the correct store opcode based on type
        Class<?> typeClass = type.getType();
        int opcode;
        if(typeClass.isPrimitive()) {
            if(typeClass == double.class)
                opcode = DSTORE;
            else if(typeClass == float.class)
                opcode = FSTORE;
            else if(typeClass == long.class)
                opcode = LSTORE;
            else
                opcode = ISTORE;
        }
        else {
            opcode = ASTORE;
        }

        context.getMethodVisitor().visitVarInsn(opcode, index);
    }
}