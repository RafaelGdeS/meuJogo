package web;

import Jogo.UserStore;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * ProfileServlet - Servlet responsável pela gestão de perfis de utilizadores
 * Permite visualizar e actualizar informações do perfil
 */
public class ProfileServlet extends HttpServlet {
    private UserStore userStore; // Armazena os dados dos utilizadores

    /**
     * Inicializa o servlet e carrega o UserStore
     */
    @Override
    public void init() throws ServletException {
        try {
            userStore = new UserStore("users.db");
        } catch (IOException e) {
            throw new ServletException("Erro ao inicializar UserStore", e);
        }
    }

    /**
     * Processa requisições GET - Visualizar perfil
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Obter utilizador da sessão
        HttpSession session = request.getSession();
        String user = (String) session.getAttribute("user");

        // Se não está autenticado, redirecionar
        if (user == null) {
            response.sendRedirect("index.jsp");
            return;
        }

        // Obter nickname (por defeito, é o utilizador actual)
        String nickname = request.getParameter("nick");
        if (nickname == null) {
            nickname = user;
        }

        // Obter dados do perfil
        String profile = userStore.profileLine(nickname);
        request.setAttribute("profile", profile);
        request.setAttribute("nickname", nickname);

        // Redirecionar para a página de perfil
        request.getRequestDispatcher("profile.jsp").forward(request, response);
    }

    /**
     * Processa requisições POST - Actualizar perfil
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Obter utilizador da sessão
        HttpSession session = request.getSession();
        String user = (String) session.getAttribute("user");

        // Se não está autenticado, redirecionar
        if (user == null) {
            response.sendRedirect("index.jsp");
            return;
        }

        // Obter ação solicitada
        String action = request.getParameter("action");

        // Processar actualização de imagem
        if ("update_image".equals(action)) {
            String image = request.getParameter("image");
            try {
                // Actualizar avatar do utilizador
                userStore.updateImage(user, image);
                response.sendRedirect("profile.jsp?msg=Imagem atualizada");
            } catch (IOException e) {
                // Erro ao actualizar
                request.setAttribute("error", "Erro ao atualizar imagem");
                request.getRequestDispatcher("profile.jsp").forward(request, response);
            }
        }
    }
}
