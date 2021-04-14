package io.github.cshunsinger.asmsauce;

import org.objectweb.asm.FieldVisitor;
import io.github.cshunsinger.asmsauce.definitions.CompleteFieldDefinition;
import io.github.cshunsinger.asmsauce.definitions.NameDefinition;
import io.github.cshunsinger.asmsauce.definitions.TypeDefinition;
import io.github.cshunsinger.asmsauce.modifiers.AccessModifiers;
import lombok.Getter;
import org.apache.commons.lang3.ClassUtils;

import static java.util.Arrays.asList;

/**
 * Represents a field in a class being generated.
 */
public class FieldNode {
    @Getter
    private CompleteFieldDefinition fieldDefinition;
    private final Object initialValue;

    /**
     * Creates a new field node from a field definition.
     * @param fieldDefinition The field definition to define the field node from.
     */
    public FieldNode(CompleteFieldDefinition fieldDefinition) {
        this(fieldDefinition, null);
    }

    /**
     * Creates a new field node from a field definition, which will be assigned to an initial value.
     * The field definition should define a static field if a non-null value is passed for initialValue.
     *
     * For a static field, the allowed types for the initial field value are:
     * int/Integer, float/Float, long/Long, double/Double, java.lang.String
     *
     * @param fieldDefinition The field definition to create this field node from.
     * @param initialValue The initial value to set the field to. This parameter should be null if fieldDefinition does not define a static field.
     * @throws IllegalArgumentException If fieldDefinition is null.
     * @throws IllegalArgumentException If fieldDefinition defines a non-static field, and initialValue is not null.
     * @throws IllegalArgumentException If fieldDefinition defines a static field, but initialValue is not one of the allowed types.
     * @throws IllegalArgumentException If fieldDefinition defines a static field, but the type of initialValue is not assignable to the field type.
     */
    public FieldNode(CompleteFieldDefinition fieldDefinition, Object initialValue) {
        if(fieldDefinition == null)
            throw new IllegalArgumentException("Field definition cannot be null.");

        AccessModifiers fieldAccess = fieldDefinition.getAccessModifiers();
        if(initialValue != null) {
            //An initial field value can only be set for static fields
            if(!fieldAccess.isStatic()) {
                throw new IllegalArgumentException("Only static fields can be given an initial value.");
            }

            //An initial field value can only be of a few certain types
            Class<?> initialValueType = initialValue.getClass();
            if(!asList(Integer.class, Float.class, Long.class, Double.class, String.class).contains(initialValueType)) {
                throw new IllegalArgumentException("An initial value of type %s was provided. ".formatted(initialValue.getClass().getName()) +
                    "The initial value of a static field must be one of: Integer, Float, Long, Double, String");
            }

            //The initial value type must match the field type
            if(!ClassUtils.isAssignable(initialValueType, fieldDefinition.getFieldType().getType(), true)) {
                throw new IllegalArgumentException("Initial value %s of type %s is not assignable to this field type: %s".formatted(
                    initialValue.toString(),
                    initialValueType.getName(),
                    fieldDefinition.getFieldType().getType().getName()
                ));
            }
        }
        else /* Initial value is null */ {
            //If this field is a primitive, then null is unacceptable
            if(fieldDefinition.getAccessModifiers().isStatic() && fieldDefinition.getFieldType().getType().isPrimitive())
                throw new IllegalArgumentException("The initial value of a static primitive field cannot be null.");
        }

        this.fieldDefinition = fieldDefinition;
        this.initialValue = initialValue;
    }

    /**
     * Called by the class builder {@link AsmClassBuilder} to build this field onto the class being generated.
     * @param context The class building context.
     * @see AsmClassBuilder {@link AsmClassBuilder}
     * @see ClassBuildingContext {@link ClassBuildingContext}
     */
    public void build(ClassBuildingContext context) {
        TypeDefinition<ThisClass> updatedOwnerType = TypeDefinition.fromCustomJvmName(context.getJvmTypeName());
        TypeDefinition<?> updatedFieldType = fieldDefinition.getFieldType().getType() == ThisClass.class ?
            updatedOwnerType :
            fieldDefinition.getFieldType();

        fieldDefinition = new CompleteFieldDefinition(
            fieldDefinition.getAccessModifiers(),
            updatedOwnerType,
            fieldDefinition.getFieldName(),
            updatedFieldType
        );

        FieldVisitor fieldVisitor = context.getClassWriter().visitField(
            fieldDefinition.getAccessModifiers().getJvmModifiers(),
            fieldDefinition.getFieldName().getName(),
            fieldDefinition.getJvmDescriptor(),
            null, //Generic types currently unsupported, therefore signature is null
            initialValue //This is ignored unless the field is static
        );

        //Field annotations are currently not supported.
        //There is also no support for setting static fields using bytecode to build an initial value
        //Lastly, instance fields are required to be initialized inside the constructor.
        fieldVisitor.visitEnd();
    }

