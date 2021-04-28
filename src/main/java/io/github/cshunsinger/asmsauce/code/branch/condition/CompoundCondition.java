package io.github.cshunsinger.asmsauce.code.branch.condition;

import org.objectweb.asm.Label;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static io.github.cshunsinger.asmsauce.MethodBuildingContext.context;

/**
 * Condition representing multiple conditions being combined using and/or.
 */
public class CompoundCondition extends Condition {
    private final List<Condition> conditions;
    private final boolean and;

    /**
     * Creates a new CompoundCondition from two single conditions which are joined by AND or OR.
     * @param firstCondition The first condition to join.
     * @param secondCondition The second condition to join.
     * @param and Boolean representing if this compound condition is an AND condition. If true, this will be an AND
     *            condition. If false, this will be an OR condition.
     */
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
    public void build(Label endLabel) {
        if(and)
            buildAnd(endLabel, endLabel, false);
        else {
            Label ifBodyLabel = new Label();
            buildOr(ifBodyLabel, endLabel);
            context().getMethodVisitor().visitLabel(ifBodyLabel);
        }
    }

    private void buildOr(Label codeLabel, Label endLabel) {
        List<Condition> allConditions = determineAllConditions();
        int lastIndex = allConditions.size() - 1;

        //Build all conditions except for the last condition
        for(int i = 0; i < lastIndex; i++) {
            Condition condition = allConditions.get(i);
            if(condition instanceof CompoundCondition) {
                CompoundCondition compoundCondition = (CompoundCondition)condition;
                Label nextConditionLabel = new Label();
                compoundCondition.buildAnd(codeLabel, nextConditionLabel, true);
                context().getMethodVisitor().visitLabel(nextConditionLabel);
            }
            else {
                condition.invert().build(codeLabel);
            }
        }

        //Build the last condition
        Condition lastCondition = allConditions.get(lastIndex);
        if(lastCondition instanceof CompoundCondition)
            ((CompoundCondition)lastCondition).buildAnd(codeLabel, endLabel, false);
        else
            lastCondition.build(endLabel);
    }

    private void buildAnd(Label codeLabel, Label endLabel, boolean nested) {
        List<Condition> allConditions = determineAllConditions();
        int lastIndex = allConditions.size() - 1;

        //Build all conditions except for the last condition
        for(int i = 0; i < lastIndex; i++) {
            Condition condition = allConditions.get(i);
            if(condition instanceof CompoundCondition) {
                Label nextConditionLabel = new Label();
                CompoundCondition compoundCondition = (CompoundCondition)condition;
                compoundCondition.buildOr(nextConditionLabel, codeLabel);
                context().getMethodVisitor().visitLabel(nextConditionLabel);
            }
            else {
                //In a compound-AND scenario nested inside of a compound-OR scenario, jump to the codeLabel if condition fails
                condition.build(endLabel);
            }
        }

        //Build the last condition
        Condition lastCondition = allConditions.get(lastIndex);
        if(lastCondition instanceof CompoundCondition)
            lastCondition.build(endLabel);
        else if(nested)
            lastCondition.invert().build(codeLabel);
        else
            lastCondition.build(endLabel);
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