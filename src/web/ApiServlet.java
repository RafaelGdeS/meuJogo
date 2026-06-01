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

public class ApiServlet extends HttpServlet {
    private UserStore userStore;
    private Gson gson = new Gson();

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
        response.setContentType("application/json");
        String pathInfo = request.getPathInfo();

        if ("/users".equals(pathInfo)) {
            handleGetUsers(response);
        } else if (pathInfo != null && pathInfo.startsWith("/profile/")) {
            String nickname = pathInfo.substring(9);
            handleGetProfile(response, nickname);
        } else {
            sendError(response, HttpServletResponse.SC_NOT_FOUND, "Endpoint não encontrado");
        }
    }

    private void handleGetUsers(HttpServletResponse response) throws IOException {
        Map<String, Object> result = new HashMap<>();
        result.put("status", "success");
        result.put("message", "Lista de utilizadores disponível");
        response.getWriter().write(gson.toJson(result));
    }

    private void handleGetProfile(HttpServletResponse response, String nickname) throws IOException {
        String profile = userStore.profileLine(nickname);
        Map<String, Object> result = new HashMap<>();
        result.put("status", "success");
        result.put("profile", profile);
        response.getWriter().write(gson.toJson(result));
    }

    private void sendError(HttpServletResponse response, int status, String message)
            throws IOException {
        response.setStatus(status);
        Map<String, Object> error = new HashMap<>();
        error.put("status", "error");
        error.put("message", message);
        response.getWriter().write(gson.toJson(error));
    }
}
