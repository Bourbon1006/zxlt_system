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
    <h2>Sign In</h2>
    <form id="loginForm">
      <input type="text" id="username" name="username" placeholder="Username..." required>
      <input type="password" id="password" name="password" placeholder="Password..." required>
      <a href="#" class="forget-password">Forgot your password?</a>
      <button type="submit">Sign In</button>
      <button type="button" class="register-button" onclick="window.location.href='register'">Sign Up</button>
    </form>
  </div>
</div>
<script src="../../source/static/js/login.js"></script>
</body>

</html>
