package com.mtaf.framework.core.jbehave;

import com.github.valfirst.jbehave.junit.monitoring.JUnitDescriptionGenerator;
import com.github.valfirst.jbehave.junit.monitoring.JUnitReportingRunner;
import com.github.valfirst.jbehave.junit.monitoring.JUnitScenarioReporter;
import com.github.valfirst.jbehave.junit.monitoring.StoryPathsExtractor;
import lombok.extern.slf4j.Slf4j;
import org.jbehave.core.ConfigurableEmbedder;
import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.embedder.Embedder;
import org.jbehave.core.embedder.PerformableTree;
import org.jbehave.core.failures.BatchFailures;
import org.jbehave.core.reporters.Format;
import org.jbehave.core.reporters.StoryReporterBuilder;
import org.jbehave.core.steps.CandidateSteps;
import org.jbehave.core.steps.InjectableStepsFactory;
import org.jbehave.core.steps.NullStepMonitor;
import org.jbehave.core.steps.StepMonitor;
import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.notification.RunNotifier;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;

@Slf4j
public class JbehaveReportingRunner extends Runner {

    private List<Description> storyDescriptions;
    private Embedder configuredEmbedder;
    private List<String> storyPaths;
    private Configuration configuration;
    private Description description;
    List<CandidateSteps> candidateSteps;
    private final ConfigurableEmbedder configurableEmbedder;
    private final Class<? extends ConfigurableEmbedder> testClass;
    private boolean runningInMaven;
    private int testCount;

    public JbehaveReportingRunner(Class<? extends ConfigurableEmbedder> testClass) throws Throwable {
        this(testClass, (ConfigurableEmbedder)testClass.newInstance());
    }

    public JbehaveReportingRunner(Class<? extends ConfigurableEmbedder> testClass, ConfigurableEmbedder embedder){
        this.testCount = 0;
        this.configurableEmbedder = embedder;
        this.testClass = testClass;
    }

    protected List<Description> getDescriptions() {
        if (this.storyDescriptions == null) {
            this.storyDescriptions = this.buildDescriptionFromStories();
        }
        return this.storyDescriptions;
    }

    @Override
    public Description getDescription() {
        if (this.description == null) {
            this.description = Description.createSuiteDescription(this.configurableEmbedder.getClass());
            Iterator desc = this.getDescriptions().iterator();

            while(desc.hasNext()) {
                Description childDescription = (Description)desc.next();
                this.description.addChild(childDescription);
            }
        }

        return this.description;
    }

    @Override
    public void run(RunNotifier notifier) {
        this.getConfiguredEmbedder().embedderControls().doIgnoreFailureInView(true);
        this.getConfiguredEmbedder().embedderControls().doIgnoreFailureInStories(false);
        this.getConfiguredEmbedder().embedderControls().useStoryTimeouts("60");
        this.getConfiguredEmbedder().embedderControls().useThreads(1);


        JUnitScenarioReporter junitReporter = new JUnitScenarioReporter(notifier, this.testCount(), this.getDescription(), this.getConfiguredEmbedder().configuration().keywords());
        junitReporter.usePendingStepStrategy(this.getConfiguration().pendingStepStrategy());
        JUnitReportingRunner.recommendedControls(this.getConfiguredEmbedder());
        this.addToStoryReporterFormats(junitReporter);

        try {
            this.getConfiguredEmbedder().runStoriesAsPaths(this.getStoryPaths());
        } catch (Throwable e) {
            throw new RuntimeException(e);
        } finally {
            this.getConfiguredEmbedder().generateCrossReference();
        }

    }

    protected Configuration getConfiguration() {
        if (this.configuration == null) {
            this.configuration = this.getConfiguredEmbedder().configuration();
        }

        return this.configuration;
    }

    public Embedder getConfiguredEmbedder() {
        if (this.configuredEmbedder == null) {
            this.configuredEmbedder = this.configurableEmbedder.configuredEmbedder();
        }

        return this.configuredEmbedder;
    }

    private void addToStoryReporterFormats(JUnitScenarioReporter junitReporter) {
        StoryReporterBuilder storyReporterBuilder = this.getConfiguration().storyReporterBuilder();
        StoryReporterBuilder.ProvidedFormat junitReportFormat = new StoryReporterBuilder.ProvidedFormat(junitReporter);
        storyReporterBuilder.withFormats(new Format[]{junitReportFormat});
    }

