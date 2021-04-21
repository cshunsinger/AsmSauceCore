package io.github.cshunsinger.asmsauce.code.branch;

import lombok.AccessLevel;
import org.objectweb.asm.Label;
import io.github.cshunsinger.asmsauce.MethodBuildingContext;
import io.github.cshunsinger.asmsauce.code.CodeInsnBuilderLike;
import io.github.cshunsinger.asmsauce.code.branch.condition.Condition;
import lombok.RequiredArgsConstructor;

/**
 * This code builder makes up the if-portion of an if or if-else statement.
 * The if-branch contains a condition, and if that condition is true, the body of the if-statement is executed.
 * If the condition is false, a jump is made to a label which is placed at the end of the if-body bytecode instructions.
 */
public class IfBranch extends IfElseBranch {
    private final Condition condition;

    IfBranch(IfBranch original, CodeInsnBuilderLike... body) {
        this(original.condition, original.endLabel, body);
    }

    /**
     * Constructs a new if-branch with a condition and if-body.
     * @param condition The condition to test. The bytecode generated by the condition will jump to a label if false.
     * @param body The if-body code builders. The bytecode generated by these will be executed if the bytecode for the
     *             condition evaluates to 'true' at runtime.
     * @throws IllegalArgumentException If the body is empty.
     */
    public IfBranch(Condition condition, CodeInsnBuilderLike... body) {
        this(condition, new Label(), body);
    }

    /**
     * Constructs a new if-branch with a condition, endLabel, and if-body. This allows an if-branch to jump to any
     * arbitrary label in the event that the bytecode generated by the condition evaluates to false.
     * @param condition The condition to test. The bytecode generated by the condition will jump to the specified
     *                  endLabel if it evaluates to false.
     * @param endLabel The label to jump to if the bytecode for the condition evaluates to false.
     * @param body The if-body code builders. They bytecode generated by these will be executed if the bytecode for the
     *             condition evaluates to 'true' at runtime.
     * @throws IllegalArgumentException If the body is empty.
     */
    public IfBranch(Condition condition, Label endLabel, CodeInsnBuilderLike... body) {
        super(endLabel, body);

        if(condition == null)
            throw new IllegalArgumentException("Condition cannot be null.");

        this.condition = condition;
    }

    @Override
    public void build(MethodBuildingContext context) {
        //Generate the bytecode for the branching
        condition.build(context, endLabel);

        //Build the if body
        super.build(context);
    }

    /**
     * Adds an else branch to this if branch.
     * @param elseBody The code builders whose generated bytecode will make up the else-body.
     * @return A new ElseBranch instance, which will be executed if this if-branch condition evaluates to false, but
     *         will not be executed if this if-branch condition evaluates to true.
     */
    public ElseBranch elseDo(CodeInsnBuilderLike... elseBody) {
        return new ElseBranch(this, elseBody);
    }

    /**
     * Builder for if statements. Allows for smooth syntax:
     * if_(condition).then(
     *   body...
     * )
     */
    public static class IfBuilder {
        private final Condition condition;

        /**
         * Creates a new IfBuilder for building an IfBranch.
         * @param condition The condition for the IfBranch.
         */
        public IfBuilder(Condition condition) {
            this.condition = condition;
        }

        /**
         * Completes the if-branch by supplying an if-body to go along with the branch condition.
         * @param body The code builders whose generated bytecode will make up the if-branch body.
         * @return A newly constructed IfBranch with it's own label, body, and condition.
         */
        public IfBranch then(CodeInsnBuilderLike... body) {
            return new IfBranch(condition, body);
        }
    }
}