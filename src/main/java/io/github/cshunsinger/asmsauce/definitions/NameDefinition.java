package io.github.cshunsinger.asmsauce.definitions;

import lombok.Getter;

/**
 * Represents the name of something such as a method, class, variable/parameter, etc.
 */
@Getter
public class NameDefinition {
    /**
     * The jvm name of a constructor.
     */
    public static final String CONSTRUCTOR_NAME = "<init>";
    /**
     * The NameDefinition representation of the jvm constructor name.
     */
    public static final NameDefinition CONSTRUCTOR_NAME_DEFINITION = new NameDefinition(CONSTRUCTOR_NAME);
    /**
     * @return The String defining this name definition.
     */
    private final String name;

    /**
     * Creates a new name definition from a name.
     * @param name The name String.
     * @throws IllegalArgumentException If name is null.
     */
    public NameDefinition(String name) {
        if(name == null)
            throw new IllegalArgumentException("Method name cannot be null.");

        this.name = name;
    }

    /**
     * Gets whether or not this name defines a constructor.
     * @return Returns true if this name defines a constructor, false if not.
     */
    public boolean isConstructorName() {
        return CONSTRUCTOR_NAME.equals(name);
    }

    @Override
    public boolean equals(Object other) {
        if(other == this)
            return true;
        else if(other instanceof NameDefinition) {
            NameDefinition otherNameDefinition = (NameDefinition)other;
            return otherNameDefinition.name.equals(this.name);
        }
        else
            return false;
    }
}