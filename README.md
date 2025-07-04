# Family Budget App

A comprehensive Java application for managing family finances, featuring advanced transaction tracking, budget planning, scheduled payments, deadline management, and detailed financial analytics. Built with JavaFX, JPA/Hibernate, and Gradle.

## 📋 Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Architecture](#architecture)
- [Technology Stack](#technology-stack)
- [Requirements](#requirements)
- [Installation](#installation)
- [Usage](#usage)
- [Project Structure](#project-structure)
- [API Documentation](#api-documentation)
- [Contributing](#contributing)
- [License](#license)

## 🎯 Overview

The Family Budget App is a sophisticated financial management system designed to help families and individuals track their income, expenses, and financial goals. The application provides a modern, user-friendly interface with powerful backend capabilities for comprehensive financial planning and analysis.

### Key Highlights

- **Multi-User Support**: Manage finances for individual users or family groups
- **Advanced Categorization**: Hierarchical tag system for flexible transaction organization
- **Budget Planning**: Create and monitor budgets with real-time tracking
- **Scheduled Transactions**: Handle recurring payments and future financial commitments
- **Comprehensive Analytics**: Detailed statistics and financial insights
- **Data Synchronization**: File-based sync for multi-device usage
- **Modern UI**: JavaFX-based interface with responsive design

## ✨ Features

### 💰 Transaction Management
- **CRUD Operations**: Create, read, update, and delete financial transactions
- **Advanced Search**: Filter transactions by date, category, amount, and description
- **Tag System**: Hierarchical categorization with parent-child relationships
- **Income/Expense Tracking**: Separate handling of income and expense transactions
- **Bulk Operations**: Import/export transactions via CSV

### 📊 Budget Planning
- **Budget Creation**: Set spending limits for categories and time periods
- **Real-time Monitoring**: Track budget utilization with visual indicators
- **Budget Alerts**: Notifications when approaching or exceeding limits
- **Budget Templates**: Reusable budget configurations
- **Forecasting**: Predict future spending based on historical data

### ⏰ Scheduled Transactions
- **Recurring Payments**: Automatically generate transactions for subscriptions, loans, etc.
- **Flexible Scheduling**: Daily, weekly, monthly, quarterly, and yearly patterns
- **Future Planning**: Schedule transactions for upcoming periods
- **Loan Management**: Track loan payments and amortization schedules

### 📅 Deadline Management
- **Payment Reminders**: Track important due dates and payment deadlines
- **Calendar Integration**: Visual calendar of upcoming financial obligations
- **Alert System**: Notifications for approaching and overdue deadlines
- **Priority Management**: Categorize deadlines by importance

### 📈 Statistics & Analytics
- **Monthly Reports**: Comprehensive monthly financial summaries
- **Category Analysis**: Spending patterns by category
- **Trend Analysis**: Historical spending trends and comparisons
- **Savings Tracking**: Monitor savings progress and goals
- **Net Worth Calculation**: Track overall financial position
- **Budget vs Actual**: Compare planned vs actual spending

### 👥 User Management
- **Multi-User Support**: Individual and family account management
- **Role-Based Access**: Different permission levels for users
- **Group Management**: Organize users into family or financial groups
- **User Settings**: Personalized preferences and configurations

### 🔄 Data Synchronization
- **File-Based Sync**: Export/import data for backup and sharing
- **Conflict Resolution**: Handle data conflicts during synchronization
- **Data Integrity**: Maintain data consistency across devices
- **Backup Support**: Automatic and manual backup capabilities

## 🏗️ Architecture

The application follows a layered architecture pattern with clear separation of concerns:

```
┌─────────────────────────────────────────────────────────────┐
│                    Presentation Layer                       │
│  ┌─────────────┐ ┌─────────────┐ ┌─────────────┐          │
│  │   Controllers│ │   JavaFX Views│ │   FXML Files │          │
│  └─────────────┘ └─────────────┘ └─────────────┘          │
└─────────────────────────────────────────────────────────────┘
┌─────────────────────────────────────────────────────────────┐
│                     Business Layer                          │
│  ┌─────────────┐ ┌─────────────┐ ┌─────────────┐          │
│  │   Services   │ │   Utilities │ │   Factories  │          │
│  └─────────────┘ └─────────────┘ └─────────────┘          │
└─────────────────────────────────────────────────────────────┘
┌─────────────────────────────────────────────────────────────┐
│                     Data Access Layer                       │
│  ┌─────────────┐ ┌─────────────┐ ┌─────────────┐          │
│  │ Repositories │ │   JPA/Hibernate│ │   EntityManager│          │
│  └─────────────┘ └─────────────┘ └─────────────┘          │
└─────────────────────────────────────────────────────────────┘
┌─────────────────────────────────────────────────────────────┐
│                      Data Layer                             │
│  ┌─────────────┐ ┌─────────────┐ ┌─────────────┐          │
│  │   Database   │ │   File Storage│ │   Cache      │          │
│  └─────────────┘ └─────────────┘ └─────────────┘          │
└─────────────────────────────────────────────────────────────┘
```

### Design Patterns

- **MVC Pattern**: Separation of Model, View, and Controller
- **Repository Pattern**: Abstract data access layer
- **Service Layer Pattern**: Business logic encapsulation
- **Factory Pattern**: Service and repository instantiation
- **Observer Pattern**: UI updates and notifications

## 🛠️ Technology Stack

### Backend
- **Java 17**: Core programming language
- **JPA/Hibernate**: Object-relational mapping and database persistence
- **H2 Database**: Embedded database for local storage
- **Gradle**: Build automation and dependency management

### Frontend
- **JavaFX**: Modern UI framework for desktop applications
- **FXML**: Declarative UI definition
- **CSS**: Styling and theming
- **Scene Builder**: Visual UI design tool

### Development Tools
- **IntelliJ IDEA/Eclipse**: IDE support
- **Git**: Version control
- **JUnit**: Unit testing framework
- **Maven Central**: Dependency repository

## 📋 Requirements

### System Requirements
- **Operating System**: Windows 10+, macOS 10.14+, or Linux
- **Java Runtime**: Java 17 or higher (JDK recommended)
- **Memory**: Minimum 2GB RAM, 4GB recommended
- **Storage**: 100MB free disk space
- **Display**: 1024x768 minimum resolution

### Development Requirements
- **Java Development Kit**: JDK 17 or higher
- **Gradle**: 7.0 or higher (wrapper included)
- **IDE**: IntelliJ IDEA, Eclipse, or VS Code
- **Git**: For version control

## 🚀 Installation

### Prerequisites
1. **Install Java 17+**
   ```bash
   # Check Java version
   java -version
   
   # If not installed, download from Oracle or OpenJDK
   ```

2. **Clone or Download the Project**
   ```bash
   # Using Git
   git clone <repository-url>
   cd FamilyBudgetApp
   
   # Or download and extract ZIP file
   ```

### Build Instructions

1. **Open Terminal/Command Prompt**
   Navigate to the project root directory.

2. **Build the Project**
   ```bash
   # On Linux/Mac
   ./gradlew build
   
   # On Windows
   gradlew.bat build
   ```

3. **Run Tests (Optional)**
   ```bash
   ./gradlew test
   ```

### Running the Application

1. **Using Gradle**
   ```bash
   # On Linux/Mac
   ./gradlew run
   
   # On Windows
   gradlew.bat run
   ```

2. **Using JAR File**
   ```bash
   # After building, run the generated JAR
   java -jar build/libs/FamilyBudgetApp.jar
   ```

3. **IDE Execution**
   - Open the project in your IDE
   - Run the `MainApp` class in `src/main/java/it/unicam/cs/mpgc/jbudget120002/view/MainApp.java`

## 📖 Usage

### Getting Started

1. **First Launch**
   - The application will create a new database file
   - Default sample data may be loaded automatically

2. **User Setup**
   - Create your user account
   - Set up your profile and preferences
   - Configure your currency and locale settings

3. **Initial Configuration**
   - Create your first budget categories (tags)
   - Set up your initial budget plans
   - Add your first transactions

### Core Workflows

#### Managing Transactions
1. Navigate to the **Transactions** tab
2. Click **Add Transaction** to create a new entry
3. Fill in the details: date, description, amount, type, and categories
4. Save the transaction
5. Use filters to search and organize your transactions

#### Creating Budgets
1. Go to the **Budgets** tab
2. Click **New Budget** to create a budget plan
3. Set the budget name, amount, and time period
4. Select relevant categories (tags)
5. Save and monitor your budget progress

#### Setting Up Scheduled Transactions
1. Navigate to **Scheduled** tab
2. Click **Add Scheduled Transaction**
3. Configure the recurring pattern and parameters
4. Set the start and end dates
5. The system will automatically generate transactions

#### Viewing Statistics
1. Open the **Statistics** tab
2. Select the desired time period
3. Choose the type of analysis (monthly, category, budget)
4. Review the generated reports and charts

### Advanced Features

#### Data Import/Export
- **CSV Import**: Import transactions from CSV files
- **Data Export**: Export your financial data for backup
- **Sample Data**: Use the included `sample-data.csv` for testing

#### Synchronization
- **File Sync**: Export data to share between devices
- **Backup**: Create regular backups of your financial data
- **Restore**: Import data from previous backups

## 📁 Project Structure

```
FamilyBudgetApp/
├── build.gradle                 # Gradle build configuration
├── settings.gradle             # Gradle settings
├── gradlew                     # Gradle wrapper (Unix)
├── gradlew.bat                 # Gradle wrapper (Windows)
├── README.md                   # This file
├── REPORT.md                   # Detailed project report
├── sample-data.csv             # Sample transaction data
├── data/                       # Database files
│   ├── jbudget.mv.db          # H2 database
│   └── jbudget.trace.db       # Database trace logs
└── src/
    ├── main/
    │   ├── java/
    │   │   └── it/unicam/cs/mpgc/jbudget120002/
    │   │       ├── controller/     # UI Controllers
    │   │       ├── model/          # Entity classes
    │   │       ├── repository/     # Data access layer
    │   │       ├── service/        # Business logic
    │   │       ├── util/           # Utility classes
    │   │       └── view/           # Main application
    │   └── resources/
    │       ├── css/               # Stylesheets
    │       ├── fxml/              # UI layouts
    │       ├── logo.png           # Application logo
    │       └── META-INF/
    │           └── persistence.xml # JPA configuration
    └── test/                      # Test files
        ├── java/
        └── resources/
```

### Package Structure

- **`controller/`**: JavaFX controllers for UI management
- **`model/`**: JPA entities and domain objects
- **`repository/`**: Data access interfaces and implementations
- **`service/`**: Business logic and service layer
- **`util/`**: Utility classes and helper methods
- **`view/`**: Main application entry point

## 📚 API Documentation

### Core Services

#### TransactionService
Manages financial transactions with comprehensive CRUD operations and financial calculations.

```java
// Create a transaction
Transaction transaction = transactionService.createTransaction(
    user, LocalDate.now(), "Groceries", new BigDecimal("50.00"), false, tagIds);

// Find transactions with filters
List<Transaction> transactions = transactionService.findTransactions(
    user, "food", startDate, endDate, foodTag, true);

// Calculate balance
BigDecimal balance = transactionService.calculateBalanceForUser(user, startDate, endDate);
```

#### BudgetService
Handles budget planning, monitoring, and forecasting.

```java
// Create and save a budget
Budget budget = new Budget("Monthly Groceries", new BigDecimal("500.00"), startDate, endDate);
budgetService.save(budget);

// Check budget status
Map<Long, BudgetStatus> status = budgetService.calculateBudgetStatus(startDate, endDate);

// Get budget forecast
Map<LocalDate, BigDecimal> forecast = budgetService.getBudgetForecast(startDate, 6);
```

#### StatisticsService
Provides comprehensive financial analytics and reporting.

```java
// Get monthly statistics
List<MonthlyStatistic> monthlyStats = statisticsService.getMonthlyStatistics(startDate, endDate);

// Calculate savings rate
double savingsRate = statisticsService.calculateSavingsRate(startDate, endDate);

// Get category analysis
List<CategoryStatistic> categoryStats = statisticsService.getCategoryStatistics(
    startDate, endDate, category, true);
```

### Key Models

#### Transaction
Represents a financial transaction with comprehensive metadata.

```java
@Entity
public class Transaction {
    private Long id;
    private LocalDate date;
    private String description;
    private BigDecimal amount;
    private boolean isIncome;
    private Set<Tag> tags;
    private User user;
    // ... getters, setters, and business methods
}
```

#### Budget
Manages budget allocations and spending limits.

```java
@Entity
public class Budget {
    private Long id;
    private String name;
    private BigDecimal amount;
    private LocalDate startDate;
    private LocalDate endDate;
    private Set<Tag> tags;
    private Group group;
    // ... getters, setters, and business methods
}
```

#### Tag
Hierarchical categorization system for transactions and budgets.

```java
@Entity
public class Tag {
    private Long id;
    private String name;
    private String description;
    private Tag parent;
    private Set<Tag> children;
    private String color;
    // ... getters, setters, and business methods
}
```

## 🤝 Contributing

### Development Setup
1. Fork the repository
2. Create a feature branch: `git checkout -b feature-name`
3. Make your changes and add tests
4. Commit your changes: `git commit -m 'Add feature'`
5. Push to the branch: `git push origin feature-name`
6. Submit a pull request

### Coding Standards
- Follow Java naming conventions
- Add comprehensive JavaDoc documentation
- Write unit tests for new functionality
- Use meaningful commit messages
- Follow the existing code structure

### Testing
```bash
# Run all tests
./gradlew test

# Run specific test class
./gradlew test --tests TransactionServiceTest

# Generate test coverage report
./gradlew jacocoTestReport
```

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.

## 📞 Support

For questions, issues, or support:
- **Documentation**: Check the `REPORT.md` file for detailed technical documentation
- **Issues**: Report bugs or feature requests through the issue tracker
- **Contact**: Reach out to the development team

## 🔄 Version History

- **v1.0.0** - Initial release with core functionality
  - Transaction management
  - Budget planning
  - Basic statistics
  - User management
  - JavaFX UI

---

**Family Budget App** - Empowering families to take control of their finances through intelligent planning and comprehensive tracking.
