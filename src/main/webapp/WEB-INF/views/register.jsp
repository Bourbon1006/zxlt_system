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
      <input type="text" id="username" name="username"  placeholder="Username..." required>
    </div>
    <div class="form-group">
      <input type="password" id="password" name="password" placeholder="Password..." required>
    </div>
    <div class="form-group">
      <input type="email" id="email" name="email" placeholder="Email..." required>
    </div>
    <div class="form-group">
      <input type="submit" class="register-button" value="注册">
    </div>
  </form>
</div>
</body>
</html>
