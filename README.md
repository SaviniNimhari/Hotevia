# Hotevia – Smart Hotel Reservation & Management System

> **Enterprise-grade Desktop Application for Hotel Operations & Reservations**  
> Built with **Java 21**, **JavaFX**, **JDBC**, **MySQL**, and **Maven** using the **Model-View-Controller (MVC)** Architecture.

---

## 🏨 1. Project Overview

**Hotevia** is a centralized Java desktop application engineered for modern hotel hospitality workflows. It delivers an intuitive, high-performance desktop experience for hotel administrators and front-desk receptionists without relying on web stacks (No React, Python, or PHP required).

### Key Highlights
- **Architecture**: Strict MVC (Model-View-Controller) separation with clean DAO & Service layers.
- **Design Theme**: Executive luxury aesthetics (Dark Navy Blue `#0F172A`, Regal Gold `#D4AF37`, and Crisp Off-White cards).
- **Security**: SHA-256 password hashing with salt, role-based authorization (Admin vs Receptionist).
- **Double Booking Prevention**: Enforced availability check across existing active reservations for overlapping stay dates.
- **Receipt Generation**: Built-in official formatted reservation confirmation and payment receipts.

---

## 🗂️ 2. Project Directory Structure

```
Hotevia/
├── src/
│   └── main/
│       ├── java/
│       │   └── com/hotevia/
│       │       ├── Main.java                        # JavaFX Application Entry Point
│       │       ├── config/
│       │       │   └── DatabaseConnection.java       # Singleton JDBC Manager & Auto-Schema Initializer
│       │       ├── model/
│       │       │   ├── User.java                     # System staff entity
│       │       │   ├── Customer.java                 # Guest profiles entity
│       │       │   ├── Room.java                     # Physical hotel room entity
│       │       │   ├── RoomCategory.java             # Room tiers & base rate entity
│       │       │   ├── Reservation.java              # Booking records entity
│       │       │   ├── Payment.java                  # Financial transactions entity
│       │       │   ├── CheckInOut.java               # Arrival/departure tracking entity
│       │       │   └── HotelSetting.java             # Hotel metadata & tax configuration
│       │       ├── dao/
│       │       │   ├── UserDAO.java
│       │       │   ├── CustomerDAO.java
│       │       │   ├── RoomDAO.java
│       │       │   ├── RoomCategoryDAO.java
│       │       │   ├── ReservationDAO.java
│       │       │   ├── PaymentDAO.java
│       │       │   ├── CheckInOutDAO.java
│       │       │   └── HotelSettingDAO.java
│       │       ├── controller/
│       │       │   ├── LoginController.java          # Authentication & session dispatch
│       │       │   ├── MainLayoutController.java     # Sidebar navigation & dynamic views
│       │       │   ├── DashboardController.java      # KPIs, occupancy pie charts, recent activity
│       │       │   ├── RoomController.java           # Inventory CRUD & status management
│       │       │   ├── CategoryController.java       # Room tier definitions & pricing
│       │       │   ├── CustomerController.java       # Guest registry & stay history
│       │       │   ├── ReservationController.java    # Availability search & booking engine
│       │       │   ├── CheckInOutController.java     # Front desk arrival & departure handler
│       │       │   ├── PaymentController.java        # Balance settlement & payment receipts
│       │       │   ├── ReportController.java         # Revenue bar charts & occupancy statistics
│       │       │   ├── UserController.java           # Staff accounts & permissions (Admin)
│       │       │   └── SettingsController.java       # Hotel branding, taxes & password reset
│       │       ├── service/
│       │       │   ├── AuthenticationService.java
│       │       │   ├── ReservationService.java
│       │       │   ├── PaymentService.java
│       │       │   └── ReportService.java
│       │       └── util/
│       │           ├── PasswordUtil.java             # SHA-256 cryptographic utility
│       │           ├── ValidationUtil.java           # Regex, date ranges & numeric validations
│       │           ├── ReceiptGenerator.java         # ASCII & printable receipt compiler
│       │           └── AlertUtil.java                # Styled JavaFX modal alerts
│       └── resources/
│           ├── db/
│           │   └── schema.sql                        # Complete DDL & initial seed data
│           ├── views/
│           │   ├── login.fxml
│           │   ├── main_layout.fxml
│           │   ├── dashboard.fxml
│           │   ├── rooms.fxml
│           │   ├── categories.fxml
│           │   ├── customers.fxml
│           │   ├── reservations.fxml
│           │   ├── checkinout.fxml
│           │   ├── payments.fxml
│           │   ├── reports.fxml
│           │   ├── users.fxml
│           │   └── settings.fxml
│           ├── styles/
│           │   └── style.css                         # Navy & Gold modern theme stylesheet
│           └── images/
├── pom.xml                                           # Maven build & JavaFX dependencies
└── README.md                                         # Documentation & setup guide
```

