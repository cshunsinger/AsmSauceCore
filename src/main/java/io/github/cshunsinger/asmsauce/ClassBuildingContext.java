package io.github.cshunsinger.asmsauce;

import org.objectweb.asm.ClassWriter;
import lombok.Getter;

import java.util.List;

/**
 * The context representing the state of the class building which is present during the building of a new class.
 * This context contains all of the information about the class being built.
 */
@Getter
public class ClassBuildingContext {
    private static final ThreadLocal<ClassBuildingContext> CONTEXT = new ThreadLocal<>();

    /**
     * Fetches the currently active class building context for the current thread. The returned context will be the
     * last instantiated ClassBuildingContext instance since the last {@link ClassBuildingContext#reset()} call.
     * @return The currently active method building context if it exists.
     * @throws IllegalStateException If there is no currently active class building context.
     * @see #reset()
     */
    public static ClassBuildingContext context() {
        ClassBuildingContext buildingContext = CONTEXT.get();
        if(buildingContext == null)
            throw new IllegalStateException("Context must be accessed from within a method building scope.");

        return buildingContext;
    }

    /**
     * Resets the active class building context for the current thread. This method is called by the AsmClassBuilder
     * after it finishes building a class.
     * After this method is called, {@link ClassBuildingContext#context()} will throw an {@link IllegalStateException}
     * until a new context is started.
     * @see #context()
     */
    public static void reset() {
        CONTEXT.remove();
    }

    /**
     * The ClassWriter which is writing this class being built.
     * @return The ClassWriter instance.
     */
    private final ClassWriter classWriter;
    /**
     * The jvm type name of this class being built.
     * @return The jvm type name.
     */
    private final String jvmTypeName;
    /**
     * The existing Java class that this generated class is inheriting.
     * @return The superclass of this class being generated.
     */
    private final Class<?> superclass;
    /**
     * The list of zero or more existing Java interface types that this generated class is implementing.
     * @return The interfaces list.
     */
    private final List<Class<?>> interfaces;
    /**
     * The list of fields defined in this class being generated.
     * @return The list of fields.
     */
    private final List<FieldNode> fields;
    /**
     * The list of methods defined in this class being generated.
     * @return The list of methods.
     */
    private final List<MethodNode> methods;
    /**
     * The list of constructors defined in this class being generated.
     * @return The list of constructors.
     */
    private final List<ConstructorNode> constructors;

    /**
     * Creates a new class building context with all of the metadata about the class being generated.
     * @param classWriter The class writer for generating this class.
     * @param jvmTypeName The jvm classname of the class being generated.
     * @param superclass The class that the generated class will be inheriting.
     * @param interfaces The interface types that the generated class will be implementing.
     * @param fields The fields to be generated in the new class.
     * @param methods The methods to be generated in the new class.
     * @param constructors The constructors to be generated in the new class.
     */
    public ClassBuildingContext(ClassWriter classWriter,
                                String jvmTypeName,
                                Class<?> superclass,
                                List<Class<?>> interfaces,
                                List<FieldNode> fields,
                                List<MethodNode> methods,
                                List<ConstructorNode> constructors) {
        this.classWriter = classWriter;
        this.jvmTypeName = jvmTypeName;
        this.superclass = superclass;
        this.interfaces = interfaces;
        this.fields = fields;
        this.methods = methods;
        this.constructors = constructors;

        CONTEXT.set(this);
    }

    /**
     * Gets the fully qualified name of the class being built. This is the fully qualified dot-name, not the jvm name.
     * @return The fully qualified dot-name of this class being built.
     */
    public String getClassName() {
        return jvmTypeName.replace('/', '.');
    }
}