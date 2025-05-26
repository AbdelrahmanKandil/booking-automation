package com.booking.automation.pages;

import java.time.Duration;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * Page Object class representing the Reservation page in Booking.com.
 * This page appears after clicking "Reserve" on the Hotel Details page.
 * Handles the final steps of the booking process including guest details and payment.
 * 
 * Features:
 * - Guest information form handling
 * - Room preferences selection
 * - Special requests handling
 * - Payment method selection
 * - Booking confirmation
 */
public class ReservationPage {
    private WebDriver driver;
    private WebDriverWait wait;

    // Locators for hotel information
    private By hotelNameOnReservationPage = By.xpath("//h1[contains(@class, 'conf-page-hotel-name')] | //div[contains(@class, 'hotel-name')] | //div[@data-testid='bui-breadcrumb-item-title']");

    /**
     * Constructor initializing the page object with WebDriver instance.
     * Sets up WebDriverWait with 30-second timeout for better stability.
     * 
     * @param driver WebDriver instance to interact with the page
     */
    public ReservationPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(30));
    }

    /**
     * Gets the hotel name from the reservation page.
     * Handles multiple possible locations of the hotel name element.
     * 
     * @return The hotel name if found, "Hotel Name Not Found" otherwise
     * @throws TimeoutException if hotel name element is not visible within timeout
     */
    public String getHotelName() {
        System.out.println("ReservationPage: Attempting to get hotel name.");
        try {
            WebElement hotelNameElement = wait.until(ExpectedConditions.visibilityOfElementLocated(hotelNameOnReservationPage));
            String name = hotelNameElement.getText().trim();
            System.out.println("✅ Hotel name on Reservation Page: " + name);
            return name;
        } catch (Exception e) {
            System.err.println("❌ Error getting hotel name on Reservation Page: " + e.getMessage());
            return "Hotel Name Not Found";
        }
    }
}