package io.github.chunsinger.asmsauce;

import aj.org.objectweb.asm.MethodVisitor;
import io.github.chunsinger.asmsauce.definitions.CompleteMethodDefinition;
import io.github.chunsinger.asmsauce.testing.BaseUnitTest;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.util.ArrayList;
import java.util.EmptyStackException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MethodBuildingContextTest extends BaseUnitTest {
    @Mock
    private MethodVisitor mockMethodVisitor;
    @Mock
    private CompleteMethodDefinition<?, ?> mockMethodDefinition;
    @Mock
    private ClassBuildingContext mockClassContext;

    private MethodBuildingContext context;

    @BeforeEach
    public void init() {
        context = new MethodBuildingContext(mockMethodVisitor, mockMethodDefinition, mockClassContext, new ArrayList<>());
    }

    @Test
    public void checkEmptyStack() {
        //Sanity check - make sure stack size is 0
        assertThat(context.stackSize(), is(0));
        //Stack should indicate it is empty
        assertThat(context.isStackEmpty(), is(true));

        //Push value to stack so it is no longer empty
        context.pushStack(DefinitionBuilders.type(Object.class));
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
        context.pushStack(DefinitionBuilders.type(Object.class));
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
        context.pushStack(DefinitionBuilders.type(Object.class));
        context.pushStack(DefinitionBuilders.type(Object.class));
        assertThat(context.stackSize(), is(2));

        //Pop 2 elements off the stack and verify stack is now empty
        context.popStack(2);
        assertThat(context.isStackEmpty(), is(true));
    }

    @Test
    public void addLocalVariableType() {
        context.addLocalType(DefinitionBuilders.type(Object.class));
        context.addLocalType(DefinitionBuilders.type(int.class));

        assertThat(context.numLocals(), is(2));
        MatcherAssert.assertThat(context, hasProperty("localTypes", Matchers.contains(
            DefinitionBuilders.type(Object.class),
            DefinitionBuilders.type(int.class)
        )));
    }

    @Test
    public void addNamedLocalVariableType() {
        context.addLocalType("myLocalVar", DefinitionBuilders.type(Object.class));
        context.addLocalType("myOtherLocalVar", DefinitionBuilders.type(int.class));

        assertThat(context.numLocals(), is(2));
        MatcherAssert.assertThat(context, hasProperty("localTypes", Matchers.contains(
            DefinitionBuilders.type(Object.class),
            DefinitionBuilders.type(int.class)
        )));
        assertThat(context.getLocalIndex("myLocalVar"), is(0));
        assertThat(context.getLocalIndex("myOtherLocalVar"), is(1));
    }

    @Test
    public void setLocalVariableType() {
        context.addLocalType(DefinitionBuilders.type(ThisClass.class));
        context.addLocalType(DefinitionBuilders.type(int.class));
        context.addLocalType(DefinitionBuilders.type(float.class));

        context.setLocalType(1, DefinitionBuilders.type(String.class));

        assertThat(context.numLocals(), is(3));
        assertThat(context, hasProperty("localTypes", contains(
            DefinitionBuilders.type(ThisClass.class),
            DefinitionBuilders.type(String.class),
            DefinitionBuilders.type(float.class)
        )));
    }

    @Test
    public void setNamedLocalVariableType() {
        context.addLocalType("myObject", DefinitionBuilders.type(ThisClass.class));
        context.addLocalType("myInt", DefinitionBuilders.type(int.class));
        context.addLocalType("myFloat", DefinitionBuilders.type(float.class));

        context.setLocalType("myInt", DefinitionBuilders.type(String.class));

        assertThat(context.numLocals(), is(3));
        assertThat(context, hasProperty("localTypes", contains(
            DefinitionBuilders.type(ThisClass.class),
            DefinitionBuilders.type(String.class),
            DefinitionBuilders.type(float.class)
        )));
        assertThat(context.getLocalType("myObject"), is(DefinitionBuilders.type(ThisClass.class)));
        MatcherAssert.assertThat(context.getLocalType("myInt"), Matchers.is(DefinitionBuilders.type(String.class)));
        MatcherAssert.assertThat(context.getLocalType("myFloat"), Matchers.is(DefinitionBuilders.type(float.class)));
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
        context.addLocalType(DefinitionBuilders.type(double.class));

        assertThat(context.numLocals(), is(2));
        MatcherAssert.assertThat(context, hasProperty("localTypes", Matchers.contains(
            DefinitionBuilders.type(double.class),
            DefinitionBuilders.type(double.class)
        )));
    }

    @Test
    public void addLocalVariableType_longValue() {
        context.addLocalType(DefinitionBuilders.type(long.class));

        assertThat(context.numLocals(), is(2));
        MatcherAssert.assertThat(context, hasProperty("localTypes", Matchers.contains(
            DefinitionBuilders.type(long.class),
            DefinitionBuilders.type(long.class)
        )));
    }

    @Test
    public void setLocalVariableType_doubleValue() {
        context.addLocalType(DefinitionBuilders.type(ThisClass.class));
        context.addLocalType(DefinitionBuilders.type(int.class));
        context.addLocalType(DefinitionBuilders.type(float.class));

        context.setLocalType(1, DefinitionBuilders.type(double.class));

        assertThat(context.numLocals(), is(3));
        assertThat(context, hasProperty("localTypes", contains(
            DefinitionBuilders.type(ThisClass.class),
            DefinitionBuilders.type(double.class),
            DefinitionBuilders.type(double.class)
        )));
    }

    @Test
    public void setLocalVariableType_longValue() {
        context.addLocalType(DefinitionBuilders.type(ThisClass.class));
        context.addLocalType(DefinitionBuilders.type(int.class));
        context.addLocalType(DefinitionBuilders.type(float.class));

        context.setLocalType(2, DefinitionBuilders.type(long.class));

        assertThat(context.numLocals(), is(4));
        assertThat(context, hasProperty("localTypes", contains(
            DefinitionBuilders.type(ThisClass.class),
            DefinitionBuilders.type(int.class),
            DefinitionBuilders.type(long.class),
            DefinitionBuilders.type(long.class)
        )));
    }

    @Test
    public void destroyScopedLocalVariablesWhenLeavingTheScopeTheyExistIn() {
        context.addLocalType("myOuterVar", DefinitionBuilders.type(Object.class));
        context.beginScope();
        context.addLocalType("myInnerVar", DefinitionBuilders.type(String.class));

        assertThat(context.numLocals(), is(2));
        MatcherAssert.assertThat(context, hasProperty("localTypes", Matchers.contains(
            DefinitionBuilders.type(Object.class),
            DefinitionBuilders.type(String.class)
        )));
        assertThat(context.getLocalIndex("myOuterVar"), is(0));
        assertThat(context.getLocalIndex("myInnerVar"), is(1));

        context.endScope();
        assertThat(context.numLocals(), is(1));
        MatcherAssert.assertThat(context, hasProperty("localTypes", Matchers.contains(
            DefinitionBuilders.type(Object.class)
        )));
        assertThat(context.getLocalIndex("myOuterVar"), is(0));

        IllegalStateException ex = assertThrows(
            IllegalStateException.class,
            () -> context.getLocalIndex("myInnerVar")
        );
        assertThat(ex, hasProperty("message", is("No local variable exists with the name myInnerVar.")));
    }
}