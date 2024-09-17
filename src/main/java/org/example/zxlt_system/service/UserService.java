package org.example.zxlt_system.service;

import org.example.zxlt_system.model.User;

import java.sql.SQLException;
import java.util.List;

public interface UserService {
    boolean register(User user) throws SQLException;
    User login(String username, String password) throws SQLException;  // 修改为返回 User 对象
    String findByUsernameAndEmail(String username, String email);
    boolean resetPassword(String username, String email, String newPassword);
    boolean updateUser(User user) throws SQLException;
    boolean deleteUser(String username) throws SQLException;
    List<User> getAllUsers() throws SQLException;
    boolean isUsernameExists(String username) throws SQLException;
    boolean isEmailExists(String email) throws SQLException;
}

