package web;

import Jogo.UserStore;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

public class ProfileServlet extends HttpServlet {
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

        String nickname = request.getParameter("nick");
        if (nickname == null) {
            nickname = user;
        }

        String profile = userStore.profileLine(nickname);
        request.setAttribute("profile", profile);
        request.setAttribute("nickname", nickname);

        request.getRequestDispatcher("profile.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession();
        String user = (String) session.getAttribute("user");

        if (user == null) {
            response.sendRedirect("index.jsp");
            return;
        }

        String action = request.getParameter("action");

        if ("update_image".equals(action)) {
            String image = request.getParameter("image");
            try {
                userStore.updateImage(user, image);
                response.sendRedirect("profile.jsp?msg=Imagem atualizada");
            } catch (IOException e) {
                request.setAttribute("error", "Erro ao atualizar imagem");
                request.getRequestDispatcher("profile.jsp").forward(request, response);
            }
        }
    }
}
