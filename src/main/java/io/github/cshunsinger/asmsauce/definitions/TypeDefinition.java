package io.github.cshunsinger.asmsauce.definitions;

import io.github.cshunsinger.asmsauce.ThisClass;
import lombok.Getter;
import lombok.ToString;
import org.apache.commons.lang3.ClassUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.github.cshunsinger.asmsauce.util.AsmUtils.jvmClassname;
import static io.github.cshunsinger.asmsauce.util.AsmUtils.jvmTypeDefinition;

/**
 * Defines a single type. A type definition is defined by a Java class.
 */
@ToString
public class TypeDefinition {
    private static final List<Class<?>> PRIMITIVES = List.of(
        byte.class, char.class, short.class, int.class, long.class, float.class, double.class
    );

    /**
     * The class that this type represents, if this type already exists as a loaded Java class.
     * @return The class backing this type definition, or null if this type definition represents a type that has not
     * been created yet, such as a class that has not been built with the AsmClassBuilder yet.
     */
    @Getter
    private final Class<?> type;
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
    TypeDefinition(Class<?> type) {
        if(type == null)
            throw new IllegalArgumentException("Type cannot be null.");

        this.type = type;
        this.jvmTypeName = jvmClassname(type);
        this.jvmTypeDefinition = jvmTypeDefinition(type);
    }

    /**
     * Wraps an existing Java class as a type definition.
     * This will return an instance of ThisTypeDefinition, which reads from the current class building context
     * during bytecode generation, if ThisClass.class is used.
     *
     * @param cls The class to create a type definition from.
     * @return Returns a TypeDefinition instance which wraps the provided `cls`. If `cls` is null, then null is returned.
     */
    public static TypeDefinition fromClass(Class<?> cls) {
        if(cls == null)
            return null;
        else if(cls == ThisClass.class)
            return new ThisTypeDefinition();
        else
            return new TypeDefinition(cls);
    }

    /**
     * Converts an array of java Class objects into an array of types defined by those classes. The returned array will
     * have type definition instances in the same order as the classes they are made from.
     * @param classes The array of classes to define types for.
     * @return An array of type definitions, defined from the provided classes.
     */
    public static TypeDefinition[] typesFromClasses(Class<?>[] classes) {
        TypeDefinition[] types = new TypeDefinition[classes.length];
        for(int i = 0; i < classes.length; i++) {
            types[i] = fromClass(classes[i]);
        }
        return types;
    }

    /**
     * Converts a wrapper type into a primitive type.
     * @param in The wrapper type to convert into a primitive type.
     * @return The provided input type if it is already a primitive, the matching primitive type if the input type is
     * a wrapper type, or null if the input type is neither a primitive type nor a wrapper type.
     * @throws NullPointerException If 'in' is null.
     */
    public static TypeDefinition wrapperToPrimitiveType(TypeDefinition in) {
        if(in.isPrimitiveWrapper())
            return fromClass(ClassUtils.wrapperToPrimitive(in.type));
        else if(in.isPrimitive())
            return in;
        else
            return null;
    }

