package org.example.zxlt_system.service;

import org.example.zxlt_system.dao.FriendRepository;
import java.sql.SQLException;
import java.util.List;

public class FriendService {
    private final FriendRepository friendRepository = new FriendRepository();

    public boolean addFriend(int userId, int friendId) throws SQLException {
        return friendRepository.addFriend(userId, friendId);
    }

    public boolean removeFriend(int userId, int friendId) throws SQLException {
        return friendRepository.removeFriend(userId, friendId);
    }

    public List<String> getFriendList(int userId) throws SQLException {
        return FriendRepository.getFriendsList(userId);
    }
}
