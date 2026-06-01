package web;

import Jogo.UserStore;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

public class GameServlet extends HttpServlet {
    private UserStore userStore;

    @Override
    public void init() throws ServletException {
        try {
            userStore = new UserStore("users.db");
        } catch (IOException e) {
            throw new ServletException("Erro ao inicializar UserStore", e);
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession();
        String user = (String) session.getAttribute("user");

        if (user == null) {
            response.sendRedirect("index.jsp");
            return;
        }

        String action = request.getParameter("action");

        if ("queue".equals(action)) {
            handleQueue(request, response, user);
        } else if ("state".equals(action)) {
            handleGameState(request, response, user);
        } else if ("move".equals(action)) {
            handleMove(request, response, user);
        }
    }

    private void handleQueue(HttpServletRequest request, HttpServletResponse response, String user)
            throws IOException {
        response.setContentType("application/json");
        response.getWriter().write("{\"status\":\"queued\", \"message\":\"Em fila de espera\"}");
    }

    private void handleGameState(HttpServletRequest request, HttpServletResponse response, String user)
            throws IOException {
        response.setContentType("application/json");
        String profile = userStore.profileLine(user);
        response.getWriter().write("{\"profile\":\"" + profile + "\"}");
    }

    private void handleMove(HttpServletRequest request, HttpServletResponse response, String user)
            throws IOException {
        String horizontal = request.getParameter("horizontal");
        String row = request.getParameter("row");
        String col = request.getParameter("col");

        response.setContentType("application/json");
        response.getWriter().write("{\"status\":\"move_received\", \"horizontal\":\"" + horizontal + "\"}");
    }
}
