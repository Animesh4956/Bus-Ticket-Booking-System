# 🚌 Bus Ticket Booking System (Java Spring Boot)

A full-stack, local web-based **Bus Ticket Booking System** built with **Java 21**, **Spring Boot**, **Thymeleaf**, **Spring Data JPA**, and **OpenPDF**.

---

## 🚀 Quick Start (Running Locally)

The server is already configured and can be run immediately!

### Option 1: Double-Click Batch File
Simply double-click **`run.bat`** in this project folder:
[run.bat](file:///C:/Users/anime/OneDrive/Desktop/BUS%20TICKET%20BOOKING%20SYSTEM/run.bat)

### Option 2: Terminal / Command Prompt
In the project directory, run:
```bash
java -jar target/busbooking-0.0.1-SNAPSHOT.jar
```
Or with Maven:
```bash
mvn spring-boot:run
```

Once started, open your web browser and go to:
👉 **`http://localhost:8080`**

---

## 🔑 Pre-Configured Accounts (Ready Out-of-the-Box)

The system is pre-seeded with sample accounts and buses so you can test immediately:

| Role | Username | Password | Purpose / Permissions |
| :--- | :--- | :--- | :--- |
| **Admin** | `admin` | `admin123` | View total revenue, fleet occupancy rate, passenger lists per bus, add/delete bus routes |
| **Dummy User 1** | `john` | `user123` | Search buses, interactive seat selection, view ticket history, download PDF tickets |
| **Dummy User 2** | `sarah` | `user123` | Search buses, book seats, view tickets, download PDF tickets |

> **Note:** Any visitor can also register a brand-new user account via the **Register** page!

---

## ✨ Features Included

1. **User Authentication & Role-Based Access:**
   * Session-based login/logout for both Admin and standard Users.
   * Direct "One-Click Quick Fill" buttons on the login screen for testing convenience.

2. **Bus Search & Filter:**
   * Filter available routes by Source city, Destination city, and Travel date.
   * Real-time seat availability indicator on bus cards.

3. **Interactive Bus Cabin Seat Picker:**
   * 32-seat bus cabin layout simulator with driver cabin and aisle.
   * Color-coded seats:
     - 🟢 **Green:** Available for booking
     - 🟣 **Indigo:** Selected seat
     - 🔴 **Red:** Already booked (disabled)

4. **Official Downloadable PDF Tickets:**
   * Generates a professional e-ticket / boarding pass with:
     - Unique PNR reference (e.g. `TKT-100234`)
     - Origin & destination with departure/arrival times
     - Passenger name, age, and assigned seat number
     - Total fare paid
     - Boarding instructions & passenger safety policies
   * Downloadable directly from the confirmation screen or user dashboard.

5. **Customer Dashboard:**
   * Displays all past and active bookings.
   * Allows downloading the PDF boarding pass or cancelling a booking anytime.

6. **Admin Dashboard & Passenger Manifest:**
   * **Key Metrics:** Total active buses, confirmed bookings count, and total gross revenue.
   * **Occupancy Tracking:** Visual progress bars displaying how many people have booked each bus.
   * **Passenger Manifest:** Click **"View Passenger List"** on any bus to see all registered passengers (name, age, gender, contact phone, ticket reference).
   * **Fleet Manager:** Add new bus routes and schedules with fares and departure times.

7. **Zero-Setup Embedded Database:**
   * Uses an in-memory H2 database.
   * Web-based database console accessible at `http://localhost:8080/h2-console` (JDBC URL: `jdbc:h2:mem:busbookingdb`, user: `sa`, password: `password`).
