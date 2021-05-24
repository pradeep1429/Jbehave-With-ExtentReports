package com.mtaf.steps;


import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;

public class HomePageSteps {
    public WebDriver driver = null;

    public HomePageSteps(WebDriver driver){
        this.driver = driver;
    }

    public void openHomePage() {
        System.out.println("Story started");
        driver.get("https://duckduckgo.com/");
        System.out.println("Title is:" +driver.getTitle());
        assertEquals("Wrong title!", "DuckDuckGo — Privacy, simplified.", driver.getTitle());
        System.out.println("**********first step executed*******");
    }

    public void searchKeyword(String  searchword){
        driver.findElement(By.id("search_form_input_homepage")).clear();
        driver.findElement(By.id("search_form_input_homepage")).sendKeys(searchword);
    }

    public void clickSearch()  throws InterruptedException {
        driver.findElement(By.id("search_button_homepage")).click();
        Thread.sleep(2000);
    }

    public List<String> searchResults(){
        List<WebElement> ele = driver.findElements(By.xpath("//*[@class='result__snippet js-result-snippet']"));
       return ele.stream().map(WebElement::getText).collect(Collectors.toList());
    }
}
