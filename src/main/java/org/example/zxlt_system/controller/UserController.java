package org.example.zxlt_system.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.zxlt_system.model.User;
import org.example.zxlt_system.service.UserService;
import org.example.zxlt_system.service.UserServiceImpl;

import java.io.IOException;

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
                request.getSession().invalidate();
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
                registerUser(request, response);
                break;
            case "/login":
                loginUser(request, response);
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

    private void registerUser(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        String email = request.getParameter("email");

        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        user.setEmail(email);

        userService.register(user);
        response.sendRedirect(request.getContextPath() + "/login");
    }

    private void loginUser(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String username = request.getParameter("username");
        String password = request.getParameter("password");

        User user = userService.login(username, password);
        response.setContentType("application/json");
        if (user != null) {
            // 登录成功，返回用户 ID
            response.getWriter().write("{\"success\": true, \"userId\": \"" + user.getId() + "\"}");
        } else {
            // 登录失败
            response.getWriter().write("{\"success\": false}");
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
