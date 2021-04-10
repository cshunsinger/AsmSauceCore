package com.chunsinger.asmsauce;

import aj.org.objectweb.asm.MethodVisitor;
import com.chunsinger.asmsauce.definitions.CompleteMethodDefinition;
import com.chunsinger.asmsauce.definitions.ParamDefinition;
import com.chunsinger.asmsauce.definitions.TypeDefinition;
import lombok.Getter;

import java.util.*;
import java.util.stream.Collectors;

@Getter
public class MethodBuildingContext {
    private static final Set<Class<?>> LARGE_LOCALS = Set.of(double.class, long.class);

    private final MethodVisitor methodVisitor;
    private final CompleteMethodDefinition<?, ?> currentMethod;
    private final com.chunsinger.asmsauce.ClassBuildingContext classContext;
    private final List<TypeDefinition<?>> localTypes;
    private final Map<String, Integer> localNames = new HashMap<>();
    private final Stack<TypeDefinition<?>> typeStack = new Stack<>();
    private final Stack<Integer> scopeStack = new Stack<>();

    public MethodBuildingContext(MethodVisitor methodVisitor,
                                 CompleteMethodDefinition<?, ?> currentMethod,
                                 com.chunsinger.asmsauce.ClassBuildingContext classContext,
                                 List<ParamDefinition> parameters) {
        this.methodVisitor = methodVisitor;
        this.currentMethod = currentMethod;
        this.classContext = classContext;
        this.localTypes = new ArrayList<>();

        parameters.forEach(param -> addLocalType(param.getParamName(), param.getParamType()));
        scopeStack.push(0);
    }

    public void beginScope() {
        //The value pushed is essentially the index of the first local variable that will be visible in the new scope
        //Any locals with this index or higher will not be visible anymore once this scope ends.
        scopeStack.push(localTypes.size());
    }

    public void endScope() {
        int beginIndex = scopeStack.pop(); //The first local variable index for the scope being ended.

        //Remove all locals on or after the beginIndex as these locals are no longer visible since the scope is ending
        while(localTypes.size() > beginIndex) {
            localTypes.remove(localTypes.size() - 1);
        }

        //Remove any local variable names from the mapping if those locals are out of scope now
        localNames.entrySet()
            .stream()
            .filter(entry -> entry.getValue() >= beginIndex)
            .map(Map.Entry::getKey)
            .collect(Collectors.toSet())
            .forEach(localNames::remove);
    }

    public TypeDefinition<?> peekStack() {
        return typeStack.peek();
    }

    public TypeDefinition<?> popStack() {
        return typeStack.pop();
    }

    public void popStack(int numElements) {
        for(int i = 0; i < numElements; i++) {
            typeStack.pop();
        }
    }

    public TypeDefinition<?> pushStack(TypeDefinition<?> type) {
        return typeStack.push(type);
    }

    public int stackSize() {
        return typeStack.size();
    }

    public boolean isStackEmpty() {
        return typeStack.isEmpty();
    }

    public int getLocalIndex(String localName) {
        if(localNames.containsKey(localName))
            return localNames.get(localName);
        else
            throw new IllegalStateException("No local variable exists with the name " + localName + ".");
    }

    public TypeDefinition<?> getLocalType(int index) {
        return localTypes.get(index);
    }

    public TypeDefinition<?> getLocalType(String name) {
        return getLocalType(getLocalIndex(name));
    }

    public int addLocalType(TypeDefinition<?> type) {
        return addLocalType(null, type);
    }

    public int addLocalType(String name, TypeDefinition<?> type) {
        int newIndex = localTypes.size();
        localTypes.add(type);

        if(LARGE_LOCALS.contains(type.getType()))
            localTypes.add(type);

        if(name != null)
            localNames.put(name, newIndex);

        return newIndex;
    }

    public void setLocalType(int index, TypeDefinition<?> type) {
        if(index == localTypes.size())
            addLocalType(type);
        else
            localTypes.set(index, type);

        if(LARGE_LOCALS.contains(type.getType())) {
            index++;
            if(index == localTypes.size())
                localTypes.add(type);
            else
                localTypes.set(index, type);
        }
    }

    public void setLocalType(String name, TypeDefinition<?> type) {
        if(localNames.containsKey(name)) {
            int index = localNames.get(name);
            setLocalType(index, type);
        }
        else
            addLocalType(name, type);
    }

    public int numLocals() {
        return localTypes.size();
    }

    public TypeDefinition<?> returnType() {
        return currentMethod.getReturnType();
    }
}