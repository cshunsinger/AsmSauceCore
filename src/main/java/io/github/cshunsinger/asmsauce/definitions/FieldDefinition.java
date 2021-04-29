package io.github.cshunsinger.asmsauce.definitions;

import io.github.cshunsinger.asmsauce.MethodBuildingContext;
import io.github.cshunsinger.asmsauce.ThisClass;
import io.github.cshunsinger.asmsauce.modifiers.AccessModifiers;
import lombok.Getter;

import java.util.Objects;
import java.util.Optional;

import static io.github.cshunsinger.asmsauce.DefinitionBuilders.type;

/**
 * Represents a defined field. This field definition may potentially be considered "incomplete" as it may lack some of
 * the details required for a complete definition.
 */
@Getter
public class FieldDefinition {
    /**
     * The access modifier flags for this defined field.
     * @return The access modifiers.
     */
    protected final AccessModifiers accessModifiers;
    /**
     * The owner type of this defined field.
     * @return The type.
     */
    protected final TypeDefinition fieldOwner;
    /**
     * The name of this defined field.
     * @return The name.
     */
    protected final NameDefinition fieldName;
    /**
     * The type of this defined field.
     * @return The type.
     */
    protected final TypeDefinition fieldType;

    /**
     * Defines a field by its metadata.
     * @param accessModifiers The access modifiers of the field.
     * @param fieldOwner The type that owns the field.
     * @param fieldName The name of the field.
     * @param fieldType The type of the field.
     * @throws IllegalArgumentException If the field name is null.
     * @throws IllegalArgumentException If the field owner is null, and the access modifiers indicate that the field is static.
     * @throws IllegalArgumentException If the field owner is not null and is an array, void, or primitive type.
     */
    public FieldDefinition(AccessModifiers accessModifiers, TypeDefinition fieldOwner, NameDefinition fieldName, TypeDefinition fieldType) {
        if(fieldName == null)
            throw new IllegalArgumentException("Field name cannot be null.");

        if(fieldOwner == null) {
            if(accessModifiers != null && accessModifiers.isStatic())
                throw new IllegalArgumentException("Field owner cannot be null when referring to a static field.");
        }
        else if(!fieldOwner.canHaveMembers())
            throw new IllegalArgumentException("Field owner cannot be void, primitive, or an array type.");

        this.accessModifiers = accessModifiers;
        this.fieldOwner = fieldOwner;
        this.fieldName = fieldName;
        this.fieldType = fieldType;
    }

    /**
     * Generates a completed field definition using the existing information in this "incomplete" field definition, and
     * using the class building context and other reflections information to fill in the missing details.
     * This method attempts to complete this field definition when the field might exist in the generated class itself,
     * in one of the parent types of this class, or in one of the interface types of this class.
     * @return A completed field definition which can be used for bytecode generation.
     * @throws IllegalStateException If no field can be found matching the incomplete details of this field definition.
     */
    public CompleteFieldDefinition completeDefinition() {
        TypeDefinition fieldOwner = this.fieldOwner == null ?
            MethodBuildingContext.context().peekStack() :
            this.fieldOwner;
        AccessModifiers accessModifiers = this.accessModifiers;
        TypeDefinition fieldType = this.fieldType;

        if(accessModifiers == null || fieldType == null) {
            Optional<CompleteFieldDefinition> foundFieldOpt = fieldOwner.flatHierarchy()
                .stream()
                .map(type -> type.getDeclaredField(this.fieldName))
                .filter(Objects::nonNull)
                .filter(field -> field.getFieldName().equals(this.fieldName))
                .filter(field -> AccessModifiers.isAccessible(
                    type(ThisClass.class),
                    field.getFieldOwner(),
                    field.getAccessModifiers()
                ))
                .findFirst();

            if(foundFieldOpt.isEmpty())
                throw createFieldNotFoundException();

            CompleteFieldDefinition foundField = foundFieldOpt.get();

            if(accessModifiers == null)
                accessModifiers = foundField.getAccessModifiers();
            if(fieldType == null)
                fieldType = foundField.getFieldType();
        }

        return new CompleteFieldDefinition(
            accessModifiers,
            fieldOwner,
            this.fieldName,
            fieldType
        );
    }

    private IllegalStateException createFieldNotFoundException() {
        return new IllegalStateException(
            "No field named %s found accessible from class %s.".formatted(fieldName.getName(), type(ThisClass.class).getClassName())
        );
    }
}