package io.github.chunsinger.asmsauce.code.method;

import io.github.chunsinger.asmsauce.MethodBuildingContext;
import io.github.chunsinger.asmsauce.code.CodeInsnBuilderLike;
import io.github.chunsinger.asmsauce.code.branch.condition.ConditionBuilderLike;
import io.github.chunsinger.asmsauce.code.branch.condition.NullConditionBuilderLike;
import io.github.chunsinger.asmsauce.definitions.*;
import io.github.chunsinger.asmsauce.DefinitionBuilders;
import io.github.chunsinger.asmsauce.definitions.*;

import static aj.org.objectweb.asm.Opcodes.DUP;
import static aj.org.objectweb.asm.Opcodes.NEW;
import static io.github.chunsinger.asmsauce.modifiers.AccessModifiers.publicOnly;

public class InstantiateObjectInsn extends InvocationInsn implements InvokableInstance, ConditionBuilderLike, NullConditionBuilderLike {
    /**
     * Minimum required information to write bytecode to construct a new object is the type being
     * instantiated, and the code builders to build up the parameters for calling the constructor.
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
    public void build(MethodBuildingContext context) {
        //The base class will stack parameters and invoke the constructor
        //but first the type must be created and stacked
        String jvmType = method.getOwner().getJvmTypeName(context.getClassContext().getJvmTypeName());

        //Create a new typed reference and place it onto the stack
        context.getMethodVisitor().visitTypeInsn(NEW, jvmType);
        context.pushStack(method.getOwner());

        //A constructor call, like a method call, consumes `this` as well as the parameters
        //The reference must be duped otherwise the constructor call will completely remove the new reference from the stack
        context.getMethodVisitor().visitInsn(DUP);
        context.pushStack(context.peekStack());

        //Now let the constructor call happen
        super.build(context);
    }
}