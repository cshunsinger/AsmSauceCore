package io.github.cshunsinger.asmsauce.code.method;

import io.github.cshunsinger.asmsauce.code.CodeInsnBuilderLike;
import io.github.cshunsinger.asmsauce.code.array.AccessibleArrayLike;
import io.github.cshunsinger.asmsauce.code.branch.condition.BooleanConditionBuilderLike;
import io.github.cshunsinger.asmsauce.code.branch.condition.ConditionBuilderLike;
import io.github.cshunsinger.asmsauce.code.branch.condition.NullConditionBuilderLike;
import io.github.cshunsinger.asmsauce.code.cast.ImplicitConversionInsn;
import io.github.cshunsinger.asmsauce.code.field.FieldAccessibleInstance;
import io.github.cshunsinger.asmsauce.code.field.FieldAssignableInstance;
import io.github.cshunsinger.asmsauce.code.math.MathOperandInstance;
import io.github.cshunsinger.asmsauce.definitions.CompleteMethodDefinition;
import io.github.cshunsinger.asmsauce.definitions.MethodDefinition;
import io.github.cshunsinger.asmsauce.definitions.NameDefinition;
import io.github.cshunsinger.asmsauce.definitions.TypeDefinition;

import java.lang.reflect.Method;

import static io.github.cshunsinger.asmsauce.DefinitionBuilders.*;
import static io.github.cshunsinger.asmsauce.modifiers.AccessModifiers.customAccess;

/**
 * Code builder for invoking an instance method on the instance at the top of the stack, with parameters.
 */
public class InvokeInstanceMethodInsn extends InvocationInsn implements
    InvokableInstance, FieldAccessibleInstance, FieldAssignableInstance, MathOperandInstance,
    ConditionBuilderLike, BooleanConditionBuilderLike, NullConditionBuilderLike, AccessibleArrayLike {

    /**
     * Invoke instance method.
     * @param thisType The instance type which owns the method to call.
     * @param method The method to call.
     * @param codeBuilders The code builders to stack the values to pass as parameters to the method.
     */
    public InvokeInstanceMethodInsn(TypeDefinition thisType, Method method, CodeInsnBuilderLike... codeBuilders) {
        super(new CompleteMethodDefinition(
            thisType,
            customAccess(validateMethod(method).getModifiers()),
            name(method.getName()),
            type(method.getReturnType()),
            parameters(method.getParameterTypes()),
            throwing(method.getExceptionTypes())
        ), codeBuilders);
    }

    /**
     * Invoke instance method.
     * @param methodName The name of the instance method to call.
     * @param codeBuilders The code builders to stack the values to pass as parameters to the method.
     */
    public InvokeInstanceMethodInsn(NameDefinition methodName, CodeInsnBuilderLike... codeBuilders) {
        super(new MethodDefinition(null, null, methodName, null, null, null), codeBuilders);
    }

    private static Method validateMethod(Method method) {
        if(method == null)
            throw new IllegalArgumentException("Method cannot be null.");
        return method;
    }

    @Override
    public void build() {
        //The element at the top of the stack is what is the element that the instance method will be invoked against
        //Perform any necessary implicit casting first
        if(method.getOwner() != null) //Ignore implicit cast if method owner type will be implied
            new ImplicitConversionInsn(super.method.getOwner()).build();

        //Perform the method call
        super.build();
    }
}