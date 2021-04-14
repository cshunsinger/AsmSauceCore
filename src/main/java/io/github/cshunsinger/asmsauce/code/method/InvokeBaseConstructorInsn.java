package io.github.cshunsinger.asmsauce.code.method;

import io.github.cshunsinger.asmsauce.DefinitionBuilders;
import io.github.cshunsinger.asmsauce.MethodBuildingContext;
import io.github.cshunsinger.asmsauce.code.CodeInsnBuilderLike;
import io.github.cshunsinger.asmsauce.code.CodeBuilders;
import io.github.cshunsinger.asmsauce.definitions.*;

import java.lang.reflect.Constructor;

import static io.github.cshunsinger.asmsauce.modifiers.AccessModifiers.customAccess;

/**
 * Creates code builder to invoke another constructor at the beginning of a defined constructor.
 * This is used for the this() or super() call built at the beginning of a constructor.
 */
public class InvokeBaseConstructorInsn extends InvocationInsn {
    /**
     * Invoke base constructor.
     * @param constructor The constructor to call.
     * @param paramStackBuilders The code builders which stack the values to be passed to the constructor as parameters.
     * @throws IllegalArgumentException If constructor is null.
     */
    @SuppressWarnings("unchecked")
    public InvokeBaseConstructorInsn(Constructor<?> constructor, CodeInsnBuilderLike... paramStackBuilders) {
        //Only need to use throwIfNull for first parameter. After that, constructor is verified as non-null if no exception was thrown
        this(
            DefinitionBuilders.type(throwIfNull(constructor).getDeclaringClass()),
            DefinitionBuilders.parameters(constructor.getParameterTypes()),
            DefinitionBuilders.throwing((Class<? extends Exception>[])constructor.getExceptionTypes()),
            paramStackBuilders
        );
    }

    private static Constructor<?> throwIfNull(Constructor<?> c) {
        if(c == null)
            throw new IllegalArgumentException("Constructor cannot be null.");
        else
            return c;
    }

    /**
     * Invoke base constructor.
     * @param owner The owner of the constructor.
     * @param parameters The parameters of the constructor.
     * @param throwsDefinition The throws definition of the constructor.
     * @param paramStackBuilders The code builders which stack the values to be passed to the constructor as parameters.
     */
    public InvokeBaseConstructorInsn(TypeDefinition<?> owner, ParametersDefinition parameters, ThrowsDefinition throwsDefinition, CodeInsnBuilderLike... paramStackBuilders) {
        super(new CompleteMethodDefinition<>(
            owner,
            customAccess(0), //access level is irrelevant here
            NameDefinition.CONSTRUCTOR_NAME_DEFINITION,
            DefinitionBuilders.voidType(),
            parameters,
            throwsDefinition
        ), paramStackBuilders);
    }

    @Override
    public void build(MethodBuildingContext context) {
        //Stack "this" onto the stack
        CodeBuilders.this_().build(context);

        //Generate bytecode to load the parameters, and then generate bytecode to invoke the constructor
        super.build(context);
    }
}