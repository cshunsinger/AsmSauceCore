package io.github.cshunsinger.asmsauce.modifiers;

import io.github.cshunsinger.asmsauce.ClassBuildingContext;
import io.github.cshunsinger.asmsauce.ThisClass;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import static org.objectweb.asm.Opcodes.*;

/**
 * Represents the access modifiers placed on a Java member (class, method, constructor, field, etc.)
 */
@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class AccessModifiers {
    /**
     * @return The underlying jvm modifier flags.
     */
    private final int jvmModifiers;

    /**
     * Gets whether or not the jvm modifier flags represented by this instance contain the public flag.
     * @return True if this instance contains the 'public' flag, else false.
     */
    public boolean isPublic() {
        return (jvmModifiers & ACC_PUBLIC) > 0;
    }

    /**
     * Gets whether or not the jvm modifier flags represented by this instance contain the protected flag.
     * @return True if this instance contains the 'protected' flag, else false.
     */
    public boolean isProtected() {
        return (jvmModifiers & ACC_PROTECTED) > 0;
    }

    /**
     * Gets whether or not the jvm modifier flags represented by this instance contain the private flag.
     * @return True if this instance contains the 'private' flag, else false.
     */
    public boolean isPrivate() {
        return (jvmModifiers & ACC_PRIVATE) > 0;
    }

    /**
     * Gets whether or not the jvm modifier flags represented by this instance contain the static flag.
     * @return True if this instance contains the 'static' flag, else false.
     */
    public boolean isStatic() {
        return (jvmModifiers & ACC_STATIC) > 0;
    }

    /**
     * Gets whether or not the jvm modifier flags represented by this instance contain the final flag.
     * @return True if this instance contains the 'final' flag, else false.
     */
    public boolean isFinal() {
        return (jvmModifiers & ACC_FINAL) > 0;
    }

    /**
     * Creates a copy of these modifiers with the public flag set.
     * @return A new instance with the public flag added.
     */
    public AccessModifiers withPublic() {
        return customAccess(jvmModifiers | ACC_PUBLIC);
    }

    /**
     * Creates a copy of these modifiers with the protected flag set.
     * @return A new instance with the protected flag added.
     */
    public AccessModifiers withProtected() {
        return customAccess(jvmModifiers | ACC_PROTECTED);
    }

    /**
     * Creates a copy of these modifiers with the private flag set.
     * @return A new instance with the private flag added.
     */
    public AccessModifiers withPrivate() {
        return customAccess(jvmModifiers | ACC_PRIVATE);
    }

    /**
     * Creates a copy of these modifiers with the static flag set.
     * @return A new instance with the static flag added.
     */
    public AccessModifiers withStatic() {
        return customAccess(jvmModifiers | ACC_STATIC);
    }

    /**
     * Creates a copy of these modifiers with the final flag set.
     * @return A new instance with the final flag added.
     */
    public AccessModifiers withFinal() {
        return customAccess(jvmModifiers | ACC_FINAL);
    }

    /**
     * Creates an instance representing private access. Equivalent to defining something as 'private'
     * @return A new access modifiers instance.
     */
    public static AccessModifiers privateOnly() {
        return customAccess(ACC_PRIVATE);
    }

    /**
     * Creates an instance representing package-private access. Equivalent to not defining any modifiers.
     * @return A new access modifiers instance.
     */
    public static AccessModifiers packageOnly() {
        return customAccess(0);
    }

    /**
     * Creates an instance representing protected access. Equivalent to defining something as 'protected'
     * @return A new access modifiers instance.
     */
    public static AccessModifiers protectedOnly() {
        return customAccess(ACC_PROTECTED);
    }

    /**
     * Creates an instance representing public access. Equivalent to defining something as 'public'
     * @return A new access modifiers instance.
     */
    public static AccessModifiers publicOnly() {
        return customAccess(ACC_PUBLIC);
    }

    /**
     * Creates an instance representing private and static access. Equivalent to defining something as 'private static'
     * @return A new access modifiers instance.
     */
    public static AccessModifiers privateStatic() {
        return customAccess(ACC_PRIVATE | ACC_STATIC);
    }

    /**
     * Creates an instance representing package-private and static access. Equivalent to defining something as 'static'
     * @return A new access modifiers instance.
     */
    public static AccessModifiers packageStatic() {
        return customAccess(ACC_STATIC);
    }

    /**
     * Creates an instance representing protected and static access. Equivalent to defining something as 'protected static'
     * @return A new access modifiers instance.
     */
    public static AccessModifiers protectedStatic() {
        return customAccess(ACC_PROTECTED | ACC_STATIC);
    }

    /**
     * Creates an instance representing public and static access. Equivalent to defining something as 'public static'
     * @return A new access modifiers instance.
     */
    public static AccessModifiers publicStatic() {
        return customAccess(ACC_PUBLIC | ACC_STATIC);
    }

    /**
     * Creates an instance representing package-private final access. Equivalent to defining something as 'final'
     * @return A new access modifiers instance.
     */
    public static AccessModifiers packageFinal() {
        return customAccess(ACC_FINAL);
    }

    /**
     * Creates an instance representing protected and final access. Equivalent to defining something as 'protected final'
     * @return A new access modifiers instance.
     */
    public static AccessModifiers protectedFinal() {
        return customAccess(ACC_PROTECTED | ACC_FINAL);
    }

    /**
     * Creates an instance representing public and final access. Equivalent to defining something as 'public final'
     * @return A new access modifiers instance.
     */
    public static AccessModifiers publicFinal() {
        return customAccess(ACC_PUBLIC | ACC_FINAL);
    }

    /**
     * Creates an instance representing package-private static final access. Equivalent to defining something as 'static final'
     * @return A new access modifiers instance.
     */
    public static AccessModifiers packageStaticFinal() {
        return customAccess(ACC_STATIC | ACC_FINAL);
    }

    /**
     * Creates an instance representing protected static final access. Equivalent to defining something as 'protected static final'
     * @return A new access modifiers instance.
     */
    public static AccessModifiers protectedStaticFinal() {
        return customAccess(ACC_PROTECTED | ACC_STATIC | ACC_FINAL);
    }

    /**
     * Creates an instance representing public static final access. Equivalent to defining something as 'public static final'.
     * @return A new access modifiers instance.
     */
    public static AccessModifiers publicStaticFinal() {
        return customAccess(ACC_PUBLIC | ACC_STATIC | ACC_FINAL);
    }

    /**
     * Creates an instance from any int representing any arbitrary access modifier flags.
     * @param accessModifiers An int representing a set of modifier flags.
     * @return A new access modifiers instance containing the custom flags provided.
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
     * @param otherAccess The access modifiers of the member of the "declaringClass" being tested.
     * @return True if `accessorClass` is allowed to access a member with X access level inside of `declaringClass`. Or else false.
     * @throws IllegalArgumentException If the provided class building context is null and accessorClass is ThisClass.class.
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