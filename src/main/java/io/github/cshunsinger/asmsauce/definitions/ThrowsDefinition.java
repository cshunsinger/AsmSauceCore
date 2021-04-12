package io.github.cshunsinger.asmsauce.definitions;

import java.util.List;

public class ThrowsDefinition {
    private final List<TypeDefinition<? extends Exception>> exceptionTypes;

    @SafeVarargs
    public ThrowsDefinition(TypeDefinition<? extends Exception>... exceptionTypes) {
        this.exceptionTypes = List.of(exceptionTypes);
    }

    /**
     * Returns the list of exceptions as an array of at least 1 exception.
     * If there are no exceptions in this list, then null is returned.
     * @return If no exceptions are defined to be thrown by the method then null is returned. Otherwise an array of
     *         jvm type names, the exceptions being thrown, is returned. These Strings are the actual jvm types that
     *         can be used by the asm library when building bytecode.
     */
    public String[] getJvmExceptions() {
        if(exceptionTypes.isEmpty())
            return null;
        else
            return exceptionTypes.stream().map(TypeDefinition::getJvmTypeName).toArray(String[]::new);
    }
}