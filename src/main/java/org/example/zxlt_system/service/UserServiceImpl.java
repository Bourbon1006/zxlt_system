package org.example.zxlt_system.service;

import org.example.zxlt_system.dao.UserRepository;
import org.example.zxlt_system.model.User;
import org.example.zxlt_system.service.UserService;

import java.sql.SQLException;
import java.util.List;

public class UserServiceImpl implements UserService {
    private UserRepository userRepository = new UserRepository();

    @Override
    public void register(User user) throws SQLException {
        // 实现用户注册
        userRepository.addUser(user);
    }

    @Override
    public User login(String username, String password) throws SQLException {
        // 实现用户登录
        return userRepository.findByUsername(username);
    }

    @Override
    public boolean findByUsernameAndEmail(String username, String email) {
        // 验证用户名和电子邮件
        return userRepository.isUsernameAndEmailValid(username, email);
    }

    @Override
    public boolean resetPassword(String username, String email, String newPassword) {
        // 重置密码
        return userRepository.resetPassword(username, newPassword, email);
    }

    @Override
    public void addUser(User user) throws SQLException {
        // 添加用户
        userRepository.addUser(user);
    }

    @Override
    public boolean updateUser(User user) throws SQLException {
        // 更新用户信息
        return userRepository.updateUser(user);
    }

    @Override
    public boolean deleteUser(int userId) throws SQLException {
        // 删除用户
        return userRepository.deleteUser(userId);
    }

    @Override
    public List<User> getAllUsers() throws SQLException {
        // 获取所有用户
        return userRepository.getAllUsers();
    }
}
