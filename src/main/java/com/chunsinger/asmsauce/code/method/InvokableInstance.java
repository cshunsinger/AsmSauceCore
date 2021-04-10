package com.chunsinger.asmsauce.code.method;

import com.chunsinger.asmsauce.code.CodeInsnBuilderLike;
import com.chunsinger.asmsauce.definitions.NameDefinition;

import java.lang.reflect.Method;

import static com.chunsinger.asmsauce.DefinitionBuilders.name;
import static com.chunsinger.asmsauce.DefinitionBuilders.type;

public interface InvokableInstance extends CodeInsnBuilderLike {
    default InvokeInstanceMethodInsn invoke(Class<?> thisType, Method method, CodeInsnBuilderLike... codeBuilders) {
        InvokeInstanceMethodInsn i = new InvokeInstanceMethodInsn(type(thisType), method, codeBuilders);
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
        return invoke(name(methodName), codeBuilders);
    }
}