# 🏥 MediBook — Medical Appointment Booking System

A full-stack web application for booking doctor appointments online,
built with **Java Servlets**, **JDBC**, **MySQL** and **Bootstrap 5**.

![Java](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=java&logoColor=white)
![MySQL](https://img.shields.io/badge/MySQL-00000F?style=for-the-badge&logo=mysql&logoColor=white)
![Bootstrap](https://img.shields.io/badge/Bootstrap-563D7C?style=for-the-badge&logo=bootstrap&logoColor=white)
![Tomcat](https://img.shields.io/badge/Apache%20Tomcat-F8DC75?style=for-the-badge&logo=apachetomcat&logoColor=black)

---

## 📌 About The Project

**MediBook** is a healthcare web application that allows users to find doctors,
book appointments, and make dummy payments — all from a clean, responsive interface.
Admins can manage the doctor listings through a dedicated admin panel.

---

## 🌟 Features

### 👤 User Side
- Register and login with role-based access (User / Admin)
- Browse doctors with animated **flip card UI**
- **Search** by name, specialization or hospital
- **Filter** by fees range, experience and specialization
- Book appointments with **date picker** and **time slot** selection
- **Dummy payment gateway** — UPI, Credit/Debit Card, Net Banking
- Session-based login with auto logout after 30 minutes

### 🔑 Admin Side
- Secure admin login via role selector
- Add new doctors with full details and photo URL
- **Inline edit** doctor details without page reload
- **Soft delete** — doctors are hidden but never permanently removed
- **Restore** soft-deleted doctors anytime
- All data dynamically loaded via **JSON API** — no page reloads

### ✅ Validation
- **Frontend (JavaScript):** Live validation on all fields —
  email format, phone 10 digits, password strength meter,
  confirm password match, card number auto-formatting,
  UPI ID format, card expiry check, Sunday restriction on dates
- **Backend (Java):** Server-side validation as security net —
  rejects invalid data even if JS is bypassed

---

## 🛠️ Tech Stack

| Layer      | Technology                              |
|------------|-----------------------------------------|
| Frontend   | HTML5, CSS3, Bootstrap 5, JavaScript    |
| Backend    | Java Servlets (Jakarta EE)              |
| Database   | MySQL 8                                 |
| Server     | Apache Tomcat 11                        |
| IDE        | Eclipse IDE (Dynamic Web Project)       |

---

## 📁 Project Structure

```
MediBook/
│
├── src/main/java/in/edu/tint/it/medibook/
│   ├── Authenticate.java          → Login logic + session creation
│   ├── SignUp.java                → User registration + validation
│   ├── LogoutServlet.java         → Destroys session on logout
│   ├── GetSessionServlet.java     → Returns session info as JSON
│   ├── AdminDoctorServlet.java    → Doctor CRUD operations (JSON API)
│   └── DoctorServlet.java         → Doctor listing + appointment booking
│
└── src/main/webapp/
    ├── index.html                 → Home / Landing page
    ├── login.html                 → Login with User/Admin role selector
    ├── signup.html                → Registration with live JS validation
    ├── reset.html                 → Forgot password page
    ├── doctor.html                → Doctor listing + flip cards + booking
    ├── admin.html                 → Admin panel (add/edit/delete doctors)
    ├── ambulance.html             → Ambulance service page
    ├── hospital.html              → Hospital information page
    └── labrep.html                → Lab reports page
```

---

## 🗄️ Database Schema

```sql
CREATE DATABASE medibook;
USE medibook;

-- Users table
CREATE TABLE userdetails (
  uid      INT PRIMARY KEY,
  name     VARCHAR(100),
  phone    VARCHAR(15),
  address  VARCHAR(200),
  city     VARCHAR(50),
  state    VARCHAR(50),
  pincode  VARCHAR(10),
  email    VARCHAR(100),
  password VARCHAR(100)
);

-- Doctors table
CREATE TABLE doctors (
  id             INT AUTO_INCREMENT PRIMARY KEY,
  name           VARCHAR(100),
  specialization VARCHAR(100),
  experience     INT,
  hospital       VARCHAR(100),
  fees           DECIMAL(10,2),
  availability   VARCHAR(100),
  photo_url      VARCHAR(255),
  is_active      INT DEFAULT 1   -- 1 = active, 0 = soft deleted
);

-- Appointments table
CREATE TABLE appointments (
  id           INT AUTO_INCREMENT PRIMARY KEY,
  doctor_id    INT,
  patient_name VARCHAR(100),
  phone        VARCHAR(15),
  appt_date    DATE,
  time_slot    VARCHAR(20),
  reason       TEXT,
  fees         DECIMAL(10,2),
  status       VARCHAR(20) DEFAULT 'Pending',
  payment_ref  VARCHAR(50),
  created_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (doctor_id) REFERENCES doctors(id)
);
```

---

## 🔄 Application Flow

```
User visits index.html (Home page)
        ↓
Browses doctors freely — no login needed
        ↓
Clicks "Book Appointment" on a doctor card
        ↓
Not logged in? ──→ login.html ──→ redirected back to booking
Logged in?     ──→ Booking Modal opens directly
        ↓
Fills: Name, Phone, Date, Time Slot, Reason
        ↓
DoctorServlet saves appointment as "Pending" in DB
        ↓
Payment Modal opens
User selects: UPI / Card / Net Banking
        ↓
2 second dummy processing simulation
        ↓
DoctorServlet updates appointment status to "Confirmed"
Payment reference ID generated and stored
```

---

## 🔐 Role Based Access

| Role  | Redirect After Login | Access                                    |
|-------|----------------------|-------------------------------------------|
| User  | `index.html`         | Browse doctors, book appointments         |
| Admin | `admin.html`         | Add / Edit / Soft-delete / Restore doctors|

---

## 🚀 How to Run Locally

### Prerequisites
- Java JDK 17+
- Apache Tomcat 11
- MySQL 8+
- Eclipse IDE (Dynamic Web Project setup)
- MySQL Connector/J JAR

### Steps

**1. Clone the repository**
```bash
git clone https://github.com/dipan313/MediBook.git
```

**2. Import into Eclipse**
```
File → Import → Existing Projects into Workspace
Select the cloned MediBook folder → Finish
```

**3. Add MySQL Connector JAR**
```
Download: mysql-connector-j-x.x.x.jar
Place in: WebContent/WEB-INF/lib/
```

**4. Setup MySQL Database**
```sql
-- Run the full schema from the Database Schema section above
```

**5. Update DB credentials in each Servlet**
```java
// Find this line in all servlet files and update:
con = DriverManager.getConnection(
    "jdbc:mysql://localhost:3306/medibook",
    "your_username",    // ← change this
    "your_password"     // ← change this
);
```

**6. Run on Tomcat**
```
Right click project → Run As → Run on Server → Apache Tomcat 11
```

**7. Open in browser**
```
http://localhost:8080/MediBook/
```

---

## 📸 Pages Overview

| Page | Description |
|------|-------------|
| `index.html` | Landing page with services carousel and service cards |
| `login.html` | Login form with User / Admin role radio selector |
| `signup.html` | Registration form with live password strength meter |
| `doctor.html` | Flip card doctor grid with search, filters and booking modal |
| `admin.html` | Admin panel with insert form and inline editable doctor table |
| `reset.html` | Forgot password page |

---

## 💡 Key Concepts Used

| Concept | Where Used |
|---------|------------|
| MVC Architecture | Servlet = Controller, HTML = View, MySQL = Model |
| JDBC | All DB operations via PreparedStatement |
| HttpSession | Login state management across pages |
| Soft Delete | Doctors flagged with `is_active=0` instead of deleted |
| JSON API | Servlets return JSON, JS builds UI dynamically |
| Fetch API | All CRUD operations without page reload |
| Client-side Validation | Live field validation before form submission |
| Server-side Validation | Java safety net even if JS is bypassed |
| Separation of Concerns | HTML has zero Java, Servlets have zero HTML |
| Role Based Redirect | Admin → admin panel, User → home page |

---

## ⚠️ Known Limitations & Future Improvements

| Issue | Status | Suggested Fix |
|-------|--------|---------------|
| Passwords stored as plain text | ⚠️ Known | Use BCrypt hashing |
| DB credentials hardcoded | ⚠️ Known | Use `context.xml` or env variables |
| No email verification | ❌ Missing | Add JavaMail / OTP verification |
| Payment is dummy only | ❌ Simulated | Integrate Razorpay / Stripe API |
| No session guard on admin page | ⚠️ Known | Add servlet filter for auth check |
| UID entered manually by user | ⚠️ Known | Use AUTO_INCREMENT |

---

## 👨‍💻 Developer

**Dipan Mazumder**
- 🐙 GitHub: [@dipan313](https://github.com/dipan313)

---

## 📄 License

This project is built for **educational purposes** as a full-stack Java learning project.

---

> 🏥 Built with ❤️ using Java Servlets, MySQL, Bootstrap 5 and JavaScript