package io.github.cshunsinger.asmsauce;

import io.github.cshunsinger.asmsauce.modifiers.AccessModifiers;
import io.github.cshunsinger.asmsauce.util.AsmUtils;
import lombok.SneakyThrows;
import org.apache.commons.lang3.reflect.ConstructorUtils;
import org.objectweb.asm.ClassWriter;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static io.github.cshunsinger.asmsauce.ConstructorNode.constructor;
import static io.github.cshunsinger.asmsauce.DefinitionBuilders.noParameters;
import static io.github.cshunsinger.asmsauce.DefinitionBuilders.type;
import static io.github.cshunsinger.asmsauce.code.CodeBuilders.returnVoid;
import static io.github.cshunsinger.asmsauce.code.CodeBuilders.superConstructor;
import static io.github.cshunsinger.asmsauce.modifiers.AccessModifiers.publicOnly;
import static io.github.cshunsinger.asmsauce.util.AsmUtils.jvmClassname;
import static java.util.Collections.emptyList;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.objectweb.asm.ClassWriter.COMPUTE_FRAMES;
import static org.objectweb.asm.ClassWriter.COMPUTE_MAXS;
import static org.objectweb.asm.Opcodes.V15;

/**
 * This class is the entrypoint to generating a new class at runtime. This class builder contains all of the
 * nodes representing parts of the class being generated.
 *
 * This class also kicks off all of the low-level bytecode generation, and caches the generated class.
 *
 * @param <T> The generic type to represent one of the base types of the new class being generated.
 */
public class AsmClassBuilder<T> {
    private final DynamicClassLoader dynamicClassLoader;
    private final ClassWriter classWriter;
    private final Class<T> instanceType;
    private final List<Class<?>> interfaces;
    private final Class<?> superclass;
    private final AccessModifiers modifiers;
    private final String newClassName;

    private Class<? extends T> builtClass;

    private final List<FieldNode> fields = new ArrayList<>();
    private final List<ConstructorNode> constructors = new ArrayList<>();
    private final List<MethodNode> methods = new ArrayList<>();

    /**
     * Constructs a new class builder in which the instance type and super type are the same, no interfaces are implemented,
     * and the resulting class has public access.
     * @param instanceType The type that the newly generated class will inherit and belong to.
     */
    public AsmClassBuilder(Class<T> instanceType) {
        this(instanceType, emptyList(), publicOnly());
    }

    /**
     * Constructs a new class builder in which the instance type and super type are the same, no interfaces are implemented,
     * and the resulting class has public access.
     * The resulting class will have the fully qualified name specified.
     * @param name The fully qualified name for the new class.
     * @param instanceType The type that the newly generated class will inherit and belong to.
     */
    public AsmClassBuilder(String name, Class<T> instanceType) {
        this(name, instanceType, emptyList(), publicOnly());
    }

    /**
     * Constructs a new class builder in which the instance type and super type are the same, no interfaces are implemented,
     * and the resulting class has public access.
     * @param parentClassLoader Specify the parent class loader to use for loading the new class when it is generated.
     * @param instanceType The type that the newly generated class will inherit and belong to.
     */
    public AsmClassBuilder(ClassLoader parentClassLoader, Class<T> instanceType) {
        this(parentClassLoader, instanceType, emptyList(), publicOnly());
    }

    /**
     * Constructs a new class builder in which the instance type and super type are the same, no interfaces are implemented,
     * and the resulting class has public access. The resulting class will also be given the fully qualified name
     * specified.
     * @param name The fully qualified name of the new class.
     * @param parentClassLoader Specify the parent class loader to use for loading the new class when it is generated.
     * @param instanceType The type that the newly generated class will inherit and belong to.
     */
    public AsmClassBuilder(String name, ClassLoader parentClassLoader, Class<T> instanceType) {
        this(name, parentClassLoader, instanceType, emptyList(), publicOnly());
    }

    /**
     * Constructs a new class builder which will generate a new class under the specified instance type.
     * The built class will also inherit the instance type, and the class will contain the specified modifiers.
     * The built class will not implement any interfaces.
     * @param instanceType The type to inherit and the type to refer to the new class under.
     * @param classModifiers The modifiers for the built class.
     */
    public AsmClassBuilder(Class<T> instanceType, AccessModifiers classModifiers) {
        this(instanceType, null, classModifiers);
    }

