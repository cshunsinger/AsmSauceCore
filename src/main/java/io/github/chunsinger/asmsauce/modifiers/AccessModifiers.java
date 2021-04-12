package io.github.chunsinger.asmsauce.modifiers;

import io.github.chunsinger.asmsauce.ClassBuildingContext;
import io.github.chunsinger.asmsauce.ThisClass;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import static aj.org.objectweb.asm.Opcodes.*;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class AccessModifiers {
    private final int jvmModifiers;

    public boolean isPublic() {
        return (jvmModifiers & ACC_PUBLIC) > 0;
    }

    public boolean isProtected() {
        return (jvmModifiers & ACC_PROTECTED) > 0;
    }

    public boolean isPrivate() {
        return (jvmModifiers & ACC_PRIVATE) > 0;
    }

    public boolean isStatic() {
        return (jvmModifiers & ACC_STATIC) > 0;
    }

    public boolean isFinal() {
        return (jvmModifiers & ACC_FINAL) > 0;
    }

    public AccessModifiers withPublic() {

        return customAccess(jvmModifiers | ACC_PUBLIC);
    }

    public AccessModifiers withProtected() {
        return customAccess(jvmModifiers | ACC_PROTECTED);
    }

    public AccessModifiers withPrivate() {
        return customAccess(jvmModifiers | ACC_PRIVATE);
    }

    public AccessModifiers withStatic() {
        return customAccess(jvmModifiers | ACC_STATIC);
    }

    public AccessModifiers withFinal() {
        return customAccess(jvmModifiers | ACC_FINAL);
    }

    /**
     * Non-Static
     */

    public static AccessModifiers privateOnly() {
        return customAccess(ACC_PRIVATE);
    }

    public static AccessModifiers packageOnly() {
        return customAccess(0);
    }

    public static AccessModifiers protectedOnly() {
        return customAccess(ACC_PROTECTED);
    }

    public static AccessModifiers publicOnly() {
        return customAccess(ACC_PUBLIC);
    }

    /**
     * Static modifiers
     */

    public static AccessModifiers privateStatic() {
        return customAccess(ACC_PRIVATE | ACC_STATIC);
    }

    public static AccessModifiers packageStatic() {
        return customAccess(ACC_STATIC);
    }

    public static AccessModifiers protectedStatic() {
        return customAccess(ACC_PROTECTED | ACC_STATIC);
    }

    public static AccessModifiers publicStatic() {
        return customAccess(ACC_PUBLIC | ACC_STATIC);
    }

    /**
     * Final modifiers
     */

    public static AccessModifiers packageFinal() {
        return customAccess(ACC_FINAL);
    }

    public static AccessModifiers protectedFinal() {
        return customAccess(ACC_PROTECTED | ACC_FINAL);
    }

    public static AccessModifiers publicFinal() {
        return customAccess(ACC_PUBLIC | ACC_FINAL);
    }

    /**
     * Static final modifiers
     */

    public static AccessModifiers packageStaticFinal() {
        return customAccess(ACC_STATIC | ACC_FINAL);
    }

    public static AccessModifiers protectedStaticFinal() {
        return customAccess(ACC_PROTECTED | ACC_STATIC | ACC_FINAL);
    }

    public static AccessModifiers publicStaticFinal() {
        return customAccess(ACC_PUBLIC | ACC_STATIC | ACC_FINAL);
    }

    /**
     * Custom Access
     */

    public static AccessModifiers customAccess(int accessModifiers) {
        return new AccessModifiers(accessModifiers);
    }

    /**
     * Given a component (field, method, etc) that exists in another class (declaringClass), can that component be accessed
     * from another class (accessorClass)?
     *
     * Note that this method does not check for a field or method existing.
     * This utility method only answers the question of "Can 'accessorClass' access a member of 'declaringClass' of that member has 'X' access?"
     *
     * @param buildingContext Class building context strictly used when accessorClass is ThisClass.class.
     * @param accessorClass The class that wants to access a field or method.
     * @param declaringClass The class that supposedly contains the field or method.
     * @return True if `accessorClass` is allowed to access a member with X access level inside of `declaringClass`. Or else false.
     */
    public static boolean isAccessible(ClassBuildingContext buildingContext, Class<?> accessorClass, Class<?> declaringClass, AccessModifiers otherAccess) {
        if(accessorClass == ThisClass.class && buildingContext == null)
            throw new IllegalArgumentException("Building context cannot be null when accessorClass is ThisClass.class");

        //public members can always be access
        if(otherAccess.isPublic())
            return true;

        //private members can only be accessed inside of the class that declares that member
        //This means this method will always return false unless the class trying to access the private member also declares it.
        //Also member are always accessible if the class accessing the member is also the declaring class. Not just when the member is private.
        if(accessorClass == declaringClass)
            return true;

        //Private members are not accessible outside of the declaring class
        if(otherAccess.isPrivate())
            return false;

        //Take ThisClass.class and the class building context into account
        String accessorPackage = accessorClass.getPackageName();
        if(accessorClass == ThisClass.class) {
            String fullClassName = buildingContext.getClassName();
            accessorPackage = fullClassName.substring(0, fullClassName.lastIndexOf('.'));
        }

        //Protected and package-private members are accessible when `accessorClass` and `declaringClass` are in the same package
        if(accessorPackage.equals(declaringClass.getPackageName()))
            return true;

        //Finally, protected members can be accessed when `accessorClass` inherits from `declaringClass`
        if(otherAccess.isProtected()) {
            if(accessorClass == ThisClass.class)
                return buildingContext.getSuperclass().isAssignableFrom(declaringClass);
            else
                return declaringClass.isAssignableFrom(accessorClass);
        }

        return false;
    }
}