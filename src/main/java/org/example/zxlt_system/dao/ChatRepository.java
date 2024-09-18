package org.example.zxlt_system.dao;

import org.example.zxlt_system.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ChatRepository {

    private final Connection con;
    private final UserRepository userRepository = new UserRepository();

    public ChatRepository() {
        this.con = DBConnection.getConnection();
    }

    public void storeChatMessage(int senderId, int receiverId, String messageContent) throws SQLException {
        String query = "INSERT INTO chat_messages (sender_id, receiver_id, message_content, sent_at) VALUES (?, ?, ?, NOW())";
        try (PreparedStatement stmt = con.prepareStatement(query)) {
            stmt.setInt(1, senderId);
            stmt.setInt(2, receiverId);
            stmt.setString(3, messageContent);
            stmt.executeUpdate();
        }
    }

    public Timestamp getFriendshipStartTime(int userId, int friendId) throws SQLException {
        String query = "SELECT updated_at FROM friends WHERE (user_id = ? AND friend_id = ? AND status = 'ACCEPT') OR (user_id = ? AND friend_id = ? AND status = 'ACCEPT')";
        try (PreparedStatement stmt = con.prepareStatement(query)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, friendId);
            stmt.setInt(3, friendId);
            stmt.setInt(4, userId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getTimestamp("updated_at");
                }
            }
        }
        return null;
    }

    public List<String> getChatRecords(int userId) throws SQLException {
        List<String> records = new ArrayList<>();
        String query = "SELECT sender_id, receiver_id, message_content, sent_at FROM chat_messages " +
                "WHERE sender_id = ? OR receiver_id = ? ORDER BY sent_at";

        try (PreparedStatement stmt = con.prepareStatement(query)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, userId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    int senderId = rs.getInt("sender_id");
                    int receiverId = rs.getInt("receiver_id");
                    String messageContent = rs.getString("message_content");

                    // 获取用户名
                    String senderUsername = userRepository.findById(senderId).getUsername();

                    // 格式化记录
                    String record = senderUsername + ": " + messageContent;
                    records.add(record);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace(); // 打印堆栈跟踪
            throw new SQLException("Failed to retrieve chat records from database", e);
        }

        return records;
    }

    // 其他可能的方法
}
