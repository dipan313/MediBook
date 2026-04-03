package in.edu.tint.it.medibook;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

@WebServlet("/DoctorPage")
public class DoctorServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private Connection getConnection() throws Exception {
        Class.forName("com.mysql.cj.jdbc.Driver");
        return DriverManager.getConnection(
            "jdbc:mysql://localhost:3306/medibook", "root", "Dipan@2005");
    }

    // ── GET: load doctors or serve doctor.html ────────────────────
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getParameter("action");
        if ("getAll".equals(action)) {
            StringBuilder json = new StringBuilder("[");
            Connection con = null; PreparedStatement ps = null; ResultSet rs = null;
            try {
                con = getConnection();
                ps  = con.prepareStatement(
                    "SELECT * FROM doctors WHERE is_active=1 ORDER BY name ASC");
                rs  = ps.executeQuery();
                boolean first = true;
                while (rs.next()) {
                    if (!first) json.append(",");
                    json.append(doctorToJson(rs));
                    first = false;
                }
            } catch (Exception e) {
                e.printStackTrace();
                json = new StringBuilder("[]");
            } finally { closeAll(rs, ps, con); }
            json.append("]");
            sendJson(response, json.toString());
            return;
        }
        request.getRequestDispatcher("doctor.html").forward(request, response);
    }

    // ── POST: bookAppointment OR confirmPayment ───────────────────
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getParameter("action");

        // ── ACTION 1: Book appointment (saves as pending + unpaid) ──
        if ("bookAppointment".equals(action)) {
            String patientName = request.getParameter("patientName");
            String phone       = request.getParameter("phone");
            String apptDate    = request.getParameter("apptDate");
            String timeSlot    = request.getParameter("timeSlot");
            String reason      = request.getParameter("reason");
            String doctorIdStr = request.getParameter("doctorId");
            String feesStr     = request.getParameter("fees");

            if (patientName == null || patientName.trim().isEmpty()) {
                sendJson(response, "{\"success\":false,\"message\":\"Patient name is required\"}"); return;
            }
            if (phone == null || !phone.matches("\\d{10}")) {
                sendJson(response, "{\"success\":false,\"message\":\"Valid 10-digit phone is required\"}"); return;
            }
            if (apptDate == null || apptDate.trim().isEmpty()) {
                sendJson(response, "{\"success\":false,\"message\":\"Appointment date is required\"}"); return;
            }
            if (timeSlot == null || timeSlot.trim().isEmpty()) {
                sendJson(response, "{\"success\":false,\"message\":\"Time slot is required\"}"); return;
            }
            if (doctorIdStr == null || doctorIdStr.trim().isEmpty()) {
                sendJson(response, "{\"success\":false,\"message\":\"Doctor not selected\"}"); return;
            }

            int doctorId;
            double fees;
            try { doctorId = Integer.parseInt(doctorIdStr.trim()); }
            catch (NumberFormatException e) { sendJson(response, "{\"success\":false,\"message\":\"Invalid doctor ID\"}"); return; }
            try { fees = Double.parseDouble(feesStr != null ? feesStr.trim() : "0"); }
            catch (NumberFormatException e) { fees = 0; }

            Connection con = null; PreparedStatement ps = null; ResultSet keys = null;
            try {
                con = getConnection();
                ps  = con.prepareStatement(
                    "INSERT INTO appointments " +
                    "(doctor_id, patient_name, phone, appt_date, time_slot, reason, fees, status, payment_status, created_at) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, 'pending', 'unpaid', NOW())",
                    Statement.RETURN_GENERATED_KEYS);
                ps.setInt   (1, doctorId);
                ps.setString(2, patientName.trim());
                ps.setString(3, phone.trim());
                ps.setDate  (4, Date.valueOf(apptDate.trim()));
                ps.setString(5, timeSlot.trim());
                ps.setString(6, reason != null ? reason.trim() : "");
                ps.setDouble(7, fees);

                int rows = ps.executeUpdate();
                if (rows > 0) {
                    keys = ps.getGeneratedKeys();
                    int newId = keys.next() ? keys.getInt(1) : -1;
                    // Return the new appointment ID so front-end can use it for payment
                    sendJson(response, "{\"success\":true,\"appointmentId\":" + newId + ",\"fees\":" + fees + "}");
                } else {
                    sendJson(response, "{\"success\":false,\"message\":\"Booking failed, please try again\"}");
                }
            } catch (Exception e) {
                e.printStackTrace();
                sendJson(response, "{\"success\":false,\"message\":\"Server error: " + escape(e.getMessage()) + "\"}");
            } finally { closeAll(keys, ps, con); }
            return;
        }

        // ── ACTION 2: Confirm payment → update status to confirmed ──
        if ("confirmPayment".equals(action)) {
            String apptIdStr   = request.getParameter("appointmentId");
            String paymentRef  = request.getParameter("paymentRef");   // dummy ref from UI

            if (apptIdStr == null || apptIdStr.trim().isEmpty()) {
                sendJson(response, "{\"success\":false,\"message\":\"Appointment ID missing\"}"); return;
            }

            int apptId;
            try { apptId = Integer.parseInt(apptIdStr.trim()); }
            catch (NumberFormatException e) { sendJson(response, "{\"success\":false,\"message\":\"Invalid appointment ID\"}"); return; }

            Connection con = null; PreparedStatement ps = null;
            try {
                con = getConnection();
                ps  = con.prepareStatement(
                    "UPDATE appointments SET status='confirmed', payment_status='paid', payment_ref=? WHERE id=?");
                ps.setString(1, paymentRef != null ? paymentRef.trim() : "DUMMY-REF");
                ps.setInt   (2, apptId);

                int rows = ps.executeUpdate();
                if (rows > 0) {
                    sendJson(response, "{\"success\":true,\"message\":\"Payment confirmed!\"}");
                } else {
                    sendJson(response, "{\"success\":false,\"message\":\"Appointment not found\"}");
                }
            } catch (Exception e) {
                e.printStackTrace();
                sendJson(response, "{\"success\":false,\"message\":\"Server error: " + escape(e.getMessage()) + "\"}");
            } finally { closeAll(null, ps, con); }
            return;
        }

        sendJson(response, "{\"success\":false,\"message\":\"Unknown action\"}");
    }

    // ── Helpers ───────────────────────────────────────────────────
    private String doctorToJson(ResultSet rs) throws SQLException {
        return "{"
            + "\"id\":"               + rs.getInt("id")                        + ","
            + "\"name\":\""           + escape(rs.getString("name"))           + "\","
            + "\"specialization\":\"" + escape(rs.getString("specialization")) + "\","
            + "\"experience\":"       + rs.getInt("experience")                + ","
            + "\"hospital\":\""       + escape(rs.getString("hospital"))       + "\","
            + "\"fees\":"             + rs.getDouble("fees")                   + ","
            + "\"availability\":\""   + escape(rs.getString("availability"))   + "\","
            + "\"photo_url\":\""      + escape(rs.getString("photo_url") != null
                                        ? rs.getString("photo_url") : "")      + "\""
            + "}";
    }

    private String escape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"")
                .replace("\n", "\\n").replace("\r", "\\r");
    }

    private void sendJson(HttpServletResponse response, String json) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();
        out.print(json);
        out.flush();
    }

    private void closeAll(AutoCloseable a, PreparedStatement ps, Connection con) {
        try { if (a   != null) a.close();   } catch (Exception e) {}
        try { if (ps  != null) ps.close();  } catch (Exception e) {}
        try { if (con != null) con.close(); } catch (Exception e) {}
    }
}