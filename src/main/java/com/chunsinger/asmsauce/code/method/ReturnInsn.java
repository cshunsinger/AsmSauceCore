package com.chunsinger.asmsauce.code.method;

import com.chunsinger.asmsauce.MethodBuildingContext;
import com.chunsinger.asmsauce.code.CodeInsnBuilder;
import com.chunsinger.asmsauce.code.CodeInsnBuilderLike;
import com.chunsinger.asmsauce.code.cast.ImplicitConversionInsn;
import com.chunsinger.asmsauce.definitions.TypeDefinition;

import static aj.org.objectweb.asm.Opcodes.*;

public class ReturnInsn extends CodeInsnBuilder {
    private final CodeInsnBuilderLike returnValueBuilder;

    public ReturnInsn() {
        this(null);
    }

    public ReturnInsn(CodeInsnBuilderLike returnValueBuilder) {
        this.returnValueBuilder = returnValueBuilder != null ? returnValueBuilder.getFirstInStack() : null;
    }

    @Override
    public void build(MethodBuildingContext context) {
        if(returnValueBuilder != null)
            returnValueBuilder.build(context);

        if(context.getCurrentMethod().getReturnType().isVoid()) {
            //If method is void method then return nothing
            context.getMethodVisitor().visitInsn(RETURN);
        }
        else /* Method being implemented here is not a void method */ {
            //Make sure the return type isn't completely stupid
            validateReturnType(context);

            //Implicit casting if necessary - either this will throw an exception, or typeStack.peek() will be equal to methodReturnType
            new ImplicitConversionInsn(context.getCurrentMethod().getReturnType()).build(context);

            //Determine and write the correct return opcode based on the return type
            context.getMethodVisitor().visitInsn(retOpcode(context.popStack()));
        }
    }

    private void validateReturnType(MethodBuildingContext context) {
        if(context.isStackEmpty()) {
            //Method does not return void, but nothing on the stack to return therefore throw exception
            throw new IllegalStateException(
                "Method being implemented has a return type of %s but no value on the stack left to return."
                    .formatted(context.returnType().getType().getName())
            );
        }
    }

    private static int retOpcode(TypeDefinition<?> returnType) {
        Class<?> returnClass = returnType.getType();
        if(!returnClass.isPrimitive())
            return ARETURN; //This is the bytecode instruction when a reference is to be returned

        //Different primitives use different return instructions
        if(returnClass == float.class)
            return FRETURN;
        else if(returnClass == double.class)
            return DRETURN;
        else if(returnClass == long.class)
            return LRETURN;
        else //byte, short, char, or int
            return IRETURN;
    }
}