    /**
     * Creates a field node with the specified modifiers, type, name, and no initial value.
     * @param accessModifiers The field modifiers.
     * @param type The field type.
     * @param name The field name.
     * @return A new field node.
     */
    public static FieldNode field(AccessModifiers accessModifiers,
                                  TypeDefinition<?> type,
                                  NameDefinition name) {
        CompleteFieldDefinition fd = new CompleteFieldDefinition(accessModifiers, DefinitionBuilders.type(ThisClass.class), name, type);
        return new FieldNode(fd);
    }

    /**
     * Creates a static int field with the specified modifiers, name, and initial int-value.
     * @param accessModifiers The field modifiers. The static flag will be automatically added if not already set.
     * @param name The field name.
     * @param initialValue The initial int-value of the field.
     * @return A new field node for a static int field with the given initial value.
     */
    public static FieldNode field(AccessModifiers accessModifiers,
                                  NameDefinition name,
                                  int initialValue) {
        CompleteFieldDefinition fd = new CompleteFieldDefinition(accessModifiers.withStatic(), DefinitionBuilders.type(ThisClass.class), name, DefinitionBuilders.type(int.class));
        return new FieldNode(fd, initialValue);
    }

    /**
     * Creates a static long field with the specified modifiers, name, and initial long-value.
     * @param accessModifiers The field modifiers. The static flag will be automatically added if not already set.
     * @param name The field name.
     * @param initialValue The initial long-value of the field.
     * @return A new field node for a static long field with the given initial value.
     */
    public static FieldNode field(AccessModifiers accessModifiers,
                                  NameDefinition name,
                                  long initialValue) {
        CompleteFieldDefinition fd = new CompleteFieldDefinition(accessModifiers.withStatic(), DefinitionBuilders.type(ThisClass.class), name, DefinitionBuilders.type(long.class));
        return new FieldNode(fd, initialValue);
    }

    /**
     * Creates a static float field with the specified modifiers, name, and initial float-value.
     * @param accessModifiers The field modifiers. The static flag will be automatically added if not already set.
     * @param name The field name.
     * @param initialValue The initial float-value of the field.
     * @return A new field node for a static float field with the given initial value.
     */
    public static FieldNode field(AccessModifiers accessModifiers,
                                  NameDefinition name,
                                  float initialValue) {
        CompleteFieldDefinition fd = new CompleteFieldDefinition(accessModifiers.withStatic(), DefinitionBuilders.type(ThisClass.class), name, DefinitionBuilders.type(float.class));
        return new FieldNode(fd, initialValue);
    }

    /**
     * Creates a static double field with the specified modifiers, name, and initial double-value.
     * @param accessModifiers The field modifiers. The static flag will be automatically added if not already set.
     * @param name The field name.
     * @param initialValue The initial double-value of the field.
     * @return A new field node for a static double field with the given initial value.
     */
    public static FieldNode field(AccessModifiers accessModifiers,
                                  NameDefinition name,
                                  double initialValue) {
        CompleteFieldDefinition fd = new CompleteFieldDefinition(accessModifiers.withStatic(), DefinitionBuilders.type(ThisClass.class), name, DefinitionBuilders.type(double.class));
        return new FieldNode(fd, initialValue);
    }

    /**
     * Creates a static String field with the specified modifiers, name, and initial String-value.
     * @param accessModifiers The field modifiers. The static flag will be automatically added if not already set.
     * @param name The field name.
     * @param initialValue The initial String-value of the field.
     * @return A new field node for a static String field with the given initial value.
     */
    public static FieldNode field(AccessModifiers accessModifiers,
                                  NameDefinition name,
                                  String initialValue) {
        CompleteFieldDefinition fd = new CompleteFieldDefinition(accessModifiers.withStatic(), DefinitionBuilders.type(ThisClass.class), name, DefinitionBuilders.type(String.class));
        return new FieldNode(fd, initialValue);
    }
}