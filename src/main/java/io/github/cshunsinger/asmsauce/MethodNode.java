package io.github.cshunsinger.asmsauce;

import io.github.cshunsinger.asmsauce.code.CodeInsnBuilderLike;
import io.github.cshunsinger.asmsauce.definitions.*;
import io.github.cshunsinger.asmsauce.modifiers.AccessModifiers;
import lombok.Getter;
import org.objectweb.asm.MethodVisitor;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.github.cshunsinger.asmsauce.ClassBuildingContext.context;
import static io.github.cshunsinger.asmsauce.DefinitionBuilders.*;

/**
 * Represents a method being built on a new class.
 * This class defines the method header (or its definition) as well as the method body which is represented as a list
 * of instruction objects.
 */
public class MethodNode {
    /**
     * The definition of this method being generated.
     * @return The definition of this method being generated.
     */
    @Getter
    protected final CompleteMethodDefinition definition;
    /**
     * The code builders which make up the code body of this method being generated.
     */
    protected final List<CodeInsnBuilderLike> methodBody;

    /**
     * Creates a new method node which defines and implements a method to generate.
     * @param definition The definition of the method to generate.
     * @param methodBody The code builders whose bytecode makes up this method's implementation.
     */
    protected MethodNode(CompleteMethodDefinition definition, CodeInsnBuilderLike... methodBody) {
        if(definition == null)
            throw new IllegalArgumentException("Method definition cannot be null.");
        List<CodeInsnBuilderLike> methodBodyList = Stream.of(methodBody)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());

