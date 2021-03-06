package io.github.cshunsinger.asmsauce;

import io.github.cshunsinger.asmsauce.code.CodeBuilders;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.junit.jupiter.api.Test;

import java.io.PrintStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import static io.github.cshunsinger.asmsauce.ConstructorNode.constructor;
import static io.github.cshunsinger.asmsauce.DefinitionBuilders.*;
import static io.github.cshunsinger.asmsauce.FieldNode.field;
import static io.github.cshunsinger.asmsauce.MethodNode.method;
import static io.github.cshunsinger.asmsauce.code.CodeBuilders.*;
import static io.github.cshunsinger.asmsauce.modifiers.AccessModifiers.privateOnly;
import static io.github.cshunsinger.asmsauce.modifiers.AccessModifiers.publicOnly;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * These are very basic tests for making sure the class building system works in general.
 */
public class AsmClassBuilderTest extends BaseUnitTest {
    @Getter
    @RequiredArgsConstructor
    public static abstract class AsmTestBaseType {
        protected final String baseString;
    }

    public interface AsmTestInterface {
        void setFirstOperand(int i); //Sets the first operand
        void setSecondOperand(int i); //Sets the second operand
        int getSum(); //Adds the two operands together
    }

    @Test
    public void createBasicInheritingClassWithMinimalConstructor() throws NoSuchMethodException {
        AsmClassBuilder<AsmTestBaseType> asmClassBuilder = new AsmClassBuilder<>(AsmTestBaseType.class, publicOnly());

        Constructor<AsmTestBaseType> baseConstructor = AsmTestBaseType.class.getConstructor(String.class);

        asmClassBuilder.withConstructor(constructor(publicOnly(), parameters(String.class), //public AsmTestBaseType(String str1)
            superConstructor(baseConstructor, getVar(1)), //super(str1);
            returnVoid() //return;
        ));

        AsmTestBaseType instance = asmClassBuilder.buildInstance("Test String");
        assertThat(instance, hasProperty("baseString", is("Test String")));
    }

    @Test
    public void createBasicInheritingClassWithMultipleImplementedConstructors() throws NoSuchMethodException {
        AsmClassBuilder<AsmTestBaseType> asmClassBuilder = new AsmClassBuilder<>(AsmTestBaseType.class, publicOnly());

        Constructor<AsmTestBaseType> baseConstructor = AsmTestBaseType.class.getConstructor(String.class);

        asmClassBuilder
            .withConstructor(constructor(publicOnly(), parameters(String.class), //public AsmTestBaseType(String str1)
                superConstructor(baseConstructor, getVar(1)), //super(str1);
                returnVoid() //return;
            ))
            .withConstructor(constructor(publicOnly(), noParameters(), //public AsmTestBaseType()
                thisConstructor(parameters(String.class), literalObj("Look ma! No hands!!")), //this("Look ma! No hands!!");
                returnVoid() //return;
            ));

        AsmTestBaseType instanceArgs = asmClassBuilder.buildInstance("Testing With Constructor Params");
        assertThat(instanceArgs, hasProperty("baseString", is("Testing With Constructor Params")));

        AsmTestBaseType instanceNoArgs = asmClassBuilder.buildInstance();
        assertThat(instanceNoArgs, hasProperty("baseString", is("Look ma! No hands!!")));
    }

    @Test
    public void createInheritingClassWithConstructorAndMethodOverride() {
        AsmClassBuilder<AsmTestBaseType> asmClassBuilder = new AsmClassBuilder<>(AsmTestBaseType.class);

        asmClassBuilder
            .withConstructor(constructor(publicOnly(), parameters(String.class), //public AsmTestBaseType(String str1)
                superConstructor(AsmTestBaseType.class, parameters(String.class), getVar(1)),  //super(str1);
                returnVoid()                                                                               //return;
            ))
            .withMethod(method(publicOnly(), name("getBaseString"), noParameters(), type(String.class), //public String getBaseString()
                returnValue( //return this.baseString.concat(this.baseString)
                    this_()
                        .getField(type(AsmTestBaseType.class), name("baseString"), type(String.class))
                        .invoke(String.class, MethodUtils.getAccessibleMethod(String.class, "concat", String.class),
                            this_().getField(type(AsmTestBaseType.class), name("baseString"), type(String.class))
                        )
                )
            ));

        AsmTestBaseType instance = asmClassBuilder.buildInstance("Test");
        assertThat(instance.getBaseString(), is("TestTest"));
    }

