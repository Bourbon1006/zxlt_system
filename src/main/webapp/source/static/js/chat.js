function logout() {
    // 清除用户 session 或其他登录状态
    fetch('/logout', { method: 'GET' })  // 调用后端 logout 服务
        .then(response => {
            if (response.ok) {
                // 重定向到登录页面
                window.location.href = "/login";
            } else {
                alert("Failed to logout. Please try again.");
            }
        })
        .catch(error => console.error('Error:', error));
}

function connectWebSocket() {
    let userId = sessionStorage.getItem('userId');
    let currentUsername = sessionStorage.getItem('username'); // 确保获取用户名
    console.log(currentUsername);
    if (!userId || !currentUsername) {
        alert("User is not logged in or username is not set.");
        return;
    }

    let ws = new WebSocket("ws://localhost:8080/chat/" + userId);

    ws.onopen = function() {
        console.log("Connected to WebSocket server.");
        ws.send("GET_USERS");  // 请求在线用户列表
        ws.send("GET_HISTORY");  // 请求历史聊天记录
    };

    ws.onmessage = function(event) {
        let data = event.data;
        console.log("Received message:", data);

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
        } else if (data.startsWith("CHAT_HISTORY:")) {
            // 加载历史聊天记录
            let history = data.substring(13).split("\n");
            let chat = document.getElementById("chat");
            history.forEach(message => {
                let messageDiv = document.createElement("div");
                messageDiv.className = "chat-message";
                // 处理消息显示格式
                if (message.startsWith(currentUsername + ":")) {
                    messageDiv.textContent = "我: " + message.substring(currentUsername.length + 1);
                } else {
                    messageDiv.textContent = message;
                }
                chat.appendChild(messageDiv);
            });
            chat.scrollTop = chat.scrollHeight;  // 滚动到最新消息
        } else {
            // 处理实时消息
            let chat = document.getElementById("chat");
            let messageDiv = document.createElement("div");
            messageDiv.className = "chat-message";
            // 处理消息显示格式
            if (data.startsWith(currentUsername + ":")) {
                messageDiv.textContent = "我: " + data.substring(currentUsername.length + 1);
            } else {
                messageDiv.textContent = data;
            }
            chat.appendChild(messageDiv);
            chat.scrollTop = chat.scrollHeight;  // 滚动到最新消息
        }
    };

    function sendMessage() {
        let message = document.getElementById("message").value;
        let targetUser = document.getElementById("targetUser").value;
        if (message.trim() === "") {
            alert("Please enter a message.");
            return;
        }
        if (targetUser) {
            // 使用用户名发送私人消息
            ws.send("SEND:" + targetUser + ":" + message);
        } else {
            // 如果没有指定目标用户，则提示错误或忽略
            alert("Please enter a recipient's username.");
        }
        document.getElementById("message").value = '';  // 清空输入框
    }

    // 暴露 sendMessage 函数给全局使用
    window.sendMessage = sendMessage;
}

// 在页面加载时连接 WebSocket
window.onload = connectWebSocket;


