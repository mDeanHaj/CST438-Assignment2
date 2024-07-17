package com.cst438.controller;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class EnrollmentControllerSystemTest {

    //    public static final String CHROME_DRIVER_FILE_LOCATION = "C:\\chromedriver.exe";
    // Haris
    public static final String CHROME_DRIVER_FILE_LOCATION =
            "/Users/hj739/Downloads/chromedriver-mac-arm64/chromedriver";
//    public static final String CHROME_DRIVER_FILE_LOCATION =
//            "C:/chromedriver-win64/chromedriver.exe";;
    public static final String URL = "http://localhost:3000";
    public static final int SLEEP_DURATION = 1000; // 1 second.

    WebDriver driver;

    @BeforeEach
    public void setUpDriver() throws Exception {
        System.setProperty("webdriver.chrome.driver", CHROME_DRIVER_FILE_LOCATION);
        ChromeOptions ops = new ChromeOptions();
        ops.addArguments("--remote-allow-origins=*");
        driver = new ChromeDriver(ops);
        driver.get(URL);
        Thread.sleep(SLEEP_DURATION);
    }

    @AfterEach
    public void terminateDriver() {
        if (driver != null) {
            driver.close();
            driver.quit();
            driver = null;
        }
    }

    @Test
    public void systemTestEnrollIntoSection() throws Exception {
        // navigate to enroll page
        WebElement we = driver.findElement(By.id("enroll"));
        we.click();
        Thread.sleep(SLEEP_DURATION);

        // select a section to enroll
        driver.findElement(By.id("sectionSelect")).click();
        Thread.sleep(SLEEP_DURATION);

        //confirm enroll
        List<WebElement> confirmButtons = driver
                .findElement(By.className("react-confirm-alert-button-group"))
                .findElements(By.tagName("button"));
        assertEquals(2, confirmButtons.size());
        confirmButtons.get(0).click();
        Thread.sleep(SLEEP_DURATION);

        // verify enrollment success
        String message = driver.findElement(By.id("message")).getText();
        assertTrue(message.startsWith("enrollment added"));

        // navigate to schedule page
        driver.findElement(By.id("schedule")).click();
        Thread.sleep(SLEEP_DURATION);

        //search for new enrollment in schedule
        driver.findElement(By.id("year")).sendKeys("2024");
        driver.findElement(By.id("semester")).sendKeys("Fall");
        driver.findElement(By.id("search")).click();
        Thread.sleep(SLEEP_DURATION);


        // verify new section appears in schedule
        WebElement row338 = driver.findElement(By.xpath("//tr[td='cst338']"));
        assertEquals("cst338",row338.findElement(By.id("courseId")).getText());

        // clean up by unenrolling
        row338.findElement(By.id("unenroll")).click();
        Thread.sleep(SLEEP_DURATION);

        // verify unenrollment success
        message = driver.findElement(By.id("message")).getText();
        assertTrue(message.startsWith("Course dropped"));
    }
}
