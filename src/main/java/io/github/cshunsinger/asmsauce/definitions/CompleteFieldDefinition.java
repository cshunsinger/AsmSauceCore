package io.github.cshunsinger.asmsauce.definitions;

import io.github.cshunsinger.asmsauce.ClassBuildingContext;
import io.github.cshunsinger.asmsauce.modifiers.AccessModifiers;

/**
 * This class represents a field definition which contains all of the required information for bytecode to be generated
 * involving a field.
 */
public class CompleteFieldDefinition extends FieldDefinition {
    /**
     * Creates a new field definition with a complete set of metadata surrounding a given field.
     * @param accessModifiers The access modifiers of the field.
     * @param fieldOwner The type which owns the field.
     * @param fieldName The name of the field.
     * @param fieldType The type of the field.
     * @throws IllegalArgumentException If the field access modifiers are null.
     * @throws IllegalArgumentException If the field owner is null, void, or primitive.
     * @throws IllegalArgumentException If the field type is null or void.
     */
    public CompleteFieldDefinition(AccessModifiers accessModifiers, TypeDefinition<?> fieldOwner, NameDefinition fieldName, TypeDefinition<?> fieldType) {
        super(accessModifiers, fieldOwner, fieldName, fieldType);

        if(accessModifiers == null)
            throw new IllegalArgumentException("Field access modifiers cannot be null.");
        if(fieldOwner == null || fieldOwner.isVoid())
            throw new IllegalArgumentException("Field owner type cannot be null or void.");
        if(fieldOwner.getType().isPrimitive())
            throw new IllegalArgumentException("Field owner cannot be a primitive type. Primitive types have no fields.");
        if(fieldType == null || fieldType.isVoid())
            throw new IllegalArgumentException("Field type cannot be null or void.");
    }

    /**
     * Generates the jvm descriptor of this field, which is just the JVM type definition of this field's type.
     * @return The jvm descriptor String of this field.
     */
    public String getJvmDescriptor() {
        return fieldType.getJvmTypeDefinition();
    }

    /**
     * This field definition is already considered "complete".
     * @return This.
     */
    @Override
    public CompleteFieldDefinition completeDefinition() {
        return this; //This field definition is already completed therefore it returns itself
    }

    /**
     * This field definition is already considered "complete".
     * @param buildingContext The class building context containing metadata about the class currently being generated.
     * @param fieldOwnerType The type which owns this field.
     * @return This.
     */
    @Override
    public CompleteFieldDefinition completeDefinition(ClassBuildingContext buildingContext, TypeDefinition<?> fieldOwnerType) {
        return this; //This field definition is already completed therefore it returns itself
    }
}