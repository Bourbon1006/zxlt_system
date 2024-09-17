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
            <button onclick="showOnlineUsersDialog()">查看在线用户</button>
            <button onclick="showAddFriendDialog()">添加好友</button>
            <button onclick="showRemoveFriendDialog()">删除好友</button>
            <button onclick="showFriendsDialog()">显示好友</button>
            <button onclick="showChangePasswordDialog()">修改密码</button> <!-- 添加修改密码按钮 -->
            <button onclick="logout()">退出登录</button> <!-- 添加退出登录按钮 -->
        </div>
    </div>

    <div id="mainContent">
        <!-- 聊天窗口 -->
        <div id="chatWindow" class="view">
            <h2>聊天窗口</h2>
            <div id="chatMessages"></div>

            <!-- 输入框 -->
            <input type="text" id="chatInput" placeholder="输入消息...">
            <input type="text" id="targetUser" placeholder="输入接收者用户名..."> <!-- 添加目标用户输入框 -->

            <!-- 发送按钮 -->
            <button onclick="sendMessage()">发送</button>
            <input type="file" id="fileInput">
        </div>
        <!-- 其他视图可以以类似方式添加 -->
    </div>
</div>

<!-- 添加好友的对话框 -->
<div id="addFriendDialog" class="dialog">
    <label for="friendName">好友用户名:</label>
    <input type="text" id="friendName">
    <button onclick="addFriend()">确认添加</button>
    <button onclick="closeDialog('addFriendDialog')">取消</button>
</div>

<!-- 删除好友的对话框 -->
<div id="removeFriendDialog" class="dialog">
    <label for="removeFriendName">好友用户名:</label>
    <input type="text" id="removeFriendName">
    <button onclick="removeFriend()">确认删除</button>
    <button onclick="closeDialog('removeFriendDialog')">取消</button>
</div>

