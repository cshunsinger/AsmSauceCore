package io.github.cshunsinger.asmsauce.code.cast;

import io.github.cshunsinger.asmsauce.code.CodeInsnBuilder;
import io.github.cshunsinger.asmsauce.definitions.ThisTypeDefinition;
import io.github.cshunsinger.asmsauce.definitions.TypeDefinition;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.objectweb.asm.MethodVisitor;

import java.lang.reflect.Method;
import java.util.Stack;

import static io.github.cshunsinger.asmsauce.DefinitionBuilders.type;
import static io.github.cshunsinger.asmsauce.MethodBuildingContext.context;
import static io.github.cshunsinger.asmsauce.util.AsmUtils.generateJvmMethodSignature;
import static org.objectweb.asm.Opcodes.*;

/**
 * Code builder for building bytecode for implicit type conversions. This code builder only supports implicit type
 * conversions and will throw if a type cannot be implicitly converted into another type.
 * These implicit conversions include casting primitives, auto boxing, and auto unboxing.
 * @see ExplicitConversionInsn {@link ExplicitConversionInsn} For explicit type conversion bytecode generation.
 */
public class ImplicitConversionInsn extends CodeInsnBuilder {
    private final TypeDefinition toType;

    /**
     * Creates an implicit conversion instruction to convert the element on the top of the jvm stack to a desired type.
     * @param toType The type to convert to.
     * @throws IllegalArgumentException If toType is null.
     *
     */
    public ImplicitConversionInsn(TypeDefinition toType) {
        if(toType == null)
            throw new IllegalArgumentException("toType cannot be null.");
        this.toType = toType;
    }

    @Override
    public void build() {
        if(context().isStackEmpty())
            throw new IllegalStateException("There is no element expected on the stack to be cast.");

        TypeDefinition fromType = context().peekStack();

        if(fromType.equals(toType)) {
            //No autoboxing, unboxing, or implicit casts necessary
            return;
        }

        if(!implicitCastAllowed(fromType, toType))
            throw new IllegalStateException("Cannot convert from type %s into type %s.".formatted(fromType.getType().getName(), toType.getType().getName()));

        if(fromType.isPrimitive() && toType.isPrimitive()) {
            //Implicit casting is possible based on the previous check
            buildImplicitCast(context().getMethodVisitor(), fromType, context().getTypeStack());
        }
        else if(fromType.isPrimitive()) {
            //Auto-boxing will occur
            buildAutoboxing(context().getMethodVisitor(), context().getTypeStack());
        }
        else if(toType.isPrimitive()) {
            //Auto-unboxing will occur
            buildAutoUnboxing(context().getMethodVisitor(), context().getTypeStack());
        }
        else {
            //Implicit casting will occur (ToType)fromTypeValue where ToType.isAssignableFrom(FromType)
            context().getMethodVisitor().visitTypeInsn(CHECKCAST, toType.getJvmTypeName(context().getClassContext().getJvmTypeName()));
            context().popStack();
            context().pushStack(toType);
        }
    }

    /**
     * Determines whether or not an implicit cast is allowed from one type to another.
     * @param from The type to convert from.
     * @param to The type to convert to.
     * @return Returns true if 'from' can be converted to 'to' implicitly. Otherwise returns false.
     */
    public static boolean implicitCastAllowed(TypeDefinition from, TypeDefinition to) {
        return ClassUtils.isAssignable(from.getType(), to.getType(), true)
            || (from instanceof ThisTypeDefinition)
            || (from.getType() == byte.class && to.getType() == char.class)
            || (from.getType() == long.class && (to.getType() == float.class || to.getType() == double.class))
            || (from.getType() == short.class && to.getType() == char.class)
            || (from.getType() == char.class && to.getType() == short.class);
    }

    private void buildImplicitCast(MethodVisitor methodVisitor, TypeDefinition fromType, Stack<TypeDefinition> classStack) {
        Class<?> fromClass = fromType.getType();
        Class<?> toClass = toType.getType();

        //the only widening conversion from float is to double
        if(fromClass == float.class && toClass == double.class)
            methodVisitor.visitInsn(F2D);
        else if(fromClass == long.class) {
            //long can only be widened into float or double
            if(toClass == double.class)
                methodVisitor.visitInsn(L2D);
            else if(toClass == float.class)
                methodVisitor.visitInsn(L2F);
        }
        else if(fromClass == int.class || fromClass == short.class || fromClass == byte.class || fromClass == char.class) {
            //int, short, byte, and char are just ints in the jvm
            //ints can be widened into long, float, or double
            if(toClass == long.class)
                methodVisitor.visitInsn(I2L);
            else if(toClass == float.class)
                methodVisitor.visitInsn(I2F);
            else if(toClass == double.class)
                methodVisitor.visitInsn(I2D);
        }

        classStack.pop();
        classStack.push(toType);
    }

    private void buildAutoboxing(MethodVisitor methodVisitor, Stack<TypeDefinition> typeStack) {
        Class<?> fromPrimitive = typeStack.pop().getType();
        Method method = MethodUtils.getAccessibleMethod(toType.getType(), "valueOf", fromPrimitive);
        methodVisitor.visitMethodInsn(
            INVOKESTATIC,
            toType.getJvmTypeName(),
            "valueOf",
            generateJvmMethodSignature(method),
            false
        );
        typeStack.push(toType);
    }

    private static void buildAutoUnboxing(MethodVisitor methodVisitor, Stack<TypeDefinition> typeStack) {
        TypeDefinition fromWrapper = typeStack.pop();
        Class<?> fromClass = fromWrapper.getType();
        Class<?> toClass = ClassUtils.wrapperToPrimitive(fromClass);

        String methodName = toClass.getSimpleName().toLowerCase() + "Value";
        Method method = MethodUtils.getAccessibleMethod(fromClass, methodName);
        methodVisitor.visitMethodInsn(
            method.getDeclaringClass().isInterface() ? INVOKEINTERFACE : INVOKEVIRTUAL,
            fromWrapper.getJvmTypeName(),
            methodName,
            generateJvmMethodSignature(method),
            method.getDeclaringClass().isInterface()
        );
        typeStack.push(type(toClass));
    }
}