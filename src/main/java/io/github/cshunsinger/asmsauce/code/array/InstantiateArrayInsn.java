package io.github.cshunsinger.asmsauce.code.array;

import io.github.cshunsinger.asmsauce.code.CodeInsnBuilder;
import io.github.cshunsinger.asmsauce.code.CodeInsnBuilderLike;
import io.github.cshunsinger.asmsauce.code.cast.ImplicitConversionInsn;
import io.github.cshunsinger.asmsauce.definitions.TypeDefinition;

import static io.github.cshunsinger.asmsauce.MethodBuildingContext.context;
import static org.objectweb.asm.Opcodes.ANEWARRAY;
import static org.objectweb.asm.Opcodes.NEWARRAY;

/**
 * This instruction class generates the bytecode to instantiate a new array.
 */
public class InstantiateArrayInsn extends CodeInsnBuilder implements AccessibleArrayLike {
    private final TypeDefinition componentType;
    private final CodeInsnBuilderLike lengthBuilder;

    /**
     * Creates a new instance for generating bytecode to instantiate a new array. The instantiated array will have the
     * desired length and component type, and will have 1 dimension.
     * @param componentType The component type of the array to instantiate.
     * @param lengthBuilder Code builder which will stack an int value representing the length of the array to instantiate.
     */
    public InstantiateArrayInsn(TypeDefinition componentType, CodeInsnBuilderLike lengthBuilder) {
        if(componentType == null)
            throw new IllegalArgumentException("Component type cannot be null.");
        if(lengthBuilder == null)
            throw new IllegalArgumentException("Array length instruction cannot be null.");

        this.componentType = componentType;
        this.lengthBuilder = lengthBuilder;
    }

    @Override
    public void build() {
        int originalStackSize = context().stackSize();
        lengthBuilder.build();
        int numStacked = context().stackSize() - originalStackSize;

        if(numStacked != 1)
            throw new IllegalStateException("Expected 1 element to be stacked. Got %d instead.".formatted(numStacked));

        //Make sure the type that was stacked is an int
        new ImplicitConversionInsn(TypeDefinition.INT).build();

        //The length value is now stacked. Create new array with 1 dimension of that length.
        int instruction = componentType.isPrimitive() ? NEWARRAY : ANEWARRAY;
        context().getMethodVisitor().visitTypeInsn(instruction, componentType.getJvmTypeName());
        context().popStack(); //The 'length' int is popped from the stack
        context().pushStack(componentType.getArrayType()); //The array type is placed onto the stack
    }
}