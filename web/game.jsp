<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="javax.servlet.http.HttpSession" %>
<!DOCTYPE html>
<html lang="pt-PT">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Dots & Boxes - Jogo</title>
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
            <h1>🎮 Dots & Boxes</h1>
            <p>Utilizador: <strong><%= user %></strong></p>
            <a href="index.jsp" class="btn btn-secondary">Voltar</a>
        </header>

        <div class="game-container">
            <div class="game-panel">
                <h2>Jogo em Progresso</h2>
                
                <div class="game-board">
                    <canvas id="gameCanvas" width="400" height="400"></canvas>
                </div>

                <div class="game-info">
                    <div class="player-info">
                        <h3>Jogador A</h3>
                        <p>Pontos: <span id="scoreA">0</span></p>
                    </div>
                    <div class="player-info">
                        <h3>Jogador B</h3>
                        <p>Pontos: <span id="scoreB">0</span></p>
                    </div>
                </div>

                <div class="game-controls">
                    <button id="queueBtn" class="btn btn-primary" onclick="joinQueue()">Entrar em Fila</button>
                    <button id="moveBtn" class="btn btn-success" onclick="makeMove()" disabled>Fazer Jogada</button>
                </div>

                <div id="gameStatus" class="status-message">Aguardando ação...</div>
            </div>
        </div>

        <footer>
            <p>&copy; 2024 Dots & Boxes Game</p>
        </footer>
    </div>

    <script>
        const canvas = document.getElementById('gameCanvas');
        const ctx = canvas.getContext('2d');
        let gameActive = false;
        let inQueue = false;

        // Estado dos botões
        function updateButtonStates() {
            const queueBtn = document.getElementById('queueBtn');
            const moveBtn = document.getElementById('moveBtn');

            if (inQueue && !gameActive) {
                // Em fila, aguardando adversário
                queueBtn.disabled = true;
                moveBtn.disabled = true;
                queueBtn.style.opacity = '0.5';
                moveBtn.style.opacity = '0.5';
            } else if (gameActive) {
                // Jogo ativo
                queueBtn.disabled = true;
                moveBtn.disabled = false;
                queueBtn.style.opacity = '0.5';
                moveBtn.style.opacity = '1';
            } else {
                // Estado inicial
                queueBtn.disabled = false;
                moveBtn.disabled = true;
                queueBtn.style.opacity = '1';
                moveBtn.style.opacity = '0.5';
            }
        }

        function joinQueue() {
            const btn = document.getElementById('queueBtn');
            const statusDiv = document.getElementById('gameStatus');

            btn.disabled = true;
            btn.textContent = 'Em fila...';
            statusDiv.textContent = '⏳ Aguardando adversário...';
            statusDiv.style.background = '#fef5e7';

            inQueue = true;
            updateButtonStates();

            fetch('game?action=queue')
                .then(response => response.json())
                .then(data => {
                    statusDiv.textContent = data.message;
                    if (data.status === 'match_started') {
                        gameActive = true;
                        statusDiv.style.background = '#d5f4e6';
                        statusDiv.textContent = '✅ Jogo iniciado! Sua vez de jogar.';
                        updateButtonStates();
                    }
                })
                .catch(error => {
                    console.error('Erro:', error);
                    statusDiv.textContent = '❌ Erro ao entrar em fila';
                    statusDiv.style.background = '#fadbd8';
                    inQueue = false;
                    btn.disabled = false;
                    btn.textContent = 'Entrar em Fila';
                    updateButtonStates();
                });
        }

        function makeMove() {
            const statusDiv = document.getElementById('gameStatus');
            const moveBtn = document.getElementById('moveBtn');

            const horizontal = confirm('Horizontal? OK=Sim | Cancelar=Não');
            const row = prompt('Linha (0-2):');
            const col = prompt('Coluna (0-2):');

            if (row === null || col === null) {
                statusDiv.textContent = '⚠️ Jogada cancelada';
                return;
            }

            moveBtn.disabled = true;
            moveBtn.textContent = 'Enviando...';
            statusDiv.textContent = '⏳ Enviando jogada...';

            fetch('game?action=move&horizontal=' + horizontal + '&row=' + row + '&col=' + col)
                .then(response => response.json())
                .then(data => {
                    if (data.status === 'move_received') {
                        statusDiv.textContent = '✅ Jogada recebida! Aguardando adversário...';
                        statusDiv.style.background = '#d5f4e6';
                        moveBtn.disabled = true;
                        moveBtn.textContent = 'Fazer Jogada';
                    } else {
                        statusDiv.textContent = '❌ Jogada inválida!';
                        statusDiv.style.background = '#fadbd8';
                        moveBtn.disabled = false;
                        moveBtn.textContent = 'Fazer Jogada';
                    }
                    updateButtonStates();
                })
                .catch(error => {
                    console.error('Erro:', error);
                    statusDiv.textContent = '❌ Erro ao enviar jogada';
                    statusDiv.style.background = '#fadbd8';
                    moveBtn.disabled = false;
                    moveBtn.textContent = 'Fazer Jogada';
                    updateButtonStates();
                });
        }

        function drawBoard() {
            ctx.fillStyle = '#f0f0f0';
            ctx.fillRect(0, 0, canvas.width, canvas.height);

            ctx.strokeStyle = '#333';
            ctx.lineWidth = 2;

            const size = 3;
            const cellSize = canvas.width / size;

            for (let i = 0; i <= size; i++) {
                ctx.beginPath();
                ctx.moveTo(i * cellSize, 0);
                ctx.lineTo(i * cellSize, canvas.height);
                ctx.stroke();

                ctx.beginPath();
                ctx.moveTo(0, i * cellSize);
                ctx.lineTo(canvas.width, i * cellSize);
                ctx.stroke();
            }

            ctx.fillStyle = '#333';
            for (let i = 0; i <= size; i++) {
                for (let j = 0; j <= size; j++) {
                    ctx.beginPath();
                    ctx.arc(i * cellSize, j * cellSize, 4, 0, 2 * Math.PI);
                    ctx.fill();
                }
            }
        }

        // Inicialização
        drawBoard();
        updateButtonStates();
    </script>
</body>
</html>