    /**
     * Constructs a new class builder which will generate a new class under the specified instance type.
     * The built class will also inherit the instance type, and the class will contain the specified modifiers.
     * The built class will not implement any interfaces.
     * @param parentClassLoader Specify the parent class loader to use for loading the new class when it is generated.
     * @param instanceType The type to inherit and the type to refer to the new class under.
     * @param classModifiers The modifiers for the built class.
     */
    public AsmClassBuilder(ClassLoader parentClassLoader, Class<T> instanceType, AccessModifiers classModifiers) {
        this(parentClassLoader, instanceType, null, classModifiers);
    }

    /**
     * Constructs a new class builder which will generate a new class under the specified instance type.
     * The built class will also inherit the instance type.
     * The built class will contain the specified modifiers.
     * The built class will implement the listed interfaces.
     * @param instanceType The class that will be inherited by the generated class.
     * @param interfaces A list of zero or more interfaces that will be implemented by the generated class.
     * @param classModifiers The access modifiers for the new class.
     */
    public AsmClassBuilder(Class<T> instanceType, List<Class<?>> interfaces, AccessModifiers classModifiers) {
        this(instanceType, instanceType, interfaces, classModifiers);
    }

    /**
     * Constructs a new class builder which will generate a new class under the specified instance type. The built
     * class will also inherit the instance type. The built class will contain the specified modifiers. The built class
     * will implemented the listed interfaces. The built class will also have the specified fully qualified name.
     * @param name The fully qualified name of the new class.
     * @param instanceType The class that will be inherited by the generated class.
     * @param interfaces A list of zero or more interfaces that will be implemented by the generated class.
     * @param classModifiers The access modifiers for the new class.
     */
    public AsmClassBuilder(String name, Class<T> instanceType, List<Class<?>> interfaces, AccessModifiers classModifiers) {
        this(name, instanceType, instanceType, interfaces, classModifiers);
    }

    /**
     * Constructs a new class builder which will generate a new class under the specified instance type.
     * The built class will also inherit the instance type.
     * The built class will contain the specified modifiers.
     * The built class will implement the listed interfaces.
     * @param parentClassLoader Specify the parent class loader to use for loading the new class when it is generated.
     * @param instanceType The class that will be inherited by the generated class.
     * @param interfaces A list of zero or more interfaces that will be implemented by the generated class.
     * @param classModifiers The access modifiers for the new class.
     */
    public AsmClassBuilder(ClassLoader parentClassLoader, Class<T> instanceType, List<Class<?>> interfaces, AccessModifiers classModifiers) {
        this(parentClassLoader, instanceType, instanceType, interfaces, classModifiers);
    }

    /**
     * Constructs a new class builder which will generate a new class under the specified instance type.
     * The built class will also inherit the instance type.
     * The built class will contain the specified modifiers.
     * The built class will implement the listed interfaces.
     * The built class will have the fully qualified name specified.
     * @param name The fully qualified name for the new class.
     * @param parentClassLoader Specify the parent class loader to use for loading the new class when it is generated.
     * @param instanceType The class that will be inherited by the generated class.
     * @param interfaces A list of zero or more interfaces that will be implemented by the generated class.
     * @param classModifiers The access modifiers for the new class.
     */
    public AsmClassBuilder(String name, ClassLoader parentClassLoader, Class<T> instanceType, List<Class<?>> interfaces, AccessModifiers classModifiers) {
        this(name, parentClassLoader, instanceType, instanceType, interfaces, classModifiers);
    }

    /**
     * Constructs a new class builder which will generate a new class under the specified instance type.
     * The built class will inherit from the specified superclass.
     * The built class will implement any interfaces that are specified.
     * The built class will contain the specified access modifiers.
     * This class builder will use instanceType for generic purposes. For example, if you specify 3 interfaces and
     * a superclass, which type should this builder use as a reference? That is what the instance type is used for here.
     * @param instanceType The reference type for the generated class. The superclass, or at least 1 interface must be assignable to the instance type.
     * @param superclass The class that the generated class will inherit.
     * @param interfaces The interfaces that will be implemented by the generated class. Can be empty or null.
     * @param classModifiers The access modifiers to apply to the generated class.
     */
    public AsmClassBuilder(Class<T> instanceType, Class<?> superclass, List<Class<?>> interfaces, AccessModifiers classModifiers) {
        this(
            AsmClassBuilder.class.getClassLoader(),
            instanceType,
            superclass,
            interfaces,
            classModifiers
        );
    }

