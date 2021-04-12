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

import static io.github.cshunsinger.asmsauce.modifiers.AccessModifiers.customAccess;

@Getter
public class FieldDefinition {
    protected final AccessModifiers accessModifiers;
    protected final TypeDefinition<?> fieldOwner;
    protected final NameDefinition fieldName;
    protected final TypeDefinition<?> fieldType;

    public FieldDefinition(AccessModifiers accessModifiers, TypeDefinition<?> fieldOwner, NameDefinition fieldName, TypeDefinition<?> fieldType) {
        if(fieldName == null)
            throw new IllegalArgumentException("Field name cannot be null.");

        this.accessModifiers = accessModifiers;
        this.fieldOwner = fieldOwner;
        this.fieldName = fieldName;
        this.fieldType = fieldType;
    }

    public CompleteFieldDefinition completeDefinition(ClassBuildingContext buildingContext) {
        Optional<CompleteFieldDefinition> fdOpt = buildingContext.getFields()
            .stream()
            .map(FieldNode::getFieldDefinition)
            .filter(fieldDefinition -> fieldDefinition.fieldName.equals(this.fieldName))
            .findFirst();

        if(fdOpt.isPresent())
            return fdOpt.get();

        //If field was not found, iterate through the superclass and interface types of the class being built
        List<Class<?>> otherTypes = new ArrayList<>();
        otherTypes.add(buildingContext.getSuperclass());
        otherTypes.addAll(buildingContext.getInterfaces());

        for(Class<?> otherType: otherTypes) {
            fdOpt = attemptCompletion(otherType);
            if(fdOpt.isPresent()) {
                FieldDefinition fd = fdOpt.get();
                if(AccessModifiers.isAccessible(buildingContext, ThisClass.class, fd.getFieldOwner().getType(), fd.getAccessModifiers()))
                    return fdOpt.get();
            }
        }

        throw createFieldNotFoundException(buildingContext.getClassName());
    }

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