package io.github.chunsinger.asmsauce.definitions;

import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode
public class NameDefinition {
    public static final String CONSTRUCTOR_NAME = "<init>";
    public static final NameDefinition CONSTRUCTOR_NAME_DEFINITION = new NameDefinition(CONSTRUCTOR_NAME);

    private final String name;

    public NameDefinition(String name) {
        if(name == null)
            throw new IllegalArgumentException("Method name cannot be null.");

        this.name = name;
    }

    public boolean isConstructorName() {
        return CONSTRUCTOR_NAME.equals(name);
    }
}