package io.github.cshunsinger.asmsauce.definitions;

import io.github.cshunsinger.asmsauce.AsmClassBuilder;
import io.github.cshunsinger.asmsauce.ThisClass;
import io.github.cshunsinger.asmsauce.code.CodeBuilders;
import io.github.cshunsinger.asmsauce.modifiers.AccessModifiers;
import io.github.cshunsinger.asmsauce.BaseUnitTest;
import io.github.cshunsinger.asmsauce.DefinitionBuilders;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static io.github.cshunsinger.asmsauce.ConstructorNode.constructor;
import static io.github.cshunsinger.asmsauce.MethodNode.method;
import static io.github.cshunsinger.asmsauce.code.CodeBuilders.*;
import static io.github.cshunsinger.asmsauce.modifiers.AccessModifiers.publicOnly;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CompleteMethodDefinitionTest extends BaseUnitTest {
    @ParameterizedTest
    @MethodSource("illegalArgumentException_badConstructorParameters_testCases")
    public void illegalArgumentException_badConstructorParameters(TypeDefinition<?> owner,
                                                                  AccessModifiers modifiers,
                                                                  NameDefinition name,
                                                                  TypeDefinition<?> returnType,
                                                                  ParametersDefinition parameters,
                                                                  ThrowsDefinition throwing,
                                                                  String expectedExceptionMessage) {
        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class,
            () -> new CompleteMethodDefinition<>(owner, modifiers, name, returnType, parameters, throwing)
        );
        assertThat(ex, hasProperty("message", is(expectedExceptionMessage)));
    }

    private static Stream<Arguments> illegalArgumentException_badConstructorParameters_testCases() {
        return Stream.of(
            Arguments.of(DefinitionBuilders.type(Object.class), publicOnly(), DefinitionBuilders.name("Name"), DefinitionBuilders.voidType(), DefinitionBuilders.noParameters(), null, "Throwing cannot be null."),
            Arguments.of(DefinitionBuilders.type(Object.class), publicOnly(), DefinitionBuilders.name("Name"), DefinitionBuilders.voidType(), null, DefinitionBuilders.noThrows(), "Parameters cannot be null."),
            Arguments.of(DefinitionBuilders.type(Object.class), publicOnly(), DefinitionBuilders.name("Name"), null, DefinitionBuilders.noParameters(), DefinitionBuilders.noThrows(), "Return type cannot be null."),
            Arguments.of(DefinitionBuilders.type(Object.class), publicOnly(), null, DefinitionBuilders.voidType(), DefinitionBuilders.noParameters(), DefinitionBuilders.noThrows(), "Name cannot be null."),
            Arguments.of(DefinitionBuilders.type(Object.class), null, DefinitionBuilders.name("Name"), DefinitionBuilders.voidType(), DefinitionBuilders.noParameters(), DefinitionBuilders.noThrows(), "Modifiers cannot be null."),
            Arguments.of(null, publicOnly(), DefinitionBuilders.name("Name"), DefinitionBuilders.voidType(), DefinitionBuilders.noParameters(), DefinitionBuilders.noThrows(), "Method owner type cannot be null."),
            Arguments.of(DefinitionBuilders.type(Object[].class), publicOnly(), DefinitionBuilders.name("Name"), DefinitionBuilders.voidType(), DefinitionBuilders.noParameters(), DefinitionBuilders.noThrows(), "Method owner type cannot be an array type.")
        );
    }

    @Test
    public void illegalStateException_noConstructorFoundForThisClass() {
        AsmClassBuilder<Object> builder = new AsmClassBuilder<>(Object.class)
            .withConstructor(constructor(publicOnly(), DefinitionBuilders.noParameters(),
                CodeBuilders.superConstructor(Object.class, DefinitionBuilders.noParameters()),
                CodeBuilders.returnVoid()
            ))
            .withMethod(method(publicOnly(), DefinitionBuilders.name("createAnotherOne"), DefinitionBuilders.noParameters(), DefinitionBuilders.type(ThisClass.class),
                CodeBuilders.returnValue(
                    instantiate(ThisClass.class, DefinitionBuilders.parameters(String.class), CodeBuilders.literalObj("Some String"))
                )
            ));

        IllegalStateException ex = assertThrows(IllegalStateException.class, builder::build);
        assertThat(ex, hasProperty("message", startsWith("Constructor not found in class")));
    }

    @Test
    public void illegalStateException_constructorInAnotherClassCannotBeAccessed() {
        AsmClassBuilder<Object> builder = new AsmClassBuilder<>(Object.class)
            .withConstructor(constructor(publicOnly(), DefinitionBuilders.noParameters(),
                CodeBuilders.superConstructor(Object.class, DefinitionBuilders.noParameters()),
                CodeBuilders.returnVoid()
            ))
            .withMethod(method(publicOnly(), DefinitionBuilders.name("createAnotherOne"), DefinitionBuilders.noParameters(), DefinitionBuilders.type(AccessModifiers.class),
                CodeBuilders.returnValue(
                    CodeBuilders.instantiate(AccessModifiers.class, DefinitionBuilders.parameters(int.class), CodeBuilders.literal(123))
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
            .withConstructor(constructor(publicOnly(), DefinitionBuilders.noParameters(),
                CodeBuilders.superConstructor(Object.class, DefinitionBuilders.noParameters()),
                CodeBuilders.returnVoid()
            ))
            .withMethod(method(publicOnly(), DefinitionBuilders.name("createAnotherOne"), DefinitionBuilders.noParameters(), DefinitionBuilders.type(String.class),
                CodeBuilders.returnValue(
                    CodeBuilders.invokeStatic(ThisClass.class, DefinitionBuilders.name("imaginaryStaticMethod"), DefinitionBuilders.noParameters(), DefinitionBuilders.type(String.class))
                )
            ));

        IllegalStateException ex = assertThrows(IllegalStateException.class, builder::build);
        assertThat(ex, hasProperty("message", startsWith("Method 'imaginaryStaticMethod' not found in class")));
    }
}