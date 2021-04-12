package com.chunsinger.asmsauce.code.branch.condition;

import com.chunsinger.asmsauce.AsmClassBuilder;
import com.chunsinger.asmsauce.definitions.ParametersDefinition;
import com.chunsinger.asmsauce.testing.BaseUnitTest;
import org.junit.jupiter.api.Test;

import static com.chunsinger.asmsauce.ConstructorNode.constructor;
import static com.chunsinger.asmsauce.DefinitionBuilders.*;
import static com.chunsinger.asmsauce.MethodNode.method;
import static com.chunsinger.asmsauce.code.CodeBuilders.*;
import static com.chunsinger.asmsauce.modifiers.AccessModifiers.publicOnly;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class CompoundConditionTest extends BaseUnitTest {
    public static abstract class TestCompoundConditionsType {
        public abstract boolean anyNull(Object first, Object second, Object third);
        public abstract boolean allNull(Object first, Object second, Object third);
        public abstract boolean atLeastTwoNull(Object first, Object second, Object third);
        public abstract boolean atLeastOneNullPerPair(Object first, Object second, Object third, Object fourth);
    }

    @Test
    public void testCompoundConditions() {
        AsmClassBuilder<TestCompoundConditionsType> builder = new AsmClassBuilder<>(TestCompoundConditionsType.class)
            .withConstructor(constructor(publicOnly(), noParameters(),
                superConstructor(TestCompoundConditionsType.class, noParameters()),
                returnVoid()
            ))
            .withMethod(method(publicOnly(), name("anyNull"), parameters(p("first", Object.class), p("second", Object.class), p("third", Object.class)), type(boolean.class),
                if_(getVar("first").isNull()
                    .or(getVar("second").isNull())
                    .or(getVar("third").isNull())).then(
                        returnValue(true_())
                ),
                returnValue(false_())
            ))
            .withMethod(method(publicOnly(), name("allNull"), parameters(p("first", Object.class), p("second", Object.class), p("third", Object.class)), type(boolean.class),
                if_(getVar("first").isNull()
                    .and(getVar("second").isNull())
                    .and(getVar("third").isNull())).then(
                        returnValue(true_())
                ),
                returnValue(false_())
            ))
            .withMethod(method(publicOnly(), name("atLeastTwoNull"), parameters(p("first", Object.class), p("second", Object.class), p("third", Object.class)), type(boolean.class),
                if_(getVar("first").isNull().and(getVar("second").isNull()).and(getVar("third").isNull())
                    .or(getVar("first").isNull().and(getVar("second").isNull()))
                    .or(getVar("first").isNull().and(getVar("third").isNull()))
                    .or(getVar("second").isNull().and(getVar("third").isNull()))
                ).then(
                        returnValue(true_())
                ),
                returnValue(false_())
            ))
            .withMethod(method(publicOnly(), name("atLeastOneNullPerPair"), parameters(p("first", Object.class), p("second", Object.class), p("third", Object.class), p("fourth", Object.class)), type(boolean.class),
                if_(getVar("first").isNull().or(getVar("second").isNull())
                    .and(getVar("third").isNull().or(getVar("fourth").isNull()))
                ).then(
                    returnValue(true_())
                ),
                returnValue(false_())
            ));

        TestCompoundConditionsType instance = builder.buildInstance();

        //anyNull: CompoundOr
        System.out.println("anyNull false: " + instance.anyNull("first", "second", "third"));
        System.out.println("anyNull true: " + instance.anyNull(null, "second", "third"));
        System.out.println("anyNull true: " + instance.anyNull("first", null, "third"));
        System.out.println("anyNull true: " + instance.anyNull("first", "second", null));
        System.out.println("anyNull true: " + instance.anyNull("first", null, null));
        System.out.println("anyNull true: " + instance.anyNull(null, "second", null));
        System.out.println("anyNull true: " + instance.anyNull(null, null, "third"));
        System.out.println("anyNull true: " + instance.anyNull(null, null, null));

        assertThat(instance.anyNull("first", "second", "third"), is(false));
        assertThat(instance.anyNull(null, "second", "third"), is(true));
        assertThat(instance.anyNull("first", null, "third"), is(true));
        assertThat(instance.anyNull("first", "second", null), is(true));
        assertThat(instance.anyNull("first", null, null), is(true));
        assertThat(instance.anyNull(null, "second", null), is(true));
        assertThat(instance.anyNull(null, null, "third"), is(true));
        assertThat(instance.anyNull(null, null, null), is(true));

        //allNull: CompoundAnd
        System.out.println("allNull false: " + instance.allNull("first", "second", "third"));
        System.out.println("allNull false: " + instance.allNull("first", "second", null));
        System.out.println("allNull false: " + instance.allNull("first", null, "third"));
        System.out.println("allNull false: " + instance.allNull(null, "second", "third"));
        System.out.println("allNull false: " + instance.allNull("first", null, null));
        System.out.println("allNull false: " + instance.allNull(null, "second", null));
        System.out.println("allNull false: " + instance.allNull(null, null, "third"));
        System.out.println("allNull  true: " + instance.allNull(null, null, null));

        assertThat(instance.allNull("first", "second", "third"), is(false));
        assertThat(instance.allNull("first", "second", null), is(false));
        assertThat(instance.allNull("first", null, "third"), is(false));
        assertThat(instance.allNull(null, "second", "third"), is(false));
        assertThat(instance.allNull("first", null, null), is(false));
        assertThat(instance.allNull(null, "second", null), is(false));
        assertThat(instance.allNull(null, null, "third"), is(false));
        assertThat(instance.allNull(null, null, null), is(true));

        //atLeastTwoNull: nested CompoundConditions AND inside of OR
        System.out.println("atLeastTwoNull false: " + instance.atLeastTwoNull("first", "second", "third"));
        System.out.println("atLeastTwoNull false: " + instance.atLeastTwoNull(null, "second", "third"));
        System.out.println("atLeastTwoNull false: " + instance.atLeastTwoNull("first", null, "third"));
        System.out.println("atLeastTwoNull false: " + instance.atLeastTwoNull("first", "second", null));
        System.out.println("atLeastTwoNull true:  " + instance.atLeastTwoNull("first", null, null));
        System.out.println("atLeastTwoNull true:  " + instance.atLeastTwoNull(null, "second", null));
        System.out.println("atLeastTwoNull true:  " + instance.atLeastTwoNull(null, null, "third"));
        System.out.println("atLeastTwoNull true:  " + instance.atLeastTwoNull(null, null, null));

        assertThat(instance.atLeastTwoNull("first", "second", "third"), is(false));
        assertThat(instance.atLeastTwoNull(null, "second", "third"), is(false));
        assertThat(instance.atLeastTwoNull("first", null, "third"), is(false));
        assertThat(instance.atLeastTwoNull("first", "second", null), is(false));
        assertThat(instance.atLeastTwoNull("first", null, null), is(true));
        assertThat(instance.atLeastTwoNull(null, "second", null), is(true));
        assertThat(instance.atLeastTwoNull(null, null, "third"), is(true));
        assertThat(instance.atLeastTwoNull(null, null, null), is(true));

        //atLeastOneNullPerPair: nested CompoundConditions OR inside of AND
        System.out.println("atLeastOneNullPerPair false: " + instance.atLeastOneNullPerPair("first", "second", "third", "fourth"));
        System.out.println("atLeastOneNullPerPair false: " + instance.atLeastOneNullPerPair(null, "second", "third", "fourth"));
        System.out.println("atLeastOneNullPerPair false: " + instance.atLeastOneNullPerPair("first", null, "third", "fourth"));
        System.out.println("atLeastOneNullPerPair false: " + instance.atLeastOneNullPerPair("first", "second", null, "fourth"));
        System.out.println("atLeastOneNullPerPair false: " + instance.atLeastOneNullPerPair("first", "second", "third", null));
        System.out.println("atLeastOneNullPerPair false: " + instance.atLeastOneNullPerPair(null, null, "third", "fourth"));
        System.out.println("atLeastOneNullPerPair true:  " + instance.atLeastOneNullPerPair(null, "second", null, "fourth"));
        System.out.println("atLeastOneNullPerPair true:  " + instance.atLeastOneNullPerPair(null, "second", "third", null));
        System.out.println("atLeastOneNullPerPair true:  " + instance.atLeastOneNullPerPair("first", null, null, "fourth"));
        System.out.println("atLeastOneNullPerPair true:  " + instance.atLeastOneNullPerPair("first", null, "third", null));
        System.out.println("atLeastOneNullPerPair false: " + instance.atLeastOneNullPerPair("first", "second", null, null));
        System.out.println("atLeastOneNullPerPair true:  " + instance.atLeastOneNullPerPair("first", null, null, null));
        System.out.println("atLeastOneNullPerPair true:  " + instance.atLeastOneNullPerPair(null, "second", null, null));
        System.out.println("atLeastOneNullPerPair true:  " + instance.atLeastOneNullPerPair(null, null, "third", null));
        System.out.println("atLeastOneNullPerPair true:  " + instance.atLeastOneNullPerPair(null, null, null, "fourth"));
        System.out.println("atLeastOneNullPerPair true:  " + instance.atLeastOneNullPerPair(null, null, null, null));

        assertThat(instance.atLeastOneNullPerPair("first", "second", "third", "fourth"), is(false));
        assertThat(instance.atLeastOneNullPerPair(null, "second", "third", "fourth"), is(false));
        assertThat(instance.atLeastOneNullPerPair("first", null, "third", "fourth"), is(false));
        assertThat(instance.atLeastOneNullPerPair("first", "second", null, "fourth"), is(false));
        assertThat(instance.atLeastOneNullPerPair("first", "second", "third", null), is(false));
        assertThat(instance.atLeastOneNullPerPair(null, null, "third", "fourth"), is(false));
        assertThat(instance.atLeastOneNullPerPair(null, "second", null, "fourth"), is(true));
        assertThat(instance.atLeastOneNullPerPair(null, "second", "third", null), is(true));
        assertThat(instance.atLeastOneNullPerPair("first", null, null, "fourth"), is(true));
        assertThat(instance.atLeastOneNullPerPair("first", null, "third", null), is(true));
        assertThat(instance.atLeastOneNullPerPair("first", "second", null, null), is(false));
        assertThat(instance.atLeastOneNullPerPair("first", null, null, null), is(true));
        assertThat(instance.atLeastOneNullPerPair(null, "second", null, null), is(true));
        assertThat(instance.atLeastOneNullPerPair(null, null, "third", null), is(true));
        assertThat(instance.atLeastOneNullPerPair(null, null, null, "fourth"), is(true));
        assertThat(instance.atLeastOneNullPerPair(null, null, null, null), is(true));
    }

    public static abstract class TestNestedCompoundsType {
        public abstract boolean allNotNull(Object first, Object second, Object third, Object fourth);
        public abstract boolean anyNotNull(Object first, Object second, Object third, Object fourth);
    }

    @Test
    public void flattenNestedCompoundConditionsIfTheyAreTheSameType() {
        ParametersDefinition methodParameters = parameters(
            p("first", Object.class),
            p("second", Object.class),
            p("third", Object.class),
            p("fourth", Object.class)
        );

        AsmClassBuilder<TestNestedCompoundsType> builder = new AsmClassBuilder<>(TestNestedCompoundsType.class)
            .withConstructor(constructor(publicOnly(), noParameters(),
                superConstructor(TestNestedCompoundsType.class, noParameters()),
                returnVoid()
            ))
            .withMethod(method(publicOnly(), name("allNotNull"), methodParameters, type(boolean.class),
                if_(getVar("first").isNotNull().and(getVar("second").isNotNull())
                    .and(getVar("third").isNotNull().and(getVar("fourth").isNotNull()))).then(
                        returnValue(true_())
                ),
                returnValue(false_())
            ))
            .withMethod(method(publicOnly(), name("anyNotNull"), methodParameters, type(boolean.class),
                if_(getVar("first").isNotNull().or(getVar("second").isNotNull())
                    .or(getVar("third").isNotNull().or(getVar("fourth").isNotNull()))).then(
                        returnValue(true_())
                ),
                returnValue(false_())
            ));

        TestNestedCompoundsType instance = builder.buildInstance();

        //allNotNull: compound-AND nested in a compound-AND
        System.out.println("allNotNull  true: " + instance.allNotNull("first", "second", "third", "fourth"));
        System.out.println("allNotNull false: " + instance.allNotNull(null, "second", "third", "fourth"));
        System.out.println("allNotNull false: " + instance.allNotNull("first", null, "third", "fourth"));
        System.out.println("allNotNull false: " + instance.allNotNull("first", "second", null, "fourth"));
        System.out.println("allNotNull false: " + instance.allNotNull("first", "second", "third", null));
        System.out.println("allNotNull false: " + instance.allNotNull(null, null, "third", "fourth"));
        System.out.println("allNotNull false: " + instance.allNotNull(null, "second", null, "fourth"));
        System.out.println("allNotNull false: " + instance.allNotNull(null, "second", "third", null));
        System.out.println("allNotNull false: " + instance.allNotNull("first", null, null, "fourth"));
        System.out.println("allNotNull false: " + instance.allNotNull("first", null, "third", null));
        System.out.println("allNotNull false: " + instance.allNotNull("first", "second", null, null));
        System.out.println("allNotNull false: " + instance.allNotNull(null, null, null, "fourth"));
        System.out.println("allNotNull false: " + instance.allNotNull(null, null, "third", null));
        System.out.println("allNotNull false: " + instance.allNotNull(null, "second", null, null));
        System.out.println("allNotNull false: " + instance.allNotNull("first", null, null, null));
        System.out.println("allNotNull false: " + instance.allNotNull(null, null, null, null));

        assertThat(instance.allNotNull("first", "second", "third", "fourth"), is(true));
        assertThat(instance.allNotNull(null, "second", "third", "fourth"), is(false));
        assertThat(instance.allNotNull("first", null, "third", "fourth"), is(false));
        assertThat(instance.allNotNull("first", "second", null, "fourth"), is(false));
        assertThat(instance.allNotNull("first", "second", "third", null), is(false));
        assertThat(instance.allNotNull(null, null, "third", "fourth"), is(false));
        assertThat(instance.allNotNull(null, "second", null, "fourth"), is(false));
        assertThat(instance.allNotNull(null, "second", "third", null), is(false));
        assertThat(instance.allNotNull("first", null, null, "fourth"), is(false));
        assertThat(instance.allNotNull("first", null, "third", null), is(false));
        assertThat(instance.allNotNull("first", "second", null, null), is(false));
        assertThat(instance.allNotNull(null, null, null, "fourth"), is(false));
        assertThat(instance.allNotNull(null, null, "third", null), is(false));
        assertThat(instance.allNotNull(null, "second", null, null), is(false));
        assertThat(instance.allNotNull("first", null, null, null), is(false));
        assertThat(instance.allNotNull(null, null, null, null), is(false));

        //anyNotNull: compound-AND nested in a compound-AND
        System.out.println("anyNotNull  true: " + instance.anyNotNull("first", "second", "third", "fourth"));
        System.out.println("anyNotNull  true: " + instance.anyNotNull(null, "second", "third", "fourth"));
        System.out.println("anyNotNull  true: " + instance.anyNotNull("first", null, "third", "fourth"));
        System.out.println("anyNotNull  true: " + instance.anyNotNull("first", "second", null, "fourth"));
        System.out.println("anyNotNull  true: " + instance.anyNotNull("first", "second", "third", null));
        System.out.println("anyNotNull  true: " + instance.anyNotNull(null, null, "third", "fourth"));
        System.out.println("anyNotNull  true: " + instance.anyNotNull(null, "second", null, "fourth"));
        System.out.println("anyNotNull  true: " + instance.anyNotNull(null, "second", "third", null));
        System.out.println("anyNotNull  true: " + instance.anyNotNull("first", null, null, "fourth"));
        System.out.println("anyNotNull  true: " + instance.anyNotNull("first", null, "third", null));
        System.out.println("anyNotNull  true: " + instance.anyNotNull("first", "second", null, null));
        System.out.println("anyNotNull  true: " + instance.anyNotNull(null, null, null, "fourth"));
        System.out.println("anyNotNull  true: " + instance.anyNotNull(null, null, "third", null));
        System.out.println("anyNotNull  true: " + instance.anyNotNull(null, "second", null, null));
        System.out.println("anyNotNull  true: " + instance.anyNotNull("first", null, null, null));
        System.out.println("anyNotNull false: " + instance.anyNotNull(null, null, null, null));

        assertThat(instance.anyNotNull("first", "second", "third", "fourth"), is(true));
        assertThat(instance.anyNotNull(null, "second", "third", "fourth"), is(true));
        assertThat(instance.anyNotNull("first", null, "third", "fourth"), is(true));
        assertThat(instance.anyNotNull("first", "second", null, "fourth"), is(true));
        assertThat(instance.anyNotNull("first", "second", "third", null), is(true));
        assertThat(instance.anyNotNull(null, null, "third", "fourth"), is(true));
        assertThat(instance.anyNotNull(null, "second", null, "fourth"), is(true));
        assertThat(instance.anyNotNull(null, "second", "third", null), is(true));
        assertThat(instance.anyNotNull("first", null, null, "fourth"), is(true));
        assertThat(instance.anyNotNull("first", null, "third", null), is(true));
        assertThat(instance.anyNotNull("first", "second", null, null), is(true));
        assertThat(instance.anyNotNull(null, null, null, "fourth"), is(true));
        assertThat(instance.anyNotNull(null, null, "third", null), is(true));
        assertThat(instance.anyNotNull(null, "second", null, null), is(true));
        assertThat(instance.anyNotNull("first", null, null, null), is(true));
        assertThat(instance.anyNotNull(null, null, null, null), is(false));
    }

    public static abstract class TestInvertedCompoundConditionType {
        public abstract boolean bothNull(Object first, Object second);
    }

    @Test
    public void invertingCompoundCondition() {
        AsmClassBuilder<TestInvertedCompoundConditionType> builder = new AsmClassBuilder<>(TestInvertedCompoundConditionType.class)
            .withConstructor(constructor(publicOnly(), noParameters(),
                superConstructor(TestInvertedCompoundConditionType.class, noParameters()),
                returnVoid()
            ))
            .withMethod(method(publicOnly(), name("bothNull"), parameters(p("first", Object.class), p("second", Object.class)), type(boolean.class),
                if_(not(
                    getVar("first").isNotNull().or(getVar("second").isNotNull())
                )).then(
                    returnValue(true_())
                ),
                returnValue(false_())
            ));

        TestInvertedCompoundConditionType instance = builder.buildInstance();

        System.out.println("bothNull  true: " + instance.bothNull(null, null));
        System.out.println("bothNull false: " + instance.bothNull("first", null));
        System.out.println("bothNull false: " + instance.bothNull(null, "second"));
        System.out.println("bothNull false: " + instance.bothNull("first", "second"));

        assertThat(instance.bothNull(null, null), is(true));
        assertThat(instance.bothNull("first", null), is(false));
        assertThat(instance.bothNull(null, "second"), is(false));
        assertThat(instance.bothNull("first", "second"), is(false));
    }
}