<%@ page contentType="text/html;charset=UTF-8" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <title>聊天系统</title>
    <link rel="stylesheet" href="source/static/css/chat.css"> <!-- 引入CSS样式 -->
</head>
<body>
<div id="container">
    <!-- 菜单栏 -->
    <div id="menu">
        <div id="menuButtons">
            <button onclick="showAddUserDialog()">增加用户</button>
            <button onclick="showRemoveUserDialog()">删除用户</button>
            <button onclick="showFriendsDialog()">更改用户信息</button>
            <button onclick="showOnlineUsersDialog()">查看在线用户</button>
            <button onclick="showChangePasswordDialog()">修改密码</button> <!-- 添加修改密码按钮 -->
            <button onclick="logout()">退出登录</button> <!-- 添加退出登录按钮 -->
        </div>
    </div>

    <!-- 主内容区域 -->
    <div id="mainContent">
        <!-- 聊天窗口 -->
        <div id="chatWindow" class="view">
            <h2>聊天窗口</h2>
            <div id="chatMessages"></div>
            <input type="text" id="chatInput" placeholder="输入消息...">
            <input type="text" id="targetUser" placeholder="输入接收者用户名..."> <!-- 添加目标用户输入框 -->
            <button onclick="sendMessage()">发送</button> <!-- 添加发送按钮 -->
        </div>
        <!-- 其他视图可以以类似方式添加 -->
    </div>
</div>

<!-- 添加好友的对话框 -->
<div id="addUserDialog" class="dialog">
    <label for="username">用户名:</label>
    <input type="text" id="username">
    <label for="password">密码:</label>
    <input type="text" id="password">
    <label for="email">邮箱:</label>
    <input type="text" id="email">
    <button onclick="addUsers()">确认添加</button>
    <button onclick="closeDialog('addUserDialog')">取消</button>
</div>

<!-- 删除好友的对话框 -->
<div id="removeUserDialog" class="dialog">
    <label for="removeUserName">用户名:</label>
    <input type="text" id="removeUserName">
    <button onclick="removeUser()">确认删除</button>
    <button onclick="closeDialog('removeUserDialog')">取消</button>
</div>

<!-- 好友请求的对话框 -->
<div id="friendRequestDialog" class="dialog">
    <p>用户 <span id="requesterName"></span> 请求添加你为好友</p>
    <button onclick="acceptFriendRequest()">接受</button>
    <button onclick="declineFriendRequest()">拒绝</button>
</div>

<!-- 修改用户信息对话框 -->
<div id="editUserInfoDialog" class="dialog">
    <input type="text" id="editUserId" placeholder="请输入用户 ID" />
    <input type="text" id="editUsername" placeholder="新用户名" />
    <input type="email" id="editEmail" placeholder="新邮箱" />
    <input type="password" id="editPassword" placeholder="新密码（可选）" />
    <button onclick="updateUserInfo()">提交</button>
    <button onclick="closeDialog('editUserInfoDialog')">关闭</button>
</div>



<!-- 显示好友的对话框 -->
<div id="friendsDialog" class="dialog">
    <h2>好友列表</h2>
    <ul id="friendsList"></ul>
    <button onclick="closeDialog('friendsDialog')">关闭</button>
</div>

<!-- 修改密码的对话框 -->
<div id="changePasswordDialog" class="dialog">
    <h2>修改密码</h2>
    <label for="oldPassword">旧密码:</label>
    <input type="password" id="oldPassword">
    <label for="newPassword">新密码:</label>
    <input type="password" id="newPassword">
    <label for="confirmNewPassword">确认新密码:</label>
    <input type="password" id="confirmNewPassword">
    <button onclick="changePassword()">确认修改</button>
    <button onclick="closeDialog('changePasswordDialog')">取消</button>
</div>


<!-- 在线用户的对话框 -->
<div id="onlineUsersDialog" class="dialog">
    <h2>在线用户</h2>
    <ul id="onlineUsersList"></ul>
    <button onclick="closeDialog('onlineUsersDialog')">关闭</button>
</div>

