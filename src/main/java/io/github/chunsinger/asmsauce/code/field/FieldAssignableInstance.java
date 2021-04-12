package io.github.chunsinger.asmsauce.code.field;

import io.github.chunsinger.asmsauce.code.CodeInsnBuilderLike;
import io.github.chunsinger.asmsauce.definitions.CompleteFieldDefinition;
import io.github.chunsinger.asmsauce.definitions.FieldDefinition;
import io.github.chunsinger.asmsauce.definitions.NameDefinition;
import io.github.chunsinger.asmsauce.definitions.TypeDefinition;
import io.github.chunsinger.asmsauce.DefinitionBuilders;

import static io.github.chunsinger.asmsauce.modifiers.AccessModifiers.customAccess;

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
        return assignField(DefinitionBuilders.name(fieldName), valueBuilder);
    }
}