        this.definition = definition;
        this.methodBody = methodBodyList;
    }

    /**
     * Defines this method as part of a class being generated and generates the bytecode which makes up the body
     * of this method being generated.
     */
    public void build() {
        CompleteMethodDefinition updatedMethodDefinition = new CompleteMethodDefinition(
            type(ThisClass.class),
            definition.getModifiers(),
            definition.getName(),
            definition.getReturnType(),
            definition.getParameters(),
            definition.getThrowing()
        );

        MethodVisitor methodVisitor = context().getClassWriter().visitMethod(
            updatedMethodDefinition.getModifiers().getJvmModifiers(),
            updatedMethodDefinition.getName().getName(),
            updatedMethodDefinition.jvmMethodSignature(),
            null,
            updatedMethodDefinition.getThrowing().getJvmExceptions()
        );

        List<ParamDefinition> methodParameters = new ArrayList<>();
        //If the method being built is an instance method, then make "this" be the first local variable
        if(!updatedMethodDefinition.getModifiers().isStatic())
            methodParameters.add(p("this", ThisClass.class));
        //Add the defined parameters for the method to the context
        methodParameters.addAll(updatedMethodDefinition.getParameters().getParams());

        //Start the method building context
        new MethodBuildingContext(methodVisitor, updatedMethodDefinition, context(), methodParameters);

        methodVisitor.visitCode();
        methodBody.stream().filter(Objects::nonNull).forEach(codeBuilder -> codeBuilder.getFirstInStack().buildClean());
        methodVisitor.visitMaxs(-1, -1); //COMPUTE_MAXS is enabled
        methodVisitor.visitEnd();

        //Stop the method building context
        MethodBuildingContext.reset();
    }

    /**
     * Creates a method for a class being generated. The generated method will have a "void" return type and will not
     * have a "throws" clause.
     * @param modifiers The access modifiers of this method.
     * @param name The name of this method.
     * @param parameters The set of parameters of this method.
     * @param methodBody The code builders which make up the body of this method.
     * @return A new MethodNode, which defines a method header and its code body.
     * @see #method(AccessModifiers, NameDefinition, ParametersDefinition, ThrowsDefinition, CodeInsnBuilderLike...)
     * @see #method(AccessModifiers, NameDefinition, ParametersDefinition, TypeDefinition, CodeInsnBuilderLike...)
     * @see #method(AccessModifiers, NameDefinition, ParametersDefinition, TypeDefinition, ThrowsDefinition, CodeInsnBuilderLike...)
     */
    public static MethodNode method(AccessModifiers modifiers,
                                    NameDefinition name,
                                    ParametersDefinition parameters,
                                    CodeInsnBuilderLike... methodBody) {
        return method(modifiers, name, parameters, voidType(), methodBody);
    }

    /**
     * Creates a method for a class being generated. The generated method will have the specified return type, and will
     * not have a "throws" clause.
     * @param modifiers The access modifiers of this method.
     * @param name The name of this method.
     * @param parameters The set of parameters of this method.
     * @param returnType The type of value or object to be returned by this method.
     * @param methodBody The code builders which make up the body of this method.
     * @return A new MethodNode, which defines a method header and its code body.
     * @see #method(AccessModifiers, NameDefinition, ParametersDefinition, CodeInsnBuilderLike...)
     * @see #method(AccessModifiers, NameDefinition, ParametersDefinition, ThrowsDefinition, CodeInsnBuilderLike...)
     * @see #method(AccessModifiers, NameDefinition, ParametersDefinition, TypeDefinition, ThrowsDefinition, CodeInsnBuilderLike...)
     */
    public static MethodNode method(AccessModifiers modifiers,
                                    NameDefinition name,
                                    ParametersDefinition parameters,
                                    TypeDefinition returnType,
                                    CodeInsnBuilderLike... methodBody) {
        return method(modifiers, name, parameters, returnType, noThrows(), methodBody);
    }

    /**
     * Creates a method for a class being generated. The generated method will have a 'void' return type, and will have
     * a "throws" clause.
     * @param modifiers The access modifiers of this method.
     * @param name The name of this method.
     * @param parameters The set of parameters of this method.
     * @param throwing The throws clause of this method.
     * @param methodBody The code builders which make up the body of this method.
     * @return A new MethodNode, which defines a method header and its code body.
     * @see #method(AccessModifiers, NameDefinition, ParametersDefinition, CodeInsnBuilderLike...)
     * @see #method(AccessModifiers, NameDefinition, ParametersDefinition, TypeDefinition, CodeInsnBuilderLike...)
     * @see #method(AccessModifiers, NameDefinition, ParametersDefinition, TypeDefinition, ThrowsDefinition, CodeInsnBuilderLike...)
     */
    public static MethodNode method(AccessModifiers modifiers,
                                    NameDefinition name,
                                    ParametersDefinition parameters,
                                    ThrowsDefinition throwing,
                                    CodeInsnBuilderLike... methodBody) {
        return method(modifiers, name, parameters, voidType(), throwing, methodBody);
    }

    /**
     * Creates a method for a class being generated. The generated method will have a return type, and will have a
     * "throws" clause.
     * @param modifiers The access modifiers of this method.
     * @param name The name of this method.
     * @param parameters The set of parameters of this method.
     * @param returnType The type of value or object to be returned by this method.
     * @param throwing The throws clause of this method.
     * @param methodBody The code builders which make up the body of this method.
     * @return A new MethodNode, which defines a method header and its code body.
     * @see #method(AccessModifiers, NameDefinition, ParametersDefinition, CodeInsnBuilderLike...)
     * @see #method(AccessModifiers, NameDefinition, ParametersDefinition, ThrowsDefinition, CodeInsnBuilderLike...)
     * @see #method(AccessModifiers, NameDefinition, ParametersDefinition, TypeDefinition, CodeInsnBuilderLike...)
     */
    public static MethodNode method(AccessModifiers modifiers,
                                    NameDefinition name,
                                    ParametersDefinition parameters,
                                    TypeDefinition returnType,
                                    ThrowsDefinition throwing,
                                    CodeInsnBuilderLike... methodBody) {
        CompleteMethodDefinition definition = new CompleteMethodDefinition(type(ThisClass.class), modifiers, name, returnType, parameters, throwing);
        return new MethodNode(definition, methodBody);
    }
}