---

## 📦 3. System Modules

| Module | Purpose |
|---|---|
| **01. Dashboard** | Real-time KPIs (Total Rooms, Available, Occupied, Today's Check-ins & Check-outs, Total Revenue), Room Distribution Chart, and Recent Reservations table. |
| **02. Room Management** | Manage physical inventory: room numbers, floor levels, category assignment, and status (`AVAILABLE`, `OCCUPIED`, `RESERVED`, `MAINTENANCE`). |
| **03. Room Categories** | Configure room types (`Standard Room`, `Deluxe Room`, `Suite Room`, `Family Room`), prices per night, descriptions, and capacity limits. |
| **04. Customer Management** | Register guests, store telephone, email, ID/passport, address, and view historical stays. |
| **05. Reservation Management** | Dynamic booking with real-time date availability, automated cost calculation including taxes/service fees, reservation code generator (`HTV-YYYY-XXXX`), and double-booking prevention. |
| **06. Check-in & Check-out** | Front desk operational console to confirm guest arrivals, record key issues, audit notes, verify balance clearance before departure, and release rooms to housekeeping. |
| **07. Payment Management** | Process cash, credit card, debit card, or online transfers; calculate balances; and view/print official payment receipts. |
| **08. Reports & Analytics** | Monthly revenue inflow Bar Chart, payment channel Pie Chart, and printable executive summary reports. |
| **09. User Management** | Secure administrator control over staff credentials, activation/deactivation, and roles (`ADMIN`, `RECEPTIONIST`). |
| **10. System Settings** | Hotel property contact info, base currency symbol, tax and service charges, and personal password changes. |

---

## 🛠️ 4. Prerequisites & Setup

### Prerequisites
1. **Java Development Kit (JDK)**: Java 21 LTS (or higher).
2. **Database**: MySQL Server 8.x or MariaDB (via XAMPP, WAMP, or standalone MySQL).
3. **IDE**: IntelliJ IDEA (Ultimate or Community) or NetBeans / Eclipse with Maven support.

### Database Setup
1. Start your MySQL Server (e.g., click **Start** next to MySQL in the **XAMPP Control Panel**).
2. The application includes **Auto-Schema Initialization**: When you launch the app, `DatabaseConnection.java` will automatically create the `hotevia_db` database and execute `src/main/resources/db/schema.sql` if not already present.
3. *Alternative manual import*: Open **phpMyAdmin** or your MySQL command line client and execute:
   ```sql
   SOURCE src/main/resources/db/schema.sql;
   ```

### Default Credentials
| Role | Username | Password |
|---|---|---|
| **Administrator** | `admin` | `admin123` |
| **Receptionist** | `receptionist` | `recep123` |

*(All passwords are cryptographically verified using SHA-256).*

---

## 🚀 5. How to Run in IntelliJ IDEA

1. Open **IntelliJ IDEA**.
2. Select **File -> Open...** and choose the `Hotevia` root directory (`d:\desktop\My own projects\Hotevia`).
3. IntelliJ will automatically detect `pom.xml` and download the dependencies (JavaFX 21 and MySQL Connector/J).
4. Navigate to `src/main/java/com/hotevia/Main.java`.
5. Right-click `Main.java` and select **Run 'Main.main()'**.
6. The Hotevia Login Window will appear. Log in using `admin` / `admin123`.

---

## 💻 6. How to Run via Maven Command Line

If you have Apache Maven installed in your terminal PATH:
```bash
# Compile and run via javafx-maven-plugin
mvn clean javafx:run
```
To run with custom database credentials:
```bash
mvn javafx:run -Ddb.host=localhost -Ddb.port=3306 -Ddb.name=hotevia_db -Ddb.user=root -Ddb.password=
```

---

## 🛡️ 7. Business Logic & Validation Rules

- **Double Booking Prevention**: When choosing dates for a room, the system queries active reservations (`CONFIRMED`, `CHECKED_IN`) and ensures that `(res.check_in_date < targetCheckOut) AND (res.check_out_date > targetCheckIn)` does not overlap.
- **Check-Out Balance Guard**: A guest cannot be checked out until their outstanding balance is `$0.00`.
- **Role Permissions**: Receptionists have access to operational modules (Customers, Rooms, Reservations, Check-in/out, Payments, Reports), while User Management is restricted to Administrators.
