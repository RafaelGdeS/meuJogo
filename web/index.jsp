<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="javax.servlet.http.HttpSession" %>
<!DOCTYPE html>
<html lang="pt-PT">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Dots & Boxes - Jogo Online</title>
    <link rel="stylesheet" href="css/style.css">
</head>
<body>
    <div class="container">
        <header>
            <h1>🎮 Dots & Boxes</h1>
            <p class="subtitle">Jogo Online de Estratégia</p>
        </header>

        <%
            // Obter utilizador da sessão
            String user = (String) session.getAttribute("user");
            // Obter mensagem de sucesso da URL
            String msg = request.getParameter("msg");
            // Obter mensagem de erro do request
            String error = (String) request.getAttribute("error");
        %>

        <!-- Se utilizador está autenticado, mostrar menu principal -->
        <% if (user != null) { %>
            <div class="user-info">
                <p>Bem-vindo, <strong><%= user %></strong>!</p>
                <a href="auth?action=logout" class="btn btn-danger">Logout</a>
                <a href="profile.jsp" class="btn btn-info">Meu Perfil</a>
                <a href="game.jsp" class="btn btn-success">Jogar</a>
            </div>
        <% } else { %>
            <!-- Se não autenticado, mostrar formulários de Login/Registo -->
            <div class="auth-container">
                <!-- Mostrar mensagem de sucesso se existir -->
                <% if (msg != null) { %>
                    <div class="alert alert-success"><%= msg %></div>
                <% } %>
                
                <!-- Mostrar mensagem de erro se existir -->
                <% if (error != null) { %>
                    <div class="alert alert-danger"><%= error %></div>
                <% } %>

                <!-- Abas para Login e Registo -->
                <div class="tabs">
                    <button class="tab-btn active" onclick="showTab('login')">Login</button>
                    <button class="tab-btn" onclick="showTab('register')">Registar</button>
                </div>

                <!-- Tab de Login -->
                <div id="login" class="tab-content active">
                    <form method="POST" action="auth" class="form-group">
                        <input type="hidden" name="action" value="login">
                        <input type="text" name="nickname" placeholder="Nickname" required class="input-field">
                        <input type="password" name="password" placeholder="Password" required class="input-field">
                        <button type="submit" class="btn btn-primary">Entrar</button>
                    </form>
                </div>

                <!-- Tab de Registo -->
                <div id="register" class="tab-content">
                    <form method="POST" action="auth" class="form-group">
                        <input type="hidden" name="action" value="register">
                        <input type="text" name="nickname" placeholder="Nickname" required class="input-field">
                        <input type="password" name="password" placeholder="Password" required class="input-field">
                        <input type="number" name="age" placeholder="Idade" required class="input-field">
                        <input type="text" name="nationality" placeholder="Nacionalidade" required class="input-field">
                        <input type="text" name="image" placeholder="Avatar (URL ou nome)" required class="input-field">
                        <button type="submit" class="btn btn-primary">Registar</button>
                    </form>
                </div>
            </div>
        <% } %>

        <footer>
            <p>&copy; 2024 Dots & Boxes Game - Todos os direitos reservados</p>
        </footer>
    </div>

    <script>
        /**
         * Função para mudar entre abas
         * @param tabName - Nome da aba a mostrar (login ou register)
         */
        function showTab(tabName) {
            // Obter todos os conteúdos das abas
            const tabs = document.querySelectorAll('.tab-content');
            // Obter todos os botões das abas
            const btns = document.querySelectorAll('.tab-btn');

            // Remover classe 'active' de todos os conteúdos
            tabs.forEach(tab => tab.classList.remove('active'));
            // Remover classe 'active' de todos os botões
            btns.forEach(btn => btn.classList.remove('active'));

            // Adicionar classe 'active' ao conteúdo selecionado
            document.getElementById(tabName).classList.add('active');
            // Adicionar classe 'active' ao botão clicado
            event.target.classList.add('active');
        }
    </script>
</body>
</html>
