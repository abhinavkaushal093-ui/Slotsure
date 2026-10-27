# Slotsure 📅

Slotsure is a desktop-based resource booking and session management application. Built as a Project-Based Learning (PBL) initiative, it provides a clean, user-friendly interface for scheduling resources, managing active sessions, and preventing booking conflicts. 

## Features
* **Secure User Authentication:** Dedicated login system for secure access.
* **Interactive Dashboard:** A centralized JavaFX dashboard to view and manage all scheduling activities.
* **Conflict Prevention System:** Built-in logic to detect and prevent double-booking of resources.
* **Resource & Session Tracking:** Easily monitor available resources and active user sessions.

## Tech Stack & Architecture
This project follows a structured **Model-View-Controller (MVC)** architectural pattern combined with a Service and Repository layer to ensure clean, maintainable code.
* **Frontend (View):** JavaFX, FXML, and CSS for a responsive desktop UI.
* **Backend (Controller/Service):** Java handling the core business logic and exception handling.
* **Data Access (Repository):** Custom database configuration handling MySQL queries.
* **Database:** MySQL relational database.
* **Build Tool:** Maven for dependency management and compiling.
* **Testing:** JUnit for business logic verification.

## 📥 Download and Run (No Setup Required!)
Don't want to build from source? You can download the ready-to-use application directly:

1. Go to the [Releases](https://github.com/abhinavkaushal093-ui/Slotsure/releases) tab on the right side of this page.
2. Download the latest `.jar` file.
3. Ensure you have Java 11+ installed and a local MySQL server running with the `slotsure_db` database configured (see **Database Setup** below).
4. Double-click the `.jar` file to run the application!

## Prerequisites
To run this application locally, you will need the following installed on your machine:
* [Java Development Kit (JDK) 11 or higher](https://www.oracle.com/java/technologies/downloads/)
* [Maven](https://maven.apache.org/download.cgi)
* [MySQL Server](https://dev.mysql.com/downloads/mysql/)

## Database Setup
Before running the application, set up the local MySQL database.
1. Create the database:
   ```sql
   CREATE DATABASE slotsure_db;
   USE slotsure_db;

2. Create the required tables: 
```
CREATE TABLE `app_users` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `full_name` varchar(120) NOT NULL,
  `email` varchar(190) NOT NULL UNIQUE,
  `password_hash` varchar(255) NOT NULL,
  `role` enum('ADMIN','STAFF','FACULTY') NOT NULL,
  `is_active` tinyint(1) NOT NULL DEFAULT 1,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
);

CREATE TABLE `resources` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `name` varchar(120) NOT NULL,
  `resource_type` varchar(40) NOT NULL,
  `location` varchar(150) NOT NULL,
  `capacity` int(11) NOT NULL DEFAULT 1,
  `is_active` tinyint(1) NOT NULL DEFAULT 1,
  UNIQUE KEY `uq_resource_name_location` (`name`,`location`)
);

CREATE TABLE `bookings` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `resource_id` bigint(20) NOT NULL,
  `booked_by` bigint(20) NOT NULL,
  `start_at` datetime NOT NULL,
  `end_at` datetime NOT NULL,
  `purpose` varchar(255) NOT NULL,
  `status` enum('CONFIRMED','CANCELLED') NOT NULL DEFAULT 'CONFIRMED',
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  FOREIGN KEY (`resource_id`) REFERENCES `resources` (`id`),
  FOREIGN KEY (`booked_by`) REFERENCES `app_users` (`id`)
);

CREATE TABLE `booking_audit` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `booking_id` bigint(20) NOT NULL,
  `actor_user_id` bigint(20) NOT NULL,
  `action` varchar(40) NOT NULL,
  `details` varchar(500) DEFAULT NULL,
  `occurred_at` timestamp NOT NULL DEFAULT current_timestamp(),
  FOREIGN KEY (`booking_id`) REFERENCES `bookings` (`id`),
  FOREIGN KEY (`actor_user_id`) REFERENCES `app_users` (`id`)
);
```
3. Insert default test users (so you can log in):
```
INSERT INTO `app_users` (`full_name`, `email`, `password_hash`, `role`) VALUES
('Development User', 'dev@slotsure.local', 'DEV_ONLY_NO_LOGIN', 'ADMIN'),
('Abhinav (Student)', 'student@college.edu', 'pass123', 'STAFF'),
('Prof. Rakesh', 'faculty@college.edu', 'admin123', 'FACULTY');
```

## Installation & Usage

### Prerequisites
Ensure you have the following installed on your local machine:
* **Java Development Kit (JDK):** Version 11 or higher
* **Maven:** For dependency management and building the project
* **MySQL Server:** For the local database

### Local Setup
1. **Clone the repository:**
   ```bash
   git clone [https://github.com/abhinavkaushal093-ui/Slotsure.git](https://github.com/abhinavkaushal093-ui/Slotsure.git)
2. **Navigate into the project directory:**
   ```bash
   cd Slotsure
3. **Configure the Database:**
   * Open MySQL and create a database named `slotsure_db` (or your preferred name).
   * Open `src/main/java/com/slotsure/config/Database.java` and update the database URL, username, and password to match your local setup.

4. **Build the project:**
   ```bash
   mvn clean install
5. **Run the application:**
```bash
mvn javafx:run
(Note: You can also run the App.java file directly from your IDE like VS Code or IntelliJ)
```
## Project Structure

```text
Slotsure/
├── src/
│   ├── main/
│   │   ├── java/com/slotsure/
│   │   │   ├── config/          # Database connection settings
│   │   │   ├── controller/      # UI logic (DashboardController, LoginController)
│   │   │   ├── model/           # Business entities (Booking, Resource, Session)
│   │   │   ├── repository/      # Data access layer for MySQL operations
│   │   │   ├── service/         # Core business logic and validations
│   │   │   └── App.java         # Main application entry point
│   │   │
│   │   └── resources/com/slotsure/
│   │       ├── dashboard.fxml   # JavaFX layout for the main dashboard
│   │       ├── login.fxml       # JavaFX layout for the login screen
│   │       └── styles.css       # Custom UI styling
│   │
│   └── test/java/com/slotsure/
│       └── service/             # JUnit test cases (BookingServiceTest)
│
├── pom.xml                      # Maven configuration and dependencies
└── README.md                    # Project documentation
