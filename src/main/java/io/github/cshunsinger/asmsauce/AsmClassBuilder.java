package io.github.cshunsinger.asmsauce;

import org.objectweb.asm.ClassWriter;
import io.github.cshunsinger.asmsauce.modifiers.AccessModifiers;
import io.github.cshunsinger.asmsauce.util.ReflectionsUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.lang3.reflect.ConstructorUtils;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.objectweb.asm.ClassWriter.COMPUTE_FRAMES;
import static org.objectweb.asm.ClassWriter.COMPUTE_MAXS;
import static org.objectweb.asm.Opcodes.V15;
import static io.github.cshunsinger.asmsauce.modifiers.AccessModifiers.publicOnly;
import static io.github.cshunsinger.asmsauce.util.ReflectionsUtils.jvmClassname;
import static java.util.Collections.emptyList;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class AsmClassBuilder<T> {
    private static final DynamicClassLoader DYNAMIC_CLASS_LOADER = new DynamicClassLoader();

    private final ClassWriter classWriter;
    private final Class<T> instanceType;
    private final List<Class<?>> interfaces;
    private final Class<?> superclass;
    private final AccessModifiers modifiers;

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
            new ClassWriter(COMPUTE_FRAMES | COMPUTE_MAXS),
            instanceType,
            interfaces,
            superclass,
            classModifiers
        );
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

    @SneakyThrows
    public T buildInstance(Object... constructorParameters) {
        Class<?>[] parameterTypes = Arrays.stream(constructorParameters).map(Object::getClass).toArray(Class<?>[]::new);

        Class<? extends T> builtType = build();
        Constructor<? extends T> constructor = ConstructorUtils.getMatchingAccessibleConstructor(builtType, parameterTypes);

        if(constructor == null)
            throw new IllegalArgumentException("No constructor found for given parameters.");

        return constructor.newInstance(constructorParameters);
    }

    public Class<? extends T> build() {
        if(builtClass == null)
            internalBuildClass();
        return builtClass;
    }

    @SuppressWarnings("unchecked")
    private void internalBuildClass() {
        if(constructors.isEmpty())
            throw new IllegalStateException("Newly built class must be supplied at least 1 constructor.");

        //Create jvm names out of all of the interfaces this class is supposed to implement
        String[] interfaceJvmNames = interfaces == null || interfaces.isEmpty() ?
            null :
            interfaces.stream().map(ReflectionsUtils::jvmClassname).toArray(String[]::new);

        //Name of the newly generated class
        String newJvmClassname = jvmClassname(instanceType) + randomAlphanumeric(16); //TODO: Allow new class name to be passed in via constructor instead

        //Start the new class
        classWriter.visit(
            V15,
            modifiers.getJvmModifiers(),
            newJvmClassname,
            null,
            jvmClassname(superclass),
            interfaceJvmNames
        );

        ClassBuildingContext classContext = new ClassBuildingContext(
            classWriter,
            newJvmClassname,
            superclass,
            interfaces,
            fields,
            methods,
            constructors
        );

        //Build each field onto the new class
        fields.forEach(fieldNode -> fieldNode.build(classContext));

        //Build each constructor onto the new class
        constructors.forEach(constructorNode -> constructorNode.build(classContext));

        //Build each method onto the new class
        methods.forEach(methodNode -> methodNode.build(classContext));

        //Finish the class
        classWriter.visitEnd();

        //Construct the class
        builtClass = (Class<? extends T>)DYNAMIC_CLASS_LOADER.defineClass(newJvmClassname.replace('/', '.'), classWriter.toByteArray());
    }
}