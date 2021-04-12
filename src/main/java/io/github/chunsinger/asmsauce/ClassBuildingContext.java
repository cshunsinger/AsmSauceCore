package io.github.chunsinger.asmsauce;

import aj.org.objectweb.asm.ClassWriter;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

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

    public String getClassName() {
        return jvmTypeName.replace('/', '.');
    }
}