    List<String> getStoryPaths() {
        if (this.storyPaths == null || this.storyPaths.isEmpty()) {
            this.storyPaths = this.storyPathsFromRunnerClass();
        }

        return this.storyPaths;
    }

    private List<String> storyPathsFromRunnerClass() {
        try {
            List<String> storyPaths = (new StoryPathsExtractor(this.configurableEmbedder)).getStoryPaths();
            String storyFilter = this.getStoryFilterFrom(this.configurableEmbedder);
            return (List)storyPaths.stream().filter((story) -> {
                return story.matches(storyFilter);
            }).collect(Collectors.toList());
        } catch (Throwable var3) {
            log.error("Could not load story paths");
            return Collections.emptyList();
        }
    }

    private String getStoryFilterFrom(ConfigurableEmbedder embedder) {
        String defaultStoryFilter = ".*";
        Optional<Method> getStoryFilter = Arrays.stream(embedder.getClass().getMethods()).filter((method) -> {
            return method.getName().equals("getStoryFilter");
        }).findFirst();
        if (getStoryFilter.isPresent()) {
            try {
                Optional<Object> storyFilterValue = Optional.ofNullable(((Method)getStoryFilter.get()).invoke(embedder));
                return storyFilterValue.orElse(defaultStoryFilter).toString();
            } catch (InvocationTargetException | IllegalAccessException var5) {
                log.warn("Could not invoke getStoryFilter() method on {}");
            }
        }
        return defaultStoryFilter;
    }

    private List<Description> buildDescriptionFromStories() {
        List<CandidateSteps> candidateSteps = this.getCandidateSteps();
        JUnitDescriptionGenerator descriptionGenerator = new JUnitDescriptionGenerator(candidateSteps, this.getConfiguration());
        List<Description> storyDescriptions = new ArrayList();
        this.addSuite(storyDescriptions, "BeforeStories");
        PerformableTree performableTree = this.createPerformableTree(candidateSteps, this.getStoryPaths());
        storyDescriptions.addAll(descriptionGenerator.createDescriptionFrom(performableTree));
        this.addSuite(storyDescriptions, "AfterStories");
        return storyDescriptions;
    }

    List<CandidateSteps> getCandidateSteps() {
        if (this.candidateSteps == null) {
            StepMonitor originalStepMonitor = this.createCandidateStepsWithNoMonitor();
            this.createCandidateStepsWith(originalStepMonitor);
        }

        return this.candidateSteps;
    }

    private void createCandidateStepsWith(StepMonitor stepMonitor) {
        this.getConfiguration().useStepMonitor(stepMonitor);
        this.candidateSteps = this.buildCandidateSteps();
        this.candidateSteps.forEach((step) -> {
            step.configuration().useStepMonitor(stepMonitor);
        });
    }

    private StepMonitor createCandidateStepsWithNoMonitor() {
        StepMonitor usedStepMonitor = this.getConfiguration().stepMonitor();
        this.createCandidateStepsWith(new NullStepMonitor());
        return usedStepMonitor;
    }

    private List<CandidateSteps> buildCandidateSteps() {
        InjectableStepsFactory stepsFactory = this.configurableEmbedder.stepsFactory();
        List candidateSteps;
        if (stepsFactory != null) {
            candidateSteps = stepsFactory.createCandidateSteps();
        } else {
            Embedder embedder = this.getConfiguredEmbedder();
            candidateSteps = embedder.candidateSteps();
            if (candidateSteps == null || candidateSteps.isEmpty()) {
                candidateSteps = embedder.stepsFactory().createCandidateSteps();
            }
        }

        return candidateSteps;
    }

    private void addSuite(List<Description> storyDescriptions, String name) {
        storyDescriptions.add(Description.createTestDescription(Object.class, name));
    }

    private PerformableTree createPerformableTree(List<CandidateSteps> candidateSteps, List<String> storyPaths) {
        Embedder configuredEmbedder = this.getConfiguredEmbedder();
        configuredEmbedder.useMetaFilters(asList("+author *", "+priority *","-skip"));
        BatchFailures failures = new BatchFailures(configuredEmbedder.embedderControls().verboseFailures());
        PerformableTree performableTree = configuredEmbedder.performableTree();
        PerformableTree.RunContext context = performableTree.newRunContext(this.getConfiguration(), candidateSteps, configuredEmbedder.embedderMonitor(), configuredEmbedder.metaFilter(), failures);
        performableTree.addStories(context, configuredEmbedder.storyManager().storiesOfPaths(storyPaths));
        return performableTree;
    }

}
