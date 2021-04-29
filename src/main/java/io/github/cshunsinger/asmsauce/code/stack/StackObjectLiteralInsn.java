package io.github.cshunsinger.asmsauce.code.stack;

import io.github.cshunsinger.asmsauce.code.CodeInsnBuilder;
import io.github.cshunsinger.asmsauce.code.branch.condition.ConditionBuilderLike;
import io.github.cshunsinger.asmsauce.code.branch.condition.NullConditionBuilderLike;
import io.github.cshunsinger.asmsauce.code.field.FieldAccessibleInstance;
import io.github.cshunsinger.asmsauce.code.field.FieldAssignableInstance;
import io.github.cshunsinger.asmsauce.code.math.MathOperandInstance;
import io.github.cshunsinger.asmsauce.code.method.InvokableInstance;

import static io.github.cshunsinger.asmsauce.DefinitionBuilders.type;
import static io.github.cshunsinger.asmsauce.MethodBuildingContext.context;
import static org.objectweb.asm.Opcodes.ACONST_NULL;

/**
 * Bytecode builder instruction for stacking a literal String, Class, or null onto the jvm stack.
 */
public class StackObjectLiteralInsn extends CodeInsnBuilder implements
    InvokableInstance, FieldAccessibleInstance, FieldAssignableInstance, MathOperandInstance,
    ConditionBuilderLike, NullConditionBuilderLike {
    private final Class<?> objClass;
    private final Object objValue;

    /**
     * Creates an instance that will stack a literal reference value (String, Class, or null) onto the jvm stack during runtime.
     * @param objValue The literal value to stack.
     */
    public StackObjectLiteralInsn(Object objValue) {
        this(objValue != null ? objValue.getClass() : null, objValue);
    }

    /**
     * Creates an instance that will stack a literal reference value (String, Class, or null) onto the jvm stack during runtime.
     * @param objClass The type of literal value to stack.
     * @param objValue The literal value to stack.
     */
    public StackObjectLiteralInsn(Class<?> objClass, Object objValue) {
        Class<?> objectType = objClass == null ? Object.class : objClass;

        this.objClass = objectType == boolean.class ? Boolean.class : objectType;
        this.objValue = objValue;
    }

    @Override
    public void build() {
        if(objValue == null) {
            context().getMethodVisitor().visitInsn(ACONST_NULL);
            context().pushStack(type(objClass));
        }
        else if(objClass == Class.class) {
            String value = type((Class<?>)objValue).getJvmTypeDefinition() + ".class";
            context().getMethodVisitor().visitLdcInsn(value);
            context().pushStack(type(objClass));
        }
        else {
            context().getMethodVisitor().visitLdcInsn(objValue);
            context().pushStack(type(objClass));
        }

        super.build(); //build next instruction if it exists
    }
}