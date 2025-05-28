# Family Budget App – Project Report

## Introduction
The Family Budget App is a Java-based application designed to help families efficiently manage their finances. In today’s fast-paced world, keeping track of expenses, planning for future projects, and ensuring financial stability are essential for every household. This application addresses these needs by providing a comprehensive platform for recording transactions, managing budgets, tracking upcoming deadlines, and analyzing financial statistics.
A key goal of the project is to deliver a solution that is not only powerful and user-friendly but also highly extensible. The application is architected to support future enhancements, such as integration with mobile and web platforms, advanced analytics, and cloud-based synchronization. By leveraging modern development practices and tools—including JavaFX for the graphical interface, JPA/Hibernate for data persistence, and a modular, service-oriented architecture—the Family Budget App ensures maintainability, scalability, and ease of integration with new features.
The app is intended for use on multiple devices and is designed to facilitate synchronization of financial data across desktops and, potentially, other platforms. This flexibility makes it a robust foundation for both current needs and future growth, empowering families to take control of their financial well-being.
---

## Implemented Functionalities
The Family Budget App provides a comprehensive set of features to support effective family financial management. The following functionalities have been implemented in this version:

Transaction Management
Users can add, view, and manage financial transactions.
Each transaction can be associated with one or more tags (categories), which can be organized hierarchically (e.g., “Utilities > Electricity”).
Support for scheduled and recurring transactions, allowing users to plan for regular expenses or incomes (such as subscriptions, salaries, or loan repayments).
Loan amortization plans can be entered as a sequence of scheduled installments, with clear separation of repayment and interest portions.

Budget Management
Users can create and manage budgets for specific periods and categories.
The application tracks expenses and income against each budget, providing real-time feedback on budget status (spent, remaining, over-budget).
Budgets can be associated with tags/categories for detailed tracking.

Calendar of Deadlines
Future expenses and their due dates can be entered and tracked.
The application provides a dedicated view for upcoming deadlines, helping users avoid missed payments and plan ahead.

Statistics and Comparisons
The app processes transaction and budget data to generate insightful statistics.
Users can monitor their financial performance over time, compare different periods, and analyze spending across categories.
Visualizations such as tables and charts are provided for better understanding.

Advanced Search and Filtering
Transactions can be filtered by date range, tags (with AND/OR logic), and text search (description or amount).
Users can quickly locate specific transactions or analyze spending patterns using flexible filters.

Synchronization (File-Based)
The application supports data synchronization across devices using a file-based sync mechanism.
This ensures that users can access and update their financial data from multiple desktops, with future extensibility for cloud or mobile sync.

User Interface Highlights
The application features a modern, intuitive UI built with JavaFX.
Scheduled/recurring transactions are visually marked in the main transactions table.
Advanced filtering and search tools are easily accessible.
The interface is designed for clarity, ease of use, and future extensibility.


## Identified Responsibilities
The Family Budget App is designed with a modular architecture that separates concerns and assigns clear responsibilities to each major component. This approach ensures maintainability, scalability, and ease of future extension. The main responsibilities are divided as follows:

Controllers
Manage the user interface and handle user interactions.
Coordinate between the UI components (JavaFX views) and the underlying business logic.
Examples: TransactionsController, BudgetsController, ScheduledController, SettingsController.

Services
Encapsulate the business logic and core operations of the application.
Provide methods for creating, updating, deleting, and querying domain objects such as transactions, budgets, and tags.
Handle complex operations such as statistics calculation, budget status evaluation, and synchronization.
Examples: TransactionService, BudgetService, ScheduledTransactionService, SyncService.

Models
Represent the core data entities of the application.
Define the structure and relationships of data objects such as transactions, budgets, tags, and scheduled transactions.
Serve as the bridge between the application logic and data persistence layer.
Examples: Transaction, Budget, Tag, ScheduledTransaction, UserSettings.

Repositories
Manage data access and persistence.
Interact with the database using JPA/Hibernate to store and retrieve entities.
Abstract the details of data storage from the rest of the application, enabling easy changes to the persistence mechanism if needed.
Examples: TransactionRepository, BudgetRepository, TagRepository.

This clear separation of responsibilities ensures that each part of the application is focused on a specific aspect, making the codebase easier to understand, maintain, and extend.

