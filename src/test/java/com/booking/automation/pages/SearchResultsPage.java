package com.booking.automation.pages;

import java.time.Duration;
import java.util.Set;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * Page Object class representing the Search Results page in Booking.com.
 * Handles interactions with the list of hotels after a search.
 * 
 * Features:
 * - Hotel search and selection
 * - Dynamic loading of more results
 * - Window handling for hotel details
 * - Scroll and visibility management
 */
public class SearchResultsPage {
    private WebDriver driver;
    private WebDriverWait wait;

    // Locators for search results elements
    private By loadMoreButton = By.xpath("//button[span[text()='Load more results']]");
    private By seeAvailabilityButtonInSearchResults = By.cssSelector("a[data-testid='availability-cta-btn']");

    /**
     * Constructor initializing the page object with WebDriver instance.
     * Sets up WebDriverWait with 20-second timeout for better stability.
     * 
     * @param driver WebDriver instance to interact with the page
     */
    public SearchResultsPage(WebDriver driver) {
        this.driver = driver;
        wait = new WebDriverWait(driver, Duration.ofSeconds(20));
    }

    /**
     * Selects a hotel by its name from the search results.
     * This method will scroll down and load more results if the hotel is not immediately found.
     * After selecting the hotel, it clicks its "See availability" button and switches to the new window.
     *
     * @param hotelName The full name of the hotel to select
     * @return An instance of HotelDetailsPage, as the action navigates to that page
     * @throws NoSuchElementException if hotel cannot be found after loading all results
     * @throws TimeoutException if elements are not clickable or visible within timeout
     */
    public HotelDetailsPage selectHotel(String hotelName) {


        System.out.println("SearchResultsPage: Attempting to select hotel: '" + hotelName + "'");
        String originalWindowHandle = driver.getWindowHandle();

        int maxLoadAttempts = 5;
        for (int i = 0; i < maxLoadAttempts; i++) {
            By hotelTitleLink = By.xpath("//div[@data-testid='property-card']//div[@data-testid='title' and contains(normalize-space(.),'" + hotelName + "')]");

            try {
                ((JavascriptExecutor) driver).executeScript("window.scrollTo(0, 0);");

                WebElement hotelTitleElement = wait.until(ExpectedConditions.visibilityOfElementLocated(hotelTitleLink));
                System.out.println("✅ Hotel title '" + hotelName + "' found. Scrolling to its 'See availability' button.");

                WebElement hotelCard = hotelTitleElement.findElement(By.xpath("./ancestor::div[@data-testid='property-card']"));
                WebElement seeAvailabilityBtn = wait.until(ExpectedConditions.elementToBeClickable(hotelCard.findElement(seeAvailabilityButtonInSearchResults)));

                ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", seeAvailabilityBtn);
                seeAvailabilityBtn.click();
                System.out.println("✅ 'See availability' button clicked for '" + hotelName + "'.");

                wait.until(ExpectedConditions.numberOfWindowsToBe(2));
                Set<String> allWindowHandles = driver.getWindowHandles();
                for (String handle : allWindowHandles) {
                    if (!handle.equals(originalWindowHandle)) {
                        driver.switchTo().window(handle);
                        System.out.println("✅ Switched to new window for hotel details.");
                        // Optional: Wait for some element on the new page to load to confirm switch
                        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("hp_hotel_name"))); // مثال: انتظر اسم الفندق في الصفحة الجديدة
                        break;
                    }
                }
                return new HotelDetailsPage(driver);
            } catch (TimeoutException | NoSuchElementException e) {
                System.out.println("Hotel '" + hotelName + "' not found on current view. Looking for 'Load more results' button. Attempt " + (i + 1));
                try {
                    WebElement loadMoreBtn = wait.until(ExpectedConditions.elementToBeClickable(loadMoreButton));
                    ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", loadMoreBtn);
                    loadMoreBtn.click();
                    System.out.println("✅ 'Load more results' button clicked. Waiting for new results.");
                    wait.until(ExpectedConditions.stalenessOf(loadMoreBtn));
                    wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button[type='submit']"))); // A simple re-load indicator
                } catch (TimeoutException ex) {
                    System.out.println("No more 'Load more results' button found or hotel not found after trying to load more.");
                    break;
                }
            }
        }
        throw new NoSuchElementException("Hotel '" + hotelName + "' not found after loading all available results or clicking 'See availability' failed.");
    }
}