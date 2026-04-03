package in.edu.tint.it.medibook;

import java.io.IOException;
import java.sql.*;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

@WebServlet("/SignUp")
public class SignUp extends HttpServlet {

    private static final long serialVersionUID = 1L;

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // ── Step 1: Read all fields ──────────────────────────────
        String uidStr          = request.getParameter("uid");
        String name            = request.getParameter("name");
        String phone           = request.getParameter("phone");
        String address         = request.getParameter("address");
        String city            = request.getParameter("city");
        String state           = request.getParameter("state");
        String pincode         = request.getParameter("pincode");
        String email           = request.getParameter("email");
        String password        = request.getParameter("password");
        String confirmPassword = request.getParameter("confirm_password");

        System.out.println("===========================================");
        System.out.println("[SignUp] doPost called");
        System.out.println("[SignUp] uid     : " + uidStr);
        System.out.println("[SignUp] name    : " + name);
        System.out.println("[SignUp] phone   : " + phone);
        System.out.println("[SignUp] email   : " + email);
        System.out.println("[SignUp] pincode : " + pincode);
        System.out.println("[SignUp] password: " + (password != null ? "****" : "NULL"));

        // ── Step 2: Server-side validation ──────────────────────
        String errorCode = null;

        // UID check
        if (uidStr == null || uidStr.trim().isEmpty()) {
            errorCode = "uid_empty";
            System.out.println("[SignUp] ❌ UID is empty");
        } else if (!uidStr.trim().matches("\\d+")) {
            errorCode = "uid_invalid";
            System.out.println("[SignUp] ❌ UID is not numeric: " + uidStr);
        }

        // Name check
        else if (name == null || name.trim().length() < 3) {
            errorCode = "name_invalid";
            System.out.println("[SignUp] ❌ Name invalid: " + name);
        }

        // Phone check — exactly 10 digits
        else if (phone == null || !phone.trim().matches("\\d{10}")) {
            errorCode = "phone_invalid";
            System.out.println("[SignUp] ❌ Phone invalid: " + phone);
        }

        // Email check — proper format
        else if (email == null ||
                 !email.trim().matches(
                   "^[a-zA-Z0-9._%+\\-]+@[a-zA-Z0-9.\\-]+\\.[a-zA-Z]{2,}$")) {
            errorCode = "email_invalid";
            System.out.println("[SignUp] ❌ Email invalid: " + email);
        }

        // Address check
        else if (address == null || address.trim().length() < 5) {
            errorCode = "address_invalid";
            System.out.println("[SignUp] ❌ Address invalid: " + address);
        }

        // City check
        else if (city == null || city.trim().isEmpty()) {
            errorCode = "city_invalid";
            System.out.println("[SignUp] ❌ City invalid: " + city);
        }

        // State check
        else if (state == null || state.trim().isEmpty()) {
            errorCode = "state_invalid";
            System.out.println("[SignUp] ❌ State invalid: " + state);
        }

        // Pincode check — exactly 6 digits
        else if (pincode == null || !pincode.trim().matches("\\d{6}")) {
            errorCode = "pincode_invalid";
            System.out.println("[SignUp] ❌ Pincode invalid: " + pincode);
        }

        // Password length check — min 6 characters
        else if (password == null || password.length() < 6) {
            errorCode = "password_short";
            System.out.println("[SignUp] ❌ Password too short: length="
                + (password != null ? password.length() : 0));
        }

        // Password match check
        else if (!password.equals(confirmPassword)) {
            errorCode = "password_mismatch";
            System.out.println("[SignUp] ❌ Passwords do not match");
        }

        // ── Step 3: If any validation failed — redirect back ─────
        if (errorCode != null) {
            System.out.println("[SignUp] ❌ Validation failed: " + errorCode);
            System.out.println("[SignUp] Redirecting to signup.html?error=" + errorCode);
            System.out.println("===========================================");
            response.sendRedirect("signup.html?error=" + errorCode);
            return;
        }

        // ── Step 4: All valid — insert into DB ───────────────────
        int uid = Integer.parseInt(uidStr.trim());

        Connection con       = null;
        PreparedStatement ps = null;
        boolean success      = false;

        try {
            System.out.println("[SignUp] Connecting to DB...");
            Class.forName("com.mysql.cj.jdbc.Driver");
            con = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/medibook", "root", "Dipan@2005");
            System.out.println("[SignUp] ✅ DB connected");

            String sql = "INSERT INTO userdetails "
                       + "(uid, name, phone, address, city, state, "
                       + "pincode, email, password) "
                       + "VALUES (?,?,?,?,?,?,?,?,?)";

            ps = con.prepareStatement(sql);
            ps.setInt(1, uid);
            ps.setString(2, name.trim());
            ps.setString(3, phone.trim());
            ps.setString(4, address.trim());
            ps.setString(5, city.trim());
            ps.setString(6, state.trim());
            ps.setString(7, pincode.trim());
            ps.setString(8, email.trim());
            ps.setString(9, password);

            int rows = ps.executeUpdate();
            if (rows > 0) {
                success = true;
                System.out.println("[SignUp] ✅ User registered: " + email);
            } else {
                System.out.println("[SignUp] ❌ Insert returned 0 rows");
            }

        } catch (SQLIntegrityConstraintViolationException e) {
            // ✅ Duplicate UID or Email
            System.out.println("[SignUp] ❌ Duplicate entry: " + e.getMessage());
            response.sendRedirect("signup.html?error=duplicate");
            return;
        } catch (Exception e) {
            System.out.println("[SignUp] ❌ DB ERROR: " + e.getMessage());
            e.printStackTrace();
            response.sendRedirect("signup.html?error=db");
            return;
        } finally {
            try { if (ps  != null) ps.close();  } catch (SQLException e) {}
            try { if (con != null) con.close(); } catch (SQLException e) {}
        }

        if (success) {
            System.out.println("[SignUp] ✅ Redirecting to login.html");
            System.out.println("===========================================");
            response.sendRedirect("login.html?registered=1");
        } else {
            System.out.println("[SignUp] ❌ Registration failed");
            System.out.println("===========================================");
            response.sendRedirect("signup.html?error=failed");
        }
    }
}