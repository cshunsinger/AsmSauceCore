package io.github.cshunsinger.asmsauce.definitions;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

@Getter
public class ParamDefinition {
    private final String paramName;
    private final TypeDefinition<?> paramType;

    public ParamDefinition(TypeDefinition<?> paramType) {
        this(null, paramType);
    }
    
    public ParamDefinition(String paramName, TypeDefinition<?> paramType) {
        paramName = StringUtils.trimToNull(paramName);
        this.paramName = paramName;
        this.paramType = paramType;
    }
}