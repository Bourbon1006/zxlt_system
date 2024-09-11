package org.example.zxlt_system.service;

import org.example.zxlt_system.model.User;

import java.sql.SQLException;
import java.util.List;

public interface UserService {
    void register(User user) throws SQLException;
    User login(String username, String password) throws SQLException;  // 修改为返回 User 对象
    boolean findByUsernameAndEmail(String username, String email);
    boolean resetPassword(String username, String email, String newPassword);
    void addUser(User user) throws SQLException;
    boolean updateUser(User user) throws SQLException;
    boolean deleteUser(int userId) throws SQLException;
    List<User> getAllUsers() throws SQLException;
}

