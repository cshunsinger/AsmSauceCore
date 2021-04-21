package io.github.cshunsinger.asmsauce.definitions;

import io.github.cshunsinger.asmsauce.ThisClass;
import io.github.cshunsinger.asmsauce.DefinitionBuilders;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import static io.github.cshunsinger.asmsauce.util.AsmUtils.jvmClassname;
import static io.github.cshunsinger.asmsauce.util.AsmUtils.jvmTypeDefinition;

/**
 * Defines a single type. A type definition is defined by a Java class.
 * @param <T> The type being defined.
 */
@ToString
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class TypeDefinition<T> {
    /**
     * The class that this type represents, if this type already exists as a loaded Java class.
     * @return The class backing this type definition, or null if this type definition represents a type that has not
     * been created yet, such as a class that has not been built with the AsmClassBuilder yet.
     */
    @Getter
    private final Class<T> type;
    /**
     * The fully-qualified JVM name of this type. This is equal to the fully qualified name of a class, but with
     * forward slashes (/) instead of dots (.)
     * @return The JVM name of this type.
     * @see io.github.cshunsinger.asmsauce.util.AsmUtils#jvmClassname(Class)
     */
    @Getter
    private final String jvmTypeName;
    /**
     * The JVM definition of this type.
     * @return The JVM definition of this type.
     * @see io.github.cshunsinger.asmsauce.util.AsmUtils#jvmTypeDefinition(Class)
     */
    @Getter
    private final String jvmTypeDefinition;

    /**
     * Defines a type from an existing Java class.
     * @param type The existing Java class.
     * @throws IllegalArgumentException If type is null.
     */
    public TypeDefinition(Class<T> type) {
        if(type == null)
            throw new IllegalArgumentException("Field type cannot be null.");

        this.type = type;
        this.jvmTypeName = jvmClassname(type);
        this.jvmTypeDefinition = jvmTypeDefinition(type);
    }

    /**
     * Defines a type from the provided jvm type name. This allows a type to be defined without a pre-existing
     * Java class.
     * @param jvmTypeName The full jvm name of the class being defined as a type.
     * @return A new type definition for a Java class that may not yet exist.
     */
    public static TypeDefinition<ThisClass> fromCustomJvmName(String jvmTypeName) {
        return new TypeDefinition<>(ThisClass.class, jvmTypeName, "L" + jvmTypeName + ";");
    }

    /**
     * Gets the JVM classname of this type. If this type represents the type of 'this' inside of a class that is being
     * constructed, then this will return the provided JVM classname override.
     * @param newJvmClassname The JVM classname to return instead of this type's JVM classname.
     * @return Returns the JVM classname of this defined type if this defined type does not represent 'this' type. Otherwise
     * the specified 'newJvmClassname' value is returned instead.
     */
    public String getJvmTypeName(String newJvmClassname) {
        if(type == ThisClass.class)
            return newJvmClassname;
        else
            return getJvmTypeName();
    }

    /**
     * Gets the fully qualified classname, in dot-notation, of the class defined by this type. The class may or may not
     * already exist.
     * @return The fully qualified name of the class defined by this type.
     */
    public String getClassName() {
        if(type == ThisClass.class)
            return getJvmTypeName().replace('/', '.');
        else
            return type.getName();
    }

    /**
     * Get the JVM type definition String for this type. If this type definition represents the type of 'this' inside
     * a class being built, then the specified 'newJvmClassname' will be used to build the JVM type instead.
     * @param newJvmClassname The JVM classname to override with if this type defines the type of 'this'.
     * @return The JVM type as a String.
     * @see io.github.cshunsinger.asmsauce.util.AsmUtils#jvmTypeDefinition(Class)
     */
    public String getJvmTypeDefinition(String newJvmClassname) {
        if(type == ThisClass.class)
            return "L" + newJvmClassname + ";";
        else
            return getJvmTypeDefinition();
    }

    /**
     * Gets whether or not this type defines the "void" type.
     * @return Returns true if this type definition defines a void type. False if otherwise.
     */
    public boolean isVoid() {
        return type == void.class || type == Void.class;
    }

    /**
     * Gets whether or not this type is defined from a primitive type.
     * @return Returns true if this type is defined by a primitive Class, or false if not.
     */
    public boolean isPrimitive() {
        return type.isPrimitive();
    }

    /**
     * Converts an array of java Class objects into an array of types defined by those classes. The returned array will
     * have type definition instances in the same order as the classes they are made from.
     * @param classes The array of classes to define types for.
     * @return An array of type definitions, defined from the provided classes.
     */
    public static TypeDefinition<?>[] typesFromClasses(Class<?>... classes) {
        TypeDefinition<?>[] types = new TypeDefinition[classes.length];
        for(int i = 0; i < classes.length; i++) {
            types[i] = DefinitionBuilders.type(classes[i]);
        }
        return types;
    }

    /**
     * Determines equality.
     * @param other The object to compare to this one.
     * @return Returns true if this type is defined by the same class as the other type.
     */
    @Override
    public boolean equals(Object other) {
        if(this == other)
            return true;

        if(other instanceof TypeDefinition<?>) {
            TypeDefinition<?> otherDef = (TypeDefinition<?>)other;
            return otherDef.getType() == this.getType();
        }
        return false;
    }
}