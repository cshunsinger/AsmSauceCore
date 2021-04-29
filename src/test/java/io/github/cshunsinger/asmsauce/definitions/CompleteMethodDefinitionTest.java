package io.github.cshunsinger.asmsauce.definitions;

import io.github.cshunsinger.asmsauce.AsmClassBuilder;
import io.github.cshunsinger.asmsauce.ThisClass;
import io.github.cshunsinger.asmsauce.code.CodeBuilders;
import io.github.cshunsinger.asmsauce.modifiers.AccessModifiers;
import io.github.cshunsinger.asmsauce.BaseUnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static io.github.cshunsinger.asmsauce.ConstructorNode.constructor;
import static io.github.cshunsinger.asmsauce.DefinitionBuilders.*;
import static io.github.cshunsinger.asmsauce.MethodNode.method;
import static io.github.cshunsinger.asmsauce.code.CodeBuilders.*;
import static io.github.cshunsinger.asmsauce.modifiers.AccessModifiers.publicOnly;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CompleteMethodDefinitionTest extends BaseUnitTest {
    @ParameterizedTest
    @MethodSource("illegalArgumentException_badConstructorParameters_testCases")
    public void illegalArgumentException_badConstructorParameters(TypeDefinition owner,
                                                                  AccessModifiers modifiers,
                                                                  NameDefinition name,
                                                                  TypeDefinition returnType,
                                                                  ParametersDefinition parameters,
                                                                  ThrowsDefinition throwing,
                                                                  String expectedExceptionMessage) {
        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class,
            () -> new CompleteMethodDefinition(owner, modifiers, name, returnType, parameters, throwing)
        );
        assertThat(ex, hasProperty("message", is(expectedExceptionMessage)));
    }

    private static Stream<Arguments> illegalArgumentException_badConstructorParameters_testCases() {
        return Stream.of(
            Arguments.of(type(Object.class), publicOnly(), name("Name"), voidType(), noParameters(), null, "Throwing cannot be null."),
            Arguments.of(type(Object.class), publicOnly(), name("Name"), voidType(), null, noThrows(), "Parameters cannot be null."),
            Arguments.of(type(Object.class), publicOnly(), name("Name"), null, noParameters(), noThrows(), "Return type cannot be null."),
            Arguments.of(type(Object.class), publicOnly(), null, voidType(), noParameters(), noThrows(), "Name cannot be null."),
            Arguments.of(type(Object.class), null, name("Name"), voidType(), noParameters(), noThrows(), "Modifiers cannot be null."),
            Arguments.of(null, publicOnly(), name("Name"), voidType(), noParameters(), noThrows(), "Method owner type cannot be null."),
            Arguments.of(type(Object[].class), publicOnly(), name("Name"), voidType(), noParameters(), noThrows(), "Method owner type cannot be an array type.")
        );
    }

    @Test
    public void illegalStateException_noConstructorFoundForThisClass() {
        AsmClassBuilder<Object> builder = new AsmClassBuilder<>(Object.class)
            .withConstructor(constructor(publicOnly(), noParameters(),
                superConstructor(Object.class, noParameters()),
                returnVoid()
            ))
            .withMethod(method(publicOnly(), name("createAnotherOne"), noParameters(), type(ThisClass.class),
                returnValue(
                    instantiate(ThisClass.class, parameters(String.class), literalObj("Some String"))
                )
            ));

        IllegalStateException ex = assertThrows(IllegalStateException.class, builder::build);
        assertThat(ex, hasProperty("message", startsWith("Constructor not found in class")));
    }

    @Test
    public void illegalStateException_constructorInAnotherClassCannotBeAccessed() {
        AsmClassBuilder<Object> builder = new AsmClassBuilder<>(Object.class)
            .withConstructor(constructor(publicOnly(), noParameters(),
                superConstructor(Object.class, noParameters()),
                returnVoid()
            ))
            .withMethod(method(publicOnly(), name("createAnotherOne"), noParameters(), type(AccessModifiers.class),
                returnValue(
                    instantiate(AccessModifiers.class, parameters(int.class), literal(123))
                )
            ));

        IllegalStateException ex = assertThrows(IllegalStateException.class, builder::build);
        assertThat(ex, hasProperty("message", is("Constructor not found in class '%s' with parameters:\n\tint".formatted(
            AccessModifiers.class.getName()
        ))));
    }

    @Test
    public void illegalStateException_methodInThisClassCannotBeFound() {
        AsmClassBuilder<Object> builder = new AsmClassBuilder<>(Object.class)
            .withConstructor(constructor(publicOnly(), noParameters(),
                superConstructor(Object.class, noParameters()),
                returnVoid()
            ))
            .withMethod(method(publicOnly(), name("createAnotherOne"), noParameters(), type(String.class),
                returnValue(
                    invokeStatic(ThisClass.class, name("imaginaryStaticMethod"), noParameters(), type(String.class))
                )
            ));

        IllegalStateException ex = assertThrows(IllegalStateException.class, builder::build);
        assertThat(ex, hasProperty("message", startsWith("Method 'imaginaryStaticMethod' not found in class")));
    }
}