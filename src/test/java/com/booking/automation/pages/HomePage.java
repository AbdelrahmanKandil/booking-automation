package com.booking.automation.pages;

import java.time.Duration;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Locale;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * Page Object class representing the Booking.com homepage.
 * Handles interactions with the main search form and common popups.
 * 
 * Features:
 * - Location search and selection
 * - Date picker for check-in/check-out
 * - Search initiation
 * - Popup handling
 */
public class HomePage {
    private WebDriver driver;
    private WebDriverWait wait;

    // Locators for search form elements
    private By locationInput = By.xpath("//input[@aria-label='Where are you going?']");
    private By alexandriaSuggestion = By.xpath("//li[@id='autocomplete-result-0']//div[contains(@class,'b08850ce41') and text()='Alexandria']");
    private By searchButton = By.xpath("//button[@type='submit']");
    private By popupCloseButton = By.cssSelector("button[aria-label='Dismiss sign in information.']");

    // Locators for date picker elements
    private By checkinDateElement = By.xpath("//div[@data-testid='searchbox-dates-checkin']");
    private By nextMonthButton = By.cssSelector("button[aria-label='Next month']");
    private By calendarMainContainer = By.cssSelector("div[data-testid='searchbox-datepicker-calendar']");
    private By calendarMonthHeader = By.cssSelector(".e7addce19e.af236b7586");

