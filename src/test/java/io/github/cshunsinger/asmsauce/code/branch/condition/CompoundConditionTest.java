package io.github.cshunsinger.asmsauce.code.branch.condition;

import io.github.cshunsinger.asmsauce.AsmClassBuilder;
import io.github.cshunsinger.asmsauce.definitions.ParametersDefinition;
import io.github.cshunsinger.asmsauce.testing.BaseUnitTest;
import io.github.cshunsinger.asmsauce.DefinitionBuilders;
import io.github.cshunsinger.asmsauce.code.CodeBuilders;
import org.junit.jupiter.api.Test;

import static io.github.cshunsinger.asmsauce.ConstructorNode.constructor;
import static io.github.cshunsinger.asmsauce.MethodNode.method;
import static io.github.cshunsinger.asmsauce.modifiers.AccessModifiers.publicOnly;
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
            .withConstructor(constructor(publicOnly(), DefinitionBuilders.noParameters(),
                CodeBuilders.superConstructor(TestCompoundConditionsType.class, DefinitionBuilders.noParameters()),
                CodeBuilders.returnVoid()
            ))
            .withMethod(method(publicOnly(), DefinitionBuilders.name("anyNull"), DefinitionBuilders.parameters(DefinitionBuilders.p("first", Object.class), DefinitionBuilders.p("second", Object.class), DefinitionBuilders.p("third", Object.class)), DefinitionBuilders.type(boolean.class),
                CodeBuilders.if_(CodeBuilders.getVar("first").isNull()
                    .or(CodeBuilders.getVar("second").isNull())
                    .or(CodeBuilders.getVar("third").isNull())).then(
                        CodeBuilders.returnValue(CodeBuilders.true_())
                ),
                CodeBuilders.returnValue(CodeBuilders.false_())
            ))
            .withMethod(method(publicOnly(), DefinitionBuilders.name("allNull"), DefinitionBuilders.parameters(DefinitionBuilders.p("first", Object.class), DefinitionBuilders.p("second", Object.class), DefinitionBuilders.p("third", Object.class)), DefinitionBuilders.type(boolean.class),
                CodeBuilders.if_(CodeBuilders.getVar("first").isNull()
                    .and(CodeBuilders.getVar("second").isNull())
                    .and(CodeBuilders.getVar("third").isNull())).then(
                        CodeBuilders.returnValue(CodeBuilders.true_())
                ),
                CodeBuilders.returnValue(CodeBuilders.false_())
            ))
            .withMethod(method(publicOnly(), DefinitionBuilders.name("atLeastTwoNull"), DefinitionBuilders.parameters(DefinitionBuilders.p("first", Object.class), DefinitionBuilders.p("second", Object.class), DefinitionBuilders.p("third", Object.class)), DefinitionBuilders.type(boolean.class),
                CodeBuilders.if_(CodeBuilders.getVar("first").isNull().and(CodeBuilders.getVar("second").isNull()).and(CodeBuilders.getVar("third").isNull())
                    .or(CodeBuilders.getVar("first").isNull().and(CodeBuilders.getVar("second").isNull()))
                    .or(CodeBuilders.getVar("first").isNull().and(CodeBuilders.getVar("third").isNull()))
                    .or(CodeBuilders.getVar("second").isNull().and(CodeBuilders.getVar("third").isNull()))
                ).then(
                        CodeBuilders.returnValue(CodeBuilders.true_())
                ),
                CodeBuilders.returnValue(CodeBuilders.false_())
            ))
            .withMethod(method(publicOnly(), DefinitionBuilders.name("atLeastOneNullPerPair"), DefinitionBuilders.parameters(DefinitionBuilders.p("first", Object.class), DefinitionBuilders.p("second", Object.class), DefinitionBuilders.p("third", Object.class), DefinitionBuilders.p("fourth", Object.class)), DefinitionBuilders.type(boolean.class),
                CodeBuilders.if_(CodeBuilders.getVar("first").isNull().or(CodeBuilders.getVar("second").isNull())
                    .and(CodeBuilders.getVar("third").isNull().or(CodeBuilders.getVar("fourth").isNull()))
                ).then(
                    CodeBuilders.returnValue(CodeBuilders.true_())
                ),
                CodeBuilders.returnValue(CodeBuilders.false_())
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
        ParametersDefinition methodParameters = DefinitionBuilders.parameters(
            DefinitionBuilders.p("first", Object.class),
            DefinitionBuilders.p("second", Object.class),
            DefinitionBuilders.p("third", Object.class),
            DefinitionBuilders.p("fourth", Object.class)
        );

        AsmClassBuilder<TestNestedCompoundsType> builder = new AsmClassBuilder<>(TestNestedCompoundsType.class)
            .withConstructor(constructor(publicOnly(), DefinitionBuilders.noParameters(),
                CodeBuilders.superConstructor(TestNestedCompoundsType.class, DefinitionBuilders.noParameters()),
                CodeBuilders.returnVoid()
            ))
            .withMethod(method(publicOnly(), DefinitionBuilders.name("allNotNull"), methodParameters, DefinitionBuilders.type(boolean.class),
                CodeBuilders.if_(CodeBuilders.getVar("first").isNotNull().and(CodeBuilders.getVar("second").isNotNull())
                    .and(CodeBuilders.getVar("third").isNotNull().and(CodeBuilders.getVar("fourth").isNotNull()))).then(
                        CodeBuilders.returnValue(CodeBuilders.true_())
                ),
                CodeBuilders.returnValue(CodeBuilders.false_())
            ))
            .withMethod(method(publicOnly(), DefinitionBuilders.name("anyNotNull"), methodParameters, DefinitionBuilders.type(boolean.class),
                CodeBuilders.if_(CodeBuilders.getVar("first").isNotNull().or(CodeBuilders.getVar("second").isNotNull())
                    .or(CodeBuilders.getVar("third").isNotNull().or(CodeBuilders.getVar("fourth").isNotNull()))).then(
                        CodeBuilders.returnValue(CodeBuilders.true_())
                ),
                CodeBuilders.returnValue(CodeBuilders.false_())
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
            .withConstructor(constructor(publicOnly(), DefinitionBuilders.noParameters(),
                CodeBuilders.superConstructor(TestInvertedCompoundConditionType.class, DefinitionBuilders.noParameters()),
                CodeBuilders.returnVoid()
            ))
            .withMethod(method(publicOnly(), DefinitionBuilders.name("bothNull"), DefinitionBuilders.parameters(DefinitionBuilders.p("first", Object.class), DefinitionBuilders.p("second", Object.class)), DefinitionBuilders.type(boolean.class),
                CodeBuilders.if_(CodeBuilders.not(
                    CodeBuilders.getVar("first").isNotNull().or(CodeBuilders.getVar("second").isNotNull())
                )).then(
                    CodeBuilders.returnValue(CodeBuilders.true_())
                ),
                CodeBuilders.returnValue(CodeBuilders.false_())
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