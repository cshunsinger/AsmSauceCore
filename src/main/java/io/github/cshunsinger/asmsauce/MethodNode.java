package io.github.cshunsinger.asmsauce;

import org.objectweb.asm.MethodVisitor;
import io.github.cshunsinger.asmsauce.code.CodeInsnBuilderLike;
import io.github.cshunsinger.asmsauce.definitions.*;
import io.github.cshunsinger.asmsauce.modifiers.AccessModifiers;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Represents a method being build on a new class.
 * This class defines the method header (or its definition) as well as the method body which is represented as a list
 * of instruction objects.
 */
public class MethodNode {
    @Getter
    protected final CompleteMethodDefinition<?, ?> definition;
    protected final List<CodeInsnBuilderLike> methodBody;

    protected MethodNode(CompleteMethodDefinition<?, ?> definition, CodeInsnBuilderLike... methodBody) {
        if(definition == null)
            throw new IllegalArgumentException("Method definition cannot be null.");
        List<CodeInsnBuilderLike> methodBodyList = Stream.of(methodBody)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());

        this.definition = definition;
        this.methodBody = methodBodyList;
    }

    public void build(ClassBuildingContext context) {
        CompleteMethodDefinition<?, ?> updatedMethodDefinition = new CompleteMethodDefinition<>(
            TypeDefinition.fromCustomJvmName(context.getJvmTypeName()),
            definition.getModifiers(),
            definition.getName(),
            definition.getReturnType(),
            definition.getParameters(),
            definition.getThrowing()
        );

        MethodVisitor methodVisitor = context.getClassWriter().visitMethod(
            updatedMethodDefinition.getModifiers().getJvmModifiers(),
            updatedMethodDefinition.getName().getName(),
            updatedMethodDefinition.jvmMethodSignature(),
            null,
            updatedMethodDefinition.getThrowing().getJvmExceptions()
        );

        List<ParamDefinition> methodParameters = new ArrayList<>();
        //If the method being built is an instance method, then make "this" be the first local variable
        if(!updatedMethodDefinition.getModifiers().isStatic())
            methodParameters.add(DefinitionBuilders.p("this", ThisClass.class));
        //Add the defined parameters for the method to the context
        methodParameters.addAll(updatedMethodDefinition.getParameters().getParams());

        MethodBuildingContext methodContext = new MethodBuildingContext(methodVisitor, updatedMethodDefinition, context, methodParameters);

        methodVisitor.visitCode();
        methodBody.stream().filter(Objects::nonNull).forEach(codeBuilder -> codeBuilder.getFirstInStack().buildClean(methodContext));
        methodVisitor.visitMaxs(-1, -1); //COMPUTE_MAXS is enabled
        methodVisitor.visitEnd();
    }

    public static MethodNode method(AccessModifiers modifiers,
                                    NameDefinition name,
                                    ParametersDefinition parameters,
                                    CodeInsnBuilderLike... methodBody) {
        return method(modifiers, name, parameters, DefinitionBuilders.voidType(), methodBody);
    }

    public static MethodNode method(AccessModifiers modifiers,
                                    NameDefinition name,
                                    ParametersDefinition parameters,
                                    TypeDefinition<?> returnType,
                                    CodeInsnBuilderLike... methodBody) {
        return method(modifiers, name, parameters, returnType, DefinitionBuilders.noThrows(), methodBody);
    }

    public static MethodNode method(AccessModifiers modifiers,
                                    NameDefinition name,
                                    ParametersDefinition parameters,
                                    ThrowsDefinition throwing,
                                    CodeInsnBuilderLike... methodBody) {
        return method(modifiers, name, parameters, DefinitionBuilders.voidType(), throwing, methodBody);
    }

    public static MethodNode method(AccessModifiers modifiers,
                                    NameDefinition name,
                                    ParametersDefinition parameters,
                                    TypeDefinition<?> returnType,
                                    ThrowsDefinition throwing,
                                    CodeInsnBuilderLike... methodBody) {
        CompleteMethodDefinition<?, ?> definition = new CompleteMethodDefinition<>(DefinitionBuilders.type(ThisClass.class), modifiers, name, returnType, parameters, throwing);
        return new MethodNode(definition, methodBody);
    }
}