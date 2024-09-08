package org.example.zxlt_system.dao;

import org.example.zxlt_system.util.DBConnection;

import java.sql.*;

public class FriendRepository {

    private static Connection connection;

    public FriendRepository() {
        this.connection = DBConnection.getConnection();
    }

    // 获取用户ID
    public int getUserIdByUsername(String username) throws SQLException {
        int userId = -1;
        String query = "SELECT id FROM users WHERE username = ?";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, username);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    userId = resultSet.getInt("id");
                }
            }
        }
        return userId;
    }

    // 添加好友
    public boolean addFriend(int senderId, int receiverId) throws SQLException {
        String query = "INSERT INTO friends (user_id, friend_id, status) VALUES (?, ?, 'pending')";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, senderId);
            statement.setInt(2, receiverId);
            int rowsAffected = statement.executeUpdate();
            return rowsAffected > 0;
        }
    }



    // 删除好友
    public boolean removeFriend(int userId, int friendId) throws SQLException {
        String query = "DELETE FROM friends WHERE user_id = ? AND friend_id = ?";
        boolean success = false;
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, userId);
            statement.setInt(2, friendId);
            int rowsAffected = statement.executeUpdate();
            success = rowsAffected > 0;
        }
        return success;
    }



    // 更新好友请求状态
    public boolean updateFriendRequestStatus(int senderId, int receiverId, String status) throws SQLException {
        String query = "UPDATE friends SET status = ? WHERE user_id = ? AND friend_id = ?";
        try (
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, status);
            statement.setInt(2, senderId);
            statement.setInt(3, receiverId);
            int rowsAffected = statement.executeUpdate();
            return rowsAffected > 0;
        }
    }

    // 添加好友请求（待处理）
    public boolean acceptFriendRequest(int senderId, int receiverId) throws SQLException {
        connection.setAutoCommit(false); // 开始事务

        try {
            // 更新请求的状态为 'accepted'
            String query = "UPDATE friends SET status = 'accepted' WHERE user_id = ? AND friend_id = ? AND status = 'pending'";
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setInt(1, senderId);
                statement.setInt(2, receiverId);
                int rowsAffected = statement.executeUpdate();
                if (rowsAffected == 0) {
                    connection.rollback(); // 请求可能已经被接受或不存在
                    return false;
                }
            }

            // 确保对方也被添加为好友
            String query2 = "INSERT INTO friends (user_id, friend_id, status) VALUES (?, ?, 'accepted')";
            try (PreparedStatement statement = connection.prepareStatement(query2)) {
                statement.setInt(1, receiverId);
                statement.setInt(2, senderId);
                int rowsAffected2 = statement.executeUpdate();
                if (rowsAffected2 > 0) {
                    connection.commit(); // 提交事务
                    return true;
                } else {
                    connection.rollback(); // 回滚事务
                    return false;
                }
            }
        } catch (SQLException e) {
            if (connection != null) {
                try {
                    connection.rollback(); // 回滚事务
                } catch (SQLException rollbackEx) {
                    rollbackEx.printStackTrace();
                }
            }
            throw e;
        } finally {
            connection.setAutoCommit(true); // 恢复自动提交
        }
    }
    public static String getFriendsList(int userIdInt) throws SQLException {
        String friendsQuery = "SELECT friend_id FROM friends WHERE user_id = ?";
        String usersQuery = "SELECT username FROM users WHERE id = ?";
        StringBuilder friendsList = new StringBuilder();

        try (PreparedStatement friendsStmt = connection.prepareStatement(friendsQuery)) {
            friendsStmt.setInt(1, userIdInt);
            try (ResultSet friendsRs = friendsStmt.executeQuery()) {
                while (friendsRs.next()) {
                    int friendId = friendsRs.getInt("friend_id");

                    // 查找好友的用户名
                    try (PreparedStatement usersStmt = connection.prepareStatement(usersQuery)) {
                        usersStmt.setInt(1, friendId);
                        try (ResultSet usersRs = usersStmt.executeQuery()) {
                            if (usersRs.next()) {
                                if (friendsList.length() > 0) {
                                    friendsList.append(",");
                                }
                                friendsList.append(usersRs.getString("username"));
                            }
                        }
                    }
                }
            }
        }

        return friendsList.toString();
    }

    // 检查用户是否已经是好友
    public boolean isFriend(int userId, int friendId) throws SQLException {
        String query = "SELECT COUNT(*) FROM friends WHERE user_id = ? AND friend_id = ? AND status = 'accepted'";
        try (
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, userId);
            statement.setInt(2, friendId);
            try (ResultSet resultSet = statement.executeQuery()) {
                resultSet.next();
                return resultSet.getInt(1) > 0;
            }
        }

    }
    // 关闭数据库连接
    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


}