## Developed Classes and Interfaces
The Family Budget App is organized into well-defined classes and interfaces, each with a clear responsibility. Below is a summary of the main components:
| Class/Interface | Responsibility |
|--------------------------------------|---------------------------------------------------------------------|
| Transaction | Represents a financial transaction, including date, amount, tags, etc. |
| ScheduledTransaction | Represents a recurring or future-dated transaction (e.g., loan installment, subscription). |
| Budget | Represents a budget for a specific period and category. |
| Tag | Represents a category or label for transactions and budgets; supports hierarchy. |
| UserSettings | Stores user preferences such as currency, locale, and sync settings. |
| TransactionService | Business logic for managing transactions (create, update, delete, query, calculate balances). |
| ScheduledTransactionService | Business logic for managing scheduled/recurring transactions. |
| BudgetService | Business logic for managing budgets, including status and alerts. |
| TagService | Business logic for managing tags/categories. |
| SyncService (interface) | Defines methods for synchronizing data across devices. |
| FileSyncService (implements SyncService) | Provides file-based synchronization implementation. |
| StatisticsService | Provides methods for generating statistics and reports. |
| ServiceFactory | Centralized factory for creating and managing service instances. |
| TransactionsController | Handles UI logic for transaction management. |
| BudgetsController | Handles UI logic for budget management. |
| ScheduledController | Handles UI logic for scheduled/recurring transactions. |
| SettingsController | Handles UI logic for application settings and synchronization. |
| BaseController | Abstract base class for shared controller logic. |
| TransactionRepository | Data access for transactions (JPA/Hibernate). |
| BudgetRepository | Data access for budgets (JPA/Hibernate). |
| TagRepository | Data access for tags (JPA/Hibernate). |
| ... | ... |
> Note:
> This table lists the most important classes and interfaces. Additional utility classes (e.g., for date/currency formatting, statistics models, etc.) are also present to support the main features.
Each class and interface is designed with a single, clear responsibility, following best practices for maintainability and extensibility.

## Data Organization and Persistence
The Family Budget App organizes and persists data using a robust, scalable approach based on Java Persistence API (JPA) and Hibernate. The main aspects of data organization and persistence are as follows:

Data Structure and Relationships
Entities: The core data entities include Transaction, ScheduledTransaction, Budget, Tag, and UserSettings.

Relationships:
Transactions can be associated with one or more Tags (categories), allowing for flexible categorization and hierarchical organization.
Budgets are linked to specific tags/categories and cover a defined time period.
ScheduledTransactions represent recurring or future-dated transactions and can also be tagged for categorization.
Tags can be organized hierarchically, supporting parent-child relationships for nested categories.
UserSettings stores preferences such as currency, locale, and synchronization options.

Persistence Mechanism
JPA/Hibernate: All main entities are annotated for JPA, enabling object-relational mapping and seamless integration with relational databases.
Repositories: Each entity has a corresponding repository (e.g., TransactionRepository, BudgetRepository) that handles data access and CRUD operations.
Entity Relationships: Relationships such as one-to-many (e.g., a tag with multiple transactions) and many-to-many (e.g., transactions with multiple tags) are mapped using JPA annotations.

Synchronization and Backup
File-Based Synchronization: The application supports synchronization of data across devices using a file-based sync mechanism. This allows users to keep their financial data consistent on multiple desktops.
Extensibility: The synchronization mechanism is designed to be extensible, allowing for future integration with cloud-based or mobile sync solutions.
Backup: Data can be backed up by copying the underlying database or sync files, ensuring data safety and recovery.

Data Integrity and Consistency
Transactions: All changes to data (add, update, delete) are managed through service classes, ensuring business rules and data integrity are enforced.
Atomic Operations: Database transactions are used to ensure that operations are atomic and consistent, reducing the risk of data corruption.
This approach ensures that data is well-structured, persistent, and ready for future growth, while also providing mechanisms for backup and multi-device synchronization.

## Extensibility and Integration Mechanisms
The Family Budget App is designed with extensibility and future integration in mind. The architecture and codebase employ several strategies to ensure that new features, device types, and integrations can be added with minimal effort:

Use of Interfaces and Abstract Classes
Core business logic is defined through interfaces (e.g., SyncService, BudgetService, TransactionService), allowing for multiple implementations.
Abstract base classes (such as BaseController) provide shared functionality for UI controllers, enabling code reuse and consistent behavior.

Factory and Service Patterns
The ServiceFactory class centralizes the creation and management of service instances. This makes it easy to swap or extend service implementations (e.g., replacing FileSyncService with a cloud-based sync service).
Services are injected into controllers, decoupling UI logic from business logic and making it easier to test and extend.

Modular and Layered Architecture
The application is organized into clear layers: UI (controllers), business logic (services), data (models/entities), and persistence (repositories).
Each layer interacts with others through well-defined interfaces, making it straightforward to add new features or replace existing components.

Adding New Features or Integrations
Example: Adding Cloud Synchronization
Implement a new class (e.g., CloudSyncService) that implements the SyncService interface.
Register the new service in the ServiceFactory.
The rest of the application can use the new sync method without any changes to the UI or business logic.
Example: Adding Mobile or Web Support
The modular design allows for the development of new front-ends (e.g., mobile app, web app) that can interact with the same service and data layers.

