package io.github.cshunsinger.asmsauce.definitions;

import io.github.cshunsinger.asmsauce.ThisClass;
import io.github.cshunsinger.asmsauce.BaseUnitTest;
import io.github.cshunsinger.asmsauce.code.CodeBlock;
import io.github.cshunsinger.asmsauce.code.CodeInsnBuilder;
import io.github.cshunsinger.asmsauce.code.CodeInsnBuilderLike;
import io.github.cshunsinger.asmsauce.modifiers.AccessModifiers;
import org.junit.jupiter.api.Test;

import static io.github.cshunsinger.asmsauce.DefinitionBuilders.*;
import static io.github.cshunsinger.asmsauce.modifiers.AccessModifiers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TypeDefinitionTest extends BaseUnitTest {
    @Test
    public void createTypeDefinitionRepresentingTheCurrentClassUnderConstruction() {
        assertThat(TypeDefinition.fromClass(ThisClass.class), instanceOf(ThisTypeDefinition.class));
        assertThat(TypeDefinition.fromClass(Object.class), not(instanceOf(ThisTypeDefinition.class)));
    }

    @Test
    public void returnNullValueIfAttemptingToCreateTypeDefinitionFromNullClass() {
        assertThat(TypeDefinition.fromClass(null), nullValue());
    }

    @Test
    public void convertWrapperToPrimitiveType_inputTypeIsAlreadyPrimitive() {
        assertThat(TypeDefinition.wrapperToPrimitiveType(type(int.class)), is(type(int.class)));
    }

    @Test
    public void convertWrapperToPrimitiveType_inputTypeIsWrapper() {
        assertThat(TypeDefinition.wrapperToPrimitiveType(type(Integer.class)), is(type(int.class)));
    }

    @Test
    public void convertWrapperToPrimitiveType_inputTypeIsNeitherWrapperNorPrimitive() {
        assertThat(TypeDefinition.wrapperToPrimitiveType(type(Object.class)), nullValue());
    }

    @Test
    public void convertPrimitiveToWrapperType_inputTypeIsAlreadyWrapper() {
        assertThat(TypeDefinition.primitiveToWrapperType(type(Integer.class)), is(type(Integer.class)));
    }

    @Test
    public void convertPrimitiveToWrapperType_inputTypeIsPrimitive() {
        assertThat(TypeDefinition.primitiveToWrapperType(type(int.class)), is(type(Integer.class)));
    }

    @Test
    public void convertPrimitiveToWrapperType_inputTypeIsNeitherWrapperNorPrimitive() {
        assertThat(TypeDefinition.primitiveToWrapperType(type(Object.class)), nullValue());
    }

    @Test
    public void produceArrayOfTypeDefinitionsFromArrayOfClasses() {
        assertThat(TypeDefinition.typesFromClasses(new Class[]{ThisClass.class, Object.class}), arrayContaining(
            type(ThisClass.class),
            type(Object.class)
        ));
    }

    @Test
    public void illegalArgumentException_nullClass() {
        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class,
            () -> new TypeDefinition(null)
        );
        assertThat(ex, hasProperty("message", is("Type cannot be null.")));
    }

    @Test
    @SuppressWarnings("ConstantConditions")
    public void testEqualityBetweenTypeDefinitions() {
        TypeDefinition typeDefinition = type(ThisClass.class);
        assertThat(typeDefinition.equals(type(Object.class)), is(false));
        assertThat(typeDefinition.equals(type(ThisClass.class)), is(true));
        assertThat(typeDefinition.equals(null), is(false));
        assertThat(type(Object.class).equals(type(Object.class)), is(true));
    }

    @Test
    public void correctlyIdentifyAsPrimitiveWrapperArrayOrVoidType() {
        assertThat(type(Object.class), allOf(
            hasProperty("void", is(false)),
            hasProperty("primitive", is(false)),
            hasProperty("primitiveWrapper", is(false)),
            hasProperty("primitiveOrWrapper", is(false)),
            hasProperty("array", is(false))
        ));

        assertThat(type(int.class), allOf(
            hasProperty("void", is(false)),
            hasProperty("primitive", is(true)),
            hasProperty("primitiveWrapper", is(false)),
            hasProperty("primitiveOrWrapper", is(true)),
            hasProperty("array", is(false))
        ));

        assertThat(type(void.class), allOf(
            hasProperty("void", is(true)),
            hasProperty("primitive", is(true)),
            hasProperty("primitiveWrapper", is(false)),
            hasProperty("primitiveOrWrapper", is(true)),
            hasProperty("array", is(false))
        ));

        assertThat(type(Void.class), allOf(
            hasProperty("void", is(true)),
            hasProperty("primitive", is(false)),
            hasProperty("primitiveWrapper", is(false)),
            hasProperty("primitiveOrWrapper", is(false)),
            hasProperty("array", is(false))
        ));

        assertThat(type(Integer.class), allOf(
            hasProperty("void", is(false)),
            hasProperty("primitive", is(false)),
            hasProperty("primitiveWrapper", is(true)),
            hasProperty("primitiveOrWrapper", is(true)),
            hasProperty("array", is(false))
        ));

        assertThat(type(int[].class), allOf(
            hasProperty("void", is(false)),
            hasProperty("primitive", is(false)),
            hasProperty("primitiveWrapper", is(false)),
            hasProperty("primitiveOrWrapper", is(false)),
            hasProperty("array", is(true))
        ));
    }

    @Test
    public void getInterfaceTypeDefinitionsOfCurrentType() {
        assertThat(type(CodeBlock.class), hasProperty("interfaces", empty()));
        assertThat(type(CodeInsnBuilder.class), hasProperty("interfaces", contains(
            type(CodeInsnBuilderLike.class)
        )));
    }

    @Test
    public void getSuperTypeDefinitionOfCurrentType() {
        assertThat(type(CodeBlock.class), hasProperty("supertype", is(type(CodeInsnBuilder.class))));
        assertThat(type(Object.class), hasProperty("supertype", nullValue()));
    }

    public interface OtherTestInterface {}

    @SuppressWarnings("unused")
    public interface TestInterface extends OtherTestInterface {
        int TEST_INTERFACE_FIELD = 10;
    }

    @SuppressWarnings("unused")
    public static class TestClass implements TestInterface {
        private final String TEST_CLASS_FIELD = "MyString";

        private void testMethod() {}
    }

    @Test
    public void getDeclaredFieldFromInterfaceType() {
        assertThat(type(TestInterface.class).getDeclaredField("TEST_INTERFACE_FIELD"), allOf(
            notNullValue(),
            hasProperty("fieldName", is(name("TEST_INTERFACE_FIELD"))),
            hasProperty("fieldType", is(type(int.class))),
            hasProperty("accessModifiers", is(publicStaticFinal())),
            hasProperty("fieldOwner", is(type(TestInterface.class)))
        ));
    }

    @Test
    public void getDeclaredFieldFromClassType() {
        assertThat(type(TestClass.class).getDeclaredField("TEST_CLASS_FIELD"), allOf(
            notNullValue(),
            hasProperty("fieldName", is(name("TEST_CLASS_FIELD"))),
            hasProperty("fieldType", is(type(String.class))),
            hasProperty("accessModifiers", is(privateFinal())),
            hasProperty("fieldOwner", is(type(TestClass.class)))
        ));
    }

    @Test
    public void getDeclaredConstructorsShouldReturnConstructorsDeclaredInBackingClass() {
        assertThat(type(TestClass.class).getDeclaredConstructors(), contains(
            allOf(
                hasProperty("name", is(NameDefinition.CONSTRUCTOR_NAME_DEFINITION)),
                hasProperty("returnType", is(voidType())),
                hasProperty("modifiers", is(publicOnly())),
                hasProperty("owner", is(type(TestClass.class)))
            )
        ));
    }

    @Test
    public void getDeclaredMethodsShouldReturnMethodsDeclaredInBackingClass() {
        assertThat(type(TestClass.class).getDeclaredMethods(), hasItem(
            allOf(
                hasProperty("name", is(name("testMethod"))),
                hasProperty("returnType", is(voidType())),
                hasProperty("modifiers", is(privateOnly())),
                hasProperty("owner", is(type(TestClass.class)))
            )
        ));
    }

    @Test
    public void generateTypeHierarchy() {
        assertThat(type(TestClass.class).hierarchy(), contains(
            contains(type(TestClass.class)),
            contains(type(TestInterface.class), type(OtherTestInterface.class)),
            contains(type(Object.class))
        ));
    }

    @Test
    public void generateFlattenedTypeHierarchy() {
        assertThat(type(TestClass.class).flatHierarchy(), contains(
            type(TestClass.class),
            type(TestInterface.class),
            type(OtherTestInterface.class),
            type(Object.class)
        ));
    }

    @Test
    public void primitivesArraysAndVoidCannotHaveMembers() {
        assertThat(type(int.class).canHaveMembers(), is(false));
        assertThat(type(Void.class).canHaveMembers(), is(false));
        assertThat(type(int[].class).canHaveMembers(), is(false));
    }

    @Test
    public void nonArrayReferenceTypesCanHaveMembers() {
        assertThat(type(Object.class).canHaveMembers(), is(true));
    }

    @Test
    public void getComponentTypeWhenTypeIsArray() {
        assertThat(type(int[].class).getComponentType(), is(TypeDefinition.INT));
    }

    @Test
    public void getComponentTypeWhenTypeIsNotAnArray() {
        assertThat(type(int.class).getComponentType(), nullValue());
    }
}