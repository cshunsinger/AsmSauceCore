package io.github.cshunsinger.asmsauce.code.array;

import io.github.cshunsinger.asmsauce.code.CodeInsnBuilder;
import io.github.cshunsinger.asmsauce.code.CodeInsnBuilderLike;
import io.github.cshunsinger.asmsauce.code.cast.ImplicitConversionInsn;
import io.github.cshunsinger.asmsauce.definitions.TypeDefinition;

import java.util.Map;

import static io.github.cshunsinger.asmsauce.MethodBuildingContext.context;
import static io.github.cshunsinger.asmsauce.definitions.TypeDefinition.*;
import static io.github.cshunsinger.asmsauce.definitions.TypeDefinition.DOUBLE;
import static io.github.cshunsinger.asmsauce.definitions.TypeDefinition.FLOAT;
import static io.github.cshunsinger.asmsauce.definitions.TypeDefinition.LONG;
import static org.objectweb.asm.Opcodes.*;

/**
 * This instruction class generates the bytecode to instantiate a new array.
 */
public class InstantiateArrayInsn extends CodeInsnBuilder implements AccessibleArrayLike {
    private static final Map<TypeDefinition, Integer> ARRAY_OPERANDS = Map.ofEntries(
        Map.entry(BYTE, T_BYTE),
        Map.entry(SHORT, T_SHORT),
        Map.entry(CHAR, T_CHAR),
        Map.entry(INT, T_INT),
        Map.entry(LONG, T_LONG),
        Map.entry(FLOAT, T_FLOAT),
        Map.entry(DOUBLE, T_DOUBLE),
        Map.entry(BOOLEAN, T_BOOLEAN)
    );

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
        new ImplicitConversionInsn(INT).build();

        //The length value is now stacked. Create new array with 1 dimension of that length.
        if(componentType.isPrimitive())
            context().getMethodVisitor().visitIntInsn(NEWARRAY, ARRAY_OPERANDS.get(componentType));
        else
            context().getMethodVisitor().visitTypeInsn(ANEWARRAY, componentType.getJvmTypeName());

        context().popStack(); //The 'length' int is popped from the stack
        context().pushStack(componentType.getArrayType()); //The array type is placed onto the stack
    }
}