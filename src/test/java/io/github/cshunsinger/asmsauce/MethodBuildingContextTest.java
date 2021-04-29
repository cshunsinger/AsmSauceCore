package io.github.cshunsinger.asmsauce;

import org.junit.jupiter.api.AfterEach;
import org.objectweb.asm.MethodVisitor;
import io.github.cshunsinger.asmsauce.definitions.CompleteMethodDefinition;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.util.ArrayList;
import java.util.EmptyStackException;

import static io.github.cshunsinger.asmsauce.DefinitionBuilders.type;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MethodBuildingContextTest extends BaseUnitTest {
    @Mock
    private MethodVisitor mockMethodVisitor;
    @Mock
    private CompleteMethodDefinition mockMethodDefinition;
    @Mock
    private ClassBuildingContext mockClassContext;

    private MethodBuildingContext context;

    @BeforeEach
    public void init() {
        context = new MethodBuildingContext(mockMethodVisitor, mockMethodDefinition, mockClassContext, new ArrayList<>());
    }

    @AfterEach
    public void end() {
        MethodBuildingContext.reset();
    }

    @Test
    public void checkEmptyStack() {
        //Sanity check - make sure stack size is 0
        assertThat(context.stackSize(), is(0));
        //Stack should indicate it is empty
        assertThat(context.isStackEmpty(), is(true));

        //Push value to stack so it is no longer empty
        context.pushStack(type(Object.class));
        //Sanity check stack size
        assertThat(context.stackSize(), is(1));
        //Stack should no longer indicate that it is empty
        assertThat(context.isStackEmpty(), is(false));
    }

    @Test
    public void peekAndPopEmptyStack() {
        //Sanity check - make sure stack is empty
        assertThat(context.isStackEmpty(), is(true));
        //Peek and pop empty stack - expected to throw exceptions
        assertThrows(EmptyStackException.class, () -> context.peekStack());
        assertThrows(EmptyStackException.class, () -> context.popStack());
    }

    @Test
    public void performStackOperations() {
        //Sanity check to make sure stack is empty
        assertThat(context.isStackEmpty(), is(true));

        //Push to stack and verify 1 element on the stack
        context.pushStack(type(Object.class));
        assertThat(context.stackSize(), is(1));

        //Peek stack and verify the stack is unmodified
        assertThat(context.peekStack(), allOf(
            notNullValue(),
            hasProperty("type", is(Object.class))
        ));
        assertThat(context.stackSize(), is(1));

        //Pop stack and verify the stack is modified
        assertThat(context.popStack(), allOf(
            notNullValue(),
            hasProperty("type", is(Object.class))
        ));
        assertThat(context.stackSize(), is(0));
    }

    @Test
    public void popMultipleItemsFromStack() {
        //Sanity check to make sure the stack is empty
        assertThat(context.isStackEmpty(), is(true));

        //Push two elements onto the stack and verify stack size
        context.pushStack(type(Object.class));
        context.pushStack(type(Object.class));
        assertThat(context.stackSize(), is(2));

        //Pop 2 elements off the stack and verify stack is now empty
        context.popStack(2);
        assertThat(context.isStackEmpty(), is(true));
    }

    @Test
    public void addLocalVariableType() {
        context.addLocalType(type(Object.class));
        context.addLocalType(type(int.class));

        assertThat(context.numLocals(), is(2));
        assertThat(context, hasProperty("localTypes", Matchers.contains(
            type(Object.class),
            type(int.class)
        )));
    }

    @Test
    public void setLocalVariableType() {
        context.addLocalType(type(ThisClass.class));
        context.addLocalType(type(int.class));
        context.addLocalType(type(float.class));

        context.setLocalType(1, type(String.class));

        assertThat(context.numLocals(), is(3));
        assertThat(context, hasProperty("localTypes", contains(
            type(ThisClass.class),
            type(String.class),
            type(float.class)
        )));
    }

    @Test
    public void setNamedLocalVariableType() {
        context.setLocalType("myObject", type(ThisClass.class));
        context.setLocalType("myInt", type(int.class));
        context.setLocalType("myFloat", type(float.class));

        context.setLocalType("myInt", type(String.class));

        assertThat(context.numLocals(), is(3));
        assertThat(context, hasProperty("localTypes", contains(
            type(ThisClass.class),
            type(String.class),
            type(float.class)
        )));
        assertThat(context.getLocalType("myObject"), is(type(ThisClass.class)));
        assertThat(context.getLocalType("myInt"), Matchers.is(type(String.class)));
        assertThat(context.getLocalType("myFloat"), Matchers.is(type(float.class)));
    }

    @Test
    public void illegalStateException_attemptingToGetLocalVariableIndex_localVariableNameDoesNotExist() {
        IllegalStateException ex = assertThrows(
            IllegalStateException.class,
            () -> context.getLocalIndex("whatUp")
        );

        assertThat(ex, hasProperty("message", is("No local variable exists with the name whatUp.")));
    }

    @Test
    public void addLocalVariableType_doubleValue() {
        context.addLocalType(type(double.class));

        assertThat(context.numLocals(), is(2));
        assertThat(context, hasProperty("localTypes", Matchers.contains(
            type(double.class),
            type(double.class)
        )));
    }

    @Test
    public void addLocalVariableType_longValue() {
        context.addLocalType(type(long.class));

        assertThat(context.numLocals(), is(2));
        assertThat(context, hasProperty("localTypes", Matchers.contains(
            type(long.class),
            type(long.class)
        )));
    }

    @Test
    public void setLocalVariableType_doubleValue() {
        context.addLocalType(type(ThisClass.class));
        context.addLocalType(type(int.class));
        context.addLocalType(type(float.class));

        context.setLocalType(1, type(double.class));

        assertThat(context.numLocals(), is(3));
        assertThat(context, hasProperty("localTypes", contains(
            type(ThisClass.class),
            type(double.class),
            type(double.class)
        )));
    }

    @Test
    public void setLocalVariableType_longValue() {
        context.addLocalType(type(ThisClass.class));
        context.addLocalType(type(int.class));
        context.addLocalType(type(float.class));

        context.setLocalType(2, type(long.class));

        assertThat(context.numLocals(), is(4));
        assertThat(context, hasProperty("localTypes", contains(
            type(ThisClass.class),
            type(int.class),
            type(long.class),
            type(long.class)
        )));
    }

    @Test
    public void destroyScopedLocalVariablesWhenLeavingTheScopeTheyExistIn() {
        context.setLocalType("myOuterVar", type(Object.class));
        context.beginScope();
        context.setLocalType("myInnerVar", type(String.class));

        assertThat(context.numLocals(), is(2));
        assertThat(context, hasProperty("localTypes", Matchers.contains(
            type(Object.class),
            type(String.class)
        )));
        assertThat(context.getLocalIndex("myOuterVar"), is(0));
        assertThat(context.getLocalIndex("myInnerVar"), is(1));

        context.endScope();
        assertThat(context.numLocals(), is(1));
        assertThat(context, hasProperty("localTypes", Matchers.contains(
            type(Object.class)
        )));
        assertThat(context.getLocalIndex("myOuterVar"), is(0));

        IllegalStateException ex = assertThrows(
            IllegalStateException.class,
            () -> context.getLocalIndex("myInnerVar")
        );
        assertThat(ex, hasProperty("message", is("No local variable exists with the name myInnerVar.")));
    }

    @Test
    public void illegalStateException_attemptingToAccessMethodBuildingContext_whenNotBuilding() {
        MethodBuildingContext.reset();
        IllegalStateException ex = assertThrows(IllegalStateException.class, MethodBuildingContext::context);
        assertThat(ex, hasProperty("message",
            is("Context must be accessed from within a method building scope.")
        ));
    }

    @Test
    public void accessMethodBuildingContext_whenOneExistsAndIsActive() {
        //context is instantiated in the @BeforeEach call, therefore it should be the value currently stored as
        //the active context.
        assertThat(MethodBuildingContext.context(), is(context));
    }
}