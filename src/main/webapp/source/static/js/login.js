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
                // 存储用户 ID 并重定向到聊天页面
                sessionStorage.setItem('userId', data.userId);
                window.location.href = '/chat.jsp';  // 假设聊天页面是 chat.jsp
            } else {
                document.getElementById('error-message').textContent = '登录失败，请检查用户名和密码。';
            }
        })
        .catch(error => {
            document.getElementById('error-message').textContent = '发生错误，请稍后再试。';
        });
});
