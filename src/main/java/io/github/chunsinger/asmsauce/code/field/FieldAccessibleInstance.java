package io.github.chunsinger.asmsauce.code.field;

import io.github.chunsinger.asmsauce.code.CodeInsnBuilderLike;
import io.github.chunsinger.asmsauce.definitions.CompleteFieldDefinition;
import io.github.chunsinger.asmsauce.definitions.FieldDefinition;
import io.github.chunsinger.asmsauce.definitions.NameDefinition;
import io.github.chunsinger.asmsauce.definitions.TypeDefinition;
import io.github.chunsinger.asmsauce.DefinitionBuilders;

import static io.github.chunsinger.asmsauce.modifiers.AccessModifiers.customAccess;

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
        return getField(DefinitionBuilders.name(fieldName));
    }
}