package io.github.cshunsinger.asmsauce;

import org.objectweb.asm.ClassWriter;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

/**
 * The context representing the state of the class building which is present during the building of a new class.
 * This context contains all of the information about the class being built.
 */
@Getter
@RequiredArgsConstructor
public class ClassBuildingContext {
    private final ClassWriter classWriter;
    private final String jvmTypeName;
    private final Class<?> superclass;
    private final List<Class<?>> interfaces;
    private final List<FieldNode> fields;
    private final List<MethodNode> methods;
    private final List<ConstructorNode> constructors;

    /**
     * Gets the fully qualified name of the class being built. This is the fully qualified dot-name, not the jvm name.
     * @return The fully qualified dot-name of this class being built.
     */
    public String getClassName() {
        return jvmTypeName.replace('/', '.');
    }
}