    /**
     * Constructor initializing the page object with WebDriver instance.
     * Handles initial popup that appears when page loads.
     * 
     * @param driver WebDriver instance to interact with the page
     */
    public HomePage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(3));
        // Handle popup that appears on page load
        dismissSignInPopup();
    }

    /**
     * Dismisses the sign-in popup if present.
     * Uses JavaScript click for better reliability.
     * Can be called from other page objects when needed.
     */
    public void dismissSignInPopup() {
        try {
            List<WebElement> closeButtons = driver.findElements(popupCloseButton);
            if (!closeButtons.isEmpty()) {
                WebElement closeBtn = closeButtons.get(0);
                if (closeBtn.isDisplayed() && closeBtn.isEnabled()) {
                    ((JavascriptExecutor) driver).executeScript("arguments[0].click();", closeBtn);
                    System.out.println("✅ Sign-in popup dismissed");
                }
            }
        } catch (Exception e) {
            // Silently continue if popup is not found or cannot be dismissed
        }
    }

    /**
     * Enters the location in the search box and selects from suggestions.
     * 
     * @param location The location to search for
     * @throws TimeoutException if location input is not clickable
     */
    public void enterLocation(String location) {
        System.out.println("HomePage: Starting enterLocation for: " + location);
        
        WebElement locationInputBox = wait.until(ExpectedConditions.elementToBeClickable(locationInput));
        locationInputBox.clear();
        System.out.println("HomePage: Cleared location input field.");
        locationInputBox.sendKeys(location);
        System.out.println("HomePage: Entered location: " + location);

        try {
            WebElement suggestion = wait.until(ExpectedConditions.visibilityOfElementLocated(alexandriaSuggestion));
            wait.until(ExpectedConditions.elementToBeClickable(suggestion)).click();
            System.out.println("✅ Alexandria selected from suggestions.");
        } catch (TimeoutException e) {
            System.err.println("❌ Alexandria not found in suggestions within 15 seconds. Trying ENTER key. Error: " + e.getMessage());
            locationInputBox.sendKeys(Keys.ENTER);
        }
        System.out.println("HomePage: Finished enterLocation for: " + location);
    }

    /**
     * Selects check-in and check-out dates on the calendar.
     * Handles calendar navigation and date selection.
     * 
     * @param checkIn Check-in date in yyyy-MM-dd format
     * @param checkOut Check-out date in yyyy-MM-dd format
     * @throws DateTimeParseException if dates are in wrong format
     * @throws TimeoutException if calendar elements are not visible
     * @throws NoSuchElementException if calendar navigation fails
     */
    public void selectCheckInOutDates(String checkIn, String checkOut) {
        System.out.println("📅 Attempting to select check-in/out dates: " + checkIn + " to " + checkOut);

        try {
            WebElement mainCalendarDiv = wait.until(ExpectedConditions.visibilityOfElementLocated(calendarMainContainer));
            System.out.println("✅ Main calendar container is now visible by data-testid.");

            wait.until(ExpectedConditions.visibilityOfElementLocated(calendarMonthHeader));
            System.out.println("✅ Calendar month/year header is visible.");

            DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ENGLISH);
            LocalDate checkInDate = LocalDate.parse(checkIn, inputFormatter);
            LocalDate checkOutDate = LocalDate.parse(checkOut, inputFormatter);

            selectDateOptimized(checkInDate);
            
            selectDateOptimized(checkOutDate);

            System.out.println("✅ Dates selected successfully.");

        } catch (DateTimeParseException e) {
            System.err.println("❌ Error parsing date string: " + e.getMessage());
            throw new IllegalArgumentException("Invalid date format provided. Please use 'yyyy-MM-dd' format.", e);
        } catch (TimeoutException e) {
            System.err.println("❌ Timeout waiting for calendar elements to be visible. Error: " + e.getMessage());
            throw new NoSuchElementException("Failed to find the calendar after selecting location.", e);
        } catch (Exception e) {
            System.err.println("❌ An unexpected error occurred during date selection: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Helper method to select a specific date in the calendar.
     * Handles month navigation and date selection.
     * 
     * @param targetDate The date to select
     * @throws NoSuchElementException if date cannot be found or selected
     * @throws DateTimeParseException if month header cannot be parsed
     */
    private void selectDateOptimized(LocalDate targetDate) {
        String targetDateFormatted = targetDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String dateLocator = String.format("span[data-date='%s']", targetDateFormatted);

        DateTimeFormatter currentMonthFormatter = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.ENGLISH);

        int maxAttempts = 24;
        for (int i = 0; i < maxAttempts; i++) {
            WebElement monthHeaderElement = wait.until(ExpectedConditions.visibilityOfElementLocated(calendarMonthHeader));
            String currentMonthText = monthHeaderElement.getText();

            YearMonth currentCalendarMonth;
            try {
                currentCalendarMonth = YearMonth.parse(currentMonthText, currentMonthFormatter);
            } catch (DateTimeParseException e) {
                System.err.println("Could not parse current calendar month text: " + currentMonthText + ". Error: " + e.getMessage());
                throw e;
            }

            YearMonth targetMonth = YearMonth.from(targetDate);

            if (currentCalendarMonth.equals(targetMonth)) {
                try {
                    WebElement dateElement = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(dateLocator)));
                    dateElement.click();
                    System.out.println("✅ Selected date: " + targetDateFormatted);
                    return;
                } catch (StaleElementReferenceException e) {
                    System.out.println("StaleElementReferenceException caught for date " + targetDateFormatted + ", retrying selection.");
                } catch (TimeoutException e) {
                    System.err.println("❌ Date " + targetDateFormatted + " not found or clickable in " + currentMonthText + " despite month being correct. Error: " + e.getMessage());
                    throw new NoSuchElementException("Date not found in the correct month view: " + targetDateFormatted);
                }
            } else if (currentCalendarMonth.isBefore(targetMonth)) {
                System.out.println("⏩ Current month: " + currentMonthText + ". Clicking next to find " + targetMonth.format(currentMonthFormatter));
                try {
                    WebElement next = wait.until(ExpectedConditions.elementToBeClickable(nextMonthButton));
                    next.click();
                } catch (TimeoutException nextBtnEx) {
                    System.err.println("❌ Next month button not clickable or visible. Error: " + nextBtnEx.getMessage());
                    throw new NoSuchElementException("Next month button not found or clickable.");
                }
            } else {
                System.err.println("❌ Current month (" + currentMonthText + ") is AFTER target month (" + targetMonth.format(currentMonthFormatter) + "). Something is wrong with the calendar navigation logic.");
                throw new NoSuchElementException("Calendar navigated past target month: " + targetDateFormatted);
            }
        }

        throw new NoSuchElementException("❌ Could not find date: " + targetDateFormatted + " after trying " + maxAttempts + " months. Check date or maxAttempts.");
    }

    /**
     * Clicks the search button to initiate the search.
     * Handles popup that may appear after clicking search.
     * 
     * @throws TimeoutException if search button is not clickable
     */
    public void clickSearch() {
        System.out.println("🔍 Attempting to click Search button.");
        wait.until(ExpectedConditions.elementToBeClickable(searchButton)).click();
        System.out.println("✅ Search button clicked.");
        dismissSignInPopup();
    }
}