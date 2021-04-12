package io.github.chunsinger.asmsauce.code.branch.condition;

import aj.org.objectweb.asm.Label;
import io.github.chunsinger.asmsauce.MethodBuildingContext;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CompoundCondition extends Condition {
    private final List<Condition> conditions;
    private final boolean and;

    public CompoundCondition(Condition firstCondition,
                             Condition secondCondition,
                             boolean and) {
        this(List.of(firstCondition, secondCondition), and);
    }

    private CompoundCondition(List<Condition> conditions, boolean and) {
        super(null);
        this.and = and;
        this.conditions = conditions;
    }

    @Override
    public CompoundCondition and(Condition c) {
        if(and)
            return withCondition(c);
        else
            return new CompoundCondition(this, c, true);
    }

    @Override
    public CompoundCondition or(Condition c) {
        if(and)
            return new CompoundCondition(this, c, false);
        else
            return withCondition(c);
    }

    private CompoundCondition withCondition(Condition c) {
        List<Condition> newConditions = new ArrayList<>(conditions);
        newConditions.add(c);
        return new CompoundCondition(newConditions, and);
    }

    @Override
    public CompoundCondition invert() {
        return new CompoundCondition(conditions.stream().map(Condition::invert).collect(Collectors.toList()), !and);
    }

    @Override
    public void build(MethodBuildingContext context, Label endLabel) {
        if(and)
            buildAnd(context, endLabel, endLabel, false);
        else {
            Label ifBodyLabel = new Label();
            buildOr(context, ifBodyLabel, endLabel);
            context.getMethodVisitor().visitLabel(ifBodyLabel);
        }
    }

    private void buildOr(MethodBuildingContext context, Label codeLabel, Label endLabel) {
        List<Condition> allConditions = determineAllConditions();
        int lastIndex = allConditions.size() - 1;

        //Build all conditions except for the last condition
        for(int i = 0; i < lastIndex; i++) {
            Condition condition = allConditions.get(i);
            if(condition instanceof CompoundCondition) {
                CompoundCondition compoundCondition = (CompoundCondition)condition;
                Label nextConditionLabel = new Label();
                compoundCondition.buildAnd(context, codeLabel, nextConditionLabel, true);
                context.getMethodVisitor().visitLabel(nextConditionLabel);
            }
            else {
                condition.invert().build(context, codeLabel);
            }
        }

        //Build the last condition
        Condition lastCondition = allConditions.get(lastIndex);
        if(lastCondition instanceof CompoundCondition)
            ((CompoundCondition)lastCondition).buildAnd(context, codeLabel, endLabel, false);
        else
            lastCondition.build(context, endLabel);
    }

    private void buildAnd(MethodBuildingContext context, Label codeLabel, Label endLabel, boolean nested) {
        List<Condition> allConditions = determineAllConditions();
        int lastIndex = allConditions.size() - 1;

        //Build all conditions except for the last condition
        for(int i = 0; i < lastIndex; i++) {
            Condition condition = allConditions.get(i);
            if(condition instanceof CompoundCondition) {
                Label nextConditionLabel = new Label();
                CompoundCondition compoundCondition = (CompoundCondition)condition;
                compoundCondition.buildOr(context, nextConditionLabel, codeLabel);
                context.getMethodVisitor().visitLabel(nextConditionLabel);
            }
            else {
                //In a compound-AND scenario nested inside of a compound-OR scenario, jump to the codeLabel if condition fails
                condition.build(context, endLabel);
            }
        }

        //Build the last condition
        Condition lastCondition = allConditions.get(lastIndex);
        if(lastCondition instanceof CompoundCondition)
            lastCondition.build(context, endLabel);
        else if(nested)
            lastCondition.invert().build(context, codeLabel);
        else
            lastCondition.build(context, endLabel);
    }

    private List<Condition> determineAllConditions() {
        List<Condition> allConditions = new ArrayList<>();
        for(Condition condition: conditions) {
            if(condition instanceof CompoundCondition && ((CompoundCondition)condition).and == this.and)
                allConditions.addAll(((CompoundCondition)condition).conditions);
            else
                allConditions.add(condition);
        }
        return allConditions;
    }
}