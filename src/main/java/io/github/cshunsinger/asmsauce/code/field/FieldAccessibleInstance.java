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
public interface FieldAccessibleInstance extends CodeInsnBuilderLike {
    /**
     * Creates a code builder to access the value of a field belonging to an instance on the stack.
     * @param fieldOwner The owner type of the field.
     * @param fieldName The name of the field to access.
     * @param fieldType The type of the field to access.
     * @return A code builder to generate the bytecode to access the value of a field of an instance on the jvm stack.
     */
    default GetInstanceFieldInsn getField(TypeDefinition fieldOwner, NameDefinition fieldName, TypeDefinition fieldType) {
        //access modifiers are irrelevant here
        GetInstanceFieldInsn f = new GetInstanceFieldInsn(new CompleteFieldDefinition(customAccess(0), fieldOwner, fieldName, fieldType));
        this.setNext(f);
        f.setPrev(this);
        return f;
    }

    /**
     * Creates a code builder to access the value of a field belonging to an instance on the stack.
     * @param fieldName The name of the field to access.
     * @return A code builder to generate the bytecode to access the value of a field of an instance on the jvm stack.
     */
    default GetInstanceFieldInsn getField(NameDefinition fieldName) {
        GetInstanceFieldInsn f = new GetInstanceFieldInsn(new FieldDefinition(null, null, fieldName, null));
        this.setNext(f);
        f.setPrev(this);
        return f;
    }

    /**
     * Creates a code builder to access the value of a field belonging to an instance on the stack.
     * @param fieldName The name of the field to access.
     * @return A code builder to generate the bytecode to access the value of a field of an instance on the jvm stack.
     */
    default GetInstanceFieldInsn getField(String fieldName) {
        return getField(name(fieldName));
    }
}