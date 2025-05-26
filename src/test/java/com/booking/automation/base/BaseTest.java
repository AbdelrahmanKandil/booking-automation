package com.booking.automation.base;

import java.time.Duration;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.edge.EdgeDriver;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;

/**
 * Base test class providing common setup and teardown functionality for all test classes.
 * Handles browser initialization, configuration, and cleanup.
 * 
 * Features:
 * - Initializes Microsoft Edge browser
 * - Configures browser window and timeouts
 * - Provides cleanup after test execution
 */
public class BaseTest {
    /**
     * WebDriver instance shared across test methods
     * Protected access allows test classes to use the driver directly
     */
    protected WebDriver driver;

    /**
     * Setup method run before each test class.
     * Initializes the browser and configures basic settings.
     * 
     * Configuration includes:
     * - Browser window maximization
     * - Implicit wait timeout (10 seconds)
     * - Navigation to Booking.com homepage
     */
    @BeforeClass
    public void setup() {
        
        // Create Microsoft Edge browser instance
        driver = new EdgeDriver();

        // Maximize browser window
        driver.manage().window().maximize();

        // Set implicit wait timeout for better stability
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(3));

        // Navigate to Booking.com
        driver.get("https://www.booking.com");
        System.out.println("✅ Booking Page is Opened.");
    }

    /**
     * Cleanup method run after each test class.
     * Ensures proper browser cleanup and resource release.
     * 
     * Actions:
     * - Closes the browser window
     * - Terminates the WebDriver session
     */
    @AfterClass
    public void tearDown() {
        // Close browser after test completion
        if (driver != null) {
            driver.quit();
            System.out.println("✅ Browser Closed.");
        }
    }
}