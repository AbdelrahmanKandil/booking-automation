package com.booking.automation.pages;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;


/**
 * Page Object class representing the Hotel Details page in Booking.com
 * This page appears after clicking "See availability" on a hotel in search results.
 * Handles room selection, date verification, and reservation initiation.
 */
public class HotelDetailsPage {
    private WebDriver driver;
    private WebDriverWait wait;

    // Locators for date display elements
    private final By checkInDateDisplay = By.cssSelector("button[data-testid='date-display-field-start'] span");
    private final By checkOutDateDisplay = By.cssSelector("button[data-testid='date-display-field-end'] span");

    // Locators for room selection and booking
    private final By roomTypeNames = By.cssSelector("span.hprt-roomtype-icon-link");
    private final By roomQuantitySelectDropdown = By.cssSelector("td.hprt-table-room-select select.hprt-nos-select");
    private final By reserveButtonLocator = By.cssSelector("button.txp-bui-main-pp.bui-button--primary.js-reservation-button");
    private final By loadingSpinnerLocator = By.cssSelector("span.bui-button__loader");

    // Search results page locators
    private final By hotelTitleOnSearchResultsPage = By.xpath("//div[@data-testid='title']");
    private final By seeAvailabilityButton = By.xpath("//button[contains(., 'See availability')]");

