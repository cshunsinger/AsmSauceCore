package com.chunsinger.asmsauce.code.field;

import aj.org.objectweb.asm.MethodVisitor;
import com.chunsinger.asmsauce.MethodBuildingContext;
import com.chunsinger.asmsauce.code.CodeInsnBuilder;
import com.chunsinger.asmsauce.definitions.FieldDefinition;
import com.chunsinger.asmsauce.definitions.TypeDefinition;

import java.util.Stack;

public abstract class FieldInsn extends CodeInsnBuilder {
    protected FieldDefinition fieldDefinition;

    protected FieldInsn(FieldDefinition fieldDefinition) {
        if(fieldDefinition == null)
            throw new IllegalArgumentException("Field definition cannot be null.");

        this.fieldDefinition = fieldDefinition;
    }

    @Override
    public void build(MethodBuildingContext context) {
        generateBytecode(context);
    }

    protected void generateBytecode(MethodBuildingContext context) {
        //pop instance from the stack before fetching the field
        TypeDefinition<?> fieldContainerType = determineFieldOwner(context.getTypeStack());

        Class<?> fieldContainerClass = fieldContainerType.getType();
        if(fieldContainerClass.isPrimitive()) {
            throw new IllegalStateException("Cannot access a field from primitive type '%s'."
                .formatted(fieldContainerClass.getSimpleName())
            );
        }

        fieldDefinition = fieldDefinition.completeDefinition(context.getClassContext());

        //Load a value from class field onto the stack
        String newClassJvmName = context.getClassContext().getJvmTypeName();
        callMethodVisitor(context.getMethodVisitor(), newClassJvmName);
        performTypeStackChanges(context.getTypeStack());

        super.build(context);
    }

    protected void callMethodVisitor(MethodVisitor methodVisitor, String newClassJvmName) {
        methodVisitor.visitFieldInsn(
            instruction(),
            fieldDefinition.getFieldOwner().getJvmTypeName(newClassJvmName),
            fieldDefinition.getFieldName().getName(),
            fieldDefinition.getFieldType().getJvmTypeDefinition(newClassJvmName)
        );
    }

    protected abstract void performTypeStackChanges(Stack<TypeDefinition<?>> typeStack);
    protected abstract int instruction();
    protected abstract TypeDefinition<?> determineFieldOwner(Stack<TypeDefinition<?>> typeStack);
}