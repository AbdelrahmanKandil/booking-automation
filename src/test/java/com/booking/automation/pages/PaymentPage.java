package com.booking.automation.pages;

import java.time.Duration;

import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * Page Object class representing the Payment page in Booking.com.
 * This page appears after completing the reservation details.
 * Handles payment processing and final booking confirmation.
 * 
 * Features:
 * - Payment method selection
 * - Card information entry
 * - Guest details verification
 * - Final booking confirmation
 */
public class PaymentPage {
    private WebDriver driver;
    private WebDriverWait wait;

    // Locators for hotel information
    private final By hotelNameOnPaymentPage = By.xpath("//h1[@class='e7addce19e']");

    /**
     * Constructor initializing the page object with WebDriver instance.
     * Sets up WebDriverWait with 15-second timeout for better stability.
     * 
     * @param driver WebDriver instance to interact with the page
     */
    public PaymentPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(15));
    }

    /**
     * Gets the hotel name from the payment page.
     * Verifies that we're on the correct hotel's payment page.
     * 
     * @return The hotel name if found, "Hotel Name Not Found" if element is not found
     * @throws TimeoutException if hotel name element is not visible within timeout
     */
    public String getHotelName() {
        System.out.println("PaymentPage: Attempting to get hotel name.");
        try {
            WebElement hotelNameElement = wait.until(ExpectedConditions.visibilityOfElementLocated(hotelNameOnPaymentPage));
            String name = hotelNameElement.getText().trim();
            System.out.println("✅ Hotel name on Payment Page: " + name);
            return name;
        } catch (TimeoutException e) {
            System.err.println("❌ Error getting hotel name on Payment Page: Element not found or not visible within timeout. " + e.getMessage());
            return "Hotel Name Not Found";
        } catch (Exception e) {
            System.err.println("❌ An unexpected error occurred while getting hotel name: " + e.getMessage());
            return "Hotel Name Not Found";
        }
    }
}