    /**
     * Constructor initializing the page object with WebDriver instance
     * Sets up WebDriverWait with 30-second timeout for better stability
     * 
     * @param driver WebDriver instance to interact with the page
     */
    public HotelDetailsPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(15));
    }

    /**
     * Clicks the "See Availability" button for a specific hotel and handles window switching
     * 
     * @param hotelName The exact name of the hotel to select
     * @throws TimeoutException if button is not clickable within timeout
     * @throws NoSuchElementException if hotel card or button is not found
     */
    public void clickSeeAvailabilityButton(String hotelName) {

        System.out.println("SearchResultsPage: Attempting to select hotel: '" + hotelName + "'");
        try {
            // Find hotel card containing hotel name and "See availability" button
            By hotelAvailabilityButtonLocator = By.xpath(
                    String.format("//div[@data-testid='title' and contains(.,'%s')]//ancestor::div[contains(@data-testid,'property-card') or contains(@class,'sr_property_card')]//button[contains(.,'See availability') or contains(.,'عرض التوافر')]", hotelName));

            WebElement availabilityBtn = wait.until(ExpectedConditions.elementToBeClickable(hotelAvailabilityButtonLocator));
            System.out.println("✅ 'See availability' button found for '" + hotelName + "'.");

            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", availabilityBtn);
            System.out.println("✅ Scrolling to 'See availability' button.");

            availabilityBtn.click();
            System.out.println("✅ 'See availability' button clicked for '" + hotelName + "'.");

            // Handle new window
            String originalWindow = driver.getWindowHandle();
            wait.until(ExpectedConditions.numberOfWindowsToBe(2));
            Set<String> allWindowHandles = driver.getWindowHandles();

            for (String windowHandle : allWindowHandles) {
                if (!originalWindow.contentEquals(windowHandle)) {
                    driver.switchTo().window(windowHandle);
                    System.out.println("✅ Switched to new window for hotel details.");
                    break;
                }
            }
        } catch (TimeoutException e) {
            Assert.fail("Failed to find or click 'See availability' button for hotel '" + hotelName + "'. Check hotel name and locator. Error: " + e.getMessage());
        } catch (NoSuchElementException e) {
            Assert.fail("Hotel card or 'See availability' button not found for '" + hotelName + "'. Check locators. Error: " + e.getMessage());
        } catch (Exception e) {
            Assert.fail("An unexpected error occurred while clicking 'See availability' button: " + e.getMessage());
        }
    }

    /**
     * Verifies that the displayed check-in and check-out dates match expected dates
     * 
     * @param expectedCheckIn Expected check-in date
     * @param expectedCheckOut Expected check-out date
     * @throws RuntimeException if date verification fails
     */
    public void verifyDates(LocalDate expectedCheckIn, LocalDate expectedCheckOut) {
        System.out.println("HotelDetailsPage: Verifying check-in/out dates.");

        DateTimeFormatter displayFormatter = DateTimeFormatter.ofPattern("EEE d MMM", Locale.ENGLISH);

        try {
            String actualCheckInText = wait.until(ExpectedConditions.visibilityOfElementLocated(checkInDateDisplay)).getText().trim();
            String actualCheckOutText = wait.until(ExpectedConditions.elementToBeClickable(checkOutDateDisplay)).getText().trim();

            String expectedCheckInFormatted = expectedCheckIn.format(displayFormatter);
            String expectedCheckOutFormatted = expectedCheckOut.format(displayFormatter);

            System.out.println("Expected Check-in: " + expectedCheckInFormatted + ", Actual: " + actualCheckInText);
            Assert.assertEquals(actualCheckInText, expectedCheckInFormatted, "Check-in date mismatch on Hotel Details Page.");

            System.out.println("Expected Check-out: " + expectedCheckOutFormatted + ", Actual: " + actualCheckOutText);
            Assert.assertEquals(actualCheckOutText, expectedCheckOutFormatted, "Check-out date mismatch on Hotel Details Page.");

            System.out.println("✅ Dates verified successfully on Hotel Details Page.");
        } catch (Exception e) {
            System.err.println("❌ Error verifying dates: " + e.getMessage());
            throw new RuntimeException("Failed to verify dates on Hotel Details Page.", e);
        }
    }

    /**
     * Selects a specific room type and quantity
     * Waits for the room selection to be processed and reserve button to become available
     * 
     * @param roomName Exact name of the room type to select
     * @param quantity Number of rooms to select
     * @throws AssertionError if room is not found or selection fails
     */
    public void selectRoomAndQuantity(String roomName, int quantity) {
        System.out.println("HotelDetailsPage: Attempting to select room: '" + roomName + "' with quantity: " + quantity);

        WebElement targetRoomRow = null;

        wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(roomTypeNames));
        List<WebElement> roomNameElements = driver.findElements(roomTypeNames);

        for (WebElement nameElement : roomNameElements) {
            String displayedRoomName = ((String) ((JavascriptExecutor) driver).executeScript("return arguments[0].textContent;", nameElement)).trim();
            if (displayedRoomName.equalsIgnoreCase(roomName)) {
                try {
                    targetRoomRow = nameElement.findElement(By.xpath("./ancestor::tr[contains(@class,'hprt-table-row') or contains(@class,'js-room-row')]"));
                    System.out.println("✅ Found room row for: " + roomName);
                    break;
                } catch (NoSuchElementException e) {
                    System.out.println("Could not find parent row for room name: " + roomName + ". Trying next element if any.");
                    continue;
                }
            }
        }

        if (targetRoomRow == null) {
            Assert.fail("Room with name '" + roomName + "' not found on Hotel Details Page.");
        }

        try {
            WebElement selectRoomsDropdown = wait.until(ExpectedConditions.elementToBeClickable(targetRoomRow.findElement(roomQuantitySelectDropdown)));
            Select select = new Select(selectRoomsDropdown);

            System.out.println("HotelDetailsPage: Selecting quantity " + quantity + " for room: " + roomName);
            select.selectByValue(String.valueOf(quantity));
            System.out.println("✅ Quantity " + quantity + " selected for room: '" + roomName + "'.");

            wait.until(ExpectedConditions.textToBePresentInElementValue(selectRoomsDropdown, String.valueOf(quantity)));

            // Wait for loading spinner to disappear and reserve button to be clickable
            wait.until(ExpectedConditions.invisibilityOfElementLocated(loadingSpinnerLocator));
            wait.until(ExpectedConditions.elementToBeClickable(reserveButtonLocator));
            System.out.println("✅ Reserve button is ready after quantity selection.");

        } catch (NoSuchElementException e) {
            Assert.fail("Quantity dropdown not found for room '" + roomName + "'. Error: " + e.getMessage());
        } catch (Exception e) {
            Assert.fail("Error selecting quantity for room '" + roomName + "'. Error: " + e.getMessage());
        }
    }

    /**
     * Clicks the reserve button for the selected room
     * Handles any popup that might appear during the process
     * 
     * @param roomType The type of room being reserved (for verification)
     * @return ReservationPage object representing the next page in the booking flow
     * @throws AssertionError if reserve button is not found or not clickable
     */
    public ReservationPage clickReserveButton(String roomType) {
        try {
            // 1. Find room row (to verify room exists)
            By roomRowLocator = By.xpath("//tr[contains(., '" + roomType.replace("'", "\\'") + "')]");
            WebElement roomRow = wait.until(ExpectedConditions.presenceOfElementLocated(roomRowLocator));
            System.out.println("✅ Room row found for: " + roomType);

            // 2. Handle any potential popup
            try {
                WebElement overlay = driver.findElement(By.cssSelector("div.bui-modal__content button[aria-label='Dismiss']"));
                if (overlay.isDisplayed()) {
                    overlay.click();
                    System.out.println("✅ Popup dismissed.");
                }
            } catch (NoSuchElementException e) {
                System.out.println("No popup found, continuing.");
            }

            // 3. Find and verify reserve button is ready
            wait.until(ExpectedConditions.invisibilityOfElementLocated(loadingSpinnerLocator));
            WebElement reserveButton = wait.until(ExpectedConditions.elementToBeClickable(reserveButtonLocator));
            System.out.println("✅ Reserve button is ready.");

            // 4. Click the button
            reserveButton.click();
            System.out.println("✅ 'I'll reserve' button clicked successfully for room: " + roomType);

            return new ReservationPage(driver);

        } catch (Exception e) {
            System.out.println("❌ Failed to click reserve button for room: " + roomType + ". Error: " + e.getMessage());
            Assert.fail("❌ Reserve button not found or not clickable. Error: " + e.getMessage());
            return null;
        }
    }
}