<script>
    let socket;
    let userId = sessionStorage.getItem('userId'); // 用户 ID，根据实际登录的用户动态设置
    let currentUsername = sessionStorage.getItem('username');

    window.onload = function() {
        // 连接到 WebSocket 服务
        socket = new WebSocket("ws://localhost:8080/chat/" + userId);

        socket.onopen = function() {
            console.log("WebSocket 连接已打开");
            fetchChatHistory(); // 获取聊天记录
        };

        socket.onmessage = function(event) {
            handleMessage(event.data);
        };

        socket.onclose = function() {
            console.log("WebSocket 连接已关闭");
        };

        socket.onerror = function(error) {
            console.error("WebSocket 错误: " + error.message);
        };
    };

    // 显示对话框
    function showDialog(dialogId) {
        document.getElementById(dialogId).classList.add("show");
    }

    // 显示更改用户信息的对话框
    function showFriendsDialog() {
        document.getElementById("editUserInfoDialog").classList.add("show");
    }

    // 处理更新用户信息的逻辑
    function updateUserInfo() {
        let userId = document.getElementById("editUserId").value.trim();
        let newUsername = document.getElementById("editUsername").value.trim();
        let newEmail = document.getElementById("editEmail").value.trim();
        let newPassword = document.getElementById("editPassword").value.trim();

        if (userId && newUsername !== "" && newEmail !== "") {
            // 构建更新信息的消息格式
            let updateMessage = "UPDATE_USER:" + userId + ":" + newUsername + ":" + newEmail;

            // 可选的密码字段
            if (newPassword !== "") {
                updateMessage += ":" + newPassword;
            }

            // 通过 WebSocket 发送更新请求
            socket.send(updateMessage);

            // 关闭对话框
            closeDialog('editUserInfoDialog');
        } else {
            alert("用户 ID、用户名和邮箱不能为空");
        }
    }


    // 关闭对话框
    function closeDialog(dialogId) {
        document.getElementById(dialogId).classList.remove("show");
    }

    // 处理收到的消息
    function handleMessage(message) {
        const prefix = message.split(":")[0];  // 获取消息前缀
        const content = message.substring(prefix.length + 1);  // 获取消息内容

        switch (prefix) {
            case "REMOVE_USER":
                removeUser(content);
                break;

            case "ADD_USER":
                addUsers(content);
                break;

            case "ONLINE_USERS":
                updateOnlineUsers(content);
                break;

            case "CHAT_HISTORY":
                displayChatHistory(content);
                break;

            case "FRIEND_REQUEST":
                openFriendRequestDialog(content);
                break;

            case "FRIENDS_LIST":
                displayFriendsList(content);
                break;

            case "ADD_FRIEND":
            case "ACCEPT_FRIEND":
            case "REJECT_FRIEND":
                alert(message);
                break;

            default:
                displayChatMessage(message);
                break;
        }
    }



    // 显示聊天消息
    function displayChatMessage(message) {
        let chatMessages = document.getElementById("chatMessages");
        let p = document.createElement("p");

        // 假设消息格式为 "用户名:消息内容"
        let [username, ...messageParts] = message.split(":");
        let messageContent = messageParts.join(":");

        p.textContent = username === currentUsername ? "我: " + messageContent : message;
        chatMessages.appendChild(p);

        // 自动滚动到最新消息
        chatMessages.scrollTop = chatMessages.scrollHeight;
    }

    // 显示聊天记录
    function displayChatHistory(history) {
        let chatMessages = document.getElementById("chatMessages");
        chatMessages.innerHTML = ""; // 清空聊天记录

        history.split("\n").forEach(function(record) {
            if (record.trim() !== "") {
                displayChatMessage(record);
            }
        });
    }

    // 其他功能函数保持不变

    function logout() {
        if (socket) {
            socket.close(); // 关闭WebSocket连接
        }
        sessionStorage.removeItem('userId'); // 移除用户会话信息
        //alert("您已成功退出登录");
        window.location.href = "/login"; // 重定向到登录页面
    }


    function openFriendRequestDialog(requester) {
        let dialog = document.getElementById("friendRequestDialog");
        document.getElementById("requesterName").textContent = requester;
        dialog.style.display = "block"; // 显示对话框
    }

    // 显示好友列表
    function displayFriendsList(friends) {
        let friendsListElement = document.getElementById("friendsList");
        friendsListElement.innerHTML = ""; // 清空当前列表

        if (friends.trim() === "") {
            friendsListElement.innerHTML = "<li>没有好友</li>";
            return;
        }

        let friendsArray = friends.split(",");
        friendsArray.forEach(function(friend) {
            let li = document.createElement("li");
            li.textContent = friend;
            friendsListElement.appendChild(li);
        });
    }


    // 显示修改密码的对话框
    function showChangePasswordDialog() {
        document.getElementById("changePasswordDialog").classList.add("show");
    }

    // 处理修改密码逻辑
    function changePassword() {

    }

    // 获取聊天记录
    function fetchChatHistory() {
        socket.send("GET_HISTORY");
    }

    // 显示添加好友的对话框
    function showAddUserDialog() {
        document.getElementById("addUserDialog").classList.add("show");
    }

    // 显示删除好友的对话框
    function showRemoveUserDialog() {
        document.getElementById("removeUserDialog").classList.add("show");
    }

    function sendFile() {
        let fileInput = document.getElementById("fileInput");
        let file = fileInput.files[0];
        let targetUsername = document.getElementById("targetUser").value;

        if (file && targetUsername) {
            let reader = new FileReader();

            reader.onload = function(event) {
                let fileData = new Uint8Array(event.target.result);
                let base64FileData = btoa(String.fromCharCode.apply(null, fileData));
                let message = "FILE:" +  targetUsername + ":" + file.name + ":" + base64FileData;
                socket.send(message);
            };

            reader.readAsArrayBuffer(file); // 读取文件内容
        }
    }

    function sendMessage() {
        let message = document.getElementById("chatInput").value;
        let targetUser = document.getElementById("targetUser").value.trim(); // 获取目标用户或群组的名称
        let maxSize = 5 * 1024 * 1024; // 最大文件大小为 5 MB

        if (message.trim() !== "") {
            if (targetUser === "") {
                // 如果目标用户为空，则发送给所有用户
                socket.send("SEND:" + "TEXT:所有用户:" + message);
            } else {
                // 否则，发送给指定的用户
                socket.send("SEND:" + "TEXT:" + targetUser + ":" + message);
            }
            document.getElementById("chatInput").value = "";
            document.getElementById("targetUser").value = ""; // 清空目标用户输入框
        }

        // 处理文件上传
        let fileInput = document.getElementById("fileInput"); // 获取文件输入框的引用
        let file = fileInput.files[0]; // 获取第一个选择的文件
        if (file) {
            // 检查文件大小
            if (file.size > maxSize) {
                alert("文件太大，最大允许上传大小为 5 MB");
                return; // 文件太大时阻止上传
            }

            let reader = new FileReader();

            reader.onload = function(event) {
                let binary = '';
                let bytes = new Uint8Array(event.target.result);
                let len = bytes.byteLength;

                for (let i = 0; i < len; i++) {
                    binary += String.fromCharCode(bytes[i]);
                }

                let base64FileData = btoa(binary);

                // 使用最新的 targetUser 值
                let currentTargetUser = targetUser !== "" ? targetUser : "所有用户";
                let fileMessage = "SEND:" + "FILE:" + currentTargetUser + ":" + file.name + ":" + base64FileData;

                socket.send(fileMessage);

                // 清空文件输入框
                fileInput.value = "";
            };

            reader.readAsArrayBuffer(file); // 读取文件内容
        }
    }


    // 添加用户
    function addUsers() {
        let username = document.getElementById("username").value;
        let password = document.getElementById("password").value;
        let email = document.getElementById("email").value;
        if (username.trim() !== ""&&password.trim() !== ""&&email.trim()!=="") {
            socket.send("ADD_USER:" + username + ":" + password + ":" + email); // 发送添加用户请求
            closeDialog("addUserDialog");
        }
    }

    // 删除好友
    function removeUser() {
        let username = document.getElementById("removeUserName").value;
        if(username.trim() !== null)
        {
            socket.send("REMOVE_USER:" + username);
            closeDialog("removeUserDialog");
        }
    }

    // 显示在线用户的对话框
    function showOnlineUsersDialog() {
        document.getElementById("onlineUsersDialog").classList.add("show");
        // 这里可以添加获取在线用户并显示的逻辑
        fetchOnlineUsers();
    }

    // 获取在线用户
    function fetchOnlineUsers() {
        socket.send("GET_USERS");
    }

    // 处理获取到的在线用户
    function updateOnlineUsers(userList) {
        let users = userList.split(",");
        let userListElement = document.getElementById("onlineUsersList");
        userListElement.innerHTML = ""; // 清空当前用户列表

        users.forEach(function(username) {
            let li = document.createElement("li");
            li.textContent = username;
            userListElement.appendChild(li);
        });
    }

</script>
</body>
</html>
