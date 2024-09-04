<%@ page contentType="text/html;charset=UTF-8" %>
<!DOCTYPE html>
<html>
<head>
  <link rel="stylesheet" href="../../source/static/css/register.css">
  <title>注册</title>
</head>
<body>
<div class="container">
  <h2>注册</h2>
  <form action="register" method="post">
    <div class="form-group">
      <label for="username">用户名:</label>
      <input type="text" id="username" name="username" required>
    </div>
    <div class="form-group">
      <label for="password">密码:</label>
      <input type="password" id="password" name="password" required>
    </div>
    <div class="form-group">
      <label for="email">邮箱:</label>
      <input type="email" id="email" name="email" required>
    </div>
    <div class="form-group">
      <input type="submit" value="注册">
    </div>
  </form>
</div>
</body>
</html>
