package io.github.chunsinger.asmsauce.definitions;

import io.github.chunsinger.asmsauce.ClassBuildingContext;
import io.github.chunsinger.asmsauce.modifiers.AccessModifiers;

public class CompleteFieldDefinition extends FieldDefinition {
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

    public String getJvmDescriptor() {
        return fieldType.getJvmTypeDefinition();
    }

    @Override
    public CompleteFieldDefinition completeDefinition(ClassBuildingContext buildingContext) {
        return this; //This field definition is already completed therefore it returns itself
    }

    @Override
    public CompleteFieldDefinition completeDefinition(ClassBuildingContext buildingContext, TypeDefinition<?> fieldOwnerType) {
        return this; //This field definition is already completed therefore it returns itself
    }
}