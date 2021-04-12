package io.github.chunsinger.asmsauce.code.method;

import io.github.chunsinger.asmsauce.code.CodeInsnBuilderLike;
import io.github.chunsinger.asmsauce.definitions.NameDefinition;
import io.github.chunsinger.asmsauce.DefinitionBuilders;

import java.lang.reflect.Method;

public interface InvokableInstance extends CodeInsnBuilderLike {
    default InvokeInstanceMethodInsn invoke(Class<?> thisType, Method method, CodeInsnBuilderLike... codeBuilders) {
        InvokeInstanceMethodInsn i = new InvokeInstanceMethodInsn(DefinitionBuilders.type(thisType), method, codeBuilders);
        this.setNext(i);
        i.setPrev(this);
        return i;
    }

    default InvokeInstanceMethodInsn invoke(NameDefinition methodName, CodeInsnBuilderLike... codeBuilders) {
        InvokeInstanceMethodInsn i = new InvokeInstanceMethodInsn(methodName, codeBuilders);
        i.setPrev(this);
        this.setNext(i);
        return i;
    }

    default InvokeInstanceMethodInsn invoke(String methodName, CodeInsnBuilderLike... codeBuilders) {
        return invoke(DefinitionBuilders.name(methodName), codeBuilders);
    }
}