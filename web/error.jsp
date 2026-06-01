<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page isErrorPage="true" %>
<!DOCTYPE html>
<html lang="pt-PT">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Erro - Dots & Boxes</title>
    <link rel="stylesheet" href="css/style.css">
</head>
<body>
    <div class="container">
        <header>
            <h1>⚠️ Erro</h1>
        </header>

        <div class="error-container">
            <div class="error-card">
                <h2>Ocorreu um Erro!</h2>
                
                <div class="error-details">
                    <p><strong>Código de Erro:</strong> <%= request.getAttribute("javax.servlet.error.status_code") %></p>
                    <p><strong>Mensagem:</strong> <%= request.getAttribute("javax.servlet.error.message") %></p>
                    <% if (exception != null) { %>
                        <p><strong>Detalhes:</strong> <%= exception.getMessage() %></p>
                    <% } %>
                </div>

                <div class="error-actions">
                    <a href="index.jsp" class="btn btn-primary">Voltar ao Início</a>
                    <a href="javascript:history.back()" class="btn btn-secondary">Voltar Atrás</a>
                </div>
            </div>
        </div>

        <footer>
            <p>&copy; 2024 Dots & Boxes Game</p>
        </footer>
    </div>
</body>
</html>
