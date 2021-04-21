package io.github.cshunsinger.asmsauce.definitions;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * Defines a single parameter for a method.
 * A parameter can be defined as unnamed, with only a type.
 * A parameter can be defined as a named parameter with a type and a non-blank name.
 */
@Getter
public class ParamDefinition {
    /**
     * @return The name of this defined parameter.
     */
    private final String paramName;

    /**
     * @return The type of this defined parameter.
     */
    private final TypeDefinition<?> paramType;

    /**
     * Defines an unnamed parameter from a type definition.
     * @param paramType The type of the unnamed parameter.
     * @throws IllegalArgumentException If the paramType is null.
     * @see io.github.cshunsinger.asmsauce.code.CodeBuilders#getVar(int)
     */
    public ParamDefinition(TypeDefinition<?> paramType) {
        this(null, paramType);
    }

    /**
     * Defines a named or unnamed parameter. If the parameter name is specified as a non-null and non-blank String,
     * then this parameter will be defined as a named parameter, which can be accessed as a named local variable.
     * @param paramName The name of this parameter. If null, this parameter will be defined as an unnamed parameter.
     * @param paramType The type of this parameter.
     * @throws IllegalArgumentException If the paramType is null.
     * @see io.github.cshunsinger.asmsauce.code.CodeBuilders#getVar(String)
     * @see io.github.cshunsinger.asmsauce.code.CodeBuilders#getVar(int)
     */
    public ParamDefinition(String paramName, TypeDefinition<?> paramType) {
        paramName = StringUtils.trimToNull(paramName);
        this.paramName = paramName;
        this.paramType = paramType;

        if(paramType == null)
            throw new IllegalArgumentException("Param type cannot be null.");
    }
}