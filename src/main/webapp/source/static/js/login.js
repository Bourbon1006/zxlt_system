document.addEventListener('DOMContentLoaded', (event) => {
    document.getElementById('loginForm').addEventListener('submit', function(event) {
        event.preventDefault(); // 防止表单提交

        let username = document.getElementById('username').value;
        let password = document.getElementById('password').value;

        fetch('/login', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded'
            },
            body: new URLSearchParams({
                username: username,
                password: password
            })
        })
            .then(response => response.json())
            .then(data => {
                if (data.success) {
                    // 存储用户 ID与用户名并重定向到聊天页面
                    sessionStorage.setItem('userId', data.userId);
                    sessionStorage.setItem('username', username);
                    window.location.href = '/chat.jsp';  // 假设聊天页面是 chat.jsp
                } else {
                    const errorMessageElement = document.getElementById('error-message');
                    if (errorMessageElement) {
                        errorMessageElement.textContent = '登录失败，请检查用户名和密码。';
                    }
                }
            })
            .catch(error => {
                const errorMessageElement = document.getElementById('error-message');
                if (errorMessageElement) {
                    errorMessageElement.textContent = '发生错误，请稍后再试。';
                }
            });
    });

});
