# ✈️🏢 Booking.com Test Automation Framework

## 📝 Overview
This project is a robust test automation framework for Booking.com, built using Selenium WebDriver and Java. It implements the Page Object Model (POM) design pattern for maintainable and scalable test automation.

## 🚀 Features
- **Page Object Model**: Clean separation of test logic and page interactions
- **Performance Optimized**: Efficient handling of popups and dynamic elements
- **Smart Wait Strategies**: Optimized waits for better stability
- **Window Management**: Handles multiple browser windows seamlessly
- **Date Handling**: Robust calendar and date selection functionality
- **Error Handling**: Comprehensive error handling and logging

## 🛠 Tech Stack
- Java
- Selenium WebDriver
- TestNG
- Maven

## 📂 Project Structure
```
Booking/
├── src/
│   └── test/
│       └── java/
│           └── com/
│               └── booking/
│                   └── automation/
│                       └── pages/
│                           ├── HomePage.java
│                           ├── SearchResultsPage.java
│                           ├── HotelDetailsPage.java
│                           └── ReservationPage.java
└── pom.xml
```

## 📋 Page Objects
1. **HomePage**
   - Handles search form interactions
   - Manages date selection
   - Optimized popup handling

2. **SearchResultsPage**
   - Hotel search and filtering
   - Dynamic loading of search results
   - Hotel selection functionality

3. **HotelDetailsPage**
   - Room selection and quantity management
   - Date verification
   - Reservation initiation

## 🔧 Setup and Configuration

### Prerequisites
- Java JDK 11 or higher
- Maven
- Chrome/Firefox browser

### Installation
1. Clone the repository:
```bash
git clone https://github.com/AbdelrahmanKandil/booking-automation.git
```

2. Install dependencies:
```bash
cd booking-automation
mvn clean install
```

## 🏃‍♂️ Running Tests
Execute tests using Maven:
```bash
mvn test
```

## 💡 Best Practices Implemented
- **Efficient Popup Handling**: Optimized approach for managing Booking.com popups
- **Smart Waits**: Using explicit waits with appropriate timeouts
- **Robust Selectors**: Reliable element locators for better stability
- **Error Recovery**: Graceful handling of common scenarios
- **Clean Code**: Well-documented and maintainable codebase

## 🔍 Key Features Explained

### Popup Management
The framework implements an efficient popup handling strategy:
```java
public void dismissSignInPopup() {
    try {
        List<WebElement> closeButtons = driver.findElements(popupCloseButton);
        if (!closeButtons.isEmpty()) {
            WebElement closeBtn = closeButtons.get(0);
            if (closeBtn.isDisplayed() && closeBtn.isEnabled()) {
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", closeBtn);
            }
        }
    } catch (Exception e) {
        // Silent handling for better performance
    }
}
```

### Date Selection
Robust calendar handling with smart navigation:
```java
public void selectCheckInOutDates(String checkIn, String checkOut) {
    // Intelligent date parsing and selection
    // Handles month navigation
    // Verifies selections
}
```

## 📈 Performance Optimizations
- Minimized unnecessary popup checks
- Optimized wait times
- Efficient element location strategies
- Smart handling of dynamic content

## 🤝 Contributing
1. Fork the repository
2. Create your feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request

## 📄 License
This project is licensed under the MIT License - see the LICENSE file for details.

## ✍️ Author
**Abdelrahman Kandil**
- Software Tester at Sumerge
- LinkedIn: [@abdulrahman-kandil](https://www.linkedin.com/in/abdulrahman-kandil/)

## 🙏 Acknowledgments
- Selenium WebDriver team
- TestNG framework
- Maven community 