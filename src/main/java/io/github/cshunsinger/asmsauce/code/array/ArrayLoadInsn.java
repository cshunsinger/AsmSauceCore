package io.github.cshunsinger.asmsauce.code.array;

import io.github.cshunsinger.asmsauce.code.CodeInsnBuilderLike;
import io.github.cshunsinger.asmsauce.code.branch.condition.BooleanConditionBuilderLike;
import io.github.cshunsinger.asmsauce.code.branch.condition.ConditionBuilderLike;
import io.github.cshunsinger.asmsauce.code.branch.condition.NullConditionBuilderLike;
import io.github.cshunsinger.asmsauce.code.field.FieldAccessibleInstance;
import io.github.cshunsinger.asmsauce.code.field.FieldAssignableInstance;
import io.github.cshunsinger.asmsauce.code.math.MathOperandInstance;
import io.github.cshunsinger.asmsauce.code.method.InvokableInstance;
import io.github.cshunsinger.asmsauce.definitions.TypeDefinition;

import java.util.Map;

import static io.github.cshunsinger.asmsauce.MethodBuildingContext.context;
import static io.github.cshunsinger.asmsauce.definitions.TypeDefinition.*;
import static io.github.cshunsinger.asmsauce.definitions.TypeDefinition.DOUBLE;
import static io.github.cshunsinger.asmsauce.definitions.TypeDefinition.FLOAT;
import static io.github.cshunsinger.asmsauce.definitions.TypeDefinition.LONG;
import static org.objectweb.asm.Opcodes.*;

/**
 * This class represents an instruction to attempt to load a value from an array at a specified index. This bytecode
 * builder will produce the bytecode to stack an array index onto the stack, which should already contain an array
 * at the top, and then retrieve a value from that array at that array index.
 */
public class ArrayLoadInsn extends ArrayAccessInsn implements
    InvokableInstance, FieldAccessibleInstance, FieldAssignableInstance, MathOperandInstance,
    ConditionBuilderLike, BooleanConditionBuilderLike, NullConditionBuilderLike {

    private static final Map<TypeDefinition, Integer> TYPE_OPCODES = Map.of(
        BOOLEAN, BALOAD,
        BYTE, BALOAD,
        CHAR, CALOAD,
        SHORT, SALOAD,
        INT, IALOAD,
        LONG, LALOAD,
        FLOAT, FALOAD,
        DOUBLE, DALOAD
    );

    /**
     * Creates a new code builder which will retrieve a value from an array at a given index.
     * @param arrayIndexCode A code builder which will place an int onto the stack as the array index from which to retrieve the array value.
     * @throws IllegalArgumentException If the code builder for stacking an array index is null.
     */
    public ArrayLoadInsn(CodeInsnBuilderLike arrayIndexCode) {
        super(arrayIndexCode);
    }

    @Override
    public void buildArrayInsn(TypeDefinition arrayType) {
        TypeDefinition singletonType = arrayType.getComponentType();

        //Execute the bytecode instruction to load value from array
        int instruction = TYPE_OPCODES.getOrDefault(singletonType, AALOAD);
        context().getMethodVisitor().visitInsn(instruction);
        context().popStack(2); //pop array and array index off of stack
        context().pushStack(singletonType); //Push the type of value loaded from the array
    }
}