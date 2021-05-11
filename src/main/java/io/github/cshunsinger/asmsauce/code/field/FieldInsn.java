package io.github.cshunsinger.asmsauce.code.field;

import org.objectweb.asm.MethodVisitor;
import io.github.cshunsinger.asmsauce.code.CodeInsnBuilder;
import io.github.cshunsinger.asmsauce.definitions.FieldDefinition;
import io.github.cshunsinger.asmsauce.definitions.TypeDefinition;

import java.util.Stack;

import static io.github.cshunsinger.asmsauce.MethodBuildingContext.context;

/**
 * This class represents any code builder instruction for field access.
 */
public abstract class FieldInsn extends CodeInsnBuilder {
    /**
     * Field definition representing the field being accessed.
     */
    protected FieldDefinition fieldDefinition;

    /**
     * Creates a new field code builder to access or assign a field.
     * @param fieldDefinition The definition of the field.
     */
    protected FieldInsn(FieldDefinition fieldDefinition) {
        if(fieldDefinition == null)
            throw new IllegalArgumentException("Field definition cannot be null.");

        this.fieldDefinition = fieldDefinition;
    }

    @Override
    public void build() {
        generateBytecode();
    }

    /**
     * Uses the building context to verify that the bytecode being generated here will be safe, and then generates
     * the bytecode to either assign or access a field.
     * @throws IllegalStateException If accessing the field is illegal due to accessibility issues.
     */
    protected void generateBytecode() {
        //pop instance from the stack before fetching the field
        TypeDefinition fieldContainerType = determineFieldOwner(context().getTypeStack());

        Class<?> fieldContainerClass = fieldContainerType.getType();
        if(fieldContainerClass.isPrimitive()) {
            throw new IllegalStateException("Cannot access a field from primitive type '%s'."
                .formatted(fieldContainerClass.getSimpleName())
            );
        }

        fieldDefinition = fieldDefinition.completeDefinition();

        //Load a value from class field onto the stack
        callMethodVisitor(context().getMethodVisitor());
        performTypeStackChanges(context().getTypeStack());

        super.build();
    }

    /**
     * Calls the MethodVisitor to generate the bytecode for accessing or assigning a field.
     * @param methodVisitor The method visitor writing the bytecode of this class.
     */
    protected void callMethodVisitor(MethodVisitor methodVisitor) {
        methodVisitor.visitFieldInsn(
            instruction(),
            fieldDefinition.getFieldOwner().getJvmTypeName(),
            fieldDefinition.getFieldName().getName(),
            fieldDefinition.getFieldType().getJvmTypeDefinition()
        );
    }

    /**
     * Classes that implement this method are expected to update the type stack in this method to reflect the
     * bytecode operations being generated.
     * @param typeStack The type stack to be updated.
     */
    protected abstract void performTypeStackChanges(Stack<TypeDefinition> typeStack);

    /**
     * Gets the opcode to use for accessing a field.
     * @return Opcode to use for building the bytecode instruction.
     */
    protected abstract int instruction();

    /**
     * Reads the type stack to determine the type of instance that owns the field.
     * @param typeStack A type stack.
     * @return The type definition of the instance type that owns the field being used.
     */
    protected abstract TypeDefinition determineFieldOwner(Stack<TypeDefinition> typeStack);
}