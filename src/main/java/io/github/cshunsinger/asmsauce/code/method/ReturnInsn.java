package io.github.cshunsinger.asmsauce.code.method;

import io.github.cshunsinger.asmsauce.code.CodeInsnBuilder;
import io.github.cshunsinger.asmsauce.code.CodeInsnBuilderLike;
import io.github.cshunsinger.asmsauce.code.cast.ImplicitConversionInsn;
import io.github.cshunsinger.asmsauce.definitions.TypeDefinition;

import static io.github.cshunsinger.asmsauce.MethodBuildingContext.context;
import static org.objectweb.asm.Opcodes.*;

/**
 * Code builder which builds the bytecode to return from a method. Can return void or return a value.
 */
public class ReturnInsn extends CodeInsnBuilder {
    private final CodeInsnBuilderLike returnValueBuilder;

    /**
     * Defines a return code builder which returns nothing. Used for void methods.
     */
    public ReturnInsn() {
        this(null);
    }

    /**
     * Defines a return code builder which returns a value stacked by the provided code builder.
     * @param returnValueBuilder The code builder which stacks the value to be returned. If null, then a 'void' return
     *                           bytecode instruction will be generated instead.
     */
    public ReturnInsn(CodeInsnBuilderLike returnValueBuilder) {
        this.returnValueBuilder = returnValueBuilder != null ? returnValueBuilder.getFirstInStack() : null;
    }

    @Override
    public void build() {
        if(returnValueBuilder != null)
            returnValueBuilder.build();

        if(context().getCurrentMethod().getReturnType().isVoid()) {
            //If method is void method then return nothing
            context().getMethodVisitor().visitInsn(RETURN);
        }
        else /* Method being implemented here is not a void method */ {
            //Make sure the return type isn't completely stupid
            validateReturnType();

            //Implicit casting if necessary - either this will throw an exception, or typeStack.peek() will be equal to methodReturnType
            new ImplicitConversionInsn(context().getCurrentMethod().getReturnType()).build();

            //Determine and write the correct return opcode based on the return type
            context().getMethodVisitor().visitInsn(retOpcode(context().popStack()));
        }
    }

    private void validateReturnType() {
        if(context().isStackEmpty()) {
            //Method does not return void, but nothing on the stack to return therefore throw exception
            throw new IllegalStateException(
                "Method being implemented has a return type of %s but no value on the stack left to return."
                    .formatted(context().returnType().getType().getName())
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