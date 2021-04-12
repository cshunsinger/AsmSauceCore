package io.github.cshunsinger.asmsauce.definitions;

import lombok.Getter;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Getter
public class ParametersDefinition {
    private final List<ParamDefinition> params;

    public ParametersDefinition(TypeDefinition<?>... paramTypes) {
        this(Stream.of(paramTypes).map(ParamDefinition::new).toArray(ParamDefinition[]::new));
    }

    public ParametersDefinition(ParamDefinition... params) {
        this.params = List.of(params);
    }

    public int count() {
        return params.size();
    }

    public TypeDefinition<?> get(int index) {
        return params.get(index).getParamType();
    }

    public List<TypeDefinition<?>> getParamTypes() {
        return params.stream()
            .map(ParamDefinition::getParamType)
            .collect(Collectors.toList());
    }
}