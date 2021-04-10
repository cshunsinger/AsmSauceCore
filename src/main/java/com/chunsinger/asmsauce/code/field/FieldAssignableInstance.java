package com.chunsinger.asmsauce.code.field;

import com.chunsinger.asmsauce.code.CodeInsnBuilderLike;
import com.chunsinger.asmsauce.definitions.CompleteFieldDefinition;
import com.chunsinger.asmsauce.definitions.FieldDefinition;
import com.chunsinger.asmsauce.definitions.NameDefinition;
import com.chunsinger.asmsauce.definitions.TypeDefinition;

import static com.chunsinger.asmsauce.DefinitionBuilders.name;
import static com.chunsinger.asmsauce.modifiers.AccessModifiers.customAccess;

public interface FieldAssignableInstance extends CodeInsnBuilderLike {
    default AssignInstanceFieldInsn assignField(TypeDefinition<?> fieldOwner,
                                        NameDefinition fieldName,
                                        TypeDefinition<?> fieldType,
                                        CodeInsnBuilderLike valueBuilder) {
        CompleteFieldDefinition fd = new CompleteFieldDefinition(customAccess(0), fieldOwner, fieldName, fieldType);
        AssignInstanceFieldInsn a = new AssignInstanceFieldInsn(fd, valueBuilder);
        a.setPrev(this);
        this.setNext(a);
        return a;
    }

    default AssignInstanceFieldInsn assignField(NameDefinition fieldName, CodeInsnBuilderLike valueBuilder) {
        FieldDefinition fd = new FieldDefinition(null, null, fieldName, null);
        AssignInstanceFieldInsn a = new AssignInstanceFieldInsn(fd, valueBuilder);
        a.setPrev(this);
        this.setNext(a);
        return a;
    }

    default AssignInstanceFieldInsn assignField(String fieldName, CodeInsnBuilderLike valueBuilder) {
        return assignField(name(fieldName), valueBuilder);
    }
}