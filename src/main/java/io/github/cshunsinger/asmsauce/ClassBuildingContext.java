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
    /**
     * @return The ClassWriter which is writing this class being built.
     */
    private final ClassWriter classWriter;
    /**
     * @return The jvm type name of this class being built.
     */
    private final String jvmTypeName;
    /**
     * @return The existing Java class that this generated class is inheriting.
     */
    private final Class<?> superclass;
    /**
     * @return The list of zero or more existing Java interface types that this generated class is implementing.
     */
    private final List<Class<?>> interfaces;
    /**
     * @return The list of fields defined in this class being generated.
     */
    private final List<FieldNode> fields;
    /**
     * @return The list of methods defined in this class being generated.
     */
    private final List<MethodNode> methods;
    /**
     * @return The list of constructors defined in this class being generated.
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
    }

    /**
     * Gets the fully qualified name of the class being built. This is the fully qualified dot-name, not the jvm name.
     * @return The fully qualified dot-name of this class being built.
     */
    public String getClassName() {
        return jvmTypeName.replace('/', '.');
    }
}