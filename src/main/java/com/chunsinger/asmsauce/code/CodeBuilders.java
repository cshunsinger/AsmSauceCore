package com.chunsinger.asmsauce.code;

import com.chunsinger.asmsauce.ThisClass;
import com.chunsinger.asmsauce.code.branch.IfBranch;
import com.chunsinger.asmsauce.code.branch.condition.Condition;
import com.chunsinger.asmsauce.code.cast.ExplicitConversionInsn;
import com.chunsinger.asmsauce.code.field.*;
import com.chunsinger.asmsauce.code.method.InstantiateObjectInsn;
import com.chunsinger.asmsauce.code.method.InvokeBaseConstructorInsn;
import com.chunsinger.asmsauce.code.method.InvokeStaticMethodInsn;
import com.chunsinger.asmsauce.code.method.ReturnInsn;
import com.chunsinger.asmsauce.code.stack.StackLocalVariableInsn;
import com.chunsinger.asmsauce.code.stack.StackObjectLiteralInsn;
import com.chunsinger.asmsauce.code.stack.StackPrimitiveLiteralInsn;
import com.chunsinger.asmsauce.code.stack.StoreLocalVariableInsn;
import com.chunsinger.asmsauce.definitions.*;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.lang.reflect.Constructor;

import static com.chunsinger.asmsauce.DefinitionBuilders.*;
import static com.chunsinger.asmsauce.modifiers.AccessModifiers.privateStatic;
import static com.chunsinger.asmsauce.modifiers.AccessModifiers.publicStatic;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CodeBuilders {
    public static StackLocalVariableInsn this_() {
        return getVar(0);
    }

    public static StackLocalVariableInsn getVar(int localIndex) {
        return new StackLocalVariableInsn(localIndex);
    }

    public static StackLocalVariableInsn getVar(String localName) {
        return new StackLocalVariableInsn(localName);
    }

    public static StoreLocalVariableInsn setVar(Integer localIndex, CodeInsnBuilderLike valueBuilder) {
        return new StoreLocalVariableInsn(localIndex, valueBuilder);
    }

    public static StoreLocalVariableInsn setVar(CodeInsnBuilderLike valueBuilder) {
        return setVar((Integer)null, valueBuilder);
    }

    public static StoreLocalVariableInsn setVar(String localName, CodeInsnBuilderLike valueBuilder) {
        return new StoreLocalVariableInsn(localName, valueBuilder);
    }

    public static InvokeBaseConstructorInsn superConstructor(Constructor<?> constructor, CodeInsnBuilderLike... paramStackBuilders) {
        return new InvokeBaseConstructorInsn(constructor, paramStackBuilders);
    }

    public static InvokeBaseConstructorInsn superConstructor(TypeDefinition<?> owner, ParametersDefinition parameters, CodeInsnBuilderLike... paramStackBuilders) {
        return new InvokeBaseConstructorInsn(owner, parameters, noThrows(), paramStackBuilders);
    }

    public static InvokeBaseConstructorInsn superConstructor(Class<?> ownerClass, ParametersDefinition parameters, CodeInsnBuilderLike... paramStackBuilders) {
        return superConstructor(type(ownerClass), parameters, paramStackBuilders);
    }

    public static InvokeBaseConstructorInsn thisConstructor(ParametersDefinition parameters, CodeInsnBuilderLike... paramStackBuilders) {
        return superConstructor(ThisClass.class, parameters, paramStackBuilders);
    }

    public static StackObjectLiteralInsn stackNull() {
        return literalObj(null);
    }

    public static StackObjectLiteralInsn literalObj(Object obj) {
        return new StackObjectLiteralInsn(obj);
    }

    public static StackPrimitiveLiteralInsn literal(byte b) {
        return new StackPrimitiveLiteralInsn(b);
    }

    public static StackPrimitiveLiteralInsn literal(short s) {
        return new StackPrimitiveLiteralInsn(s);
    }

    public static StackPrimitiveLiteralInsn literal(char c) {
        return new StackPrimitiveLiteralInsn(c);
    }

    public static StackPrimitiveLiteralInsn literal(int i) {
        return new StackPrimitiveLiteralInsn(i);
    }

    public static StackPrimitiveLiteralInsn literal(long l) {
        return new StackPrimitiveLiteralInsn(l);
    }

    public static StackPrimitiveLiteralInsn literal(float f) {
        return new StackPrimitiveLiteralInsn(f);
    }

    public static StackPrimitiveLiteralInsn literal(double d) {
        return new StackPrimitiveLiteralInsn(d);
    }

    public static StackPrimitiveLiteralInsn literal(boolean b) {
        return new StackPrimitiveLiteralInsn(b);
    }

    public static StackPrimitiveLiteralInsn true_() {
        return literal(true);
    }

    public static StackPrimitiveLiteralInsn false_() {
        return literal(false);
    }

    public static ReturnInsn returnVoid() {
        return returnValue(null);
    }

    public static ReturnInsn returnValue(CodeInsnBuilderLike codeInsnBuilder) {
        return new ReturnInsn(codeInsnBuilder);
    }

    public static InvokeStaticMethodInsn invokeStatic(TypeDefinition<?> ownerType, NameDefinition name, CodeInsnBuilderLike... paramBuilders) {
        return new InvokeStaticMethodInsn(ownerType, name, paramBuilders);
    }

    public static InvokeStaticMethodInsn invokeStatic(Class<?> ownerClass, String name, CodeInsnBuilderLike... paramBuilders) {
        return invokeStatic(type(ownerClass), name(name), paramBuilders);
    }

    public static InvokeStaticMethodInsn invokeStatic(TypeDefinition<?> type, NameDefinition name, ParametersDefinition parameters, TypeDefinition<?> returnType, CodeInsnBuilderLike... paramBuilders) {
        return new InvokeStaticMethodInsn(type, name, parameters, returnType, noThrows(), paramBuilders);
    }

    public static InvokeStaticMethodInsn invokeStatic(Class<?> typeClass, NameDefinition name, ParametersDefinition parameters, TypeDefinition<?> returnType, CodeInsnBuilderLike... parameterBuilders) {
        return invokeStatic(type(typeClass), name, parameters, returnType, parameterBuilders);
    }

    public static InvokeStaticMethodInsn invokeStatic(TypeDefinition<?> type, NameDefinition name, ParametersDefinition parameters, CodeInsnBuilderLike... parameterBuilders) {
        return new InvokeStaticMethodInsn(type, name, parameters, voidType(), noThrows(), parameterBuilders);
    }

    public static InvokeStaticMethodInsn invokeStatic(Class<?> typeClass, NameDefinition name, ParametersDefinition parameters, CodeInsnBuilderLike... parameterBuilders) {
        return invokeStatic(type(typeClass), name, parameters, parameterBuilders);
    }

    public static GetStaticFieldInsn getStatic(TypeDefinition<?> owner, NameDefinition name, TypeDefinition<?> fieldType) {
        return new GetStaticFieldInsn(new CompleteFieldDefinition(
            publicStatic(), //Access is irrelevant here, other than that this be static
            owner, name, fieldType
        ));
    }

    public static GetStaticFieldInsn getStatic(TypeDefinition<?> owner, NameDefinition name) {
        return new GetStaticFieldInsn(new FieldDefinition(privateStatic(), owner, name, null));
    }

    public static GetStaticFieldInsn getStatic(Class<?> ownerClass, String fieldName) {
        return getStatic(type(ownerClass), name(fieldName));
    }

    public static AssignStaticFieldInsn setStatic(TypeDefinition<?> owner, NameDefinition name, TypeDefinition<?> fieldType, CodeInsnBuilderLike valueBuilder) {
        return new AssignStaticFieldInsn(new CompleteFieldDefinition(
            publicStatic(),
            owner, name, fieldType
        ), valueBuilder);
    }

    public static AssignStaticFieldInsn setStatic(TypeDefinition<?> owner, NameDefinition name, CodeInsnBuilderLike valueBuilder) {
        return new AssignStaticFieldInsn(new FieldDefinition(
            publicStatic(),
            owner, name, null
        ), valueBuilder);
    }

    public static AssignStaticFieldInsn setStatic(Class<?> owner, String name, CodeInsnBuilderLike valueBuilder) {
        return setStatic(type(owner), name(name), valueBuilder);
    }

    public static ExplicitConversionInsn cast(TypeDefinition<?> toType, CodeInsnBuilderLike valueBuilder) {
        return new ExplicitConversionInsn(toType, valueBuilder);
    }

    public static ExplicitConversionInsn cast(Class<?> toType, CodeInsnBuilderLike valueBuilder) {
        return cast(type(toType), valueBuilder);
    }

    public static InstantiateObjectInsn instantiate(TypeDefinition<?> type, ParametersDefinition parameters, CodeInsnBuilderLike... paramBuilders) {
        return new InstantiateObjectInsn(type, parameters, paramBuilders);
    }

    public static InstantiateObjectInsn instantiate(Class<?> type, ParametersDefinition parameters, CodeInsnBuilderLike... paramBuilders) {
        return instantiate(type(type), parameters, paramBuilders);
    }

    public static InstantiateObjectInsn instantiate(TypeDefinition<?> type, CodeInsnBuilderLike... paramBuilders) {
        return new InstantiateObjectInsn(type, paramBuilders);
    }

    public static InstantiateObjectInsn instantiate(Class<?> type, CodeInsnBuilderLike... paramBuilders) {
        return instantiate(type(type), paramBuilders);
    }

    public static CodeBlock block(CodeInsnBuilderLike... builders) {
        return new CodeBlock(builders);
    }

    public static IfBranch.IfBuilder if_(Condition condition) {
        return new IfBranch.IfBuilder(condition);
    }

    public static Condition not(Condition condition) {
        return condition.invert();
    }
}