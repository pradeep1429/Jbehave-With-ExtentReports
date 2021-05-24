package com.mtaf.runner;

import com.mtaf.framework.core.jbehave.JbehaveExtentReporter;
import com.mtaf.framework.core.jbehave.JbehaveListener;
import com.mtaf.framework.core.jbehave.JbehaveStepCollector;
import com.mtaf.framework.core.jbehave.RootPackage;
import org.jbehave.core.Embeddable;
import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.configuration.MostUsefulConfiguration;
import org.jbehave.core.context.Context;
import org.jbehave.core.embedder.StoryControls;
import org.jbehave.core.i18n.LocalizedKeywords;
import org.jbehave.core.io.LoadFromClasspath;
import org.jbehave.core.io.StoryFinder;
import org.jbehave.core.io.UnderscoredCamelCaseResolver;
import org.jbehave.core.junit.JUnitStories;
import org.jbehave.core.model.ExamplesTableFactory;
import org.jbehave.core.model.TableTransformers;
import org.jbehave.core.parsers.RegexStoryParser;
import org.jbehave.core.reporters.ContextOutput;
import org.jbehave.core.reporters.CrossReference;
import org.jbehave.core.reporters.FilePrintStreamFactory;
import org.jbehave.core.reporters.Format;
import org.jbehave.core.reporters.StoryReporterBuilder;
import org.jbehave.core.steps.InjectableStepsFactory;
import org.jbehave.core.steps.MarkUnmatchedStepsAsPending;
import org.jbehave.core.steps.ParameterControls;
import org.jbehave.core.steps.ParameterConverters;
import org.junit.runner.RunWith;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Properties;

import static java.util.Arrays.asList;
import static org.jbehave.core.io.CodeLocations.codeLocationFromClass;

@RunWith(JbehaveListener.class)
public class JbehaveRunner extends JUnitStories {

    private String storyNamePattern = "**/*.story";

    private final CrossReference xref = new CrossReference();
    private Context context = new Context();
    private Format contextFormat = new ContextOutput(context);

    private final Configuration configuration;

    public JbehaveRunner() {
        Format[] formats = new Format[] { Format.CONSOLE,Format.HTML};
        Class<? extends Embeddable> embeddableClass = this.getClass();
        Properties viewResources = new Properties();
        viewResources.put("decorateNonHtml", "true");
        ParameterConverters parameterConverters = new ParameterConverters();
        ExamplesTableFactory examplesTableFactory = new ExamplesTableFactory(new LocalizedKeywords(),
                new LoadFromClasspath(embeddableClass), new TableTransformers());
        parameterConverters.addConverters(new ParameterConverters.DateConverter(new SimpleDateFormat("yyyy-MM-dd")),
                new ParameterConverters.ExamplesTableConverter(examplesTableFactory));
        configuration = new MostUsefulConfiguration()
                            .useStoryControls(new StoryControls().doDryRun(false).doSkipScenariosAfterFailure(false))
                            .useStoryLoader(new LoadFromClasspath(embeddableClass))
                            .useStoryParser(new RegexStoryParser(examplesTableFactory))
                            .useStoryPathResolver(new UnderscoredCamelCaseResolver())
                            .useStoryReporterBuilder(
                                    new StoryReporterBuilder()
                                            .withCodeLocation(codeLocationFromClass(embeddableClass))
                                            .withPathResolver(new FilePrintStreamFactory.ResolveToPackagedName())
                                            .withViewResources(viewResources).withReporters(new JbehaveExtentReporter()).withFormats(formats)
                                            .withFailureTrace(true).withFailureTraceCompression(true).withCrossReference(xref))
                            .useParameterConverters(parameterConverters)
                            .useParameterControls(new ParameterControls("<",">",true))
                            .useStepCollector(new MarkUnmatchedStepsAsPending());
    }

    @Override
    public Configuration configuration() {
        return configuration;
    }

    @Override
    public InjectableStepsFactory stepsFactory() {
        return JbehaveStepCollector.withStepsFromPackage(this.getRootPackage(), configuration()).andClassLoader(this.getClassLoader());
    }

    @Override
    protected List<String> storyPaths() {
        return new StoryFinder()
                .findPaths(codeLocationFromClass(this.getClass()).getFile(), asList(storyNamePattern), null);
    }

    public ClassLoader getClassLoader() {
        return Thread.currentThread().getContextClassLoader();
    }

    protected String getRootPackage() {
        return RootPackage.forPackage(this.getClass().getPackage());
    }


}
