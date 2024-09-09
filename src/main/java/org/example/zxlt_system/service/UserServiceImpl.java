package org.example.zxlt_system.service;

import org.example.zxlt_system.dao.UserRepository;
import org.example.zxlt_system.model.User;

public class UserServiceImpl implements UserService {
    private UserRepository userRepository = new UserRepository();

    @Override
    public void register(User user) {
        try {
            userRepository.saveUser(user);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public User login(String username, String password) {
        try {
            User user = userRepository.findByUsername(username);
            if (user != null && user.getPassword().equals(password)) {
                return user;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean findByUsernameAndEmail(String username, String email) {
        try {
            return userRepository.isUsernameAndEmailValid(username, email);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean resetPassword(String username, String email,String newPassword) {
        try {
            // 调用 UserRepository 的 resetPassword 方法
            return userRepository.resetPassword(username, email, newPassword);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }



}
