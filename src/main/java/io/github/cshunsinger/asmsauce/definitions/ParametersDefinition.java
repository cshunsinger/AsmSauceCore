package io.github.cshunsinger.asmsauce.definitions;

import lombok.Getter;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Defines the parameter set of a method or constructor. Contains a list of individual parameter definitions.
 */
@Getter
public class ParametersDefinition {
    /**
     * The list of parameters defined in this parameter set.
     * @return The list of parameters.
     */
    private final List<ParamDefinition> params;

    /**
     * Creates a new parameters definition from a list of types. The parameters in this parameter set will all be
     * unnamed and will have to be referenced as unnamed local variables.
     * @param paramTypes A list of type definitions. One type definition to represent each parameter.
     */
    public ParametersDefinition(TypeDefinition... paramTypes) {
        this(Stream.of(paramTypes).map(ParamDefinition::new).toArray(ParamDefinition[]::new));
    }

    /**
     * Creates a new parameters definition from a list of individual parameter definitions.
     * The parameters in this set will either be named or unnamed depending on whether or not each parameter definition
     * defines a parameter name, or only a parameter type.
     * @param params The list of parameters defined in this parameter set.
     */
    public ParametersDefinition(ParamDefinition... params) {
        this.params = List.of(params);
    }

    /**
     * @return The number of parameters defined.
     */
    public int count() {
        return params.size();
    }

    /**
     * Gets the parameter type at a given index in the list of defined parameters.
     * @param index The index of the parameter in the list to fetch the type.
     * @return The parameter type at the given index.
     */
    public TypeDefinition get(int index) {
        return params.get(index).getParamType();
    }

    /**
     * Gets a list of parameter types for the defined parameters.
     * @return A list of parameter types.
     */
    public List<TypeDefinition> getParamTypes() {
        return params.stream()
            .map(ParamDefinition::getParamType)
            .collect(Collectors.toList());
    }

    /**
     * Gets whether another set of parameters match these parameters.
     * Another set of parameters will match these parameters if the other parameter set has the same count of
     * parameters as this set, and if all of the parameters in the other set are assignable to their adjacent
     * parameter type in this set.
     * @param other The other parameter set to test against this one.
     * @return True if the other parameters match these, else false.
     * @throws NullPointerException if 'other' is null.
     */
    public boolean matches(ParametersDefinition other) {
        //If counts are different then no match
        int count = this.count();
        if(count != other.count())
            return false;

        //If any parameter type from the other set is not assignable to the adjacent parameter in this set
        //then no match.
        for(int i = 0; i < count; i++) {
            TypeDefinition declared = this.get(i);
            TypeDefinition actual = other.get(i);

            if(!declared.isAssignableFrom(actual))
                return false;
        }

        //The other parameter set matches this one
        return true;
    }
}