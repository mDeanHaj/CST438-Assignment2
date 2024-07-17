package com.cst438.controller;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class AssignmentControllerSystemTest {

    // TODO edit the following to give the location and file name
    // of the Chrome driver.
    //  for WinOS the file name will be chromedriver.exe
    //  for MacOS the file name will be chromedriver
//    public static final String CHROME_DRIVER_FILE_LOCATION =
//            "C:/chromedriver_win32/chromedriver.exe";

    // Haris
    public static final String CHROME_DRIVER_FILE_LOCATION =
            "/Users/hj739/Downloads/chromedriver-mac-arm64/chromedriver";
    public static final String URL = "http://localhost:3000";

    public static final int SLEEP_DURATION = 1000; // 1 second.


    // add selenium dependency to pom.xml

    // these tests assumes that test data does NOT contain any
    // sections for course cst499 in 2024 Spring term.

    WebDriver driver;

    @BeforeEach
    public void setUpDriver() throws Exception {

        // set properties required by Chrome Driver
        System.setProperty(
                "webdriver.chrome.driver", CHROME_DRIVER_FILE_LOCATION);
        ChromeOptions ops = new ChromeOptions();
        ops.addArguments("--remote-allow-origins=*");

        // start the driver
        driver = new ChromeDriver(ops);

        driver.get(URL);
        // must have a short wait to allow time for the page to download
        Thread.sleep(SLEEP_DURATION);

    }

    @AfterEach
    public void terminateDriver() {
        if (driver != null) {
            // quit driver
            driver.close();
            driver.quit();
            driver = null;
        }
    }

    @Test
    public void systemTestCreateAssignment() throws Exception {
        // add an assignment for 2024 Spring term
        // verify section shows on the list of sections for Spring 2024
        // delete the section
        // verify the section is gone

        // enter 2024, Spring and click search sections
        driver.findElement(By.id("year")).sendKeys("2024");
        driver.findElement(By.id("semester")).sendKeys("Spring");
        driver.findElement(By.id("searchSections")).click();
        Thread.sleep(SLEEP_DURATION);

        // click link to navigate to Sections
        WebElement we = driver.findElement(By.id("viewAssignments"));
        we.click();
        Thread.sleep(SLEEP_DURATION);

        // verify that "Added Assignment" is not in the list of assignments
        // if it exists, then delete it
        // Selenium throws NoSuchElementException when the element is not found
        try {
            while (true) {
                WebElement newAssignment = driver.findElement(By.xpath("//tr[td='Assignment Added']"));
                List<WebElement> buttons = newAssignment.findElements(By.tagName("button"));
                // delete is the third button
                assertEquals(3, buttons.size());
                buttons.get(2).click();
                Thread.sleep(SLEEP_DURATION);
                // find the YES to confirm button
                List<WebElement> confirmButtons = driver
                        .findElement(By.className("react-confirm-alert-button-group"))
                        .findElements(By.tagName("button"));
                assertEquals(2, confirmButtons.size());
                confirmButtons.get(0).click();
                Thread.sleep(SLEEP_DURATION);
            }
        } catch (NoSuchElementException e) {
            // do nothing, continue with test
        }

        // find and click button to add an assignment
        driver.findElement(By.id("addAssignment")).click();
        Thread.sleep(SLEEP_DURATION);

        // enter data
        //  title: "Assignment Added",
        driver.findElement(By.id("aTitle")).sendKeys("Assignment Added");
        //  aDueDate: 2024-03-08,
        driver.findElement(By.id("aDueDate")).sendKeys("2024-03-08");

        // click Save
        driver.findElement(By.id("saveAssignment")).click();
        Thread.sleep(SLEEP_DURATION);

        String addMessage = driver.findElement(By.id("addAssignmentMessage")).getText();
        assertTrue(addMessage.startsWith("Assignment added"));

        // close the dialog
        driver.findElement(By.id("closeDialog")).click();

        // verify that new Assignment shows up on Assignments list
        // find the row for new assignment
        WebElement newAssignment = driver.findElement(By.xpath("//tr[td='Assignment Added']"));
        List<WebElement> buttons = newAssignment.findElements(By.tagName("button"));
        // delete is the second button
        assertEquals(3, buttons.size());
        buttons.get(2).click();
        Thread.sleep(SLEEP_DURATION);
        // find the YES to confirm button
        List<WebElement> confirmButtons = driver
                .findElement(By.className("react-confirm-alert-button-group"))
                .findElements(By.tagName("button"));
        assertEquals(2, confirmButtons.size());
        confirmButtons.get(0).click();
        Thread.sleep(SLEEP_DURATION);

        // verify that assignment is now deleted
        String message = driver.findElement(By.id("assignmentMessage")).getText();
        assertTrue(message.startsWith("Assignment deleted"));
        assertThrows(NoSuchElementException.class, () ->
                driver.findElement(By.xpath("//tr[td='Assignment Added']")));

    }

    @Test
    public void systemTestEnterGrade() throws Exception {
        // verify grades are added to added assignment through enrollments


        // enter 2024, Spring,  and click show sections
        driver.findElement(By.id("year")).sendKeys("2024");
        driver.findElement(By.id("semester")).sendKeys("Spring");
        driver.findElement(By.id("searchSections")).click();
        Thread.sleep(SLEEP_DURATION);

        // find and click button to view enrollments for section 8
        WebElement rowSec8 = driver.findElement(By.xpath("//tr[td='8']"));
        WebElement link = rowSec8.findElement(By.id("viewEnrollments"));
        link.click();
        Thread.sleep(SLEEP_DURATION);

        // edit and save grade in dialog
        WebElement rowE2 = driver.findElement(By.xpath("//tr[td='2']"));
        link = rowE2.findElement(By.id("editEnrollment"));
        link.click();
        Thread.sleep(SLEEP_DURATION);

        //enter grade for assignment and save
        WebElement eGrade = driver.findElement(By.id("editGrade"));
        String ogGrade = eGrade.getAttribute("value");
        eGrade.sendKeys(Keys.chord(Keys.CONTROL,"a", Keys.DELETE));
        Thread.sleep(SLEEP_DURATION);
        eGrade.sendKeys("D");
        driver.findElement(By.id("save")).click();
        Thread.sleep(SLEEP_DURATION);
        String message = driver.findElement(By.id("message")).getText();
        assertTrue(message.startsWith("Grade saved"));

        //verify grade was saved
        rowE2 = driver.findElement(By.xpath("//tr[td='2']"));
        link = rowE2.findElement(By.id("editEnrollment"));
        link.click();
        Thread.sleep(SLEEP_DURATION);
        eGrade = driver.findElement(By.id("editGrade"));
        assertEquals("D",eGrade.getAttribute("value"));
        //return grade to original value
        eGrade.sendKeys(Keys.chord(Keys.CONTROL,"a", Keys.DELETE));
        Thread.sleep(SLEEP_DURATION);
        eGrade.sendKeys(ogGrade);
        driver.findElement(By.id("save")).click();
        Thread.sleep(SLEEP_DURATION);
        message = driver.findElement(By.id("message")).getText();
        assertTrue(message.startsWith("Grade saved"));


    }

    @Test
    public void systemTestAddGrade() throws Exception {
        // verify grades are added to assignments


        // enter 2024, Spring,  and click show sections
        driver.findElement(By.id("year")).sendKeys("2024");
        driver.findElement(By.id("semester")).sendKeys("Spring");
        driver.findElement(By.id("sections")).click();
        Thread.sleep(SLEEP_DURATION);

        // find and click button to view assignments for section 8
        WebElement rowSec8 = driver.findElement(By.xpath("//tr[td='8']"));
        WebElement link = rowSec8.findElement(By.id("assignments"));
        link.click();
        Thread.sleep(SLEEP_DURATION);

        // find and click button to grade assignment 1
        WebElement rowA1 = driver.findElement(By.xpath("//tr[td='1']"));
        link = rowA1.findElement(By.id("viewGrade"));
        link.click();
        Thread.sleep(SLEEP_DURATION);

        //enter grade for assignment and save
        WebElement sGrade = driver.findElement(By.tagName("input"));
        String ogGrade = sGrade.getAttribute("value");
        sGrade.sendKeys(Keys.chord(Keys.CONTROL,"a", Keys.DELETE));
        Thread.sleep(SLEEP_DURATION);
        sGrade.sendKeys("55");
        driver.findElement(By.id("save")).click();
        Thread.sleep(SLEEP_DURATION);
        String message = driver.findElement(By.id("message")).getText();
        assertTrue(message.startsWith("Grades saved"));
        driver.findElement(By.id("close")).click();
        Thread.sleep(SLEEP_DURATION);

        //verify grade is saved and revert to original grade
        rowA1 = driver.findElement(By.xpath("//tr[td='1']"));
        link = rowA1.findElement(By.id("viewGrade"));
        link.click();
        Thread.sleep(SLEEP_DURATION);
        sGrade = driver.findElement(By.tagName("input"));
        assertEquals("55",sGrade.getAttribute("value"));
        sGrade.sendKeys(Keys.chord(Keys.CONTROL,"a", Keys.DELETE));
        Thread.sleep(SLEEP_DURATION);
        sGrade.sendKeys(ogGrade);
        driver.findElement(By.id("save")).click();
        Thread.sleep(SLEEP_DURATION);
        message = driver.findElement(By.id("message")).getText();
        assertTrue(message.startsWith("Grades saved"));
        driver.findElement(By.id("close")).click();
        Thread.sleep(SLEEP_DURATION);

    }
}
