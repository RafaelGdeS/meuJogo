<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="javax.servlet.http.HttpSession" %>
<!DOCTYPE html>
<html lang="pt-PT">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Meu Perfil - Dots & Boxes</title>
    <link rel="stylesheet" href="css/style.css">
</head>
<body>
    <%
        String user = (String) session.getAttribute("user");
        if (user == null) {
            response.sendRedirect("index.jsp");
            return;
        }
    %>

    <div class="container">
        <header>
            <h1>👤 Meu Perfil</h1>
            <a href="index.jsp" class="btn btn-secondary">Voltar</a>
        </header>

        <div class="profile-container">
            <div class="profile-card">
                <h2><%= user %></h2>
                
                <div class="profile-section">
                    <h3>Informações Pessoais</h3>
                    <p><strong>Utilizador:</strong> <%= user %></p>
                    <p><strong>Avatar:</strong> <input type="text" id="imageInput" placeholder="URL da imagem" class="input-field"></p>
                </div>

                <div class="profile-section">
                    <h3>Estatísticas</h3>
                    <%
                        String profile = (String) request.getAttribute("profile");
                        if (profile != null) {
                    %>
                        <p><%= profile %></p>
                    <%
                        }
                    %>
                </div>

                <div class="profile-section">
                    <h3>Ações</h3>
                    <form method="POST" action="profile" class="form-group">
                        <input type="hidden" name="action" value="update_image">
                        <input type="text" name="image" id="imageField" placeholder="Nova URL da imagem" required class="input-field">
                        <button type="submit" class="btn btn-primary">Atualizar Imagem</button>
                    </form>
                </div>

                <div class="profile-section">
                    <a href="game.jsp" class="btn btn-success">Jogar Agora</a>
                    <a href="auth?action=logout" class="btn btn-danger">Logout</a>
                </div>
            </div>
        </div>

        <footer>
            <p>&copy; 2024 Dots & Boxes Game</p>
        </footer>
    </div>
</body>
</html>
