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
public static final String CHROME_DRIVER_FILE_LOCATION =
        "/Users/hj739/Downloads/chromedriver-mac-arm64/chromedriver";
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
        WebElement section = driver.findElement(By.id("sectionSelect"));
        section.sendKeys("1"); // replace with sectionNo
        WebElement studentId = driver.findElement(By.id("studentId"));
        studentId.sendKeys("1"); // replace with studentId
        WebElement enrollButton = driver.findElement(By.id("enrollButton"));
        enrollButton.click();
        Thread.sleep(SLEEP_DURATION);

        // verify enrollment success
        String message = driver.findElement(By.id("enrollMessage")).getText();
        assertTrue(message.contains("Enrollment successful"));

        // navigate to schedule page
        WebElement scheduleLink = driver.findElement(By.id("schedule"));
        scheduleLink.click();
        Thread.sleep(SLEEP_DURATION);

        // verify new section appears in schedule
        WebElement newSection = driver.findElement(By.xpath("//tr[td='1']")); // replace with sectionNo
        assertNotNull(newSection);

        // clean up by unenrolling
        WebElement unenrollButton = newSection.findElement(By.id("unenrollButton"));
        unenrollButton.click();
        Thread.sleep(SLEEP_DURATION);

        // verify unenrollment success
        String unenrollMessage = driver.findElement(By.id("unenrollMessage")).getText();
        assertTrue(unenrollMessage.contains("Unenrollment successful"));
    }
}