    /**
     * Constructs a new class builder which will generate a new class under the specified instance type.
     * The built class will inherit from the specified superclass.
     * The built class will implement any interfaces that are specified.
     * The built class will contain the specified access modifiers.
     * The built class will have the specified fully qualified name.
     * This class builder will use instanceType for generic purposes. For example, if you specify 3 interfaces and
     * a superclass, which type should this builder use as a reference? That is what the instance type is used for here.
     * @param name The fully qualified name of the new class.
     * @param instanceType The reference type for the generated class. The superclass, or at least 1 interface must be assignable to the instance type.
     * @param superclass The class that the generated class will inherit.
     * @param interfaces The interfaces that will be implemented by the generated class. Can be empty or null.
     * @param classModifiers The access modifiers to apply to the generated class.
     */
    public AsmClassBuilder(String name, Class<T> instanceType, Class<?> superclass, List<Class<?>> interfaces, AccessModifiers classModifiers) {
        this(
            name,
            AsmClassBuilder.class.getClassLoader(),
            instanceType,
            superclass,
            interfaces,
            classModifiers
        );
    }

    /**
     * Constructs a new class builder which will generate a new class under the specified instance type.
     * The built class will inherit from the specified superclass.
     * The built class will implement any interfaces that are specified.
     * The built class will contain the specified access modifiers.
     * This class builder will use instanceType for generic purposes. For example, if you specify 3 interfaces and
     * a superclass, which type should this builder use as a reference? That is what the instance type is used for here.
     * @param parentClassLoader Specify the parent class loader to use for loading the new class when it is generated.
     * @param instanceType The reference type for the generated class. The superclass, or at least 1 interface must be assignable to the instance type.
     * @param superclass The class that the generated class will inherit.
     * @param interfaces The interfaces that will be implemented by the generated class. Can be empty or null.
     * @param classModifiers The access modifiers to apply to the generated class.
     */
    public AsmClassBuilder(ClassLoader parentClassLoader, Class<T> instanceType, Class<?> superclass, List<Class<?>> interfaces, AccessModifiers classModifiers) {
        this(
            new DynamicClassLoader(parentClassLoader),
            instanceType.getName() + randomAlphanumeric(16),
            new ClassWriter(COMPUTE_FRAMES | COMPUTE_MAXS),
            instanceType,
            interfaces,
            superclass,
            classModifiers
        );
    }

    /**
     * Constructs a new class builder which will generate a new class under the specified instance type.
     * The built class will inherit from the specified superclass.
     * The built class will implement any interfaces that are specified.
     * The built class will contain the specified access modifiers.
     * The built class will have the fully qualified name specified.
     * This class builder will use instanceType for generic purposes. For example, if you specify 3 interfaces and
     * a superclass, which type should this builder use as a reference? That is what the instance type is used for here.
     * @param name The fully qualified name of the new class.
     * @param parentClassLoader Specify the parent class loader to use for loading the new class when it is generated.
     * @param instanceType The reference type for the generated class. The superclass, or at least 1 interface must be assignable to the instance type.
     * @param superclass The class that the generated class will inherit.
     * @param interfaces The interfaces that will be implemented by the generated class. Can be empty or null.
     * @param classModifiers The access modifiers to apply to the generated class.
     */
    public AsmClassBuilder(String name, ClassLoader parentClassLoader, Class<T> instanceType, Class<?> superclass, List<Class<?>> interfaces, AccessModifiers classModifiers) {
        this(
            new DynamicClassLoader(parentClassLoader),
            name,
            new ClassWriter(COMPUTE_FRAMES | COMPUTE_MAXS),
            instanceType,
            interfaces,
            superclass,
            classModifiers
        );
    }

    private AsmClassBuilder(DynamicClassLoader classLoader,
                            String fullyQualifiedClassName,
                            ClassWriter classWriter,
                            Class<T> instanceType,
                            List<Class<?>> interfaces,
                            Class<?> superclass,
                            AccessModifiers modifiers) {
        this.dynamicClassLoader = classLoader;
        this.classWriter = classWriter;
        this.instanceType = instanceType;
        this.interfaces = interfaces;
        this.superclass = superclass;
        this.modifiers = modifiers;
        this.newClassName = fullyQualifiedClassName;
    }

