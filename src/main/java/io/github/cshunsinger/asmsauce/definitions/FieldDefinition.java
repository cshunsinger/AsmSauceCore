package io.github.cshunsinger.asmsauce.definitions;

import io.github.cshunsinger.asmsauce.ClassBuildingContext;
import io.github.cshunsinger.asmsauce.FieldNode;
import io.github.cshunsinger.asmsauce.ThisClass;
import io.github.cshunsinger.asmsauce.modifiers.AccessModifiers;
import io.github.cshunsinger.asmsauce.DefinitionBuilders;
import lombok.Getter;
import org.apache.commons.lang3.reflect.FieldUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static io.github.cshunsinger.asmsauce.ClassBuildingContext.context;
import static io.github.cshunsinger.asmsauce.modifiers.AccessModifiers.customAccess;

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
    protected final TypeDefinition<?> fieldOwner;
    /**
     * The name of this defined field.
     * @return The name.
     */
    protected final NameDefinition fieldName;
    /**
     * The type of this defined field.
     * @return The type.
     */
    protected final TypeDefinition<?> fieldType;

    /**
     * Defines a field by its metadata.
     * @param accessModifiers The access modifiers of the field.
     * @param fieldOwner The type that owns the field.
     * @param fieldName The name of the field.
     * @param fieldType The type of the field.
     * @throws IllegalArgumentException If the field name is null.
     */
    public FieldDefinition(AccessModifiers accessModifiers, TypeDefinition<?> fieldOwner, NameDefinition fieldName, TypeDefinition<?> fieldType) {
        if(fieldName == null)
            throw new IllegalArgumentException("Field name cannot be null.");

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
        Optional<CompleteFieldDefinition> fdOpt = context().getFields()
            .stream()
            .map(FieldNode::getFieldDefinition)
            .filter(fieldDefinition -> fieldDefinition.fieldName.equals(this.fieldName))
            .findFirst();

        if(fdOpt.isPresent())
            return fdOpt.get();

        //If field was not found, iterate through the superclass and interface types of the class being built
        List<Class<?>> otherTypes = new ArrayList<>();
        otherTypes.add(context().getSuperclass());
        otherTypes.addAll(context().getInterfaces());

        for(Class<?> otherType: otherTypes) {
            fdOpt = attemptCompletion(otherType);
            if(fdOpt.isPresent()) {
                FieldDefinition fd = fdOpt.get();
                if(AccessModifiers.isAccessible(context(), ThisClass.class, fd.getFieldOwner().getType(), fd.getAccessModifiers()))
                    return fdOpt.get();
            }
        }

        throw createFieldNotFoundException(context().getClassName());
    }

    /**
     * Generates a completed field definition using the existing information in this "incomplete" field definition, and
     * using the class building context and other reflections information to fill in the missing details.
     * This method attempts to complete this field definition when the field might exist in some given owner type, or one
     * of the parent types or interface types of the owner type.
     * @param buildingContext The class building context containing the details about this class being generated.
     * @param fieldOwnerType The owner type to search for an existing field within.
     * @return A completed field definition which can be used for bytecode generation.
     * @throws IllegalStateException If no field can be found matching the incomplete details of this field definition.
     */
    public CompleteFieldDefinition completeDefinition(ClassBuildingContext buildingContext, TypeDefinition<?> fieldOwnerType) {
        Optional<CompleteFieldDefinition> fdOpt = attemptCompletion(fieldOwnerType.getType());
        if(fdOpt.isPresent()) {
            CompleteFieldDefinition fd = fdOpt.get();
            if(AccessModifiers.isAccessible(buildingContext, ThisClass.class, fieldOwnerType.getType(), fd.getAccessModifiers()))
                return fd;
        }

        throw createFieldNotFoundException(buildingContext.getClassName());
    }

    private Optional<CompleteFieldDefinition> attemptCompletion(Class<?> fieldDeclaringClass) {
        return Optional.ofNullable(FieldUtils.getField(fieldDeclaringClass, fieldName.getName(), true))
            .map(field -> new CompleteFieldDefinition(
                customAccess(field.getModifiers()),
                DefinitionBuilders.type(field.getDeclaringClass()),
                fieldName,
                DefinitionBuilders.type(field.getType())
            ));
    }

    private IllegalStateException createFieldNotFoundException(String fieldClassName) {
        return new IllegalStateException(
            "No field named %s found accessible from class %s.".formatted(fieldName.getName(), fieldClassName)
        );
    }
}