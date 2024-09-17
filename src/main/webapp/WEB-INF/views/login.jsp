<%@ page contentType="text/html;charset=UTF-8" %>
<!DOCTYPE html>
<html lang="en">

<head>
    <meta charset="UTF-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Login</title>
    <link rel="stylesheet" href="../../source/static/css/login.css">
    <script>
        document.addEventListener('DOMContentLoaded', (event) => {
            document.getElementById('loginForm').addEventListener('submit', function(event) {
                event.preventDefault(); // 防止表单提交

                let username = document.getElementById('username').value;
                let password = document.getElementById('password').value;

                // 将表单数据转换为 URL 编码格式
                let formData = new URLSearchParams();
                formData.append('username', username);
                formData.append('password', password);

                fetch('/login', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/x-www-form-urlencoded'
                    },
                    body: formData.toString()
                })
                    .then(response => {
                        if (!response.ok) {
                            throw new Error('网络响应不是 OK');
                        }
                        return response.text(); // 获取原始文本数据
                    })
                    .then(data => {
                        // 解析 URL 编码格式的响应数据
                        let params = new URLSearchParams(data);
                        let success = params.get('success');
                        let userId = params.get('userId');
                        let role = params.get('role');

                        if (success === 'true') {
                            sessionStorage.setItem('userId', userId);
                            sessionStorage.setItem('username', username);

                            if (role === 'admin') {
                                window.location.href = '/admin.jsp';
                            } else {
                                window.location.href = '/chat.jsp';
                            }
                        } else {
                            const errorMessageElement = document.getElementById('error-message');
                            if (errorMessageElement) {
                                errorMessageElement.textContent = '登录失败，请检查用户名和密码。';
                            }
                        }
                    })
                    .catch(error => {
                        console.error('发生错误:', error);
                        const errorMessageElement = document.getElementById('error-message');
                        if (errorMessageElement) {
                            errorMessageElement.textContent = '发生错误，请稍后再试。';
                        }
                    });
            });
        });
    </script>
</head>

<body>
<div class="container">
    <div class="form">
        <h2>欢迎登录</h2>
        <form id="loginForm" method="post">
            <input type="text" id="username" name="username" placeholder="用户名..." required>
            <input type="password" id="password" name="password" placeholder="密码..." required>
            <a href="forgotPassword" class="forget-password">忘记密码？</a>
            <button type="submit">登录</button>
            <button type="button" class="register-button" onclick="window.location.href='register'">注册</button>
        </form>
        <div id="error-message" style="color: red;"></div> <!-- 添加错误消息显示 -->
    </div>
</div>
</body>

</html>
