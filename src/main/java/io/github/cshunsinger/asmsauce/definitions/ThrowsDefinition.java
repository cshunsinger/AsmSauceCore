package io.github.cshunsinger.asmsauce.definitions;

import java.util.List;

/**
 * This class represents the "throws" clause of a method. This definition contains a list of exception types which
 * can potentially be thrown by a method.
 */
public class ThrowsDefinition {
    private final List<TypeDefinition> exceptionTypes;

    /**
     * Creates a throws definition containing a set of zero or more exception types that can be thrown by a method.
     * @param exceptionTypes The exception types to declare in this throws definition.
     */
    public ThrowsDefinition(TypeDefinition... exceptionTypes) {
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