    /**
     * Converts a primitive type into a wrapper type.
     * @param in The primitive type to convert into a wrapper type.
     * @return The provided input type if it is already a wrapper, the matching wrapper type if the input type is
     * a primitive type, or null if the input type is neither a primitive type nor wrapper type.
     * @throws NullPointerException If 'in' is null.
     */
    public static TypeDefinition primitiveToWrapperType(TypeDefinition in) {
        if(in.isPrimitive())
            return fromClass(ClassUtils.primitiveToWrapper(in.type));
        else if(in.isPrimitiveWrapper())
            return in;
        else
            return null;
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
     * Gets whether or not this type is defined from a primitive type OR a primitive wrapper type.
     * @return True if this type represents a primitive type OR a primitive wrapper type, or else false.
     */
    public boolean isPrimitiveOrWrapper() {
        return ClassUtils.isPrimitiveOrWrapper(type);
    }

    /**
     * Gets whether or not this type is a primitive wrapper type.
     * @return True if this type represents a primitive wrapper, else false.
     */
    public boolean isPrimitiveWrapper() {
        return ClassUtils.isPrimitiveWrapper(type);
    }

    /**
     * Gets whether or not this type is defined from an array type.
     * @return True if this type is defined from an array class.
     */
    public boolean isArray() {
        return type.isArray();
    }

    /**
     * Gets the interface types that this type implements.
     * If no interface types are implemented by this type, then an empty list will be returned.
     * @return A list of 0 or more interface types implemented by this type.
     */
    public List<TypeDefinition> getInterfaces() {
        return List.of(typesFromClasses(type.getInterfaces()));
    }

    /**
     * Gets the type that this type inherits.
     * @return The supertype of this type, or null if there is no supertype.
     */
    public TypeDefinition getSupertype() {
        return fromClass(type.getSuperclass());
    }

    /**
     * Attempts to find a declared field in this type and return it.
     * @param fieldName The name of the declared field to get.
     * @return Returns a field definition of the found field, or null if no field was found with that name.
     * @see #getDeclaredField(NameDefinition)
     */
    public CompleteFieldDefinition getDeclaredField(String fieldName) {
        try {
            return CompleteFieldDefinition.fromField(type.getDeclaredField(fieldName));
        }
        catch(NoSuchFieldException ignored) {
            return null;
        }
    }

    /**
     * Attempts to find a declared field in this type and return it.
     * @param fieldName The name of the declared field to get.
     * @return Returns a field definition of the found field, or null if no field was found with that name.
     * @see #getDeclaredField(String)
     */
    public CompleteFieldDefinition getDeclaredField(NameDefinition fieldName) {
        return getDeclaredField(fieldName.getName());
    }

    /**
     * Gets the list of declared methods in this type.
     * @return The list of declared methods.
     */
    public List<CompleteMethodDefinition> getDeclaredMethods() {
        return Stream.of(type.getDeclaredMethods())
            .map(CompleteMethodDefinition::fromExecutable)
            .collect(Collectors.toList());
    }

    /**
     * Gets the list of declared constructors in this type.
     * @return The list of declared constructors.
     */
    public List<CompleteMethodDefinition> getDeclaredConstructors() {
        return Stream.of(type.getDeclaredConstructors())
            .map(CompleteMethodDefinition::fromExecutable)
            .collect(Collectors.toList());
    }

    /**
     * Generates a list of methods declared by this type whose name and parameter count match the provided name and
     * number of provided parameters, and whose parameter types are all assignable from the provided parameter types.
     * @param name The name of the method to search for.
     * @param parameters The method parameters to match on.
     * @return A list of 0 or more methods declared by this type.
     */
    public List<CompleteMethodDefinition> findDeclaredMatchingMethods(NameDefinition name, ParametersDefinition parameters) {
        return getDeclaredMethods().stream()
            .filter(method -> method.getName().equals(name))
            .filter(method -> method.getParameters().matches(parameters))
            .sorted((left, right) -> sortMatchingExecutables(left, right, parameters))
            .collect(Collectors.toList());
    }

    /**
     * Generates a list of constructors declared by this type whose parameter count matches the provided parameter count,
     * and whose parameter types are all assignable from the provided parameter types.
     * @param parameters The constructor parameters to match on.
     * @return A list of 0 or more constructors declared by this type.
     */
    public List<CompleteMethodDefinition> findDeclaredMatchingConstructors(ParametersDefinition parameters) {
        return getDeclaredConstructors().stream()
            .filter(constructor -> constructor.getParameters().matches(parameters))
            .sorted((left, right) -> sortMatchingExecutables(left, right, parameters))
            .collect(Collectors.toList());
    }

    /**
     * Creates a full type hierarchy as a list of type lists. Every element in the returned list is another list of
     * one or more types. The first element is a singleton list containing this type, the final element will be
     * the type representation of Object.class. The hierarchy list goes this order:
     * <pre>
     * [
     *     [current-type],
     *     [interfaces of current type],
     *     [supertype of current-type],
     *     [interfaces of supertype of current-type],
     *     ...
     *     [Object type]
     * ]
     * </pre>
     *
     * @return Returns a list of types that make up this type's inheritance hierarchy.
     * @see #flatHierarchy()
     */
    public List<List<TypeDefinition>> hierarchy() {
        List<List<TypeDefinition>> hierarchyList = new ArrayList<>();

        for(TypeDefinition current = this; current != null; current = current.getSupertype()) {
            hierarchyList.add(List.of(current));
            List<TypeDefinition> currentInterfaces = current.recursiveInterfaces();
            if(!currentInterfaces.isEmpty())
                hierarchyList.add(currentInterfaces);
        }

        return hierarchyList;
    }

    /**
     * Creates a full type hierarchy as a list of types. The list starts with this type, and ends with the Object type.
     * This method behaves like {@link #hierarchy()}, but this method returns a single list which is the equivalent
     * to flattening the list of lists returned by {@link #hierarchy()}.
     * @return Returns a flattens list of types representing the hierarchy of types.
     * @see #hierarchy()
     */
    public List<TypeDefinition> flatHierarchy() {
        List<TypeDefinition> types = new ArrayList<>();

        for(TypeDefinition current = this; current != null; current = current.getSupertype()) {
            types.add(current);
            List<TypeDefinition> currentInterfaces = current.recursiveInterfaces();
            if(!currentInterfaces.isEmpty())
                types.addAll(current.recursiveInterfaces());
        }

        return types;
    }

    /**
     * Recursively obtains every interface type of this type, which includes interfaces of interfaces spanning into infinity
     * @return A list of all interfaces implemented by this type, recursively scanning interfaces.
     */
    protected List<TypeDefinition> recursiveInterfaces() {
        List<TypeDefinition> interfaces = this.getInterfaces();
        if(interfaces.isEmpty())
            return interfaces;

        List<TypeDefinition> everything = new ArrayList<>();
        for(TypeDefinition interfaceType: interfaces) {
            everything.add(interfaceType);
            everything.addAll(interfaceType.recursiveInterfaces());
        }

        return everything;
    }

    /**
     * Gets whether or not this type definition represents a type which can contain fields, methods,
     * or constructors.
     * @return Returns true if this type is an object type, returns false if this type is a primitive type,
     * void type, or array type.
     */
    public boolean canHaveMembers() {
        return !isPrimitive() && !isVoid() && !isArray();
    }

    /**
     * Determines whether or not a value of another type is assignable to a field of this type.
     * @param other The other type.
     * @return Returns true if the other type is assignable to this type, following the same rules as
     * Java's {@link Class#isAssignableFrom(Class)} method.
     * @throws NullPointerException If other type is null.
     */
    public boolean isAssignableFrom(TypeDefinition other) {
        //If the other type is equal to this type, then the other type is assignable to this type
        if(other.equals(this))
            return true;

        //If the other type represents a type that does not exist in the jvm yet (because it's still being generated)
        //then it can only be assignable to this type if it's supertype or one of it's interface types are assignable
        //to this type
        if(other instanceof ThisTypeDefinition)
            return this.isAssignableFrom(other.getSupertype()) || other.recursiveInterfaces().stream().anyMatch(this::isAssignableFrom);
        else //Else just check if the other type's underlying class is assignable to this type's underlying class.
            return this.type.isAssignableFrom(other.type);
    }

    /**
     * Gets the simple name of this type, which is the fully qualified name of this type without the package.
     * @return The simple name of this type.
     */
    public String getSimpleClassName() {
        String fullName = getClassName();
        return fullName.substring(fullName.lastIndexOf('.') + 1);
    }

    /**
     * Gets the name of the package this type is located in.
     * @return The package name for this type.
     */
    public String getPackageName() {
        String fullName = getClassName();
        return fullName.substring(0, fullName.lastIndexOf('.'));
    }

    /**
     * Gets the "conversion distance" for converting this type into the other type. This distance is based on
     * how far away this type is from the other type in the other type's hierarchy.
     * This method assumes that this type is assignable to the other type. This method also assumes that if this
     * type is a primitive or wrapper type, that the other type will also be a primitive or wrapper type.
     * @param otherType The other type that this type wants to be converted to (and we need to know the distance)
     * @return The conversion distance to convert from this type into the other type.
     */
    private int getConversionDistanceTo(TypeDefinition otherType) {
        assert this.isPrimitiveOrWrapper() == otherType.isPrimitiveOrWrapper();

        if(this.isPrimitiveOrWrapper()) {
            //If auto-boxing or auto-unboxing is needed, then the distance will be higher
            int distance = this.isPrimitive() == otherType.isPrimitive() ? 0 : 1;
            int thisPrimitiveIndex = PRIMITIVES.indexOf(Objects.requireNonNull(wrapperToPrimitiveType(this)).type);
            int otherPrimitiveIndex = PRIMITIVES.indexOf(Objects.requireNonNull(wrapperToPrimitiveType(otherType)).type);
            return distance + Math.abs(thisPrimitiveIndex - otherPrimitiveIndex);
        }

        //Find how far into the other type's inheritance hierarchy this type goes in order to determine the
        //distance between this type and the other type.
        int distance = 0;
        List<List<TypeDefinition>> otherHierarchy = otherType.hierarchy();
        for(int i = 0; i < otherHierarchy.size(); i++) {
            List<TypeDefinition> types = otherHierarchy.get(i);
            if(types.stream().anyMatch(t -> t.isAssignableFrom(this))) {
                distance = i;
                break;
            }
        }

        return distance;
    }

    private static int getTotalConversionDistance(List<TypeDefinition> declared, List<TypeDefinition> actual) {
        assert declared.size() == actual.size();

        int distance = 0;
        for(int i = 0; i < declared.size(); i++) {
            TypeDefinition decl = declared.get(i);
            TypeDefinition act = actual.get(i);
            distance += act.getConversionDistanceTo(decl);
        }

        return distance;
    }

    private static int sortMatchingExecutables(CompleteMethodDefinition left,
                                               CompleteMethodDefinition right,
                                               ParametersDefinition parameters) {
        int leftDistance = getTotalConversionDistance(left.getParameters().getParamTypes(), parameters.getParamTypes());
        int rightDistance = getTotalConversionDistance(right.getParameters().getParamTypes(), parameters.getParamTypes());
        return Integer.compare(leftDistance, rightDistance);
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

        if(other instanceof TypeDefinition) {
            TypeDefinition otherDef = (TypeDefinition)other;
            return otherDef.getType() == this.getType();
        }
        return false;
    }
}