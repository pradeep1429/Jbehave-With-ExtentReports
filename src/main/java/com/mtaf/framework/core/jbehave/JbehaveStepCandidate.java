package com.mtaf.framework.core.jbehave;

import org.jbehave.core.configuration.Keywords;
import org.jbehave.core.parsers.RegexPrefixCapturingPatternParser;
import org.jbehave.core.steps.InjectableStepsFactory;
import org.jbehave.core.steps.ParameterControls;
import org.jbehave.core.steps.ParameterConverters;
import org.jbehave.core.steps.Step;
import org.jbehave.core.steps.StepCandidate;
import org.jbehave.core.steps.StepType;
import org.jbehave.core.steps.context.StepsContext;

import java.lang.reflect.Method;
import java.util.Map;

public class JbehaveStepCandidate extends StepCandidate {

    private final StepCandidate stepCandidate;

    public JbehaveStepCandidate(StepCandidate stepCandidate) {
        super(stepCandidate.getPatternAsString(),
                stepCandidate.getPriority(),
                stepCandidate.getStepType(),
                stepCandidate.getMethod(),
                (Class) ExtractClass.field("stepsType").from(stepCandidate),
                (InjectableStepsFactory)ExtractClass.field("stepsFactory").from(stepCandidate),
                new StepsContext(),
                (Keywords)ExtractClass.field("keywords").from(stepCandidate),
                new RegexPrefixCapturingPatternParser(),
                new ParameterConverters(),
                new ParameterControls());
        this.composedOf(stepCandidate.composedSteps());
        this.stepCandidate = stepCandidate;
    }

    public Method getMethod() {
        return this.stepCandidate.getMethod();
    }

    public Integer getPriority() {
        return this.stepCandidate.getPriority();
    }

    public String getPatternAsString() {
        return this.stepCandidate.getPatternAsString();
    }

    public StepType getStepType() {
        return this.stepCandidate.getStepType();
    }

    public String getStartingWord() {
        return this.stepCandidate.getStartingWord();
    }

    public boolean isComposite() {
        return this.stepCandidate.isComposite();
    }

    public String[] composedSteps() {
        return this.stepCandidate.composedSteps();
    }

    public boolean ignore(String stepAsString) {
        return this.stepCandidate.ignore(stepAsString);
    }

    public boolean isPending() {
        return this.stepCandidate.isPending();
    }

    public boolean matches(String stepAsString) {
        return this.stepCandidate.matches(stepAsString);
    }

    public boolean matches(String step, String previousNonAndStep) {
        return this.stepCandidate.matches(step, previousNonAndStep);
    }

    public Step createMatchedStep(String stepAsString, Map<String, String> namedParameters) {
        return this.stepCandidate.createMatchedStep(stepAsString, namedParameters);
    }

    public String toString() {
        return this.stepCandidate.toString();
    }
}
