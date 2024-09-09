<%@ page contentType="text/html;charset=UTF-8" %>
<!DOCTYPE html>
<html lang="en">

<head>
    <meta charset="UTF-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Forgot Password</title>
    <link rel="stylesheet" href="../../source/static/css/forgotPassword.css">
    <script>
        function handleForgotPassword() {
            let username = document.getElementById('username').value;
            let email = document.getElementById('email').value;

            fetch('/forgotPassword', {
                method: 'POST',
                body: new URLSearchParams({
                    username: username,
                    email: email
                })
            })
                .then(response => response.json())
                .then(data => {
                    if (data.success) {
                        document.getElementById('resetPasswordSection').style.display = 'block';
                    } else {
                        alert(data.message);
                    }
                });
        }

        function handleResetPassword() {
            let username = document.getElementById('username').value;
            let email = document.getElementById('email').value;
            let newPassword = document.getElementById('newPassword').value;

            fetch('/resetPassword', {
                method: 'POST',
                body: new URLSearchParams({
                    username: username,
                    newPassword: newPassword,
                    email: email
                })
            })
                .then(response => response.json())
                .then(data => {
                    if (data.success) {
                        alert('密码已成功重置。');
                        window.location.href = '/login'; // 根据实际路径修改
                    } else {
                        alert(data.message);
                    }
                });
        }

    </script>
</head>

<body>
<div class="container">
    <div class="form">
        <h2>忘记密码</h2>
        <form id="forgotPasswordForm" method="post" onsubmit="handleForgotPassword(); return false;">
            <input type="text" id="username" name="username" placeholder="用户名..." required>
            <input type="email" id="email" name="email" placeholder="电子邮件..." required>
            <button type="submit">发送重置链接</button>
        </form>

        <div id="resetPasswordSection" style="display:none;">
            <h2>重置密码</h2>
            <form id="resetPasswordForm" method="post" onsubmit="handleResetPassword(); return false;">
                <input type="hidden" id="resetToken" name="token">
                <input type="password" id="newPassword" name="newPassword" placeholder="新密码..." required>
                <button type="submit">重置密码</button>
            </form>
        </div>
    </div>
</div>
</body>

</html>
