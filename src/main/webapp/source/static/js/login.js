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
                console.log(role);
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
