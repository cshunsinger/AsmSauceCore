package io.github.cshunsinger.asmsauce.code.branch;

import io.github.cshunsinger.asmsauce.AsmClassBuilder;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

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
    public void implementWorkingWhileLoop_terribleMultiplicationAlgorithm() {
        AsmClassBuilder<TestMaths> builder = new AsmClassBuilder<>(TestMaths.class)
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

    public static abstract class TestCollections {
        public abstract void countUp(List<Integer> list, int count);
    }

    @Test
    public void implementWorkingWhileLoop_countUpInList() {
        AsmClassBuilder<TestCollections> builder = new AsmClassBuilder<>(TestCollections.class)
            .withMethod(method(publicOnly(), name("countUp"), parameters(p("list", List.class), p("count", int.class)),
                setVar("index", literal(0)), //int index = 0;

                while_(getVar("index").lt(getVar("count"))).do_( //while(index < count) {...}
                    getVar("list").invoke("add", cast(Integer.class, getVar("index").add(literal(1)))), //list.add((Integer)(index+1));
                    setVar("index", getVar("index").add(literal(1))) //index = index + 1;
                ),

                returnVoid()
            ));

        TestCollections instance = builder.buildInstance();
        ArrayList<Integer> testList = new ArrayList<>();
        instance.countUp(testList, 10);

        assertThat(testList, hasItems(1, 2, 3, 4, 5, 6, 7, 8, 9, 10));
    }
}