package io.github.cshunsinger.asmsauce.code.branch;

import io.github.cshunsinger.asmsauce.AsmClassBuilder;
import org.junit.jupiter.api.Test;

import static io.github.cshunsinger.asmsauce.ConstructorNode.constructor;
import static io.github.cshunsinger.asmsauce.DefinitionBuilders.*;
import static io.github.cshunsinger.asmsauce.MethodNode.method;
import static io.github.cshunsinger.asmsauce.code.CodeBuilders.*;
import static io.github.cshunsinger.asmsauce.modifiers.AccessModifiers.publicOnly;
import static org.apache.commons.lang3.RandomUtils.nextInt;
import static org.junit.jupiter.api.Assertions.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class WhileLoopTest {
    @Test
    public void illegalArgumentException_nullCondition() {
        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class,
            () -> new WhileLoop(null)
        );

        assertThat(ex, hasProperty("message", is("Condition cannot be null.")));
    }

    public static abstract class TestMaths {
        public abstract int multiply(int first, int second);
    }

    @Test
    public void implementWorkingWhileLoop() {
        AsmClassBuilder<TestMaths> builder = new AsmClassBuilder<>(TestMaths.class)
            .withConstructor(constructor(publicOnly(), noParameters(),
                superConstructor(TestMaths.class, noParameters()),
                returnVoid()
            ))
            .withMethod(method(publicOnly(), name("multiply"), parameters(p("first", int.class), p("second", int.class)), type(int.class),
                //Yes, every developer knows you aren't supposed to perform multiplication using a while loop
                //This is strictly to test out the bytecode generation abilities
                //Don't do while-loop multiplication, kids!

                setVar("sum", literal(0)), //int sum = 0;
                setVar("counter", literal(0)), //int counter = 0;

                //while(counter < second) { ... }
                while_(getVar("counter").lt(getVar("second"))).do_(
                    setVar("sum", getVar("sum").add(getVar("first"))), //sum = sum + first;
                    setVar("counter", getVar("counter").add(literal(1))) //counter = counter + 1;
                ),

                //return sum;
                returnValue(getVar("sum"))
            ));

        TestMaths instance = builder.buildInstance();

        int first = nextInt(1, 100);
        int second = nextInt(1, 100);
        assertThat(instance.multiply(first, second), is(first * second));
    }
}