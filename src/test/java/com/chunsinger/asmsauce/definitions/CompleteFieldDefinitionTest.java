package com.chunsinger.asmsauce.definitions;

import com.chunsinger.asmsauce.modifiers.AccessModifiers;
import com.chunsinger.asmsauce.testing.BaseUnitTest;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static com.chunsinger.asmsauce.DefinitionBuilders.name;
import static com.chunsinger.asmsauce.DefinitionBuilders.type;
import static com.chunsinger.asmsauce.modifiers.AccessModifiers.publicOnly;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CompleteFieldDefinitionTest extends BaseUnitTest {
    @ParameterizedTest
    @MethodSource("illegalArgumentException_invalidParameters_testCases")
    public void illegalArgumentException_invalidParameters(AccessModifiers accessModifiers,
                                                           TypeDefinition<?> fieldOwner,
                                                           NameDefinition fieldName,
                                                           TypeDefinition<?> fieldType,
                                                           String expectedExceptionMessage) {
        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class,
            () -> new CompleteFieldDefinition(accessModifiers, fieldOwner, fieldName, fieldType)
        );
        assertThat(ex, hasProperty("message", is(expectedExceptionMessage)));
    }

    private static Stream<Arguments> illegalArgumentException_invalidParameters_testCases() {
        return Stream.of(
            Arguments.of(null, type(Object.class), name("Name"), type(Object.class), "Field access modifiers cannot be null."),
            Arguments.of(publicOnly(), null, name("Name"), type(Object.class), "Field owner type cannot be null or void."),
            Arguments.of(publicOnly(), type(void.class), name("Name"), type(Object.class), "Field owner type cannot be null or void."),
            Arguments.of(publicOnly(), type(int.class), name("Name"), type(Object.class), "Field owner cannot be a primitive type. Primitive types have no fields."),
            Arguments.of(publicOnly(), type(Object.class), name("Name"), null, "Field type cannot be null or void."),
            Arguments.of(publicOnly(), type(Object.class), name("Name"), type(void.class), "Field type cannot be null or void."),
            Arguments.of(publicOnly(), type(Object.class), null, type(Object.class), "Field name cannot be null.")
        );
    }
}