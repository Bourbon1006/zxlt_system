package org.example.zxlt_system.service;

import org.example.zxlt_system.model.User;

public interface UserService {
    void register(User user);
    User login(String username, String password);  // 修改为返回 User 对象
}
