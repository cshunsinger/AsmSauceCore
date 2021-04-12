package io.github.chunsinger.asmsauce.code.field;

import io.github.chunsinger.asmsauce.MethodBuildingContext;
import io.github.chunsinger.asmsauce.code.branch.condition.BooleanConditionBuilderLike;
import io.github.chunsinger.asmsauce.code.branch.condition.ConditionBuilderLike;
import io.github.chunsinger.asmsauce.code.branch.condition.NullConditionBuilderLike;
import io.github.chunsinger.asmsauce.code.math.MathOperandInstance;
import io.github.chunsinger.asmsauce.code.method.InvokableInstance;
import io.github.chunsinger.asmsauce.definitions.FieldDefinition;
import io.github.chunsinger.asmsauce.definitions.TypeDefinition;

import java.util.Stack;

import static aj.org.objectweb.asm.Opcodes.GETFIELD;

public class GetInstanceFieldInsn extends FieldInsn implements
    InvokableInstance, FieldAccessibleInstance, MathOperandInstance,
    ConditionBuilderLike, BooleanConditionBuilderLike, NullConditionBuilderLike {
    public GetInstanceFieldInsn(FieldDefinition fieldDefinition) {
        super(fieldDefinition);
    }

    @Override
    public void build(MethodBuildingContext context) {
        if(context.isStackEmpty()) {
            throw new IllegalStateException(
                "No instance on stack to access field '%s' from.".formatted(fieldDefinition.getFieldName().getName())
            );
        }

        //Generate the actual bytecode to get field value
        super.build(context);
    }

    @Override
    protected void performTypeStackChanges(Stack<TypeDefinition<?>> typeStack) {
        //Pop the instance type from the stack
        typeStack.pop();
        //Push the field value type onto the stack
        typeStack.push(fieldDefinition.getFieldType());
    }

    @Override
    protected int instruction() {
        return GETFIELD;
    }

    @Override
    protected TypeDefinition<?> determineFieldOwner(Stack<TypeDefinition<?>> typeStack) {
        return typeStack.peek(); //popping and pushing on stack done later
    }
}