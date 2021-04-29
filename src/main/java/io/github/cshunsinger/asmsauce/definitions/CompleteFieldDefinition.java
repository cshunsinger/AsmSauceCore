package io.github.cshunsinger.asmsauce.definitions;

import io.github.cshunsinger.asmsauce.modifiers.AccessModifiers;

import java.lang.reflect.Field;

import static io.github.cshunsinger.asmsauce.DefinitionBuilders.name;
import static io.github.cshunsinger.asmsauce.DefinitionBuilders.type;

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
     * @throws IllegalArgumentException If the field name is null.
     * @throws IllegalArgumentException If the field owner is null.
     * @throws IllegalArgumentException If the field owner is void, primitive, or an array type.
     * @throws IllegalArgumentException If the field access modifiers are null.
     */
    public CompleteFieldDefinition(AccessModifiers accessModifiers, TypeDefinition fieldOwner, NameDefinition fieldName, TypeDefinition fieldType) {
        super(accessModifiers, fieldOwner, fieldName, fieldType);

        if(accessModifiers == null)
            throw new IllegalArgumentException("Field access modifiers cannot be null.");
        if(fieldOwner == null)
            throw new IllegalArgumentException("Field owner type cannot be null.");
        if(fieldType == null || fieldType.isVoid())
            throw new IllegalArgumentException("Field type cannot be null or void.");
    }

    /**
     * Creates a complete field definition from an existing Java {@link Field}
     * @param field The Java field to create a field definition out of.
     * @return A new complete field definition.
     */
    public static CompleteFieldDefinition fromField(Field field) {
        return new CompleteFieldDefinition(
            AccessModifiers.customAccess(field.getModifiers()),
            type(field.getDeclaringClass()),
            name(field.getName()),
            type(field.getType())
        );
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
}