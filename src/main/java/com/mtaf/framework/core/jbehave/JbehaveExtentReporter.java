package com.mtaf.framework.core.jbehave;

import com.aventstack.extentreports.ExtentTest;
import lombok.SneakyThrows;
import org.jbehave.core.model.Scenario;
import org.jbehave.core.model.Story;
import org.jbehave.core.reporters.NullStoryReporter;

import java.io.IOException;


public class JbehaveExtentReporter extends NullStoryReporter{

    ExtentReport extentReport = ExtentReport.getExtentReport();
    ExtentTest extentTest;
    ExtentTest logger;
    ThreadLocal<Story> currentStory = new ThreadLocal<Story>();
    ThreadLocal<Scenario> currentScenario = new ThreadLocal<Scenario>();


    @Override
    public void beforeStory(Story story, boolean givenStory) {
        currentStory.set(story);
        if(!(currentStory.get().getName().contains("BeforeStories")) && !(currentStory.get().getName().contains("AfterStories"))) {
            extentReport.startReport(currentStory.get().getName().substring(0, currentStory.get().getName().lastIndexOf(".")));
            System.out.println(currentStory.get().getName() +" Story Started");
        }
    }

    @Override
    public void beforeScenario(Scenario scenario) {
        if(null!=currentStory.get() && currentScenario.get()==null)
            currentScenario.set(scenario);
        extentTest = extentReport.createTest(currentScenario.get().getTitle());
        System.out.println(currentScenario.get().getTitle() +" Scenario Started");
    }

    @SneakyThrows
    @Override
    public void beforeStep(String step) {
        extentReport.createNode(step,"step description",extentTest);
    }

    @Override
    public void afterScenario() {
        System.out.println(currentScenario.get().getTitle() +" Scenario Ended");
        currentScenario.set(null);
    }

    @Override
    public void afterStory(boolean givenOrRestartingStory) {
        System.out.println(currentStory.get().getName() +" Story Completed");
        if(!(currentStory.get().getName().contains("BeforeStories")) && !(currentStory.get().getName().contains("AfterStories")))
            extentReport.generateReports();
        currentStory.set(null);
    }

    @Override
    public void successful(String step) {
        System.out.println(step+" [PASSED]");
    }

    @Override
    public void pending(String step) {
        System.out.println(step+" [PENDING]");
    }

    @Override
    public void notPerformed(String step) {
        extentReport.createNode(step, extentTest);
        extentReport.getCurrentExtentTest().warning("[NOT PERFORMED]");
    }

    @Override
    public void failed(String step, Throwable cause) {
        try {
            extentReport.getCurrentExtentTest().addScreenCaptureFromPath("target/Screenshots",step).fail(cause);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
