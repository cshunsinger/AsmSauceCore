package io.github.cshunsinger.asmsauce.code.method;

import io.github.cshunsinger.asmsauce.code.CodeInsnBuilderLike;
import io.github.cshunsinger.asmsauce.definitions.NameDefinition;
import io.github.cshunsinger.asmsauce.DefinitionBuilders;

import java.lang.reflect.Method;

/**
 * Interface representing any code builder which is capable of stacking a reference value which can have an instance
 * method executed against it.
 */
public interface InvokableInstance extends CodeInsnBuilderLike {
    /**
     * Invoke an instance method against the instance on the top of the stack.
     * @param thisType The instance type.
     * @param method The instance method to invoke.
     * @param codeBuilders The code builders to stack the parameters to pass to the instance method.
     * @return A code builder which generates bytecode to invoke a method against an object instance.
     */
    default InvokeInstanceMethodInsn invoke(Class<?> thisType, Method method, CodeInsnBuilderLike... codeBuilders) {
        InvokeInstanceMethodInsn i = new InvokeInstanceMethodInsn(DefinitionBuilders.type(thisType), method, codeBuilders);
        this.setNext(i);
        i.setPrev(this);
        return i;
    }

    /**
     * Invoke an instance method against the instance on the top of the stack.
     * @param methodName The name of the instance method to execute.
     * @param codeBuilders The code builders to stack the parameters to pass to the instance method.
     * @return A code builder which generates bytecode to invoke a method against an object instance.
     */
    default InvokeInstanceMethodInsn invoke(NameDefinition methodName, CodeInsnBuilderLike... codeBuilders) {
        InvokeInstanceMethodInsn i = new InvokeInstanceMethodInsn(methodName, codeBuilders);
        i.setPrev(this);
        this.setNext(i);
        return i;
    }

    /**
     * Invoke an instance method against the instance on the top of the stack.
     * @param methodName The name of the instance method to execute.
     * @param codeBuilders The code builders to stack the parameters to pass to the instance method.
     * @return A code builder which generates bytecode to invoke a method against an object instance.
     */
    default InvokeInstanceMethodInsn invoke(String methodName, CodeInsnBuilderLike... codeBuilders) {
        return invoke(DefinitionBuilders.name(methodName), codeBuilders);
    }
}