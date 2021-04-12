package io.github.chunsinger.asmsauce.definitions;

import io.github.chunsinger.asmsauce.AsmClassBuilder;
import io.github.chunsinger.asmsauce.ThisClass;
import io.github.chunsinger.asmsauce.code.CodeBuilders;
import io.github.chunsinger.asmsauce.testing.BaseUnitTest;
import io.github.chunsinger.asmsauce.DefinitionBuilders;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.ThrowingSupplier;

import static io.github.chunsinger.asmsauce.ConstructorNode.constructor;
import static io.github.chunsinger.asmsauce.MethodNode.method;
import static io.github.chunsinger.asmsauce.modifiers.AccessModifiers.privateOnly;
import static io.github.chunsinger.asmsauce.modifiers.AccessModifiers.publicOnly;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class MethodDefinitionTest extends BaseUnitTest {
    @Test
    public void illegalStateException_attemptToGenerateMethodSignature_noParametersDefined() {
        MethodDefinition<?, ?> methodDefinition = new MethodDefinition<>(null, null, DefinitionBuilders.name("Name"), null, null, null);

        IllegalStateException ex = assertThrows(IllegalStateException.class, methodDefinition::jvmMethodSignature);
        assertThat(ex, hasProperty("message", is("Cannot build jvm method signature without defined parameters.")));
    }

    @Test
    public void illegalStateException_attemptToGenerateMethodSignature_noReturnTypeDefined() {
        MethodDefinition<?, ?> methodDefinition = new MethodDefinition<>(null, null, DefinitionBuilders.name("Name"), DefinitionBuilders.noParameters(), null, null);

        IllegalStateException ex = assertThrows(IllegalStateException.class, methodDefinition::jvmMethodSignature);
        assertThat(ex, hasProperty("message", is("Cannot build jvm method signature without defined return type.")));
    }

    @Test
    public void illegalStateException_noConstructorFoundInThisClass() {
        AsmClassBuilder<Object> builder = new AsmClassBuilder<>(Object.class)
            .withConstructor(constructor(publicOnly(), DefinitionBuilders.noParameters(),
                CodeBuilders.superConstructor(Object.class, DefinitionBuilders.noParameters()),
                CodeBuilders.instantiate(ThisClass.class, CodeBuilders.literal(123), CodeBuilders.literal(123)),
                CodeBuilders.returnVoid()
            ));

        IllegalStateException ex = assertThrows(IllegalStateException.class, builder::build);
        assertThat(ex, hasProperty("message", startsWith("Constructor not found in class")));
    }

    @Test
    public void illegalStateException_failedToFindMethodIntThisClass_withCorrectNumberOfParameters() {
        AsmClassBuilder<Object> builder = new AsmClassBuilder<>(Object.class)
            .withConstructor(constructor(publicOnly(), DefinitionBuilders.noParameters(),
                CodeBuilders.superConstructor(Object.class, DefinitionBuilders.noParameters()),
                CodeBuilders.this_().invoke("myOwnMethod"),
                CodeBuilders.returnVoid()
            ))
            .withMethod(method(publicOnly(), DefinitionBuilders.name("myOwnMethod"), DefinitionBuilders.parameters(int.class),
                CodeBuilders.returnVoid()
            ));

        IllegalStateException ex = assertThrows(IllegalStateException.class, builder::build);
        assertThat(ex, hasProperty("message", startsWith("Method 'myOwnMethod' not found in class")));
    }

    @Test
    public void illegalStateException_failedToFindMethodInThisClass_parameterTypesDoNotMatch() {
        AsmClassBuilder<Object> builder = new AsmClassBuilder<>(Object.class)
            .withConstructor(constructor(publicOnly(), DefinitionBuilders.noParameters(),
                CodeBuilders.superConstructor(Object.class, DefinitionBuilders.noParameters()),
                CodeBuilders.this_().invoke("myOwnMethod", CodeBuilders.this_()),
                CodeBuilders.returnVoid()
            ))
            .withMethod(method(publicOnly(), DefinitionBuilders.name("myOwnMethod"), DefinitionBuilders.parameters(int.class),
                CodeBuilders.returnVoid()
            ));

        IllegalStateException ex = assertThrows(IllegalStateException.class, builder::build);
        assertThat(ex, hasProperty("message", startsWith("Method 'myOwnMethod' not found in class")));
    }

    @Test
    public void illegalStateException_failedToFindMethodInAnotherClass_parameterTypesDoNotMatch() {
        AsmClassBuilder<Object> builder = new AsmClassBuilder<>(Object.class)
            .withConstructor(constructor(publicOnly(), DefinitionBuilders.noParameters(),
                CodeBuilders.superConstructor(Object.class, DefinitionBuilders.noParameters()),
                CodeBuilders.this_().invoke("myOwnMethod", CodeBuilders.literalObj("My String Value")),
                CodeBuilders.returnVoid()
            ))
            .withMethod(method(publicOnly(), DefinitionBuilders.name("myOwnMethod"), DefinitionBuilders.parameters(int.class),
                CodeBuilders.returnVoid()
            ));

        IllegalStateException ex = assertThrows(IllegalStateException.class, builder::build);
        assertThat(ex, hasProperty("message", startsWith("Method 'myOwnMethod' not found in class")));
    }

    @Test
    public void illegalStateException_impliedMethodOwnerIsInvalid() {
        AsmClassBuilder<Object> builder = new AsmClassBuilder<>(Object.class)
            .withConstructor(constructor(publicOnly(), DefinitionBuilders.noParameters(),
                CodeBuilders.superConstructor(Object.class, DefinitionBuilders.noParameters()),
                CodeBuilders.cast(int.class, CodeBuilders.literal(1)).invoke("someMethod"),
                CodeBuilders.returnVoid()
            ));

        IllegalStateException ex = assertThrows(IllegalStateException.class, builder::build);
        assertThat(ex, hasProperty("message", startsWith("Method owner type cannot be a primitive type.")));
    }

    public static abstract class MethodTestingType {
        public abstract String createString();
    }

    @Test
    public void successfullyCallPrivateMethodInsideOfOwnClassBeingBuilt() {
        AsmClassBuilder<MethodTestingType> builder = new AsmClassBuilder<>(MethodTestingType.class)
            .withConstructor(constructor(publicOnly(), DefinitionBuilders.noParameters(),
                CodeBuilders.superConstructor(MethodTestingType.class, DefinitionBuilders.noParameters()),
                CodeBuilders.returnVoid()
            ))
            .withMethod(method(privateOnly(), DefinitionBuilders.name("generateString"), DefinitionBuilders.parameters(String.class), DefinitionBuilders.type(String.class),
                CodeBuilders.returnValue(
                    CodeBuilders.literalObj("My Test String").invoke("concat", CodeBuilders.getVar(1))
                )
            ))
            .withMethod(method(publicOnly(), DefinitionBuilders.name("createString"), DefinitionBuilders.noParameters(), DefinitionBuilders.type(String.class),
                CodeBuilders.returnValue(
                    CodeBuilders.this_().invoke("generateString", CodeBuilders.literalObj("!"))
                )
            ));

        MethodTestingType instance = builder.buildInstance();
        assertThat(instance.createString(), is("My Test String!"));
    }

    @Test
    public void allowThisInstanceToBePassedAsParameterWhenThisInstanceIsAssignableToTheParameterType() {
        AsmClassBuilder<MethodTestingType> builder = new AsmClassBuilder<>(MethodTestingType.class)
            .withMethod(method(privateOnly(), DefinitionBuilders.name("innerPrintln"), DefinitionBuilders.parameters(MethodTestingType.class),
                //System.out.println(param1.toString())
                CodeBuilders.getStatic(System.class, "out").invoke("println",
                    CodeBuilders.getVar(1).invoke("toString")
                ),
                CodeBuilders.returnVoid()
            ))
            .withConstructor(constructor(publicOnly(), DefinitionBuilders.noParameters(),
                CodeBuilders.superConstructor(MethodTestingType.class, DefinitionBuilders.noParameters()),
                CodeBuilders.this_().invoke("innerPrintln", CodeBuilders.this_()), //this.innerPrintln(this);
                CodeBuilders.returnVoid()
            ));

        assertDoesNotThrow((ThrowingSupplier<MethodTestingType>)builder::buildInstance);
    }
}