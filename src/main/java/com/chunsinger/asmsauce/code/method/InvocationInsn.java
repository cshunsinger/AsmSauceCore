package com.chunsinger.asmsauce.code.method;

import com.chunsinger.asmsauce.MethodBuildingContext;
import com.chunsinger.asmsauce.code.CodeInsnBuilder;
import com.chunsinger.asmsauce.code.CodeInsnBuilderLike;
import com.chunsinger.asmsauce.code.cast.ImplicitConversionInsn;
import com.chunsinger.asmsauce.definitions.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static aj.org.objectweb.asm.Opcodes.*;

public abstract class InvocationInsn extends CodeInsnBuilder {
    protected final List<CodeInsnBuilderLike> parameterBuilders;
    protected MethodDefinition<?, ?> method;

    protected InvocationInsn(MethodDefinition<?, ?> method, CodeInsnBuilderLike[] parameterBuilders) {
        //Make sure the number of code builders is correct for the number of parameters of the instance method.
        if(method.getParameters() != null) {
            int numParameters = method.getParameters().count();
            int numBuilders = parameterBuilders.length;
            if(numBuilders != numParameters) {
                throw new IllegalArgumentException(("Expected %d builders to satisfy the method parameters." +
                    " Found %d builders instead.").formatted(numParameters, numBuilders)
                );
            }
        }

        this.parameterBuilders = Arrays.stream(parameterBuilders).map(CodeInsnBuilderLike::getFirstInStack).collect(Collectors.toList());
        this.method = method;
    }

    @Override
    public void build(MethodBuildingContext context) {
        //Each paramStackBuilder is expected to put 1 element onto the stack
        stackParameters(context);

        //Complete the method definition to fill in the missing values
        method = method.completeDefinition(context, parameterBuilders.size());

        invokeMethodVisitor(context);

        //Pop each of the parameters off of the stack
        context.popStack(method.getParameters().count());

        if(!method.getModifiers().isStatic())
            context.popStack(); //Pop "this" off of the stack

        if(!method.getReturnType().isVoid())
            context.pushStack(method.getReturnType()); //Push method return type onto the stack

        super.build(context);
    }

    protected void invokeMethodVisitor(MethodBuildingContext context) {
        int instruction = INVOKEVIRTUAL;
        boolean isInterface = false;
        if(method.getModifiers().isStatic())
            instruction = INVOKESTATIC;
        else if(method.getModifiers().isPrivate() || method.getName().isConstructorName())
            instruction = INVOKESPECIAL; //Use for constructor calls and private methods
        else if(method.getOwner().getType().isInterface()) {
            instruction = INVOKEINTERFACE;
            isInterface = true;
        }

        String ownerTypeName = method.getOwner().getJvmTypeName(context.getClassContext().getJvmTypeName());
        String methodName = method.getName().getName();
        String methodSignature = method.jvmMethodSignature();

        context.getMethodVisitor().visitMethodInsn(instruction, ownerTypeName, methodName, methodSignature, isInterface);
    }

    private void stackParameters(MethodBuildingContext context) {
        if(method.getParameters() != null) {
            ParametersDefinition methodParameters = method.getParameters();
            for(int i = 0; i < methodParameters.count(); i++) {
                TypeDefinition<?> paramType = methodParameters.get(i);

                CodeInsnBuilderLike builder = parameterBuilders.get(i);
                stackParameter(context, builder);

                //Perform implicit casting if necessary
                new ImplicitConversionInsn(paramType).build(context);
            }
        }
        else {
            for(CodeInsnBuilderLike builder : parameterBuilders) {
                stackParameter(context, builder);
            }
        }
    }

    private void stackParameter(MethodBuildingContext context, CodeInsnBuilderLike builder) {
        int preStackCount = context.stackSize();
        builder.build(context);
        int postStackCount = context.stackSize();

        //Make sure each code builder adds exactly 1 element to the stack
        if(postStackCount != preStackCount+1)
            throw new IllegalStateException("Code builder expected to add 1 element to the stack. Instead %d elements were added.".formatted(postStackCount-preStackCount));
    }
}