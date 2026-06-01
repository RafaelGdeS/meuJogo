package web;

import Jogo.UserStore;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * GameServlet - Servlet responsável pela lógica do jogo
 * Processa requisições de fila, estado do jogo e movimentos
 */
public class GameServlet extends HttpServlet {
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
     * Processa requisições GET (fila, estado, movimentos)
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

        // Obter ação solicitada
        String action = request.getParameter("action");

        // Processar ação
        if ("queue".equals(action)) {
            handleQueue(request, response, user);
        } else if ("state".equals(action)) {
            handleGameState(request, response, user);
        } else if ("move".equals(action)) {
            handleMove(request, response, user);
        }
    }

    /**
     * Trata a adição do utilizador à fila de espera
     */
    private void handleQueue(HttpServletRequest request, HttpServletResponse response, String user)
            throws IOException {
        // Devolver resposta em JSON
        response.setContentType("application/json");
        response.getWriter().write("{\"status\":\"queued\", \"message\":\"Em fila de espera\"}");
    }

    /**
     * Trata a obtenção do estado actual do jogo
     */
    private void handleGameState(HttpServletRequest request, HttpServletResponse response, String user)
            throws IOException {
        // Obter perfil do utilizador
        response.setContentType("application/json");
        String profile = userStore.profileLine(user);
        response.getWriter().write("{\"profile\":\"" + profile + "\"}");
    }

    /**
     * Trata o envio de uma jogada
     */
    private void handleMove(HttpServletRequest request, HttpServletResponse response, String user)
            throws IOException {
        // Obter parâmetros da jogada
        String horizontal = request.getParameter("horizontal");
        String row = request.getParameter("row");
        String col = request.getParameter("col");

        // Devolver confirmação da jogada
        response.setContentType("application/json");
        response.getWriter().write("{\"status\":\"move_received\", \"horizontal\":\"" + horizontal + "\"}");
    }
}
