package web;

import Jogo.UserStore;
import com.google.gson.Gson;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * ApiServlet - Servlet responsável pela API REST
 * Fornece endpoints para acesso aos dados em formato JSON
 * Endpoints:
 *  - GET /api/users - Lista de utilizadores
 *  - GET /api/profile/{nickname} - Perfil de um utilizador
 */
public class ApiServlet extends HttpServlet {
    private UserStore userStore; // Armazena os dados dos utilizadores
    private Gson gson = new Gson(); // Converte objetos para JSON

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
     * Processa requisições GET - Endpoints da API
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Definir tipo de conteúdo como JSON
        response.setContentType("application/json");
        
        // Obter caminho do endpoint
        String pathInfo = request.getPathInfo();

        // Processar diferentes endpoints
        if ("/users".equals(pathInfo)) {
            // Endpoint: /api/users
            handleGetUsers(response);
        } else if (pathInfo != null && pathInfo.startsWith("/profile/")) {
            // Endpoint: /api/profile/{nickname}
            String nickname = pathInfo.substring(9); // Remover "/profile/"
            handleGetProfile(response, nickname);
        } else {
            // Endpoint não encontrado
            sendError(response, HttpServletResponse.SC_NOT_FOUND, "Endpoint não encontrado");
        }
    }

    /**
     * Trata o endpoint GET /api/users
     * Retorna lista de utilizadores disponíveis
     */
    private void handleGetUsers(HttpServletResponse response) throws IOException {
        // Criar resposta em formato JSON
        Map<String, Object> result = new HashMap<>();
        result.put("status", "success");
        result.put("message", "Lista de utilizadores disponível");
        
        // Enviar resposta
        response.getWriter().write(gson.toJson(result));
    }

    /**
     * Trata o endpoint GET /api/profile/{nickname}
     * Retorna dados do perfil de um utilizador
     */
    private void handleGetProfile(HttpServletResponse response, String nickname) throws IOException {
        // Obter dados do perfil
        String profile = userStore.profileLine(nickname);
        
        // Criar resposta em formato JSON
        Map<String, Object> result = new HashMap<>();
        result.put("status", "success");
        result.put("profile", profile);
        
        // Enviar resposta
        response.getWriter().write(gson.toJson(result));
    }

    /**
     * Envia uma resposta de erro em formato JSON
     */
    private void sendError(HttpServletResponse response, int status, String message)
            throws IOException {
        // Definir status de erro
        response.setStatus(status);
        
        // Criar resposta de erro em formato JSON
        Map<String, Object> error = new HashMap<>();
        error.put("status", "error");
        error.put("message", message);
        
        // Enviar resposta
        response.getWriter().write(gson.toJson(error));
    }
}
