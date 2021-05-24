package com.mtaf.framework.core.jbehave;

import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.steps.AbstractStepsFactory;
import org.jbehave.core.steps.CandidateSteps;
import org.jbehave.core.steps.InjectableStepsFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

public class JbehaveStepCollector extends AbstractStepsFactory {

    private final String rootPackage;
    private ClassLoader classLoader;

    private static Logger logger = LoggerFactory.getLogger(JbehaveStepCollector.class);

    public JbehaveStepCollector(Configuration configuration, String rootPackage, ClassLoader classLoader){
        super(configuration);
        this.classLoader = classLoader;
        this.rootPackage = rootPackage;
    }

    public List<CandidateSteps> createCandidateSteps() {
        return (List)super.createCandidateSteps().stream().map(JbehaveCandidateSteps::new).collect(Collectors.toList());
    }

    @Override
    protected List<Class<?>> stepsTypes() {
        List<Class<?>> types = new ArrayList();
        Iterator var2 = this.getCandidateClasses().iterator();

        while(var2.hasNext()) {
            Class candidateClass = (Class)var2.next();
            if (this.hasAnnotatedMethods(candidateClass)) {
                types.add(candidateClass);
            }
        }
        return types;
    }

    protected List<Class> getCandidateClasses() {
        List<Class<?>> allClassesUnderRootPackage = ClassFinder.loadClasses().withClassLoader(this.classLoader).fromPackage(this.rootPackage);
        List<Class> candidateClasses = new ArrayList();
        Iterator var3 = allClassesUnderRootPackage.iterator();

        while(var3.hasNext()) {
            Class classUnderRootPackage = (Class)var3.next();

            try {
                if (this.hasAnnotatedMethods(classUnderRootPackage)) {
                    candidateClasses.add(classUnderRootPackage);
                }
            } catch (NoClassDefFoundError var6) {
                logger.warn("Potential library conflict: " + var6.getMessage());
            }
        }

        return candidateClasses;
    }

    @Override
    public Object createInstanceOfType(Class<?> type) {
        Object stepInstance = null;
        try {
            stepInstance = type.newInstance();
            
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        
        return stepInstance;
    }

    public static JbehaveStepCollector withStepsFromPackage(String rootPackage, Configuration configuration) {
        return new JbehaveStepCollector(configuration, rootPackage, defaultClassLoader());
    }

    private static ClassLoader defaultClassLoader() {
        return Thread.currentThread().getContextClassLoader();
    }

    public InjectableStepsFactory andClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
        return this;
    }
}
