package com.chunsinger.asmsauce.definitions;

import com.chunsinger.asmsauce.AsmClassBuilder;
import com.chunsinger.asmsauce.ThisClass;
import com.chunsinger.asmsauce.testing.BaseUnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.ThrowingSupplier;

import static com.chunsinger.asmsauce.ConstructorNode.constructor;
import static com.chunsinger.asmsauce.DefinitionBuilders.*;
import static com.chunsinger.asmsauce.MethodNode.method;
import static com.chunsinger.asmsauce.code.CodeBuilders.*;
import static com.chunsinger.asmsauce.modifiers.AccessModifiers.privateOnly;
import static com.chunsinger.asmsauce.modifiers.AccessModifiers.publicOnly;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class MethodDefinitionTest extends BaseUnitTest {
    @Test
    public void illegalStateException_attemptToGenerateMethodSignature_noParametersDefined() {
        MethodDefinition<?, ?> methodDefinition = new MethodDefinition<>(null, null, name("Name"), null, null, null);

        IllegalStateException ex = assertThrows(IllegalStateException.class, methodDefinition::jvmMethodSignature);
        assertThat(ex, hasProperty("message", is("Cannot build jvm method signature without defined parameters.")));
    }

    @Test
    public void illegalStateException_attemptToGenerateMethodSignature_noReturnTypeDefined() {
        MethodDefinition<?, ?> methodDefinition = new MethodDefinition<>(null, null, name("Name"), noParameters(), null, null);

        IllegalStateException ex = assertThrows(IllegalStateException.class, methodDefinition::jvmMethodSignature);
        assertThat(ex, hasProperty("message", is("Cannot build jvm method signature without defined return type.")));
    }

    @Test
    public void illegalStateException_noConstructorFoundInThisClass() {
        AsmClassBuilder<Object> builder = new AsmClassBuilder<>(Object.class)
            .withConstructor(constructor(publicOnly(), noParameters(),
                superConstructor(Object.class, noParameters()),
                instantiate(ThisClass.class, stackValue(123), stackValue(123)),
                returnVoid()
            ));

        IllegalStateException ex = assertThrows(IllegalStateException.class, builder::build);
        assertThat(ex, hasProperty("message", startsWith("Constructor not found in class")));
    }

    @Test
    public void illegalStateException_failedToFindMethodIntThisClass_withCorrectNumberOfParameters() {
        AsmClassBuilder<Object> builder = new AsmClassBuilder<>(Object.class)
            .withConstructor(constructor(publicOnly(), noParameters(),
                superConstructor(Object.class, noParameters()),
                thisInstance().invoke("myOwnMethod"),
                returnVoid()
            ))
            .withMethod(method(publicOnly(), name("myOwnMethod"), parameters(int.class),
                returnVoid()
            ));

        IllegalStateException ex = assertThrows(IllegalStateException.class, builder::build);
        assertThat(ex, hasProperty("message", startsWith("Method 'myOwnMethod' not found in class")));
    }

    @Test
    public void illegalStateException_failedToFindMethodInThisClass_parameterTypesDoNotMatch() {
        AsmClassBuilder<Object> builder = new AsmClassBuilder<>(Object.class)
            .withConstructor(constructor(publicOnly(), noParameters(),
                superConstructor(Object.class, noParameters()),
                thisInstance().invoke("myOwnMethod", thisInstance()),
                returnVoid()
            ))
            .withMethod(method(publicOnly(), name("myOwnMethod"), parameters(int.class),
                returnVoid()
            ));

        IllegalStateException ex = assertThrows(IllegalStateException.class, builder::build);
        assertThat(ex, hasProperty("message", startsWith("Method 'myOwnMethod' not found in class")));
    }

    @Test
    public void illegalStateException_failedToFindMethodInAnotherClass_parameterTypesDoNotMatch() {
        AsmClassBuilder<Object> builder = new AsmClassBuilder<>(Object.class)
            .withConstructor(constructor(publicOnly(), noParameters(),
                superConstructor(Object.class, noParameters()),
                thisInstance().invoke("myOwnMethod", stackObject("My String Value")),
                returnVoid()
            ))
            .withMethod(method(publicOnly(), name("myOwnMethod"), parameters(int.class),
                returnVoid()
            ));

        IllegalStateException ex = assertThrows(IllegalStateException.class, builder::build);
        assertThat(ex, hasProperty("message", startsWith("Method 'myOwnMethod' not found in class")));
    }

    @Test
    public void illegalStateException_impliedMethodOwnerIsInvalid() {
        AsmClassBuilder<Object> builder = new AsmClassBuilder<>(Object.class)
            .withConstructor(constructor(publicOnly(), noParameters(),
                superConstructor(Object.class, noParameters()),
                cast(int.class, stackValue(1)).invoke("someMethod"),
                returnVoid()
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
            .withConstructor(constructor(publicOnly(), noParameters(),
                superConstructor(MethodTestingType.class, noParameters()),
                returnVoid()
            ))
            .withMethod(method(privateOnly(), name("generateString"), parameters(String.class), type(String.class),
                returnValue(
                    stackObject("My Test String").invoke("concat", localVar(1))
                )
            ))
            .withMethod(method(publicOnly(), name("createString"), noParameters(), type(String.class),
                returnValue(
                    thisInstance().invoke("generateString", stackObject("!"))
                )
            ));

        MethodTestingType instance = builder.buildInstance();
        assertThat(instance.createString(), is("My Test String!"));
    }

    @Test
    public void allowThisInstanceToBePassedAsParameterWhenThisInstanceIsAssignableToTheParameterType() {
        AsmClassBuilder<MethodTestingType> builder = new AsmClassBuilder<>(MethodTestingType.class)
            .withMethod(method(privateOnly(), name("innerPrintln"), parameters(MethodTestingType.class),
                //System.out.println(param1.toString())
                getStaticField(System.class, "out").invoke("println",
                    localVar(1).invoke("toString")
                ),
                returnVoid()
            ))
            .withConstructor(constructor(publicOnly(), noParameters(),
                superConstructor(MethodTestingType.class, noParameters()),
                thisInstance().invoke("innerPrintln", thisInstance()), //this.innerPrintln(this);
                returnVoid()
            ));

        assertDoesNotThrow((ThrowingSupplier<MethodTestingType>)builder::buildInstance);
    }
}