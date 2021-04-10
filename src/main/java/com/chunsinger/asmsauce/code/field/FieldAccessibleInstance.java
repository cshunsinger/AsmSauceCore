package com.chunsinger.asmsauce.code.field;

import com.chunsinger.asmsauce.code.CodeInsnBuilderLike;
import com.chunsinger.asmsauce.definitions.CompleteFieldDefinition;
import com.chunsinger.asmsauce.definitions.FieldDefinition;
import com.chunsinger.asmsauce.definitions.NameDefinition;
import com.chunsinger.asmsauce.definitions.TypeDefinition;

import static com.chunsinger.asmsauce.DefinitionBuilders.name;
import static com.chunsinger.asmsauce.modifiers.AccessModifiers.customAccess;

public interface FieldAccessibleInstance extends CodeInsnBuilderLike {
    default GetInstanceFieldInsn getField(TypeDefinition<?> fieldOwner, NameDefinition fieldName, TypeDefinition<?> fieldType) {
        //access modifiers are irrelevant here
        GetInstanceFieldInsn f = new GetInstanceFieldInsn(new CompleteFieldDefinition(customAccess(0), fieldOwner, fieldName, fieldType));
        this.setNext(f);
        f.setPrev(this);
        return f;
    }

    default GetInstanceFieldInsn getField(NameDefinition fieldName) {
        GetInstanceFieldInsn f = new GetInstanceFieldInsn(new FieldDefinition(null, null, fieldName, null));
        this.setNext(f);
        f.setPrev(this);
        return f;
    }

    default GetInstanceFieldInsn getField(String fieldName) {
        return getField(name(fieldName));
    }
}