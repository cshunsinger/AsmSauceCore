package com.chunsinger.asmsauce.code.field;

import com.chunsinger.asmsauce.MethodBuildingContext;
import com.chunsinger.asmsauce.code.branch.condition.BooleanConditionBuilderLike;
import com.chunsinger.asmsauce.code.branch.condition.ConditionBuilderLike;
import com.chunsinger.asmsauce.code.branch.condition.NullConditionBuilderLike;
import com.chunsinger.asmsauce.code.math.MathOperandInstance;
import com.chunsinger.asmsauce.code.method.InvokableInstance;
import com.chunsinger.asmsauce.definitions.FieldDefinition;
import com.chunsinger.asmsauce.definitions.TypeDefinition;

import java.util.Stack;

import static aj.org.objectweb.asm.Opcodes.GETSTATIC;

public class GetStaticFieldInsn extends FieldInsn implements
    InvokableInstance, FieldAccessibleInstance, MathOperandInstance,
    ConditionBuilderLike, BooleanConditionBuilderLike, NullConditionBuilderLike {
    public GetStaticFieldInsn(FieldDefinition fieldDefinition) {
        super(fieldDefinition);

        if(!fieldDefinition.getAccessModifiers().isStatic()) {
            throw new IllegalArgumentException(
                "This instruction only handles getting static fields. Field '%s.%s' is not static.".formatted(
                    fieldDefinition.getFieldType().getType().getName(),
                    fieldDefinition.getFieldName().getName()
                )
            );
        }
    }

    @Override
    public void build(MethodBuildingContext context) {
        Class<?> fieldOwnerClass = fieldDefinition.getFieldOwner().getType();

        if(fieldOwnerClass.isArray())
            throw new IllegalStateException("Cannot access static field from array type %s.".formatted(fieldOwnerClass.getSimpleName()));

        fieldDefinition = fieldDefinition.completeDefinition(context.getClassContext(), fieldDefinition.getFieldOwner());

        super.build(context);
    }

    @Override
    protected void performTypeStackChanges(Stack<TypeDefinition<?>> typeStack) {
        //Push the field value type onto the stack
        typeStack.push(fieldDefinition.getFieldType());
    }

    @Override
    protected int instruction() {
        return GETSTATIC;
    }

    protected TypeDefinition<?> determineFieldOwner(Stack<TypeDefinition<?>> typeStack) {
        return fieldDefinition.getFieldOwner();
    }
}