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
                return user; // 返回用户对象
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null; // 登录失败，返回 null
    }
}
