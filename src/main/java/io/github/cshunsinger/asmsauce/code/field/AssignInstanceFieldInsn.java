package io.github.cshunsinger.asmsauce.code.field;

import io.github.cshunsinger.asmsauce.MethodBuildingContext;
import io.github.cshunsinger.asmsauce.code.CodeInsnBuilderLike;
import io.github.cshunsinger.asmsauce.code.cast.ImplicitConversionInsn;
import io.github.cshunsinger.asmsauce.code.math.MathOperandInstance;
import io.github.cshunsinger.asmsauce.code.method.InvokableInstance;
import io.github.cshunsinger.asmsauce.definitions.FieldDefinition;
import io.github.cshunsinger.asmsauce.definitions.TypeDefinition;

import java.util.Stack;

import static org.objectweb.asm.Opcodes.PUTFIELD;

public class AssignInstanceFieldInsn extends FieldInsn implements InvokableInstance, MathOperandInstance {
    private final CodeInsnBuilderLike valueBuilder;

    public AssignInstanceFieldInsn(FieldDefinition fieldDefinition, CodeInsnBuilderLike valueBuilder) {
        super(fieldDefinition);

        if(valueBuilder == null)
            throw new IllegalArgumentException("Value builder cannot be null.");

        this.valueBuilder = valueBuilder.getFirstInStack();
    }

    @Override
    public void build(MethodBuildingContext context) {
        if(context.isStackEmpty()) {
            throw new IllegalStateException(
                "No instance on stack to access field '%s' from.".formatted(fieldDefinition.getFieldName().getName())
            );
        }

        TypeDefinition<?> instanceType = context.peekStack();

        if(instanceType.getType().isArray() && "length".equals(fieldDefinition.getFieldName().getName()))
            throw new IllegalStateException("Cannot assign a value to the 'length' field of an array.");

        fieldDefinition = fieldDefinition.completeDefinition(context.getClassContext());

        //Build up the value that will be placed into the instance field
        executeValueBuilder(context);

        //Generate the bytecode to set the instance field value
        super.build(context);
    }

    protected void executeValueBuilder(MethodBuildingContext context) {
        int stackSize = context.stackSize();
        valueBuilder.build(context);

        //Validate that the size of the stack hasn't been fucked
        if(context.stackSize() != stackSize+1) {
            throw new IllegalStateException(
                "Expected 1 element placed onto the stack. Instead %d elements were added/removed."
                    .formatted(context.stackSize() - stackSize)
            );
        }

        //Implicit casting and/or auto-boxing/auto-unboxing if necessary
        //This will throw an exception if the value on the stack cannot be assigned to the field
        new ImplicitConversionInsn(fieldDefinition.getFieldType()).build(context);
    }

    @Override
    protected void performTypeStackChanges(Stack<TypeDefinition<?>> typeStack) {
        //Pop the value assigned to the field from the stack
        typeStack.pop();
        //Pop the instance type off of the stack
        typeStack.pop();
    }

    @Override
    protected int instruction() {
        return PUTFIELD;
    }

    @Override
    protected TypeDefinition<?> determineFieldOwner(Stack<TypeDefinition<?>> typeStack) {
        TypeDefinition<?> valueType = typeStack.pop(); //Temporarily pop the value type off the stack to look at what the instance type is
        TypeDefinition<?> instanceType = typeStack.peek(); //The instance type (aka the object type we're setting the field on)
        typeStack.push(valueType); //Push the value type back onto the stack so that stack is unaffected by this method call
        return instanceType;
    }
}