package io.github.cshunsinger.asmsauce.code.method;

import io.github.cshunsinger.asmsauce.code.CodeInsnBuilderLike;
import io.github.cshunsinger.asmsauce.code.branch.condition.ConditionBuilderLike;
import io.github.cshunsinger.asmsauce.code.branch.condition.NullConditionBuilderLike;
import io.github.cshunsinger.asmsauce.DefinitionBuilders;
import io.github.cshunsinger.asmsauce.definitions.*;

import static io.github.cshunsinger.asmsauce.MethodBuildingContext.context;
import static org.objectweb.asm.Opcodes.DUP;
import static org.objectweb.asm.Opcodes.NEW;
import static io.github.cshunsinger.asmsauce.modifiers.AccessModifiers.publicOnly;

/**
 * Code builder which generates the bytecode to invoke a constructor and instantiate a new object of a given type.
 */
public class InstantiateObjectInsn extends InvocationInsn implements InvokableInstance, ConditionBuilderLike, NullConditionBuilderLike {
    /**
     * Minimum required information to write bytecode to construct a new object is the type being
     * instantiated, and the code builders to build up the parameters for calling the constructor.
     * @param instantiatedType The type of object being instantiated.
     * @param paramBuilders The code builders for stacking the parameters to pass to the constructor being invoked.
     */
    public InstantiateObjectInsn(TypeDefinition<?> instantiatedType, CodeInsnBuilderLike... paramBuilders) {
        super(new MethodDefinition<>(
            instantiatedType,
            null,
            NameDefinition.CONSTRUCTOR_NAME_DEFINITION,
            null,
            DefinitionBuilders.voidType(),
            null
        ), paramBuilders);

        //MethodDefinition does not do a null check on instantiatedType because it's considered an
        //incomplete object. So this is one case where a null check is required.
        if(instantiatedType == null)
            throw new IllegalArgumentException("Instantiated type cannot be null.");
    }

    /**
     * The required information to write bytecode to construct a new object, without having to
     * use context to derive certain data. This means knowing the type of object being instantiated,
     * and the types of all of the parameters that will be passed to the constructor and the code
     * builders to stack those parameters.
     * @param instantiatedType The type of object being instantiated.
     * @param parameters The parameters defined in the constructor to be called.
     * @param paramBuilders The code builders for stacking the parameters to pass to the constructor being invoked.
     */
    public InstantiateObjectInsn(TypeDefinition<?> instantiatedType, ParametersDefinition parameters, CodeInsnBuilderLike... paramBuilders) {
        super(new CompleteMethodDefinition<>(
            instantiatedType, //CompleteMethodDefinition will verify that instantiatedType is not null
            publicOnly(), //Not too relevant here
            NameDefinition.CONSTRUCTOR_NAME_DEFINITION,
            DefinitionBuilders.voidType(),
            parameters,
            DefinitionBuilders.noThrows() //does not matter here
        ), paramBuilders);
    }

    @Override
    public void build() {
        //The base class will stack parameters and invoke the constructor
        //but first the type must be created and stacked
        String jvmType = method.getOwner().getJvmTypeName(context().getClassContext().getJvmTypeName());

        //Create a new typed reference and place it onto the stack
        context().getMethodVisitor().visitTypeInsn(NEW, jvmType);
        context().pushStack(method.getOwner());

        //A constructor call, like a method call, consumes `this` as well as the parameters
        //The reference must be duped otherwise the constructor call will completely remove the new reference from the stack
        context().getMethodVisitor().visitInsn(DUP);
        context().pushStack(context().peekStack());

        //Now let the constructor call happen
        super.build();
    }
}