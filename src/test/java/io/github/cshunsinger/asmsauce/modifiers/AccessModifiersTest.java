package io.github.cshunsinger.asmsauce.modifiers;

import io.github.cshunsinger.asmsauce.ClassBuildingContext;
import io.github.cshunsinger.asmsauce.ThisClass;
import io.github.cshunsinger.asmsauce.BaseUnitTest;
import org.junit.jupiter.api.Test;

import java.util.stream.Stream;

import static io.github.cshunsinger.asmsauce.DefinitionBuilders.type;
import static java.util.Collections.emptyList;
import static org.hamcrest.Matchers.*;
import static org.objectweb.asm.Opcodes.*;
import static io.github.cshunsinger.asmsauce.modifiers.AccessModifiers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class AccessModifiersTest extends BaseUnitTest {
    @Test
    public void publicAccessAlwaysAccessible() {
        assertTrue(AccessModifiers.isAccessible(type(Object.class), type(String.class), publicOnly()));
    }

    @Test
    public void privateMembersOnlyAccessibleFromDeclaringClass() {
        assertTrue(AccessModifiers.isAccessible(type(String.class), type(String.class), privateOnly()));
    }

    @Test
    public void privateMembersNotAccessibleOutsideOfDeclaringClass() {
        assertFalse(AccessModifiers.isAccessible(type(String.class), type(Object.class), privateOnly()));
    }

    @Test
    public void packageAndProtectedMembersAreAccessibleFromWithinTheSamePackage() {
        assertTrue(AccessModifiers.isAccessible(type(String.class), type(Integer.class), packageOnly()));
        assertTrue(AccessModifiers.isAccessible(type(String.class), type(Integer.class), protectedOnly()));
    }

    @Test
    public void packageAndProtectedMembersAreNotAccessibleFromOutsideThePackage() {
        assertFalse(AccessModifiers.isAccessible(type(Stream.class), type(String.class), packageOnly()));
        assertFalse(AccessModifiers.isAccessible(type(Stream.class), type(String.class), protectedOnly()));
    }

    @Test
    public void protectedMembersAreAccessibleFromChildrenOfTheDeclaringClass() {
        assertTrue(AccessModifiers.isAccessible(type(String.class), type(CharSequence.class), protectedOnly()));
    }

    @Test
    public void classBeingBuiltCanAccessProtectedMembersOfBaseClass() {
        new ClassBuildingContext(null, "com/example/MyNewClass", Object.class, emptyList(), emptyList(), emptyList(), emptyList());

        assertTrue(AccessModifiers.isAccessible(type(ThisClass.class), type(Object.class), protectedOnly()));
    }

    @Test
    public void classBeingBuiltCanAccessPackageMembersInSamePackage() {
        new ClassBuildingContext(null, "java/lang/ThisType", Object.class, emptyList(), emptyList(), emptyList(), emptyList());

        assertTrue(AccessModifiers.isAccessible(type(ThisClass.class), type(String.class), packageOnly()));
    }

    @Test
    public void differentAccessModifierValues() {
        validateModifiers(privateOnly(), ACC_PRIVATE);
        validateModifiers(packageOnly(), 0);
        validateModifiers(protectedOnly(), ACC_PROTECTED);
        validateModifiers(publicOnly(), ACC_PUBLIC);
        validateModifiers(packageOnly().withPublic(), ACC_PUBLIC);
        validateModifiers(packageOnly().withProtected(), ACC_PROTECTED);
        validateModifiers(packageOnly().withPrivate(), ACC_PRIVATE);
        validateModifiers(packageOnly().withStatic(), ACC_STATIC);
        validateModifiers(packageOnly().withFinal(), ACC_FINAL);
        validateModifiers(privateStatic(), ACC_PRIVATE | ACC_STATIC);
        validateModifiers(packageStatic(), ACC_STATIC);
        validateModifiers(protectedStatic(), ACC_PROTECTED | ACC_STATIC);
        validateModifiers(publicStatic(), ACC_PUBLIC | ACC_STATIC);
        validateModifiers(privateFinal(), ACC_PRIVATE | ACC_FINAL);
        validateModifiers(packageFinal(), ACC_FINAL);
        validateModifiers(protectedFinal(), ACC_FINAL | ACC_PROTECTED);
        validateModifiers(publicFinal(), ACC_FINAL | ACC_PUBLIC);
        validateModifiers(packageStaticFinal(), ACC_STATIC | ACC_FINAL);
        validateModifiers(protectedStaticFinal(), ACC_STATIC | ACC_PROTECTED | ACC_FINAL);
        validateModifiers(publicStaticFinal(), ACC_STATIC | ACC_PUBLIC | ACC_FINAL);
    }

    private void validateModifiers(AccessModifiers modifiers, int access) {
        assertThat(modifiers, hasProperty("jvmModifiers", is(access)));
    }

    @Test
    @SuppressWarnings("ConstantConditions")
    public void accessModifiersEquality() {
        assertThat(publicOnly(), is(publicOnly()));
        assertThat(publicOnly().equals(null), is(false));
    }
}