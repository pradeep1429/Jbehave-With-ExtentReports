package com.mtaf.framework.core.jbehave;

import com.mtaf.framework.driver.DriverManager;
import org.jbehave.core.annotations.AfterScenario;
import org.jbehave.core.annotations.AfterStories;
import org.jbehave.core.annotations.BeforeScenario;
import org.jbehave.core.annotations.BeforeStories;
import org.jbehave.core.annotations.ScenarioType;

import static com.mtaf.framework.driver.DriverManager.getDriver;
import static com.mtaf.framework.driver.DriverManager.quitDriver;

public class BeforeAfterScenario {

    @BeforeStories
    public void beforeStories() {

    }
    @BeforeScenario(uponType = ScenarioType.ANY)
    public void beforeScenario() {
        if(getDriver() == null) {
            DriverManager.startDriver();
            getDriver().manage().deleteAllCookies();
        }
    }

    @AfterScenario(uponType = ScenarioType.ANY)
    public void afterScenario(){
        if(getDriver() != null) {
            quitDriver();
        }
    }

    @AfterStories
    public void tearDown() {

    }
}
