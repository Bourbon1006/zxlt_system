<%@ page contentType="text/html;charset=UTF-8" %>
<!DOCTYPE html>
<html lang="en">

<head>
    <meta charset="UTF-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Login</title>
    <link rel="stylesheet" href="../../source/static/css/login.css">
</head>

<body>
<div class="container">
    <div class="form">
        <h2>欢迎登录</h2>
        <form id="loginForm" method="post" onsubmit="handleLogin(event)">
            <input type="text" id="username" name="username" placeholder="用户名..." required>
            <input type="password" id="password" name="password" placeholder="密码..." required>
            <a href="forgotPassword" class="forget-password">忘记密码？</a>
            <button type="submit">登录</button>
            <button type="button" class="register-button" onclick="window.location.href='register'">注册</button>
        </form>
    </div>
</div>
<script src="../../source/static/js/login.js"></script>
</body>

</html>
