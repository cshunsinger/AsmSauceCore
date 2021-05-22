package io.github.cshunsinger.asmsauce.code.array;

import io.github.cshunsinger.asmsauce.code.CodeInsnBuilder;
import io.github.cshunsinger.asmsauce.code.CodeInsnBuilderLike;
import io.github.cshunsinger.asmsauce.code.cast.ImplicitConversionInsn;
import io.github.cshunsinger.asmsauce.definitions.TypeDefinition;

import static io.github.cshunsinger.asmsauce.DefinitionBuilders.type;
import static io.github.cshunsinger.asmsauce.MethodBuildingContext.context;

/**
 * This class is the base class representing instructions which access an array element. This means the instructions
 * for storing elements into an array and retrieving elements from an array will inherit this class.
 */
public abstract class ArrayAccessInsn extends CodeInsnBuilder {
    protected final CodeInsnBuilderLike arrayIndexCode;

    /**
     * Creates a new code builder which will access an array at a given index.
     * @param arrayIndexCode A code builder which will place an int onto the stack as the array index from which to retrieve the array value.
     * @throws IllegalArgumentException If the code builder for stacking an array index is null.
     */
    protected ArrayAccessInsn(CodeInsnBuilderLike arrayIndexCode) {
        if(arrayIndexCode == null)
            throw new IllegalArgumentException("The array index code builder cannot be null.");

        this.arrayIndexCode = arrayIndexCode.getFirstInStack();
    }

    @Override
    public void build() {
        validateArrayTypeStacked();
        TypeDefinition arrayType = context().peekStack();

        int startingStackSize = context().stackSize();
        arrayIndexCode.build();
        int endingStackSize = context().stackSize();
        int numStacked = endingStackSize - startingStackSize;

        //Verify that exactly 1 value was stacked
        if(numStacked != 1)
            throw new IllegalStateException("Expected 1 element to be stacked. Got " + numStacked + " instead.");

        //Verify that the correct type of value was stacked (a type that can be implicitly converted to int)
        TypeDefinition stackedType = context().peekStack();
        if(!ImplicitConversionInsn.implicitCastAllowed(stackedType, type(int.class)))
            throw new IllegalStateException("Expected an int value to be stacked. Found %s instead.".formatted(stackedType.getClassName()));

        //Perform implicit cast into int
        new ImplicitConversionInsn(type(int.class));

        //Generate the actual array bytecode
        buildArrayInsn(arrayType);

        super.build();
    }

    protected abstract void buildArrayInsn(TypeDefinition arrayType);

    static void validateArrayTypeStacked() {
        TypeDefinition stackedType = context().peekStack();
        if(!stackedType.isArray())
            throw new IllegalStateException("Array type expected on stack. Got type %s instead.".formatted(stackedType.getClassName()));
    }
}