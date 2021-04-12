package io.github.cshunsinger.asmsauce.code.stack;

import io.github.cshunsinger.asmsauce.MethodBuildingContext;
import io.github.cshunsinger.asmsauce.code.CodeInsnBuilder;
import io.github.cshunsinger.asmsauce.code.branch.condition.ConditionBuilderLike;
import io.github.cshunsinger.asmsauce.code.branch.condition.NullConditionBuilderLike;
import io.github.cshunsinger.asmsauce.code.field.FieldAccessibleInstance;
import io.github.cshunsinger.asmsauce.code.field.FieldAssignableInstance;
import io.github.cshunsinger.asmsauce.code.math.MathOperandInstance;
import io.github.cshunsinger.asmsauce.code.method.InvokableInstance;
import io.github.cshunsinger.asmsauce.DefinitionBuilders;

import static org.objectweb.asm.Opcodes.ACONST_NULL;

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
            context.pushStack(DefinitionBuilders.type(objClass));
        }
        else if(objClass == Class.class) {
            String value = DefinitionBuilders.type((Class<?>)objValue).getJvmTypeDefinition() + ".class";
            context.getMethodVisitor().visitLdcInsn(value);
            context.pushStack(DefinitionBuilders.type(objClass));
        }
        else {
            context.getMethodVisitor().visitLdcInsn(objValue);
            context.pushStack(DefinitionBuilders.type(objClass));
        }

        super.build(context); //build next instruction if it exists
    }
}