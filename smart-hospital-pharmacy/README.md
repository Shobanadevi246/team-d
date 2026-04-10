# 🏥 Smart Hospital Pharmacy System

A full-stack Hospital Pharmacy Management System built with **Spring Boot 3**, **Thymeleaf**, **Spring Security (JWT)**, **MySQL**, and **Bootstrap 5**.

---

## 📋 Features

### Admin Panel
- ✅ Hardcoded Admin login (`admin` / `Admin@123`)
- ✅ Admin Activities (login info, added products, stock history)
- ✅ Add / View Staff members
- ✅ Staff Details with medicines & transactions
- ✅ Add Medicine with **barcode scanner** + **voice input**
- ✅ Medicine Details with full stock history
- ✅ Stock In / Stock Out with expiry date update
- ✅ Low Stock Alert (< 10 units)
- ✅ Expiry Medicine Alert (within 10 days)
- ✅ Reports & Analysis with PDF + Excel download
- ✅ Fast-selling & slow-selling medicine analysis with charts
- ✅ Customer Feedback management
- ✅ Locked Account management (unlock staff)
- ✅ Notification bell for low stock + expiry alerts

### Staff Panel
- ✅ Staff login via email + password (created by admin)
- ✅ Forgot Password → OTP via Gmail → Reset Password
- ✅ Account locked after 3 failed login attempts
- ✅ All medicine features (add, view, stock in/out)
- ✅ Barcode scanner + Voice input for adding medicines
- ✅ Low Stock & Expiry alerts
- ✅ Reports with PDF + Excel download
- ✅ Customer Feedback submission

---

## 🔧 Tech Stack

| Layer | Technology |
|-------|-----------|
| Backend | Java 17, Spring Boot 3.2 |
| Security | Spring Security, JWT |
| Database | MySQL 8, JPA/Hibernate |
| Frontend | Thymeleaf, Bootstrap 5, Chart.js |
| Email | Spring Mail (Gmail SMTP) |
| PDF | iText 5 |
| Excel | Apache POI |

---

## 🚀 Setup & Run

### Prerequisites
- Java 17+
- Maven 3.8+
- MySQL 8.0+
- Gmail account with App Password

### Step 1: Build & Run
```bash
cd smart-hospital-pharmacy
mvn clean package -DskipTests
java -jar target/smart-hospital-pharmacy-1.0.0.jar
```
Or with Maven:
```bash
mvn spring-boot:run
```

By default, the app uses the bundled H2 database at `./data/smart_pharmacy`, so no MySQL setup is required for local use.

### Optional: Use MySQL Instead
Set these environment variables before starting the app:

```properties
SPRING_DATASOURCE_URL=jdbc:mysql://localhost:3306/smart_pharmacy?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
SPRING_DATASOURCE_USERNAME=root
SPRING_DATASOURCE_PASSWORD=YOUR_MYSQL_PASSWORD
SPRING_DATASOURCE_DRIVER_CLASS_NAME=com.mysql.cj.jdbc.Driver
SPRING_JPA_HIBERNATE_DIALECT=org.hibernate.dialect.MySQLDialect
```

### Optional: Configure Email / AI Keys
```properties
SPRING_MAIL_USERNAME=your-email@gmail.com
SPRING_MAIL_PASSWORD=YOUR_16_CHAR_APP_PASSWORD
ANTHROPIC_API_KEY=your-api-key
```

### Step 2: Access the Application
Open: **http://localhost:8080**

---

## 🔐 Login Credentials

### Admin Login
| Field | Value |
|-------|-------|
| Username | `admin` |
| Password | `Admin@123` |

### Staff Login
Staff accounts are created by the admin. Staff login using **email** and password.

---

## 📏 Password Policy
All passwords must contain:
- At least **1 uppercase** letter
- At least **1 number**
- At least **1 special character** (`!@#$%^&*` etc.)
- Minimum **8 characters**

Example valid password: `Hospital@123`

---

## 📧 OTP Email Setup (Gmail)

1. Enable 2-Factor Authentication on your Gmail
2. Go to: **Google Account → Security → 2-Step Verification → App Passwords**
3. Select app: **Mail**, device: **Other** → name it "Smart Pharmacy"
4. Copy the 16-character app password
5. Paste it in `spring.mail.password` in application.properties

---

## 📁 Project Structure

```
smart-hospital-pharmacy/
├── src/main/java/com/pharmacy/
│   ├── SmartPharmacyApplication.java
│   ├── config/
│   │   ├── SecurityConfig.java
│   │   └── WebMvcConfig.java
│   ├── controller/
│   │   ├── AdminController.java
│   │   ├── AuthController.java
│   │   └── StaffController.java
│   ├── entity/
│   │   ├── Staff.java
│   │   ├── Medicine.java
│   │   ├── StockTransaction.java
│   │   ├── OtpToken.java
│   │   └── CustomerFeedback.java
│   ├── repository/ (5 JPA repositories)
│   ├── security/
│   │   ├── CustomUserDetailsService.java
│   │   └── JwtAuthenticationFilter.java
│   ├── service/
│   │   ├── EmailService.java
│   │   ├── FeedbackService.java
│   │   ├── MedicineService.java
│   │   ├── OtpService.java
│   │   ├── ReportService.java
│   │   └── StaffService.java
│   └── util/
│       ├── JwtUtil.java
│       └── SkuGenerator.java
├── src/main/resources/
│   ├── application.properties
│   └── templates/
│       ├── auth/          (login, forgot-password, verify-otp, reset-password)
│       ├── admin/         (dashboard, activities, staff-*, medicine-*, stock, alerts, reports, feedback, locked-accounts)
│       ├── staff/         (dashboard, medicine-*, stock, alerts, reports, feedback)
│       └── fragments/     (layout with sidebars, headers, shared CSS)
└── pom.xml
```

---

## 🌟 Special Features

### Barcode Scanner
- Click "Scan Barcode" button on Add Medicine page
- Camera opens in-browser using WebRTC
- Requires HTTPS or localhost for camera access

### Voice Input
- Click "Voice Input" or microphone icon
- Say the medicine name clearly
- Supported in Chrome and Edge browsers

### Auto SKU Generation
- Leave SKU field blank when adding medicine
- System auto-generates: `MED-[3-letter prefix]-[8 random chars]`
- Example: `MED-PAR-A1B2C3D4`

---

## 🔒 Security Features

- JWT tokens stored in HttpOnly cookies
- BCrypt password hashing
- Account lockout after 3 failed login attempts
- Admin-only unlock of locked staff accounts
- Role-based access control (ADMIN / STAFF)
- CSRF protection (disabled only for API endpoints)

---

## 📊 Reports

All reports available as PDF and Excel:
- Medicine Inventory Report
- Staff Details Report  
- Customer Feedback Report
- Complete System Report (all data, multi-sheet Excel)

Analysis charts:
- Fast-selling medicines (bar chart)
- Slow/non-selling medicines (bar chart)
- Feedback rating analysis by medicine (doughnut chart)

---

## 👨‍💻 Developed By
Smart Hospital Pharmacy System — Full-Stack Java Spring Boot Application
