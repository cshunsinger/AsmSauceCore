package io.github.cshunsinger.asmsauce.definitions;

import io.github.cshunsinger.asmsauce.ThisClass;
import io.github.cshunsinger.asmsauce.DefinitionBuilders;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import static io.github.cshunsinger.asmsauce.util.AsmUtils.jvmClassname;
import static io.github.cshunsinger.asmsauce.util.AsmUtils.jvmTypeDefinition;

@ToString
@RequiredArgsConstructor
public class TypeDefinition<T> {
    @Getter
    private final Class<T> type;
    @Getter
    private final String jvmTypeName;
    @Getter
    private final String jvmTypeDefinition;

    public TypeDefinition(Class<T> type) {
        if(type == null)
            throw new IllegalArgumentException("Field type cannot be null.");

        this.type = type;
        this.jvmTypeName = jvmClassname(type);
        this.jvmTypeDefinition = jvmTypeDefinition(type);
    }

    public static TypeDefinition<ThisClass> fromCustomJvmName(String jvmTypeName) {
        return new TypeDefinition<>(ThisClass.class, jvmTypeName, "L" + jvmTypeName + ";");
    }

    public String getJvmTypeName(String newJvmClassname) {
        if(type == ThisClass.class)
            return newJvmClassname;
        else
            return getJvmTypeName();
    }

    public String getClassName() {
        if(type == ThisClass.class)
            return getJvmTypeName().replace('/', '.');
        else
            return type.getName();
    }

    public String getJvmTypeDefinition(String newJvmClassname) {
        if(type == ThisClass.class)
            return "L" + newJvmClassname + ";";
        else
            return getJvmTypeDefinition();
    }

    public boolean isVoid() {
        return type == void.class || type == Void.class;
    }

    public boolean isPrimitive() {
        return type.isPrimitive();
    }

    public static TypeDefinition<?>[] typesFromClasses(Class<?>... classes) {
        TypeDefinition<?>[] types = new TypeDefinition[classes.length];
        for(int i = 0; i < classes.length; i++) {
            types[i] = DefinitionBuilders.type(classes[i]);
        }
        return types;
    }

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