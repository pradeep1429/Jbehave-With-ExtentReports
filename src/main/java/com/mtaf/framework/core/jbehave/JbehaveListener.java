package com.mtaf.framework.core.jbehave;

import org.jbehave.core.ConfigurableEmbedder;
import org.jbehave.core.configuration.Configuration;
import org.junit.Test;
import org.junit.runner.notification.RunNotifier;

public class JbehaveListener extends JbehaveReportingRunner {


    public JbehaveListener(Class<? extends ConfigurableEmbedder> testClass) throws Throwable {
        super(testClass);
    }

    @Override
    protected Configuration getConfiguration() {
        final Configuration configuration = super.getConfiguration();
        return configuration;
    }

    @Test
    @Override
    public void run(final RunNotifier notifier) {
        super.run(notifier);
    }
}
