package com.chunsinger.asmsauce;

import com.chunsinger.asmsauce.testing.BaseUnitTest;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static aj.org.objectweb.asm.Opcodes.ACC_FINAL;
import static aj.org.objectweb.asm.Opcodes.ACC_STATIC;
import static com.chunsinger.asmsauce.ConstructorNode.constructor;
import static com.chunsinger.asmsauce.modifiers.AccessModifiers.customAccess;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ConstructorNodeTest extends BaseUnitTest {
    @ParameterizedTest
    @CsvSource({
        ACC_FINAL + ",A constructor cannot have the 'final' modifier.",
        ACC_STATIC + ",A constructor cannot have the 'static' modifier."
    })
    public void illegalArgumentException_invalidModifiers(int modifiers, String expectedExceptionMessage) {
        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class,
            () -> constructor(customAccess(modifiers), null, null)
        );
        assertThat(ex, hasProperty("message", is(expectedExceptionMessage)));
    }
}