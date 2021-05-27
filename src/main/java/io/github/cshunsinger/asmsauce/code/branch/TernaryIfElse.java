package io.github.cshunsinger.asmsauce.code.branch;

import io.github.cshunsinger.asmsauce.code.CodeBlock;
import io.github.cshunsinger.asmsauce.code.CodeInsnBuilder;
import io.github.cshunsinger.asmsauce.code.CodeInsnBuilderLike;
import io.github.cshunsinger.asmsauce.code.array.AccessibleArrayLike;
import io.github.cshunsinger.asmsauce.code.branch.condition.BooleanConditionBuilderLike;
import io.github.cshunsinger.asmsauce.code.branch.condition.Condition;
import io.github.cshunsinger.asmsauce.code.branch.condition.ConditionBuilderLike;
import io.github.cshunsinger.asmsauce.code.branch.condition.NullConditionBuilderLike;
import io.github.cshunsinger.asmsauce.code.cast.ImplicitConversionInsn;
import io.github.cshunsinger.asmsauce.code.field.FieldAccessibleInstance;
import io.github.cshunsinger.asmsauce.code.field.FieldAssignableInstance;
import io.github.cshunsinger.asmsauce.code.math.MathOperandInstance;
import io.github.cshunsinger.asmsauce.code.method.InvokableInstance;
import io.github.cshunsinger.asmsauce.definitions.TypeDefinition;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ArrayUtils;
import org.objectweb.asm.Label;

import static io.github.cshunsinger.asmsauce.MethodBuildingContext.context;

/**
 * This code insn builder class represents a ternary statement in Java. Just like in Java, these ternary statements
 * have a condition. If the condition evaluates to 'true' then one value is stacked, otherwise a different value is stacked.
 * Unlike in Java source code where each branch of the ternary statement is expected to be a single statement which returns
 * a value, asmsauce ternary statements can have as many instructions as you want. The last instruction of each branch
 * of the ternary statement will be treated as the "return" instruction which stacks a value.
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class TernaryIfElse extends CodeInsnBuilder implements
    AccessibleArrayLike, BooleanConditionBuilderLike, ConditionBuilderLike, NullConditionBuilderLike,
    FieldAccessibleInstance, FieldAssignableInstance, MathOperandInstance, InvokableInstance
{
    private final Condition condition;
    private final CodeBlock trueBody;
    private final CodeBlock falseBody;
    private final CodeInsnBuilderLike trueReturn;
    private final CodeInsnBuilderLike falseReturn;

    @Override
    public void build() {
        //Build the condition bytecode
        Label endIfBodyLabel = new Label();
        Label endElseBodyLabel = new Label();
        condition.build(endIfBodyLabel);

        //build the "then" bytecode
        context().beginScope();
        trueBody.buildClean();
        stackSingleValue(trueReturn);
        TypeDefinition trueType = context().popStack();
        new GotoInsn(endElseBodyLabel).build();
        context().endScope();
        context().getMethodVisitor().visitLabel(endIfBodyLabel);

        //Build the "else" bytecode
        context().beginScope();
        falseBody.buildClean();
        stackSingleValue(falseReturn);
        TypeDefinition falseType = context().peekStack();
        context().endScope();
        context().getMethodVisitor().visitLabel(endElseBodyLabel);

        //Make sure the two types are compatible
        if(!ImplicitConversionInsn.implicitCastAllowed(falseType, trueType)) {
            throw new IllegalStateException("Ternary else-value of type %s is not compatible with the if-value type %s.".formatted(
                falseType.getClassName(), trueType.getClassName()
            ));
        }

        //Implicit cast the second type to match the first type
        new ImplicitConversionInsn(trueType).build();
    }

    private static void stackSingleValue(CodeInsnBuilderLike codeBuilder) {
        int initialStackSize = context().stackSize();
        codeBuilder.build();
        int numStacked = context().stackSize() - initialStackSize;

        if(numStacked != 1)
            throw new IllegalStateException("Ternary if-else expected 1 element to be stacked. Got %d instead.".formatted(numStacked));
    }

    /**
     * Class for beginning the construction of a ternary if/else statement.
     */
    public static class TernaryConditionBuilder {
        private final Condition condition;

        /**
         * Begin a ternary if/else statement with a condition.
         * @param condition The condition for the if/else statement.
         * @throws IllegalArgumentException If condition is null.
         */
        public TernaryConditionBuilder(Condition condition) {
            if(condition == null)
                throw new IllegalArgumentException("Ternary condition may not be null.");

            this.condition = condition;
        }

        /**
         * Add the if-body to the ternary statement being built.
         * @param ifBody The code instructions making up the if-body.
         * @return The next builder in the process of building a ternary if-else statement.
         * @throws IllegalArgumentException If the ifBody is null or empty.
         * @throws IllegalArgumentException If the last element of ifBody is null.
         */
        public TernaryIfBuilder thenCalculate(CodeInsnBuilderLike... ifBody) {
            if(ifBody == null)
                throw new IllegalArgumentException("Ternary if-body cannot be null.");
            else if(ifBody.length == 0)
                throw new IllegalArgumentException("Ternary if-body cannot be empty.");

            int ifBodyLastIndex = ifBody.length - 1;
            CodeInsnBuilderLike ifReturn = ifBody[ifBodyLastIndex];

            if(ifReturn == null)
                throw new IllegalArgumentException("The last statement of the ternary if-body cannot be null.");

            return new TernaryIfBuilder(condition, ArrayUtils.subarray(ifBody, 0, ifBodyLastIndex), ifReturn);
        }
    }

    /**
     * Class for continuing the construction of a ternary if/else statement
     */
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static class TernaryIfBuilder {
        private final Condition condition;
        private final CodeInsnBuilderLike[] ifBody;
        private final CodeInsnBuilderLike ifReturn;

        /**
         * Completes the construction of a Ternary if-else statement with the else body. This method will create a new
         * TernaryIfElse code building instruction.
         * @param elseBody The code instruction making up the else-body.
         * @return A new instance of the TernaryIfElse code instruction, which generates the bytecode for a ternary
         * Java statement.
         * @throws IllegalArgumentException If elseBody is null or empty.
         * @throws IllegalArgumentException If the last element of elseBody is null.
         */
        public TernaryIfElse elseCalculate(CodeInsnBuilderLike... elseBody) {
            if(elseBody == null)
                throw new IllegalArgumentException("Ternary else-body cannot be null.");
            else if(elseBody.length == 0)
                throw new IllegalArgumentException("Ternary else-body cannot be empty.");

            CodeBlock ifBlock = new CodeBlock(ifBody);
            CodeBlock elseBlock = new CodeBlock(ArrayUtils.subarray(elseBody, 0, elseBody.length - 1));
            CodeInsnBuilderLike elseReturn = elseBody[elseBody.length - 1];

            if(elseReturn == null)
                throw new IllegalArgumentException("The last statement of the ternary else-body cannot be null.");

            return new TernaryIfElse(condition, ifBlock, elseBlock, ifReturn.getFirstInStack(), elseReturn.getFirstInStack());
        }
    }
}