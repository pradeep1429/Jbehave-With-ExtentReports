package com.mtaf.framework.driver;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.SessionId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

public class DriverManager {

    private static DriverManager instance = null;
    private static Logger logger = LoggerFactory.getLogger(DriverManager.class);
    private static ThreadLocal<WebDriver> webDrivers = new ThreadLocal<>();
    private static ThreadLocal<String> sessionIds = new ThreadLocal<>();

    private DriverManager() {
    }

    public static DriverManager getInstance() {
        if (instance == null) {
            instance = new DriverManager();
        }
        return instance;
    }

    public static WebDriver getDriver() {
        return webDrivers.get();
    }

    public static String getSessionIds() {
        return sessionIds.get();
    }

    public static void startDriver() {
        System.setProperty("webdriver.chrome.driver","driver\\chromedriver.exe");
        ChromeOptions options = new ChromeOptions();
        options.addArguments("test-type");
        options.addArguments("start-maximized");
        WebDriver driver = new ChromeDriver(options);
        webDrivers.set(driver);
        SessionId sessionId = ((RemoteWebDriver) webDrivers.get()).getSessionId();
        sessionIds.set(sessionId.toString());
        getDriver().manage().window().maximize();
        getDriver().manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);
        logger.info("Initialized Browser:'chrome' sessionID:'{}'  threadID:'{}'", sessionId, Thread.currentThread().getName());
    }

    public static void quitDriver() {
        getDriver().quit();
        webDrivers.remove();
        sessionIds.remove();
    }
}