    /**
     * Adds a field to the class that will be generated.
     * @param field The field specification.
     * @return This.
     */
    public AsmClassBuilder<T> withField(FieldNode field) {
        fields.add(field);
        return this;
    }

    /**
     * Adds a constructor to the class that will be generated.
     * @param constructor The constructor specification.
     * @return This.
     */
    public AsmClassBuilder<T> withConstructor(ConstructorNode constructor) {
        constructors.add(constructor);
        return this;
    }

    /**
     * Adds a method to the class that will be generated.
     * @param method The method specification.
     * @return This.
     */
    public AsmClassBuilder<T> withMethod(MethodNode method) {
        methods.add(method);
        return this;
    }

    /**
     * Builds the new class and instantiates it using a provided set of parameters. If the new class has already been
     * built then it will not be built again. Instead, the already-loaded class will be instantiated.
     * The types of the constructor parameters passed to this method, as well as the count of parameters passed to this
     * method, will be used to determine which constructor is used from the generated class.
     *
     * @param constructorParameters The set of parameters used to invoke a constructor.
     * @return A new instance of the Class which was generated by this class builder.
     */
    @SneakyThrows
    public T buildInstance(Object... constructorParameters) {
        Class<?>[] parameterTypes = Arrays.stream(constructorParameters).map(Object::getClass).toArray(Class<?>[]::new);

        Class<? extends T> builtType = build();
        Constructor<? extends T> constructor = ConstructorUtils.getMatchingAccessibleConstructor(builtType, parameterTypes);

        if(constructor == null)
            throw new IllegalArgumentException("No constructor found for given parameters.");

        return constructor.newInstance(constructorParameters);
    }

    /**
     * Builds the new class. If the new class has already been built, then it will not be built again. Instead the
     * existing Class object will be returned.
     * @return The newly built and loaded Java class.
     */
    public Class<? extends T> build() {
        if(builtClass == null)
            internalBuildClass();
        return builtClass;
    }

    @SuppressWarnings("unchecked")
    private void internalBuildClass() {
        //Create jvm names out of all of the interfaces this class is supposed to implement
        String[] interfaceJvmNames = interfaces == null || interfaces.isEmpty() ?
            null :
            interfaces.stream().map(AsmUtils::jvmClassname).toArray(String[]::new);

        //Name of the newly generated class
        String newJvmClassname = newClassName.replace('.', '/');

        //Start the new class
        classWriter.visit(
            V15,
            modifiers.getJvmModifiers(),
            newJvmClassname,
            null,
            jvmClassname(superclass),
            interfaceJvmNames
        );

        //Start the class building context for this thread
        new ClassBuildingContext(
            classWriter,
            newJvmClassname,
            superclass,
            interfaces == null ? emptyList() : interfaces,
            fields,
            methods,
            constructors
        );

        //Build each field onto the new class
        fields.forEach(FieldNode::build);

        if(constructors.isEmpty()) {
            //Determine if a no-args super constructor exists which is accessible from this class being built
            boolean noArgsSuperConstructorExists = type(superclass).findDeclaredMatchingConstructors(noParameters())
                .stream()
                .anyMatch(superConstructor ->
                    AccessModifiers.isAccessible(type(ThisClass.class), type(superclass), superConstructor.getModifiers())
                );

            if(noArgsSuperConstructorExists) {
                //Generate bytecode for an empty no-args constructor if the superclass has an accessible no-args constructor.
                ConstructorNode defaultConstructor = constructor(publicOnly(), noParameters(),
                    superConstructor(superclass, noParameters()),
                    returnVoid()
                );
                defaultConstructor.build();
            }
            else
                throw new IllegalStateException("Newly built class must be supplied at least 1 constructor.");
        }
        else {
            //Build each constructor onto the new class
            constructors.forEach(MethodNode::build);
        }

        //Build each method onto the new class
        methods.forEach(MethodNode::build);

        //Finish the class
        classWriter.visitEnd();

        //End the class building context for this thread
        ClassBuildingContext.reset();

        //Construct the class
        builtClass = (Class<? extends T>)dynamicClassLoader.defineClass(newJvmClassname.replace('/', '.'), classWriter.toByteArray());
    }
}