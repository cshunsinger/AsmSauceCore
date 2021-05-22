package io.github.cshunsinger.asmsauce.code.branch;

import io.github.cshunsinger.asmsauce.code.CodeInsnBuilder;
import io.github.cshunsinger.asmsauce.code.CodeInsnBuilderLike;
import io.github.cshunsinger.asmsauce.code.branch.condition.Condition;
import org.objectweb.asm.Label;

import java.util.List;

import static io.github.cshunsinger.asmsauce.MethodBuildingContext.context;

/**
 * This code builder generates a while loop which executes a body of instructions until a condition becomes false.
 * While a condition is true, the while-body is executed. At the end of the execution of the while-body, a jump is made
 * back to the condition bytecode so the while loop's condition can be executed again.
 */
public class WhileLoop extends CodeInsnBuilder {
    private final Condition condition;
    private final List<CodeInsnBuilderLike> body;
    private final Label startLabel = new Label();
    private final Label endLabel = new Label();

    /**
     * Creates a new while-loop construct. This instance will be used by the Asm building system to generate the
     * JVM bytecode of a while-loop.
     * @param condition The while-loop condition. While true, the body of the loop will repeatedly execute.
     * @param body The while-loop body. This gets executed as long as the condition remains true.
     */
    public WhileLoop(Condition condition, CodeInsnBuilderLike... body) {
        if(condition == null)
            throw new IllegalArgumentException("Condition cannot be null.");

        this.condition = condition;
        this.body = List.of(body);
    }

    @Override
    public void build() {
        //Start label
        context().getMethodVisitor().visitLabel(startLabel);

        //Condition
        condition.build(endLabel);

        //While-Body
        body.stream().map(CodeInsnBuilderLike::getFirstInStack).forEach(CodeInsnBuilderLike::buildClean);

        //Jump back to beginning
        new GotoInsn(startLabel).build();

        //End
        context().getMethodVisitor().visitLabel(endLabel);
    }

    /**
     * Class for building a while loop construct
     */
    public static class WhileBuilder {
        private final Condition condition;

        /**
         * Creates a new while-loop builder from a condition. This while loop builder allows
         * for tidy syntax when creating a while-loop in bytecode.
         * @param condition The condition to use in the while loop.
         */
        public WhileBuilder(Condition condition) {
            this.condition = condition;
        }

        /**
         * Completes the while-loop by adding a body to it.
         * @param body The body of the while loop.
         * @return A new while loop construct which will build the actual JVM bytecode for a while-loop.
         */
        public WhileLoop do_(CodeInsnBuilderLike... body) {
            return new WhileLoop(condition, body);
        }
    }
}