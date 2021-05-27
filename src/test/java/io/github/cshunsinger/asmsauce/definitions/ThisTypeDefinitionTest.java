package io.github.cshunsinger.asmsauce.definitions;

import io.github.cshunsinger.asmsauce.*;
import io.github.cshunsinger.asmsauce.code.CodeInsnBuilderLike;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;

import static io.github.cshunsinger.asmsauce.DefinitionBuilders.*;
import static io.github.cshunsinger.asmsauce.code.CodeBuilders.*;
import static io.github.cshunsinger.asmsauce.modifiers.AccessModifiers.*;
import static java.util.Collections.emptyList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

class ThisTypeDefinitionTest {
    private static final String SIMPLE_NAME = "TestingType";
    private static final String PACKAGE_NAME = "io.github.cshunsinger.asmsauce";
    private static final String JVM_TYPE_NAME = PACKAGE_NAME.replace('.', '/') + '/' + SIMPLE_NAME;
    private static final String CLASS_NAME = PACKAGE_NAME + "." + SIMPLE_NAME;
    private static final Class<?> SUPER_CLASS = ThisTypeDefinitionTest.class;
    private static final List<Class<?>> INTERFACE_CLASSES = List.of(CodeInsnBuilderLike.class);
    private static final FieldNode FIELD1 = FieldNode.field(
        publicOnly(),
        type(Object.class),
        name("firstField")
    );
    private static final FieldNode FIELD2 = FieldNode.field(
        privateStatic(),
        type(Object.class),
        name("secondField")
    );
    private static final MethodNode METHOD1 = MethodNode.method(
        publicOnly(),
        name("testMethod1"),
        noParameters()
    );
    private static final MethodNode METHOD2 = MethodNode.method(
        privateStatic(),
        name("testMethod2"),
        noParameters()
    );
    private static final ConstructorNode CONSTRUCTOR1 = ConstructorNode.constructor(
        privateOnly(),
        parameters(String.class),
        superConstructor(Object.class, noParameters())
    );
    private static final ConstructorNode CONSTRUCTOR2 = ConstructorNode.constructor(
        publicOnly(),
        noParameters(),
        thisConstructor(parameters(String.class), literalObj("MyValue"))
    );

    private final ThisTypeDefinition typeDefinition = new ThisTypeDefinition();

    @BeforeEach
    public void init() {
        new ClassBuildingContext(
            null,
            JVM_TYPE_NAME,
            SUPER_CLASS,
            INTERFACE_CLASSES,
            List.of(FIELD1, FIELD2),
            List.of(METHOD1, METHOD2),
            List.of(CONSTRUCTOR1, CONSTRUCTOR2)
        );
    }

    @AfterEach
    public void end() {
        ClassBuildingContext.reset();
    }

    @Test
    public void thisTypeIsNeverVoid() {
        assertFalse(typeDefinition.isVoid());
    }

    @Test
    public void thisTypeIsNeverPrimitive() {
        assertFalse(typeDefinition.isPrimitive());
    }

    @Test
    public void thisTypeIsNeverWrapper() {
        assertFalse(typeDefinition.isPrimitiveWrapper());
    }

    @Test
    public void thisTypeIsNeverPrimitiveOrWrapper() {
        assertFalse(typeDefinition.isPrimitiveOrWrapper());
    }

    @Test
    public void thisTypeGetsJvmTypeNameFromLiveClassBuildingContext() {
        assertThat(typeDefinition, hasProperty("jvmTypeName", is(JVM_TYPE_NAME)));
    }

    @Test
    public void thisTypeGetsFullyQualifiedTypeNameFromLiveClassBuildingContext() {
        assertThat(typeDefinition, hasProperty("className", is(CLASS_NAME)));
    }

    @Test
    public void thisTypeReturnsCorrectSimpleTypeNameAndPackageName() {
        assertThat(typeDefinition, allOf(
            hasProperty("simpleClassName", is(SIMPLE_NAME)),
            hasProperty("packageName", is(PACKAGE_NAME))
        ));
    }

    @Test
    public void thisTypeReturnsInterfaceTypesFromLiveClassBuildingContext() {
        assertThat(typeDefinition, hasProperty("interfaces",
            is(INTERFACE_CLASSES.stream().map(DefinitionBuilders::type).collect(Collectors.toList()))
        ));
    }

    @Test
    public void thisTypeReturnsSuperTypeFromLiveClassBuildingContext() {
        assertThat(typeDefinition, hasProperty("supertype", is(type(SUPER_CLASS))));
    }

    @Test
    public void thisTypeIsEqualToAnyOtherThisTypeInstance() {
        assertNotEquals(typeDefinition, null);
        assertEquals(typeDefinition, new ThisTypeDefinition());
    }

    @Test
    public void getDeclaredField_fieldNodeDoesNotExistInLiveClassBuildingContextWithName() {
        assertThat(typeDefinition.getDeclaredField("**NO FIELD**"), nullValue());
    }

    @Test
    public void getDeclaredField_fieldNodeFoundInLiveClassBuildingContext() {
        String firstFieldName = FIELD1.getFieldDefinition().getFieldName().getName();
        String secondFieldName = FIELD2.getFieldDefinition().getFieldName().getName();
        assertThat(typeDefinition.getDeclaredField(firstFieldName), is(FIELD1.getFieldDefinition()));
        assertThat(typeDefinition.getDeclaredField(secondFieldName), is(FIELD2.getFieldDefinition()));
    }

    @Test
    public void getDeclaredMethodsFromLiveClassBuildingContext() {
        assertThat(typeDefinition.getDeclaredMethods(), contains(
            METHOD1.getDefinition(),
            METHOD2.getDefinition()
        ));
    }

    @Test
    public void getDeclaredConstructorsFromLiveClassBuildingContext() {
        assertThat(typeDefinition.getDeclaredConstructors(), contains(
            CONSTRUCTOR1.getDefinition(),
            CONSTRUCTOR2.getDefinition()
        ));
    }

    @Test
    public void getJvmTypeDefinitionFromLiveClassBuildingContext() {
        assertThat(typeDefinition.getJvmTypeDefinition(), is("L" + JVM_TYPE_NAME + ";"));
    }

    @Test
    public void thisTypeIsAlwaysAssignableFromItself() {
        assertThat(typeDefinition.isAssignableFrom(new ThisTypeDefinition()), is(true));
    }

    @Test
    public void determineIfTypeIsInterfaceAbstractOrConcrete() {
        assertThat(typeDefinition.isInterface(), is(false));
        assertThat(typeDefinition.isAbstractClass(), is(false));
        assertThat(typeDefinition.isConcreteClass(), is(true));
    }
}