Plugin-Friendly Design
The use of interfaces and factories makes it possible to add plugins or extensions (e.g., new analytics modules, import/export tools) without modifying the core codebase.

Configuration and Settings
User preferences and application settings are managed through the UserSettings entity, making it easy to add new configurable options in the future.
This extensible design ensures that the Family Budget App can evolve to meet new requirements, integrate with other systems, and support additional platforms with minimal refactoring.

## SOLID Principles and Code Quality
The Family Budget App is developed with a strong emphasis on code quality and adherence to the SOLID principles, ensuring maintainability, scalability, and ease of future development.

Single Responsibility Principle (SRP)
Each class and method in the application is designed to have a single, well-defined responsibility.
For example, controllers manage UI logic, services handle business operations, and models represent data entities.

Open/Closed Principle (OCP)
The system is open for extension but closed for modification.
New features (such as additional synchronization methods or analytics modules) can be added by implementing interfaces without altering existing code.

Liskov Substitution Principle (LSP)
Interfaces and abstract classes are used so that new implementations can be substituted without affecting the correctness of the application.
For example, any class implementing SyncService can be used interchangeably in the application.

Interface Segregation Principle (ISP)
Interfaces are designed to be specific to the needs of the clients, avoiding large, monolithic interfaces.
Services such as TransactionService, BudgetService, and SyncService each define focused sets of operations.

Dependency Inversion Principle (DIP)
High-level modules do not depend on low-level modules; both depend on abstractions.
Controllers depend on service interfaces rather than concrete implementations, allowing for easy testing and extension.

Code Style and Modularity
The codebase follows consistent naming conventions and formatting for readability.
The project is organized into logical packages and layers (controllers, services, models, repositories).
Utility classes are used for common tasks (e.g., date and currency formatting), reducing code duplication.

Efficiency and Maintainability
Efficient data access and processing are ensured through the use of JPA/Hibernate and optimized queries.

The modular design and clear separation of concerns make the codebase easy to maintain and extend.
This commitment to SOLID principles and code quality ensures that the Family Budget App is robust, adaptable, and ready for future growth.

## Tools and Methodologies
The development of the Family Budget App leverages a range of modern tools and software engineering methodologies to ensure a robust, maintainable, and user-friendly application. The main tools and methodologies used include:

Programming Language and Frameworks
Java: The primary programming language for all application logic.
JavaFX: Used for building the graphical user interface, providing a modern and responsive user experience.
Build and Dependency Management
Gradle: Utilized as the build automation tool, managing dependencies, compiling code, and packaging the application for distribution.

Persistence and Data Management
JPA (Java Persistence API): Used for object-relational mapping and database interactions.
Hibernate: The JPA implementation chosen for efficient and reliable data persistence.

Architecture and Design Patterns
MVC (Model-View-Controller): The application follows the MVC pattern, separating concerns between data (models), business logic (services), and user interface (controllers/views).
Service and Factory Patterns: Services encapsulate business logic, and the factory pattern is used for service instantiation and management.

Version Control
Git: Used for source code management, enabling collaborative development and version tracking.
Other Utilities
Utility Classes: Custom utility classes are used for tasks such as date formatting, currency formatting, and statistics calculations.

Development Methodologies
Modular Design: The codebase is organized into logical modules and packages for clarity and maintainability.
SOLID Principles: The code adheres to SOLID principles to ensure extensibility and code quality.
Iterative Development: Features are developed and tested incrementally, allowing for continuous improvement and integration of feedback.

These tools and methodologies collectively contribute to the reliability, maintainability, and extensibility of the Family Budget App.

## Conclusion
The Family Budget App successfully delivers a comprehensive solution for managing household finances, combining robust functionality with a user-friendly interface. Through features such as transaction and budget management, scheduled and recurring transactions, deadline tracking, advanced statistics, and flexible search and filtering, the application empowers users to gain control over their financial planning and daily spending.

The project’s architecture is designed for extensibility and maintainability, following SOLID principles and leveraging modern development tools and patterns. The use of JavaFX for the UI, JPA/Hibernate for persistence, and a modular service-oriented structure ensures that the application is both reliable and ready for future enhancements.

While the current version provides all core functionalities required for effective family budget management, the design allows for seamless integration of new features, such as cloud synchronization, mobile or web interfaces, and advanced analytics. The file-based synchronization mechanism already lays the groundwork for multi-device support, and the clear separation of concerns makes it easy to extend or adapt the application as user needs evolve.
In summary, the Family Budget App is a solid foundation for ongoing development and real-world use, offering both immediate value and long-term flexibility for families seeking to improve their financial well-being.

## Screenshots (Optional but Recommended)
Include screenshots of the main UI screens to illustrate features.

---

## Appendix (Optional)
- Any additional notes, diagrams, or references. 