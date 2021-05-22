package io.github.cshunsinger.asmsauce.code.array;

import io.github.cshunsinger.asmsauce.AsmClassBuilder;
import org.junit.jupiter.api.Test;

import static io.github.cshunsinger.asmsauce.DefinitionBuilders.*;
import static io.github.cshunsinger.asmsauce.MethodNode.method;
import static io.github.cshunsinger.asmsauce.code.CodeBuilders.getVar;
import static io.github.cshunsinger.asmsauce.code.CodeBuilders.returnValue;
import static io.github.cshunsinger.asmsauce.modifiers.AccessModifiers.publicOnly;
import static org.apache.commons.lang3.RandomUtils.nextInt;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class ArrayLengthInsnTest {
    public static abstract class TestBaseType {
        public abstract int getArrayLength(Object[] array);
    }

    @Test
    public void successfullyAccessArrayLength() {
        AsmClassBuilder<TestBaseType> builder = new AsmClassBuilder<>(TestBaseType.class)
            .withMethod(method(publicOnly(), name("getArrayLength"), parameters(p("array", Object[].class)), type(int.class),
                returnValue(getVar("array").length())
            ));

        TestBaseType instance = builder.buildInstance();
        int randomLength = nextInt(1, 100);
        Object[] testArray = new Object[randomLength];

        assertThat(instance.getArrayLength(testArray), is(randomLength));
    }
}