    @Test
    public void createClassWithFieldsThatImplementsAnInterface() {
        String newClassName = AsmTestInterface.class.getName() + "Impl";

        AsmClassBuilder<AsmTestInterface> asmClassBuilder = new AsmClassBuilder<>(
            newClassName,
            AsmTestInterface.class, //The reference type used by the builder
            Object.class, //Generated class extends from Object.class
            singletonList(AsmTestInterface.class), //Generate class implementing this interface
            publicOnly() //Generate public class
        );

        asmClassBuilder
            .withField(field(privateOnly(), type(int.class), name("firstOperand")))
            .withField(field(privateOnly(), type(int.class), name("secondOperand")))
            .withConstructor(constructor(publicOnly(), noParameters(), //public Constructor()
                CodeBuilders.thisConstructor(parameters(int.class, int.class), //this(0, 0);
                    literal(0),
                    literal(0)
                ),
                returnVoid() //return;
            ))
            .withConstructor(constructor(publicOnly(), parameters(int.class, int.class), //public Constructor(int first, int second)
                superConstructor(Object.class, noParameters()), //super();
                this_() //this.firstOperand = first;
                    .assignField(type(ThisClass.class), name("firstOperand"), type(int.class),
                        getVar(1)
                    ),
                this_() //this.secondOperand = second;
                    .assignField(type(ThisClass.class), name("secondOperand"), type(int.class),
                        getVar(2)
                    ),
                returnVoid() //return;
            ))
            .withMethod(method(publicOnly(), name("setFirstOperand"), parameters(int.class), //public void setFirstOperand(int i)
                this_() //this.firstOperand = i;
                    .assignField(type(ThisClass.class), name("firstOperand"), type(int.class),
                        getVar(1)
                    ),
                returnVoid() //return;
            ))
            .withMethod(method(publicOnly(), name("setSecondOperand"), parameters(int.class), //public void setSecondOperand(int i)
                this_() //this.secondOperand = i;
                    .assignField(type(ThisClass.class), name("secondOperand"), type(int.class),
                        getVar(1)
                    ),
                returnVoid() //return;
            ))
            .withMethod(method(publicOnly(), name("getSum"), noParameters(), type(int.class),
                returnValue( //return this.firstOperand + this.secondOperand;
                    this_()
                        .getField(type(ThisClass.class), name("firstOperand"), type(int.class))
                        .add(this_()
                            .getField(type(ThisClass.class), name("secondOperand"), type(int.class))
                        )
                )
            ));

        //Create instance with no-args constructor
        AsmTestInterface instance = asmClassBuilder.buildInstance();
        assertThat(instance.getSum(), is(0)); //operands are 0 by default
        instance.setFirstOperand(10);
        instance.setSecondOperand(5);
        assertThat(instance.getSum(), is(15));

        //Create instance with args constructor
        instance = asmClassBuilder.buildInstance(1, 2);
        assertThat(instance.getSum(), is(3));
        instance.setFirstOperand(10);
        instance.setSecondOperand(5);
        assertThat(instance.getSum(), is(15));
    }

    @Test
    public void throwExceptionWhenClassHasNoConstructor() {
        AsmClassBuilder<AsmTestBaseType> asmClassBuilder = new AsmClassBuilder<>(AsmTestBaseType.class);
        IllegalStateException ex = assertThrows(IllegalStateException.class, asmClassBuilder::build);
        assertThat(ex, hasProperty("message", is("Newly built class must be supplied at least 1 constructor.")));
    }

    @Test
    public void throwExceptionWhenNoConstructorFoundForParameters() {
        AsmClassBuilder<AsmTestBaseType> builder = new AsmClassBuilder<>(AsmTestBaseType.class)
            .withConstructor(constructor(publicOnly(), noParameters(), //public NewAsmTestBaseType()
                superConstructor(AsmTestBaseType.class, parameters(String.class), literalObj("Str")), //super("Str");
                returnVoid() //return;
            ));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> builder.buildInstance("Parameter1", "Parameter2", "Parameter3"));
        assertThat(exception, hasProperty("message", is("No constructor found for given parameters.")));
    }

    public abstract static class StaticsTestType {
        public static String LAST_PRINTED = null;

        public abstract void printText(String text);
    }

    @Test
    public void performSystemOutPrintCallUsingCodeBuilders_andAutomaticallyGeneratedEmptyConstructor() {
        Method printlnMethod = MethodUtils.getAccessibleMethod(PrintStream.class, "println", String.class);

        AsmClassBuilder<StaticsTestType> builder = new AsmClassBuilder<>(StaticsTestType.class)
            .withMethod(method(publicOnly(), name("printText"), parameters(String.class),
                getStatic(type(System.class), name("out"), type(PrintStream.class))
                    .invoke(PrintStream.class, printlnMethod,
                        getVar(1)
                    ),
                CodeBuilders.setStatic(type(StaticsTestType.class), name("LAST_PRINTED"), type(String.class),
                    getVar(1)
                ),
                returnVoid()
            ));

        StaticsTestType instance = builder.buildInstance();
        instance.printText("CLAYTON!!!!!!!!!!!!!!");
        assertThat(StaticsTestType.LAST_PRINTED, is("CLAYTON!!!!!!!!!!!!!!"));
    }
}