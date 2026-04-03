package in.edu.tint.it.medibook;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

@WebServlet("/AdminDoctor")
public class AdminDoctorServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private Connection getConnection() throws Exception {
        Class.forName("com.mysql.cj.jdbc.Driver");
        return DriverManager.getConnection(
            "jdbc:mysql://localhost:3306/medibook", "root", "Dipan@2005");
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String action = request.getParameter("action");

        // ✅ Soft Delete
        if ("delete".equals(action)) {
            int id = Integer.parseInt(request.getParameter("id"));
            try (Connection con = getConnection();
                 PreparedStatement ps = con.prepareStatement(
                     "UPDATE doctors SET is_active=0 WHERE id=?")) {
                ps.setInt(1, id);
                ps.executeUpdate();
            } catch (Exception e) {
                e.printStackTrace();
            }
            sendJson(response, "{\"status\":\"deleted\"}");
            return;
        }

        // ✅ Restore
        if ("restore".equals(action)) {
            int id = Integer.parseInt(request.getParameter("id"));
            try (Connection con = getConnection();
                 PreparedStatement ps = con.prepareStatement(
                     "UPDATE doctors SET is_active=1 WHERE id=?")) {
                ps.setInt(1, id);
                ps.executeUpdate();
            } catch (Exception e) {
                e.printStackTrace();
            }
            sendJson(response, "{\"status\":\"restored\"}");
            return;
        }

        // ✅ Get all doctors as JSON
        if ("getAll".equals(action)) {
            StringBuilder json = new StringBuilder();
            json.append("{\"active\":[");

            Connection con       = null;
            PreparedStatement ps = null;
            ResultSet rs         = null;

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
                json.append("],\"deleted\":[");

                rs.close(); ps.close();
                ps  = con.prepareStatement(
                    "SELECT * FROM doctors WHERE is_active=0 ORDER BY name ASC");
                rs  = ps.executeQuery();
                first = true;
                while (rs.next()) {
                    if (!first) json.append(",");
                    json.append(doctorToJson(rs));
                    first = false;
                }
                json.append("]}");

            } catch (Exception e) {
                e.printStackTrace();
                json = new StringBuilder("{\"active\":[],\"deleted\":[]}");
            } finally {
                try { if (rs  != null) rs.close();  } catch (SQLException e) {}
                try { if (ps  != null) ps.close();  } catch (SQLException e) {}
                try { if (con != null) con.close(); } catch (SQLException e) {}
            }

            sendJson(response, json.toString());
            return;
        }

        // ✅ Default — forward to admin.html
        request.getRequestDispatcher("admin.html")
               .forward(request, response);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String action         = request.getParameter("action");
        String name           = request.getParameter("name");
        String specialization = request.getParameter("specialization");
        String hospital       = request.getParameter("hospital");
        String availability   = request.getParameter("availability");
        String photoUrl       = request.getParameter("photo_url");

        int    experience = 0;
        double fees       = 0.0;

        try {
            experience = Integer.parseInt(request.getParameter("experience"));
        } catch (NumberFormatException e) {
            System.out.println("❌ experience parse error");
        }
        try {
            fees = Double.parseDouble(request.getParameter("fees"));
        } catch (NumberFormatException e) {
            System.out.println("❌ fees parse error");
        }

        System.out.println("=== AdminDoctor doPost ===");
        System.out.println("action: "         + action);
        System.out.println("name: "           + name);
        System.out.println("specialization: " + specialization);
        System.out.println("experience: "     + experience);
        System.out.println("hospital: "       + hospital);
        System.out.println("fees: "           + fees);
        System.out.println("availability: "   + availability);
        System.out.println("photo_url: "      + photoUrl);

        Connection con       = null;
        PreparedStatement ps = null;
        String status        = "error";

        try {
            con = getConnection();

            if ("insert".equals(action)) {
                String sql = "INSERT INTO doctors "
                           + "(name, specialization, experience, hospital, "
                           + "fees, availability, photo_url, is_active) "
                           + "VALUES (?,?,?,?,?,?,?,1)";
                ps = con.prepareStatement(sql);
                ps.setString(1, name);
                ps.setString(2, specialization);
                ps.setInt(3, experience);
                ps.setString(4, hospital);
                ps.setDouble(5, fees);
                ps.setString(6, availability);
                ps.setString(7, photoUrl);
                int rows = ps.executeUpdate();
                if (rows > 0) status = "inserted";
                System.out.println("✅ Rows inserted: " + rows);

            } else if ("update".equals(action)) {
                int id = Integer.parseInt(request.getParameter("id"));
                String sql = "UPDATE doctors SET name=?, specialization=?, "
                           + "experience=?, hospital=?, fees=?, "
                           + "availability=?, photo_url=? WHERE id=?";
                ps = con.prepareStatement(sql);
                ps.setString(1, name);
                ps.setString(2, specialization);
                ps.setInt(3, experience);
                ps.setString(4, hospital);
                ps.setDouble(5, fees);
                ps.setString(6, availability);
                ps.setString(7, photoUrl);
                ps.setInt(8, id);
                int rows = ps.executeUpdate();
                if (rows > 0) status = "updated";
                System.out.println("✅ Rows updated: " + rows);
            }

        } catch (Exception e) {
            System.out.println("❌ DB ERROR: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try { if (ps  != null) ps.close();  } catch (SQLException e) {}
            try { if (con != null) con.close(); } catch (SQLException e) {}
        }

        sendJson(response, "{\"status\":\"" + status + "\"}");
    }

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
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }

    private void sendJson(HttpServletResponse response, String json)
            throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();
        out.print(json);
        out.flush();
    }
}