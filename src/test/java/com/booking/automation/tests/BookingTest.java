package com.booking.automation.tests;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Map;

import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.booking.automation.base.BaseTest;
import com.booking.automation.pages.HomePage;
import com.booking.automation.pages.HotelDetailsPage;
import com.booking.automation.pages.PaymentPage;
import com.booking.automation.pages.ReservationPage;
import com.booking.automation.pages.SearchResultsPage;
import com.booking.automation.utils.ExcelDataProvider;

public class BookingTest extends BaseTest {

    @DataProvider(name = "bookingData")
    public Object[][] getBookingData() throws IOException {
        String excelFilePath = "src/test/resources/bookingData.xlsx";
        String sheetName = "Sheet1";
        return ExcelDataProvider.getTestData(excelFilePath, sheetName);
    }

    @Test(dataProvider = "bookingData")
    public void makeBookingTest(Map<String, String> testData) {
        // Validate required test data
        String location = testData.get("Location");
        String checkInDateStr = testData.get("CheckInDate");
        String checkOutDateStr = testData.get("CheckOutDate");
        String hotelName = testData.get("HotelName");
        String roomType = testData.get("RoomType");
        String numberOfRoomsStr = testData.get("NumberOfRooms");

        // Verify all required fields are not empty
        if (isEmpty(location) || isEmpty(checkInDateStr) || isEmpty(checkOutDateStr) ||
                isEmpty(hotelName) || isEmpty(roomType) || isEmpty(numberOfRoomsStr)) {
            System.out.println("Skipping test case due to missing or empty data: " + testData);
            throw new SkipException("Missing or empty data in Excel: " + testData);
        }

        // Validate and parse number of rooms
        int numberOfRooms;
        try {
            numberOfRooms = Integer.parseInt(numberOfRoomsStr.trim());
            if (numberOfRooms <= 0) {
                System.out.println("Warning: Invalid number of rooms (" + numberOfRoomsStr + "), defaulting to 1");
                numberOfRooms = 1;
            }
        } catch (NumberFormatException e) {
            System.out.println("Error parsing number of rooms (" + numberOfRoomsStr + "), defaulting to 1: " + e.getMessage());
            numberOfRooms = 1;
        }

        System.out.println("\n--- Starting Test Case for: " + hotelName + " ---");
        System.out.println("Test Data: Location=" + location + ", CheckIn=" + checkInDateStr + ", CheckOut=" + checkOutDateStr +
                ", Hotel=" + hotelName + ", RoomType=" + roomType + ", Rooms=" + numberOfRooms);

        // Step 1: Navigate to Home Page and perform initial search
        HomePage homePage = new HomePage(driver);
        homePage.enterLocation(location);
        homePage.selectCheckInOutDates(checkInDateStr, checkOutDateStr);
        homePage.clickSearch();

        // Parse dates for verification in HotelDetailsPage
        DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ENGLISH);
        LocalDate checkInDate = LocalDate.parse(checkInDateStr, inputFormatter);
        LocalDate checkOutDate = LocalDate.parse(checkOutDateStr, inputFormatter);

        // Step 2: Handle Search Results Page - Select hotel and proceed
        SearchResultsPage searchResultsPage = new SearchResultsPage(driver);
        HotelDetailsPage hotelDetailsPage = searchResultsPage.selectHotel(hotelName);

        // Step 3: Handle Hotel Details Page - Verify dates, select room, click reserve
        hotelDetailsPage.verifyDates(checkInDate, checkOutDate);
        hotelDetailsPage.selectRoomAndQuantity(roomType, numberOfRooms);
        ReservationPage reservationPage = hotelDetailsPage.clickReserveButton(roomType);

        // Step 4: Verify hotel name on Payment Page
        PaymentPage paymentPage = new PaymentPage(driver);
        String actualHotelNameOnPaymentPage = paymentPage.getHotelName();
        Assert.assertTrue(actualHotelNameOnPaymentPage.contains(hotelName),
                "Hotel name mismatch on payment page. Expected to contain: '" + hotelName + "', Actual: '" + actualHotelNameOnPaymentPage + "'");
    }

    /**
     * Helper method to check for null or empty strings
     * @param value The string to check
     * @return true if the string is null or empty, false otherwise
     */
    private boolean isEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }
}