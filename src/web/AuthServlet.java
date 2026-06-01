package web;

import Jogo.User;
import Jogo.UserStore;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * AuthServlet - Servlet responsável por autenticação (Login/Registo/Logout)
 * Processa requisições de registo de novos utilizadores, login e logout
 */
public class AuthServlet extends HttpServlet {
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
     * Processa requisições POST (registo, login, logout)
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getParameter("action");

        // Determina qual ação executar
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

    /**
     * Trata o registo de um novo utilizador
     */
    private void handleRegister(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        // Obter parâmetros do formulário
        String nickname = request.getParameter("nickname");
        String password = request.getParameter("password");
        String age = request.getParameter("age");
        String nationality = request.getParameter("nationality");
        String image = request.getParameter("image");

        try {
            // Converter idade para inteiro
            int ageInt = Integer.parseInt(age);
            
            // Registar novo utilizador
            User user = userStore.register(nickname, password, image, nationality, ageInt);

            if (user != null) {
                // Registo bem-sucedido - criar sessão
                HttpSession session = request.getSession();
                session.setAttribute("user", nickname);
                response.sendRedirect("index.jsp?msg=Registado com sucesso!");
            } else {
                // Nickname já existe
                request.setAttribute("error", "Nickname já existe");
                request.getRequestDispatcher("index.jsp").forward(request, response);
            }
        } catch (NumberFormatException e) {
            // Erro na conversão da idade
            request.setAttribute("error", "Idade deve ser um número");
            request.getRequestDispatcher("index.jsp").forward(request, response);
        } catch (IOException e) {
            // Erro ao guardar dados
            request.setAttribute("error", "Erro ao guardar dados");
            request.getRequestDispatcher("index.jsp").forward(request, response);
        }
    }

    /**
     * Trata o login de um utilizador
     */
    private void handleLogin(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        // Obter credenciais do formulário
        String nickname = request.getParameter("nickname");
        String password = request.getParameter("password");

        // Validar credenciais
        User user = userStore.login(nickname, password);

        if (user != null) {
            // Login bem-sucedido - criar sessão
            HttpSession session = request.getSession();
            session.setAttribute("user", nickname);
            response.sendRedirect("game.jsp");
        } else {
            // Credenciais inválidas
            request.setAttribute("error", "Nickname ou password inválidos");
            request.getRequestDispatcher("index.jsp").forward(request, response);
        }
    }

    /**
     * Trata o logout do utilizador
     */
    private void handleLogout(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        // Invalidar sessão
        HttpSession session = request.getSession();
        session.invalidate();
        
        // Redirecionar para página inicial
        response.sendRedirect("index.jsp?msg=Logout realizado");
    }
}
