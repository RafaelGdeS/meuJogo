package web;

import Jogo.User;
import Jogo.UserStore;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

public class AuthServlet extends HttpServlet {
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
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getParameter("action");

        if ("register".equals(action)) {
            handleRegister(request, response);
        } else if ("login".equals(action)) {
            handleLogin(request, response);
        } else if ("logout".equals(action)) {
            handleLogout(request, response);
        } else {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Ação desconhecida");
        }
    }

    private void handleRegister(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        String nickname = request.getParameter("nickname");
        String password = request.getParameter("password");
        String age = request.getParameter("age");
        String nationality = request.getParameter("nationality");
        String image = request.getParameter("image");

        try {
            int ageInt = Integer.parseInt(age);
            User user = userStore.register(nickname, password, image, nationality, ageInt);

            if (user != null) {
                HttpSession session = request.getSession();
                session.setAttribute("user", nickname);
                response.sendRedirect("index.jsp?msg=Registado com sucesso!");
            } else {
                request.setAttribute("error", "Nickname já existe");
                request.getRequestDispatcher("index.jsp").forward(request, response);
            }
        } catch (NumberFormatException e) {
            request.setAttribute("error", "Idade deve ser um número");
            request.getRequestDispatcher("index.jsp").forward(request, response);
        } catch (IOException e) {
            request.setAttribute("error", "Erro ao guardar dados");
            request.getRequestDispatcher("index.jsp").forward(request, response);
        }
    }

    private void handleLogin(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        String nickname = request.getParameter("nickname");
        String password = request.getParameter("password");

        User user = userStore.login(nickname, password);

        if (user != null) {
            HttpSession session = request.getSession();
            session.setAttribute("user", nickname);
            response.sendRedirect("game.jsp");
        } else {
            request.setAttribute("error", "Nickname ou password inválidos");
            request.getRequestDispatcher("index.jsp").forward(request, response);
        }
    }

    private void handleLogout(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        HttpSession session = request.getSession();
        session.invalidate();
        response.sendRedirect("index.jsp?msg=Logout realizado");
    }
}
