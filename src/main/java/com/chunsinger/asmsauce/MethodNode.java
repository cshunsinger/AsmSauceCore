package com.chunsinger.asmsauce;

import aj.org.objectweb.asm.MethodVisitor;
import com.chunsinger.asmsauce.code.CodeInsnBuilderLike;
import com.chunsinger.asmsauce.definitions.*;
import com.chunsinger.asmsauce.modifiers.AccessModifiers;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.chunsinger.asmsauce.DefinitionBuilders.*;

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
            methodParameters.add(p("this", ThisClass.class));
        //Add the defined parameters for the method to the context
        methodParameters.addAll(updatedMethodDefinition.getParameters().getParams());

        MethodBuildingContext methodContext = new MethodBuildingContext(methodVisitor, updatedMethodDefinition, context, methodParameters);

        methodVisitor.visitCode();
        methodBody.stream().filter(Objects::nonNull).forEach(codeBuilder -> codeBuilder.getFirstInStack().buildBytecode(methodContext));
        methodVisitor.visitMaxs(-1, -1); //COMPUTE_MAXS is enabled
        methodVisitor.visitEnd();
    }

    public static MethodNode method(AccessModifiers modifiers,
                                    NameDefinition name,
                                    ParametersDefinition parameters,
                                    CodeInsnBuilderLike... methodBody) {
        return method(modifiers, name, parameters, voidType(), methodBody);
    }

    public static MethodNode method(AccessModifiers modifiers,
                                    NameDefinition name,
                                    ParametersDefinition parameters,
                                    TypeDefinition<?> returnType,
                                    CodeInsnBuilderLike... methodBody) {
        return method(modifiers, name, parameters, returnType, noThrows(), methodBody);
    }

    public static MethodNode method(AccessModifiers modifiers,
                                    NameDefinition name,
                                    ParametersDefinition parameters,
                                    ThrowsDefinition throwing,
                                    CodeInsnBuilderLike... methodBody) {
        return method(modifiers, name, parameters, voidType(), throwing, methodBody);
    }

    public static MethodNode method(AccessModifiers modifiers,
                                    NameDefinition name,
                                    ParametersDefinition parameters,
                                    TypeDefinition<?> returnType,
                                    ThrowsDefinition throwing,
                                    CodeInsnBuilderLike... methodBody) {
        CompleteMethodDefinition<?, ?> definition = new CompleteMethodDefinition<>(type(ThisClass.class), modifiers, name, returnType, parameters, throwing);
        return new MethodNode(definition, methodBody);
    }
}