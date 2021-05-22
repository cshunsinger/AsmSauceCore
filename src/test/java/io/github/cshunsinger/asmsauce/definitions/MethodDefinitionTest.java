package io.github.cshunsinger.asmsauce.definitions;

import io.github.cshunsinger.asmsauce.AsmClassBuilder;
import io.github.cshunsinger.asmsauce.BaseUnitTest;
import io.github.cshunsinger.asmsauce.ThisClass;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.ThrowingSupplier;

import static io.github.cshunsinger.asmsauce.ConstructorNode.constructor;
import static io.github.cshunsinger.asmsauce.DefinitionBuilders.*;
import static io.github.cshunsinger.asmsauce.MethodNode.method;
import static io.github.cshunsinger.asmsauce.code.CodeBuilders.*;
import static io.github.cshunsinger.asmsauce.modifiers.AccessModifiers.privateOnly;
import static io.github.cshunsinger.asmsauce.modifiers.AccessModifiers.publicOnly;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class MethodDefinitionTest extends BaseUnitTest {
    @Test
    public void illegalStateException_attemptToGenerateMethodSignature_noParametersDefined() {
        MethodDefinition methodDefinition = new MethodDefinition(null, null, name("Name"), null, null, null);

        IllegalStateException ex = assertThrows(IllegalStateException.class, methodDefinition::jvmMethodSignature);
        assertThat(ex, hasProperty("message", is("Cannot build jvm method signature without defined parameters.")));
    }

    @Test
    public void illegalStateException_attemptToGenerateMethodSignature_noReturnTypeDefined() {
        MethodDefinition methodDefinition = new MethodDefinition(null, null, name("Name"), noParameters(), null, null);

        IllegalStateException ex = assertThrows(IllegalStateException.class, methodDefinition::jvmMethodSignature);
        assertThat(ex, hasProperty("message", is("Cannot build jvm method signature without defined return type.")));
    }

    @Test
    public void illegalStateException_noConstructorFoundInThisClass() {
        AsmClassBuilder<Object> builder = new AsmClassBuilder<>(Object.class)
            .withConstructor(constructor(publicOnly(), noParameters(),
                superConstructor(Object.class, noParameters()),
                instantiate(ThisClass.class, literal(123), literal(123)),
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
                this_().invoke("myOwnMethod"),
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
                this_().invoke("myOwnMethod", this_()),
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
                this_().invoke("myOwnMethod", literalObj("My String Value")),
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
                cast(int.class, literal(1)).invoke("someMethod"),
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
                    literalObj("My Test String").invoke("concat", getVar(1))
                )
            ))
            .withMethod(method(publicOnly(), name("createString"), noParameters(), type(String.class),
                returnValue(
                    this_().invoke("generateString", literalObj("!"))
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
                getStatic(System.class, "out").invoke("println",
                    getVar(1).invoke("toString")
                ),
                returnVoid()
            ))
            .withConstructor(constructor(publicOnly(), noParameters(),
                superConstructor(MethodTestingType.class, noParameters()),
                this_().invoke("innerPrintln", this_()), //this.innerPrintln(this);
                returnVoid()
            ));

        assertDoesNotThrow((ThrowingSupplier<MethodTestingType>)builder::buildInstance);
    }

    @SuppressWarnings("unused")
    public interface TestBaseInterfaceMethodsType {
        default int getSuperInterfaceSpecialValue() {
            return 5000;
        }
    }

    @SuppressWarnings("unused")
    public interface TestInterfaceMethodsType extends TestBaseInterfaceMethodsType {
        default int getOtherSpecialValue() {
            return 500;
        }

        int determineSpecialValue();
        int determineOtherSpecialValue();
    }

    @SuppressWarnings("unused")
    public static class TestBaseProtectedMethodsType {
        protected int getSuperSpecialValue() {
            return 100;
        }
    }

    @SuppressWarnings("unused")
    public static class TestProtectedMethodsType extends TestBaseProtectedMethodsType {
        protected int getSpecialValue() {
            return 10;
        }
    }

    @Test
    public void allowGeneratedClassToCallMethodsInItsSuperclassOrInterfaces() {
        AsmClassBuilder<TestInterfaceMethodsType> builder = new AsmClassBuilder<>(
            TestInterfaceMethodsType.class, TestProtectedMethodsType.class, singletonList(TestInterfaceMethodsType.class), publicOnly()
        )
            .withConstructor(constructor(publicOnly(), noParameters(),
                superConstructor(TestProtectedMethodsType.class, noParameters()),
                returnVoid()
            ))
            .withMethod(method(publicOnly(), name("determineSpecialValue"), noParameters(), type(int.class),
                //This forces the class building system to search in superclasses for methods to call
                returnValue(this_().invoke("getSpecialValue"))
            ))
            .withMethod(method(publicOnly(), name("determineOtherSpecialValue"), noParameters(), type(int.class),
                //This forces the class building system to search in the implemented interfaces for methods to call
                returnValue(this_().invoke("getOtherSpecialValue"))
            ));

        TestInterfaceMethodsType instance = builder.buildInstance();
        assertThat(instance.determineSpecialValue(), is(10));
        assertThat(instance.determineOtherSpecialValue(), is(500));
    }

    @Test
    public void allowGeneratedClassToCallMethodsInInterfacesOrSuperclasses_multipleLevelsUp() {
        AsmClassBuilder<TestInterfaceMethodsType> builder = new AsmClassBuilder<>(
            TestInterfaceMethodsType.class, TestProtectedMethodsType.class, singletonList(TestInterfaceMethodsType.class), publicOnly()
        )
            .withConstructor(constructor(publicOnly(), noParameters(),
                superConstructor(TestProtectedMethodsType.class, noParameters()),
                returnVoid()
            ))
            .withMethod(method(publicOnly(), name("determineSpecialValue"), noParameters(), type(int.class),
                //This forces the class building system to search in superclass of the superclass for methods to call
                returnValue(this_().invoke("getSuperSpecialValue"))
            ))
            .withMethod(method(publicOnly(), name("determineOtherSpecialValue"), noParameters(), type(int.class),
                //This forces the class building system to search in the implemented interfaces for methods to call
                returnValue(this_().invoke("getSuperInterfaceSpecialValue"))
            ));

        TestInterfaceMethodsType instance = builder.buildInstance();
        assertThat(instance.determineSpecialValue(), is(100));
        assertThat(instance.determineOtherSpecialValue(), is(5000));
    }
}