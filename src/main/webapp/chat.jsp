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
            <!-- 将输入框的占位符从 User ID 改为 Username -->
            <input id="targetUser" type="text" placeholder="Enter recipient's username"/>
            <button onclick="sendMessage()">Send</button>
        </div>
    </div>
</div>

<script src="source/static/js/chat.js">
</script>
</body>
</html>
