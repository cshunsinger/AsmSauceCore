package io.github.chunsinger.asmsauce.modifiers;

import io.github.chunsinger.asmsauce.ClassBuildingContext;
import io.github.chunsinger.asmsauce.ThisClass;
import io.github.chunsinger.asmsauce.testing.BaseUnitTest;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.util.stream.Stream;

import static aj.org.objectweb.asm.Opcodes.*;
import static io.github.chunsinger.asmsauce.modifiers.AccessModifiers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class AccessModifiersTest extends BaseUnitTest {
    @Mock
    private ClassBuildingContext mockBuildingContext;

    @Test
    public void illegalArgumentException_noBuildingContextWhenAccessorClassIsTheClassBeingBuilt() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> AccessModifiers.isAccessible(null, ThisClass.class, Object.class, publicOnly()));
        assertThat(ex, hasProperty("message", is("Building context cannot be null when accessorClass is ThisClass.class")));
    }

    @Test
    public void publicAccessAlwaysAccessible() {
        assertTrue(AccessModifiers.isAccessible(null, null, null, publicOnly()));
    }

    @Test
    public void privateMembersOnlyAccessibleFromDeclaringClass() {
        assertTrue(AccessModifiers.isAccessible(null, String.class, String.class, privateOnly()));
    }

    @Test
    public void privateMembersNotAccessibleOutsideOfDeclaringClass() {
        assertFalse(AccessModifiers.isAccessible(null, String.class, Object.class, privateOnly()));
    }

    @Test
    public void packageAndProtectedMembersAreAccessibleFromWithinTheSamePackage() {
        assertTrue(AccessModifiers.isAccessible(null, String.class, Integer.class, packageOnly()));
        assertTrue(AccessModifiers.isAccessible(null, String.class, Integer.class, protectedOnly()));
    }

    @Test
    public void packageAndProtectedMembersAreNotAccessibleFromOutsideThePackage() {
        assertFalse(AccessModifiers.isAccessible(null, Stream.class, String.class, packageOnly()));
        assertFalse(AccessModifiers.isAccessible(null, Stream.class, String.class, protectedOnly()));
    }

    @Test
    public void protectedMembersAreAccessibleFromChildrenOfTheDeclaringClass() {
        assertTrue(AccessModifiers.isAccessible(null, String.class, CharSequence.class, protectedOnly()));
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void classBeingBuiltCanAccessProtectedMembersOfBaseClass() {
        when(mockBuildingContext.getSuperclass()).thenReturn((Class)Object.class);
        when(mockBuildingContext.getClassName()).thenReturn("test.class.Name");

        assertTrue(AccessModifiers.isAccessible(mockBuildingContext, ThisClass.class, Object.class, protectedOnly()));
    }

    @Test
    public void classBeingBuiltCanAccessPackageMembersInSamePackage() {
        //Simulating ThisClass existing in the same package as String.class (java.lang)
        when(mockBuildingContext.getClassName()).thenReturn("java.lang.ThisType");

        assertTrue(AccessModifiers.isAccessible(mockBuildingContext, ThisClass.class, String.class, packageOnly()));
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
}