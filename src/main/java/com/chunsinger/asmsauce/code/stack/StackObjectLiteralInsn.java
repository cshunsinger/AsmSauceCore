package com.chunsinger.asmsauce.code.stack;

import com.chunsinger.asmsauce.MethodBuildingContext;
import com.chunsinger.asmsauce.code.CodeInsnBuilder;
import com.chunsinger.asmsauce.code.branch.condition.ConditionBuilderLike;
import com.chunsinger.asmsauce.code.branch.condition.NullConditionBuilderLike;
import com.chunsinger.asmsauce.code.field.FieldAccessibleInstance;
import com.chunsinger.asmsauce.code.field.FieldAssignableInstance;
import com.chunsinger.asmsauce.code.math.MathOperandInstance;
import com.chunsinger.asmsauce.code.method.InvokableInstance;

import static aj.org.objectweb.asm.Opcodes.ACONST_NULL;
import static com.chunsinger.asmsauce.DefinitionBuilders.type;

public class StackObjectLiteralInsn extends CodeInsnBuilder implements
    InvokableInstance, FieldAccessibleInstance, FieldAssignableInstance, MathOperandInstance,
    ConditionBuilderLike, NullConditionBuilderLike {
    private final Class<?> objClass;
    private final Object objValue;

    public StackObjectLiteralInsn(Object objValue) {
        this(objValue != null ? objValue.getClass() : null, objValue);
    }

    public StackObjectLiteralInsn(Class<?> objClass, Object objValue) {
        Class<?> objectType = objClass == null ? Object.class : objClass;
        this.objClass = objectType == boolean.class ? Boolean.class : objectType;
        this.objValue = objValue;
    }

    @Override
    public void build(MethodBuildingContext context) {
        if(objValue == null) {
            context.getMethodVisitor().visitInsn(ACONST_NULL);
            context.pushStack(type(objClass));
        }
        else if(objClass == Class.class) {
            String value = type((Class<?>)objValue).getJvmTypeDefinition() + ".class";
            context.getMethodVisitor().visitLdcInsn(value);
            context.pushStack(type(objClass));
        }
        else {
            context.getMethodVisitor().visitLdcInsn(objValue);
            context.pushStack(type(objClass));
        }

        super.build(context); //build next instruction if it exists
    }
}