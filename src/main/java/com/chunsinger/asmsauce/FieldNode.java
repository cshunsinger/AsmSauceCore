package com.chunsinger.asmsauce;

import aj.org.objectweb.asm.FieldVisitor;
import com.chunsinger.asmsauce.definitions.CompleteFieldDefinition;
import com.chunsinger.asmsauce.definitions.NameDefinition;
import com.chunsinger.asmsauce.definitions.TypeDefinition;
import com.chunsinger.asmsauce.modifiers.AccessModifiers;
import lombok.Getter;
import org.apache.commons.lang3.ClassUtils;

import static com.chunsinger.asmsauce.DefinitionBuilders.type;
import static java.util.Arrays.asList;

public class FieldNode {
    @Getter
    private CompleteFieldDefinition fieldDefinition;
    private final Object initialValue;

    public FieldNode(CompleteFieldDefinition fieldDefinition) {
        this(fieldDefinition, null);
    }

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

    public static FieldNode field(AccessModifiers accessModifiers,
                                  TypeDefinition<?> type,
                                  NameDefinition name) {
        CompleteFieldDefinition fd = new CompleteFieldDefinition(accessModifiers, type(ThisClass.class), name, type);
        return new FieldNode(fd);
    }

    public static FieldNode field(AccessModifiers accessModifiers,
                                  NameDefinition name,
                                  int initialValue) {
        CompleteFieldDefinition fd = new CompleteFieldDefinition(accessModifiers.withStatic(), type(ThisClass.class), name, type(int.class));
        return new FieldNode(fd, initialValue);
    }

    public static FieldNode field(AccessModifiers accessModifiers,
                                  NameDefinition name,
                                  long initialValue) {
        CompleteFieldDefinition fd = new CompleteFieldDefinition(accessModifiers.withStatic(), type(ThisClass.class), name, type(long.class));
        return new FieldNode(fd, initialValue);
    }

    public static FieldNode field(AccessModifiers accessModifiers,
                                  NameDefinition name,
                                  float initialValue) {
        CompleteFieldDefinition fd = new CompleteFieldDefinition(accessModifiers.withStatic(), type(ThisClass.class), name, type(float.class));
        return new FieldNode(fd, initialValue);
    }

    public static FieldNode field(AccessModifiers accessModifiers,
                                  NameDefinition name,
                                  double initialValue) {
        CompleteFieldDefinition fd = new CompleteFieldDefinition(accessModifiers.withStatic(), type(ThisClass.class), name, type(double.class));
        return new FieldNode(fd, initialValue);
    }

    public static FieldNode field(AccessModifiers accessModifiers,
                                  NameDefinition name,
                                  String initialValue) {
        CompleteFieldDefinition fd = new CompleteFieldDefinition(accessModifiers.withStatic(), type(ThisClass.class), name, type(String.class));
        return new FieldNode(fd, initialValue);
    }
}