<!-- 好友请求的对话框 -->
<div id="friendRequestDialog" class="dialog">
    <p>用户 <span id="requesterName"></span> 请求添加你为好友</p>
    <button onclick="acceptFriendRequest()">接受</button>
    <button onclick="declineFriendRequest()">拒绝</button>
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
        initializeWebSocket();
    };

    function initializeWebSocket() {
        socket = new WebSocket("ws://127.0.0.1:8080/chat/" + userId);

        socket.onopen = function() {
            console.log("WebSocket 连接已打开");
            fetchChatHistory(); // 获取聊天记录
        };

        socket.onmessage = function(event) {
            handleMessage(event.data); // 调用统一的消息处理函数
        };

        socket.onclose = function() {
            console.log("WebSocket 连接已关闭");
        };

        socket.onerror = function(error) {
            console.error("WebSocket 错误: " + error.message);
        };
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

    // 当用户点击“接受”按钮
    function acceptFriendRequest() {
        let requester = document.getElementById("requesterName").textContent;
        socket.send("ACCEPT_FRIEND:" + requester); // 发送接受好友请求
        closeDialog("friendRequestDialog");
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



    // 处理接收到的消息
    function handleMessage(message) {
        if (message.startsWith("ONLINE_USERS:")) {
            updateOnlineUsers(message.substring(13));
        } else if (message.startsWith("CHAT_HISTORY:")) {
            displayChatHistory(message.substring(13));
        } else if (message.startsWith("FRIEND_REQUEST:")) {
            let requester = message.substring(15);
            showFriendRequestDialog(requester);
        } else if (message.startsWith("FRIENDS_LIST:")) {
            let friends = message.substring(13);
            displayFriendsList(friends);
        } else if (message.startsWith("ADD_FRIEND")) {
            alert(message); // 好友请求已发送
        } else if (message.startsWith("ACCEPT_FRIEND")) {
            alert(message); // 好友请求已接受
        } else if (message.startsWith("REJECT_FRIEND")) {
            alert(message); // 好友请求已拒绝
        } else {
            displayChatMessage(message);
        }
    }
    // 显示修改密码的对话框
    function showChangePasswordDialog() {
        document.getElementById("changePasswordDialog").classList.add("show");
    }

    // 处理修改密码逻辑
    function changePassword() {
        let oldPassword = document.getElementById("oldPassword").value;
        let newPassword = document.getElementById("newPassword").value;
        let confirmNewPassword = document.getElementById("confirmNewPassword").value;

        if (newPassword !== confirmNewPassword) {
            alert("新密码和确认密码不一致，请重新输入！");
            return;
        }

        if (oldPassword.trim() === "" || newPassword.trim() === "" || confirmNewPassword.trim() === "") {
            alert("所有字段都是必填项！");
            return;
        }

        // 使用 WebSocket 发送修改密码请求
        socket.send("CHANGE_PASSWORD:" + oldPassword + ":" + newPassword);

        // 关闭对话框
        closeDialog("changePasswordDialog");
    }


    // 显示好友列表的对话框
    function showFriendsDialog() {
        document.getElementById("friendsDialog").classList.add("show");
        fetchFriendsList(); // 获取好友列表
    }


    // 获取好友列表
    function fetchFriendsList() {
        socket.send("GET_FRIENDS");
    }

    // 更新好友列表
    function updateFriendsList(friends) {
        let friendsListElement = document.getElementById("friendsList");
        friendsListElement.innerHTML = ""; // 清空当前好友列表

        friends.split(",").forEach(function(username) {
            let li = document.createElement("li");
            li.textContent = username;
            friendsListElement.appendChild(li);
        });
    }


    // 处理服务器返回的好友列表
    socket.onmessage = function(event) {
        let message = event.data;
        if (message.startsWith("FRIENDS_LIST:")) {
            let friends = message.substring(13); // 提取好友列表
            updateFriendsList(friends); // 更新好友列表显示
        } else if (message.startsWith("FRIEND_REQUEST:")) {
            let requester = message.substring(15);
            showFriendRequestDialog(requester);
        } else if (message.startsWith("ADD_FRIEND")) {
            alert(message);
        } else if (message.startsWith("ACCEPT_FRIEND")) {
            alert(message);
        } else if (message.startsWith("REJECT_FRIEND")) {
            alert(message);
        } else {
            displayChatMessage(message);
        }
    };


    // 获取聊天记录
    function fetchChatHistory() {
        socket.send("GET_HISTORY");
    }

    // 显示聊天消息
    function displayChatMessage(message) {
        let chatMessages = document.getElementById("chatMessages");
        let p = document.createElement("p");

        // 假设消息格式为 "用户名:消息内容"
        let messageParts = message.split(":");
        let username = messageParts[0];
        let messageContent = messageParts.slice(1).join(":");

        // 判断消息是否是自己发的（假设 currentUsername 是当前用户的用户名）
        if (username === currentUsername) {
            p.textContent = "我: " + messageContent;
        } else {
            p.textContent = message;
        }

        chatMessages.appendChild(p);
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


    // 显示添加好友的对话框
    function showAddFriendDialog() {
        document.getElementById("addFriendDialog").classList.add("show");
    }

    // 显示删除好友的对话框
    function showRemoveFriendDialog() {
        document.getElementById("removeFriendDialog").classList.add("show");
    }


    // 添加好友
    function addFriend() {
        let friendName = document.getElementById("friendName").value;
        if (friendName.trim() !== "") {
            socket.send("ADD_FRIEND:" + friendName); // 发送添加好友请求
            closeDialog("addFriendDialog");
        }
    }

    // 删除好友
    function removeFriend() {
        let friendName = document.getElementById("removeFriendName").value;
        if (friendName.trim() !== "") {
            socket.send("REMOVE_FRIEND:" + friendName); // 发送删除好友请求
            closeDialog("removeFriendDialog");
        }
    }

    // 显示好友请求的对话框
    function showFriendRequestDialog(requester) {
        let dialog = document.getElementById("friendRequestDialog");
        document.getElementById("requesterName").textContent = requester;
        dialog.classList.add("show");
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

    // 关闭对话框
    function closeDialog(dialogId) {
        document.getElementById(dialogId).classList.remove("show");
    }



    // 处理服务器返回的好友请求更新
    socket.onmessage = function(event) {
        let message = event.data;
        if (message.startsWith("FRIEND_REQUEST:")) {
            // 处理收到的好友请求
            let requester = message.substring(14); // 提取请求者用户名
            // 例如，显示一个弹窗或更新好友请求列表
        } else if (message.startsWith("FRIEND_REQUEST_ACCEPTED:")) {
            // 处理好友请求被接受的情况
            let requester = message.substring(24); // 提取请求者用户名
            // 更新界面，显示请求已被接受
            alert("Your friend request has been accepted by " + requester);
        }
        // 处理其他消息
    };

    // 拒绝好友请求
    function declineFriendRequest() {
        closeDialog("friendRequestDialog");
    }
</script>
</body>
</html>
