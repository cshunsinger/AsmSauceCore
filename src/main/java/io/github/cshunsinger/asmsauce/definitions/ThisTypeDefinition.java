package io.github.cshunsinger.asmsauce.definitions;

import io.github.cshunsinger.asmsauce.ClassBuildingContext;
import io.github.cshunsinger.asmsauce.FieldNode;
import io.github.cshunsinger.asmsauce.MethodNode;
import io.github.cshunsinger.asmsauce.ThisClass;

import java.util.List;
import java.util.stream.Collectors;

/**
 * This is an instance of TypeDefinition which represents the type being dynamically created at runtime.
 */
public class ThisTypeDefinition extends TypeDefinition {
    ThisTypeDefinition() {
        super(ThisClass.class);
    }

    @Override
    public String getJvmTypeName() {
        return ClassBuildingContext.context().getJvmTypeName();
    }

    @Override
    public String getClassName() {
        return ClassBuildingContext.context().getClassName();
    }

    @Override
    public String getJvmTypeDefinition() {
        return 'L' + ClassBuildingContext.context().getJvmTypeName() + ';';
    }

    @Override
    public boolean isVoid() {
        return false;
    }

    @Override
    public boolean isPrimitive() {
        return false;
    }

    @Override
    public boolean isPrimitiveOrWrapper() {
        return false;
    }

    @Override
    public boolean isPrimitiveWrapper() {
        return false;
    }

    @Override
    public List<TypeDefinition> getInterfaces() {
        return ClassBuildingContext.context().getInterfaces();
    }

    @Override
    public TypeDefinition getSupertype() {
        return ClassBuildingContext.context().getSuperType();
    }

    @Override
    public CompleteFieldDefinition getDeclaredField(String fieldName) {
        return ClassBuildingContext.context()
            .getFields()
            .stream()
            .map(FieldNode::getFieldDefinition)
            .filter(field -> field.getFieldName().getName().equals(fieldName))
            .findFirst()
            .orElse(null);
    }

    @Override
    public List<CompleteMethodDefinition> getDeclaredMethods() {
        return ClassBuildingContext.context()
            .getMethods()
            .stream()
            .map(MethodNode::getDefinition)
            .collect(Collectors.toList());
    }

    @Override
    public List<CompleteMethodDefinition> getDeclaredConstructors() {
        return ClassBuildingContext.context()
            .getConstructors()
            .stream()
            .map(MethodNode::getDefinition)
            .collect(Collectors.toList());
    }

    @Override
    public boolean isAssignableFrom(TypeDefinition other) {
        if(other.equals(this))
            return true;

        //If the other type is assignable to the supertype of this type OR one of the interfaces
        //of this type then the other type is assignable to this type.
        return getSupertype().isAssignableFrom(other) ||
            super.recursiveInterfaces().stream().anyMatch(type -> type.isAssignableFrom(other));
    }

    /**
     * Determines equality.
     * @param other The object to compare to this one.
     * @return Returns true when compared to another ThisTypeDefinition.
     */
    @Override
    public boolean equals(Object other) {
        return other instanceof ThisTypeDefinition;
    }
}