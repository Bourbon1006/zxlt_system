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
            <button onclick="showAddFriendDialog()">添加好友</button>
            <button onclick="showRemoveFriendDialog()">删除好友</button>
            <button onclick="showOnlineUsersDialog()">查看在线用户</button> <!-- 添加在线用户按钮 -->
            <button onclick="showFriendsDialog()">显示好友</button> <!-- 添加显示好友按钮 -->
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

<!-- 在线用户的对话框 -->
<div id="onlineUsersDialog" class="dialog">
    <h2>在线用户</h2>
    <ul id="onlineUsersList"></ul>
    <button onclick="closeDialog('onlineUsersDialog')">关闭</button>
</div>

<script>
    let socket;
    let userId = sessionStorage.getItem('userId'); // 用户 ID，根据实际登录的用户动态设置

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


    // 发送聊天消息
    function sendMessage() {
        let message = document.getElementById("chatInput").value;
        let targetUser = document.getElementById("targetUser").value; // 获取目标用户或群组的名称

        if (message.trim() !== "") {
            if (targetUser.trim() === "") {
                // 如果目标用户为空，则发送给所有用户
                socket.send("SEND:所有用户:" + message);
            } else {
                // 否则，发送给指定的用户
                socket.send("SEND:" + targetUser + ":" + message);
            }
            document.getElementById("chatInput").value = "";
            document.getElementById("targetUser").value = ""; // 清空目标用户输入框
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
        p.textContent = message;
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
