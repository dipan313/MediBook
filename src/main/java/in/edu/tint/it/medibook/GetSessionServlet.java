package in.edu.tint.it.medibook;

import java.io.IOException;
import java.io.PrintWriter;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

@WebServlet("/GetSession")
public class GetSessionServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();

        HttpSession session = request.getSession(false);

        if (session != null && session.getAttribute("userName") != null) {
            String name = (String) session.getAttribute("userName");
            String role = (String) session.getAttribute("userRole");
            out.print("{"
                + "\"loggedIn\":true,"
                + "\"name\":\"" + escape(name) + "\","
                + "\"role\":\"" + escape(role) + "\""
                + "}");
        } else {
            out.print("{\"loggedIn\":false}");
        }

        out.flush();
    }

    private String escape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"");
    }
}