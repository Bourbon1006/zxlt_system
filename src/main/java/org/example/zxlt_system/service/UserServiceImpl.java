package org.example.zxlt_system.service;

import org.example.zxlt_system.dao.UserRepository;
import org.example.zxlt_system.model.User;

import java.sql.SQLException;
import java.util.List;

public class UserServiceImpl implements UserService {
    private UserRepository userRepository = new UserRepository();

    @Override
    public boolean register(User user) throws SQLException {
        // 实现用户注册
        if(userRepository.isEmailExists(user.getEmail()) || userRepository.isEmailExists(user.getEmail()))
        {
            return false;
        }
        userRepository.addUser(user);
        return true;
    }

    @Override
    public User login(String username, String password) throws SQLException {
        // 实现用户登录
        return userRepository.login(username,password);
    }

    @Override
    public String findByUsernameAndEmail(String username, String email) {
        // 获取用户角色
        String role = userRepository.isUsernameAndEmailValid(username, email);
        return role; // 返回角色，如果是 null，说明用户不存在
    }


    @Override
    public boolean resetPassword(String username,  String newPassword,String email) {
        // 重置密码
        return userRepository.resetPassword(username, email ,newPassword);
    }


    @Override
    public boolean updateUser(User user) throws SQLException {
        // 更新用户信息
        return userRepository.updateUser(user);
    }

    @Override
    public boolean deleteUser(String username) throws SQLException {
        // 删除用户
        return userRepository.deleteUser(username);
    }

    @Override
    public List<User> getAllUsers() throws SQLException {
        // 获取所有用户
        return userRepository.getAllUsers();
    }

    @Override
    public boolean isUsernameExists(String username) throws SQLException {
        return userRepository.isUsernameExists(username);
    }

    @Override
    public boolean isEmailExists(String email) throws SQLException {
        return userRepository.isEmailExists(email);
    }


    @Override
    public User findUserById(int userId) throws SQLException {
        return userRepository.findById(userId);
    }

    @Override
    public boolean updateUser(int userId, String newUsername, String newEmail, String newPassword) throws SQLException {
        User user = userRepository.findById(userId);
        if (user != null) {
            user.setUsername(newUsername);
            user.setEmail(newEmail);
            if (newPassword != null && !newPassword.isEmpty()) {
                user.setPassword(newPassword);
            }
            return userRepository.update(user);
        }
        return false;
    }
}
