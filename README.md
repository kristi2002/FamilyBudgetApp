# Family Budget App

A Java application for managing family finances, including transactions, budgets, scheduled/recurring payments, deadlines, statistics, and more. Built with JavaFX, JPA/Hibernate, and Gradle.

---

## Features
- Transaction management (with tags/categories, scheduled/recurring, loan plans)
- Budget management
- Calendar of deadlines
- Statistics and comparisons
- Advanced search/filtering
- Synchronization (file-based)
- Modern, user-friendly interface

---

## Requirements
- Java 17 or higher (JDK)
- Gradle (wrapper included, no need to install separately)
- Internet connection for first build (to download dependencies)

---

## Build Instructions

1. **Clone or extract the project folder**
   - If you received a `.zip` or `.tar.gz`, extract it to your desired location.

2. **Open a terminal/command prompt in the project root directory.**

3. **Build the project using Gradle:**
   ```sh
   ./gradlew build   # On Linux/Mac
   gradlew.bat build # On Windows
   ```
   This will compile the code and run any tests.

---

## Run Instructions

1. **Run the application using Gradle:**
   ```sh
   ./gradlew run   # On Linux/Mac
   gradlew.bat run # On Windows
   ```
   The application window should appear.

2. **Alternatively, run the generated JAR (if available):**
   - After building, look for the JAR file in `build/libs/`.
   - Run with:
     ```sh
     java -jar build/libs/FamilyBudgetApp.jar
     ```

---

## Notes
- The application stores data using a local database (via JPA/Hibernate) and supports file-based synchronization for multi-device use.
- If you encounter issues with JavaFX, ensure your JDK includes JavaFX or add the required modules to your run configuration.
- For screenshots and detailed documentation, see `REPORT.md`.

---

## Contact
For questions or support, contact the project maintainer or refer to the report.

## Sample Data

A sample data file is included to help you quickly test and explore the Family Budget App's features.

**File:** `sample-data.csv`

**How to use:**
- If your app supports importing CSV files, use the import feature to load `sample-data.csv`.
- If not, you can manually add these transactions to your database for demo purposes.
- The file contains a variety of income and expense transactions with different categories and dates.

**Sample entries:**
```
Date,Description,Amount,Type,Tags
2024-05-01,Salary,2000,Income,Salary
2024-05-03,Groceries,-150,Expense,Food
... (more in the file)
```

Feel free to modify or extend this file for your own testing or demonstration needs. 