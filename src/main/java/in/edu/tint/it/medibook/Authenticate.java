package in.edu.tint.it.medibook;

import java.io.IOException;
import java.sql.*;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

@WebServlet("/Authenticate")
public class Authenticate extends HttpServlet {

    private static final long serialVersionUID = 1L;

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.sendRedirect("login.html");
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String email    = request.getParameter("email");
        String password = request.getParameter("password");
        String role     = request.getParameter("role");
        String redirect = request.getParameter("redirect");
        String docId    = request.getParameter("docId");

        Connection con       = null;
        PreparedStatement ps = null;
        ResultSet rs         = null;
        String loggedInName  = null;

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            con = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/medibook", "root", "Dipan@2005");

            String sql = "SELECT * FROM userdetails WHERE email=? AND password=?";
            ps = con.prepareStatement(sql);
            ps.setString(1, email);
            ps.setString(2, password);
            rs = ps.executeQuery();

            if (rs.next()) {
                loggedInName = rs.getString("name");
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try { if (rs  != null) rs.close();  } catch (SQLException e) {}
            try { if (ps  != null) ps.close();  } catch (SQLException e) {}
            try { if (con != null) con.close(); } catch (SQLException e) {}
        }

        if (loggedInName != null) {
            // ✅ Create session
            HttpSession session = request.getSession();
            session.setAttribute("userName", loggedInName);
            session.setAttribute("userRole", role);
            session.setMaxInactiveInterval(30 * 60);

            if ("admin".equals(role)) {
                response.sendRedirect("admin.html");
            } else if (redirect != null && !redirect.isEmpty()) {
                // ✅ Redirect back to where user came from
                String target = redirect;
                if (docId != null && !docId.isEmpty()) {
                    target += "?docId=" + docId;
                }
                response.sendRedirect(target);
            } else {
                response.sendRedirect("index.html");
            }
        } else {
            response.sendRedirect("login.html?error=1");
        }
    }
}