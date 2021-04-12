package io.github.cshunsinger.asmsauce.code.field;

import io.github.cshunsinger.asmsauce.MethodBuildingContext;
import io.github.cshunsinger.asmsauce.code.CodeInsnBuilderLike;
import io.github.cshunsinger.asmsauce.definitions.FieldDefinition;
import io.github.cshunsinger.asmsauce.definitions.TypeDefinition;

import java.util.Stack;

import static org.objectweb.asm.Opcodes.PUTSTATIC;

public class AssignStaticFieldInsn extends AssignInstanceFieldInsn {
    public AssignStaticFieldInsn(FieldDefinition fieldDefinition, CodeInsnBuilderLike valueBuilder) {
        super(fieldDefinition, valueBuilder);

        if(!fieldDefinition.getAccessModifiers().isStatic()) {
            throw new IllegalArgumentException(
                "This instruction only handles assigning static fields. Field '%s.%s' is not static.".formatted(
                    fieldDefinition.getFieldType().getType().getName(),
                    fieldDefinition.getFieldName().getName()
                )
            );
        }
    }

    @Override
    public void build(MethodBuildingContext context) {
        Class<?> fieldOwnerClass = fieldDefinition.getFieldOwner().getType();
        if(fieldOwnerClass.isArray()) {
            throw new IllegalStateException("Cannot access static field from array type %s.".formatted(fieldOwnerClass.getSimpleName()));
        }

        fieldDefinition = fieldDefinition.completeDefinition(context.getClassContext(), fieldDefinition.getFieldOwner());

        //Execute the value builder to place item onto stack that will be assigned to field
        executeValueBuilder(context);

        //Generate the bytecode to set the static field value
        generateBytecode(context);
    }

    @Override
    protected void performTypeStackChanges(Stack<TypeDefinition<?>> typeStack) {
        //Pop the value assigned to the static field from the stack
        typeStack.pop();
    }

    @Override
    protected int instruction() {
        return PUTSTATIC;
    }

    @Override
    protected TypeDefinition<?> determineFieldOwner(Stack<TypeDefinition<?>> ignored) {
        return fieldDefinition.getFieldOwner();
    }
}