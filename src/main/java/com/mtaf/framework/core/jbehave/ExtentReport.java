package com.mtaf.framework.core.jbehave;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.reporter.ExtentHtmlReporter;

public class ExtentReport {

    public ExtentHtmlReporter htmlReporter;
    public ExtentReports extent;
    public ExtentTest test;
    private static ExtentReport report;

    private ExtentReport() {}

    public static ExtentReport getExtentReport() {
        if(report==null) {
            report = new ExtentReport();
        }
        return report;
    }

    public ExtentTest getCurrentExtentTest() {
        return test;
    }

    public ExtentTest setCurrentExtentTest(ExtentTest newTest) {
        this.test=newTest;
        return test;
    }

    public void startReport(String storyName) {
        htmlReporter = new ExtentHtmlReporter("target/html-report/"+storyName+"_Report.html");
        extent = new ExtentReports();
        extent.attachReporter(htmlReporter);
        htmlReporter.config().setDocumentTitle("Collab::Test Automation Report");
    }

    public ExtentTest createTest(String scenarioName) {
        return extent.createTest(scenarioName);
    }

    public ExtentTest createNode(String step, ExtentTest testArg) {
        this.test = testArg.createNode(step);
        return test;
    }

    public ExtentTest createNode(String step, String description, ExtentTest testArg) {
        this.test = testArg.createNode(step, description);
        return test;
    }

    public void generateReports() {
        extent.flush();
    }

}
