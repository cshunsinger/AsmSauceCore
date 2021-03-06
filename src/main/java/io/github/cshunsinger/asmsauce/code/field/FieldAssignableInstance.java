package io.github.cshunsinger.asmsauce.code.field;

import io.github.cshunsinger.asmsauce.code.CodeInsnBuilderLike;
import io.github.cshunsinger.asmsauce.definitions.CompleteFieldDefinition;
import io.github.cshunsinger.asmsauce.definitions.FieldDefinition;
import io.github.cshunsinger.asmsauce.definitions.NameDefinition;
import io.github.cshunsinger.asmsauce.definitions.TypeDefinition;

import static io.github.cshunsinger.asmsauce.DefinitionBuilders.name;
import static io.github.cshunsinger.asmsauce.modifiers.AccessModifiers.customAccess;

/**
 * Interface representing any code builder who is capable of stacking an instance type that may contain fields.
 */
public interface FieldAssignableInstance extends CodeInsnBuilderLike {
    /**
     * Create a code builder to assign a value to a field of an instance on the stack.
     * @param fieldOwner The owner type of the field.
     * @param fieldName The name of the field to assign to.
     * @param fieldType The type of the field being assigned to.
     * @param valueBuilder A code builder whose generated bytecode will stack the value to assign to the instance field.
     * @return A code builder to generate the bytecode to assign a value to a field on an instance of an object.
     */
    default AssignInstanceFieldInsn assignField(TypeDefinition fieldOwner,
                                        NameDefinition fieldName,
                                        TypeDefinition fieldType,
                                        CodeInsnBuilderLike valueBuilder) {
        CompleteFieldDefinition fd = new CompleteFieldDefinition(customAccess(0), fieldOwner, fieldName, fieldType);
        AssignInstanceFieldInsn a = new AssignInstanceFieldInsn(fd, valueBuilder);
        a.setPrev(this);
        this.setNext(a);
        return a;
    }

    /**
     * Create a code builder to assign a value to a field of an instance on the stack.
     * @param fieldName The name of the field to assign to.
     * @param valueBuilder A code builder whose generated bytecode will stack the value to assign to the instance field.
     * @return A code builder to generate the bytecode to assign a value to a field on an instance of an object.
     */
    default AssignInstanceFieldInsn assignField(NameDefinition fieldName, CodeInsnBuilderLike valueBuilder) {
        FieldDefinition fd = new FieldDefinition(null, null, fieldName, null);
        AssignInstanceFieldInsn a = new AssignInstanceFieldInsn(fd, valueBuilder);
        a.setPrev(this);
        this.setNext(a);
        return a;
    }

    /**
     * Create a code builder to assign a value to a field of an instance on the stack.
     * @param fieldName The name of the field to assign to.
     * @param valueBuilder A code builder whose generated bytecode will stack the value to assign to the instance field.
     * @return A code builder to generate the bytecode to assign a value to a field on an instance of an object.
     */
    default AssignInstanceFieldInsn assignField(String fieldName, CodeInsnBuilderLike valueBuilder) {
        return assignField(name(fieldName), valueBuilder);
    }
}