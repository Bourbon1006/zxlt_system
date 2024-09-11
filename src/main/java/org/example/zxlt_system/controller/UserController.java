package org.example.zxlt_system.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.example.zxlt_system.model.User;
import org.example.zxlt_system.service.UserService;
import org.example.zxlt_system.service.UserServiceImpl;

import java.io.IOException;
import java.sql.SQLException;

@WebServlet({"/login", "/register", "/logout", "/forgotPassword", "/resetPassword"})
public class UserController extends HttpServlet {
    private UserService userService = new UserServiceImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String path = request.getServletPath();
        switch (path) {
            case "/register":
                request.getRequestDispatcher("/WEB-INF/views/register.jsp").forward(request, response);
                break;
            case "/login":
                request.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(request, response);
                break;
            case "/logout":
                // 使当前会话无效，清除所有会话数据
                HttpSession session = request.getSession(false);
                if (session != null) {
                    session.invalidate();
                }
                // 重定向到登录页面
                response.sendRedirect(request.getContextPath() + "/login");
                break;

            case "/forgotPassword":
                request.getRequestDispatcher("/WEB-INF/views/forgotPassword.jsp").forward(request, response);
                break;
            default:
                response.sendRedirect(request.getContextPath() + "/login");
                break;
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String path = request.getServletPath();
        switch (path) {
            case "/register":
                try {
                    registerUser(request, response);
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
                break;
            case "/login":
                try {
                    loginUser(request, response);
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
                break;
            case "/forgotPassword":
                forgotPassword(request, response);
                break;
            case "/resetPassword":
                resetPassword(request, response);
                break;
            default:
                response.sendRedirect(request.getContextPath() + "/login");
                break;
        }
    }

    private void registerUser(HttpServletRequest request, HttpServletResponse response) throws IOException, SQLException, ServletException {
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        String email = request.getParameter("email");

        // 检查输入是否为空
        if (username == null || username.trim().isEmpty() ||
                password == null || password.trim().isEmpty() ||
                email == null || email.trim().isEmpty()) {
            request.setAttribute("errorMessage", "所有字段都是必填项！");
            request.getRequestDispatcher("/register.jsp").forward(request, response);
            return;
        }

        // 检查用户名或邮箱是否已存在
        if (userService.isUsernameExists(username)) {
            request.setAttribute("errorMessage", "用户名已存在！");
            request.getRequestDispatcher("/register.jsp").forward(request, response);
            return;
        }

        if (userService.isEmailExists(email)) {
            request.setAttribute("errorMessage", "邮箱已存在！");
            request.getRequestDispatcher("/register.jsp").forward(request, response);
            return;
        }

        // 创建用户并注册
        User user = new User();
        user.setUsername(username);
        user.setPassword(password);  // 你可能需要对密码进行加密
        user.setEmail(email);

        boolean registrationSuccessful = userService.register(user);
        if (registrationSuccessful) {
            response.sendRedirect(request.getContextPath() + "/login");
        } else {
            request.setAttribute("errorMessage", "注册失败，请重试！");
            request.getRequestDispatcher("/register.jsp").forward(request, response);
        }
    }


    private void loginUser(HttpServletRequest request, HttpServletResponse response) throws IOException, SQLException {
        String username = request.getParameter("username");
        String password = request.getParameter("password");

        User user = userService.login(username, password);
        response.setContentType("application/x-www-form-urlencoded");

        if (user != null) {
            HttpSession session = request.getSession();
            session.setAttribute("username", username);

            // 确保 role 不为 null
            String role = user.getRole() != null ? user.getRole() : "user"; // 默认设置为 'user'

            // 返回用户 ID 和角色
            response.getWriter().write("success=true&userId=" + user.getId() + "&role=" + role);
        } else {
            response.getWriter().write("success=false");
        }
    }





    private void forgotPassword(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String username = request.getParameter("username");
        String email = request.getParameter("email");

        boolean userExists = userService.findByUsernameAndEmail(username, email);
        response.setContentType("application/json");

        if (userExists) {
            response.getWriter().write("{\"success\": true, \"message\": \"重置链接已发送到您的电子邮件。\"}");
        } else {
            response.getWriter().write("{\"success\": false, \"message\": \"用户名和电子邮件不匹配。\"}");
        }
    }

    private void resetPassword(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String username = request.getParameter("username");
        String newPassword = request.getParameter("newPassword");
        String email = request.getParameter("email");

        boolean success = userService.resetPassword(username, newPassword, email);
        response.setContentType("application/json");

        if (success) {
            response.getWriter().write("{\"success\": true, \"message\": \"密码已成功重置。\"}");
        } else {
            response.getWriter().write("{\"success\": false, \"message\": \"重置密码失败。\"}");
        }
    }
}
