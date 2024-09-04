<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Chat</title>
    <link rel="stylesheet" href="source/static/css/chat.css">
</head>
<body>
<h1>Chat</h1>

<div class="container">
    <div class="users-container">
        <h2>Online Users</h2>
        <ul id="users" class="online-users-list"></ul>
    </div>

    <div class="chat-container">
        <div id="chat" class="chat-box"></div>
        <div class="message-input-container">
            <input id="message" type="text" placeholder="Enter your message"/>
            <input id="targetUser" type="text" placeholder="Enter recipient's user ID"/>
            <button onclick="sendMessage()">Send</button>
        </div>
    </div>
</div>

<script>
    function connectWebSocket() {
        let userId = sessionStorage.getItem('userId');
        if (!userId) {
            alert("User is not logged in.");
            return;
        }

        let ws = new WebSocket("ws://localhost:8080/chat/" + userId);

        ws.onopen = function() {
            console.log("Connected to WebSocket server.");
            ws.send("GET_USERS");  // 请求在线用户列表
        };

        ws.onmessage = function(event) {
            let data = event.data;
            if (data.startsWith("ONLINE_USERS:")) {
                // 更新在线用户列表
                let users = data.substring(13).split(",");
                let usersList = document.getElementById("users");
                usersList.innerHTML = "";
                users.forEach(user => {
                    if (user) {
                        let li = document.createElement("li");
                        li.textContent = user;
                        usersList.appendChild(li);
                    }
                });
            } else {
                // 显示收到的消息
                let chat = document.getElementById("chat");
                let messageDiv = document.createElement("div");
                messageDiv.className = "chat-message";
                messageDiv.textContent = data;
                chat.appendChild(messageDiv);
                chat.scrollTop = chat.scrollHeight;  // 滚动到最新消息
            }
        };

        function sendMessage() {
            let message = document.getElementById("message").value;
            let targetUser = document.getElementById("targetUser").value;
            if (targetUser) {
                // 发送私人消息
                ws.send("SEND:" + targetUser + ":" + message);
            } else {
                // 广播消息（如果你想支持的话）
                ws.send(message);
            }
            document.getElementById("message").value = '';  // 清空输入框
        }

        // 暴露 sendMessage 函数给全局使用
        window.sendMessage = sendMessage;
    }

    // 在页面加载时连接 WebSocket
    window.onload = connectWebSocket;
</script>
</body>
</html>
