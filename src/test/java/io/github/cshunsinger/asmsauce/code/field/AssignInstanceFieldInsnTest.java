package io.github.cshunsinger.asmsauce.code.field;

import io.github.cshunsinger.asmsauce.AsmClassBuilder;
import io.github.cshunsinger.asmsauce.MethodBuildingContext;
import io.github.cshunsinger.asmsauce.ThisClass;
import io.github.cshunsinger.asmsauce.code.CodeInsnBuilderLike;
import io.github.cshunsinger.asmsauce.definitions.CompleteFieldDefinition;
import io.github.cshunsinger.asmsauce.BaseUnitTest;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.lang.reflect.Array;

import static io.github.cshunsinger.asmsauce.ConstructorNode.constructor;
import static io.github.cshunsinger.asmsauce.DefinitionBuilders.*;
import static io.github.cshunsinger.asmsauce.FieldNode.field;
import static io.github.cshunsinger.asmsauce.MethodNode.method;
import static io.github.cshunsinger.asmsauce.code.CodeBuilders.*;
import static io.github.cshunsinger.asmsauce.modifiers.AccessModifiers.privateOnly;
import static io.github.cshunsinger.asmsauce.modifiers.AccessModifiers.publicOnly;
import static java.util.Collections.emptyList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

class AssignInstanceFieldInsnTest extends BaseUnitTest {
    @Mock
    private CompleteFieldDefinition mockFieldDefinition;

    @Mock
    private CodeInsnBuilderLike mockCodeBuilder;

    @Test
    public void illegalArgumentException_nullFieldDefinition() {
        test_illegalArgumentException_base(null, mockCodeBuilder, "Field definition cannot be null.");
    }

    @Test
    public void illegalArgumentException_nullCodeBuilder() {
        test_illegalArgumentException_base(mockFieldDefinition, null, "Value builder cannot be null.");
    }

    private void test_illegalArgumentException_base(CompleteFieldDefinition fieldDefinition,
                                                    CodeInsnBuilderLike codeBuilder,
                                                    String exceptionMessage) {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> new AssignInstanceFieldInsn(fieldDefinition, codeBuilder));
        assertThat(ex, hasProperty("message", is(exceptionMessage)));
    }

    @Test
    public void illegalStateException_noInstanceOnStackToAccessFieldFrom() {
        new MethodBuildingContext(null, null, null, emptyList());

        when(mockCodeBuilder.getFirstInStack()).thenReturn(mockCodeBuilder);
        when(mockFieldDefinition.getFieldName()).thenReturn(name("Test Field"));

        AssignInstanceFieldInsn op = new AssignInstanceFieldInsn(mockFieldDefinition, mockCodeBuilder);
        IllegalStateException ex = assertThrows(IllegalStateException.class, op::build);
        assertThat(ex, hasProperty("message", is("No instance on stack to access field 'Test Field' from.")));
    }

    @Test
    public void illegalStateException_codeBuilderDoesNotPlaceExactlyOneElementOntoTheStack() {
        MethodBuildingContext methodContext = new MethodBuildingContext(null, null, null, emptyList());
        methodContext.pushStack(type(Object.class));

        when(mockCodeBuilder.getFirstInStack()).thenReturn(mockCodeBuilder);
        doAnswer(i -> null).when(mockCodeBuilder).build();

        AssignInstanceFieldInsn op = new AssignInstanceFieldInsn(mockFieldDefinition, mockCodeBuilder);
        IllegalStateException ex = assertThrows(IllegalStateException.class, op::build);
        assertThat(ex, hasProperty("message", is("Expected 1 element placed onto the stack. Instead 0 elements were added/removed.")));
    }

    @Test
    public void illegalStateException_instanceTypeOnStackIsPrimitive() {
        AsmClassBuilder<Object> builder = new AsmClassBuilder<>(Object.class)
            .withConstructor(constructor(publicOnly(), noParameters(),
                superConstructor(Object.class, noParameters()), //basic super() call
                //Invoke a static method that will place a primitive onto the stack as the "instance type"
                invokeStatic(Math.class, name("abs"), parameters(int.class), type(int.class),
                    literal(1234)
                ).assignField(type(Object.class), name("fieldName"), type(int.class),
                    literal(1234) //trying to assign 1234 to a field named "fieldName" on an primitive value, which will result in an exception
                ),
                returnVoid()
            ));

        IllegalStateException ex = assertThrows(IllegalStateException.class, builder::build);
        assertThat(ex, hasProperty("message", is("Cannot access a field from primitive type 'int'.")));
    }

    @Test
    public void illegalStateException_arrayTypeOnStack() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
            invokeStatic(Array.class, name("newInstance"), parameters(Class.class, int.class), type(int[].class),
                literalObj(int.class),
                literal(10)
            ).assignField(type(int[].class), name("length"), type(int.class),
                literal(123) //Attempting to set the length field of an array which will result in an IllegalArgumentException from the builder
            )
        );
        assertThat(ex, hasProperty("message", is("Field owner cannot be void, primitive, or an array type.")));
    }

    public static abstract class TestBaseType {
        public abstract String getStr();
    }

    @Test
    public void successfullyAssignInstanceField() {
        AsmClassBuilder<TestBaseType> builder = new AsmClassBuilder<>(TestBaseType.class, publicOnly())
            .withField(field(privateOnly(), type(String.class), name("str"))) //private String str;
            .withConstructor(constructor(publicOnly(), parameters(String.class), //public TestBaseTypeImpl(String strParam)
                //super();
                superConstructor(TestBaseType.class, noParameters()),
                //this.str = strParam;
                this_().assignField(type(ThisClass.class), name("str"), type(String.class),
                    getVar(1)
                ),
                //return;
                returnVoid()
            ))
            .withMethod(method(publicOnly(), name("getStr"), noParameters(), type(String.class), //public String getStr()
                returnValue( //return this.str;
                    this_().getField(type(ThisClass.class), name("str"), type(String.class))
                )
            ));

        String testString = RandomStringUtils.randomAlphanumeric(64);
        TestBaseType instance = builder.buildInstance(testString);

        assertThat(instance.getStr(), is(testString));
    }
}