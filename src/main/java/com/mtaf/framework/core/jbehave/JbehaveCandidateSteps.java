package com.mtaf.framework.core.jbehave;

import org.jbehave.core.annotations.ScenarioType;
import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.steps.BeforeOrAfterStep;
import org.jbehave.core.steps.CandidateSteps;
import org.jbehave.core.steps.StepCandidate;

import java.util.List;
import java.util.stream.Collectors;

public class JbehaveCandidateSteps implements CandidateSteps {

    private final CandidateSteps candidateSteps;

    JbehaveCandidateSteps(CandidateSteps candidateSteps) {
        this.candidateSteps = candidateSteps;
    }

    @Override
    public List<StepCandidate> listCandidates() {
        return (List)this.candidateSteps.listCandidates().parallelStream().map(JbehaveStepCandidate::new).collect(Collectors.toList());
    }

    @Override
    public List<BeforeOrAfterStep> listBeforeOrAfterStories() {
        return this.candidateSteps.listBeforeOrAfterStories();
    }

    @Override
    public List<BeforeOrAfterStep> listBeforeOrAfterStory(boolean givenStory) {
        return this.candidateSteps.listBeforeOrAfterStory(givenStory);
    }

    @Override
    public List<BeforeOrAfterStep> listBeforeOrAfterScenario(ScenarioType type) {
        return this.candidateSteps.listBeforeOrAfterScenario(type);
    }

    @Override
    public Configuration configuration() {
        return this.candidateSteps.configuration();
    }
}
