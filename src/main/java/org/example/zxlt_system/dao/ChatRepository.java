package org.example.zxlt_system.dao;

import org.example.zxlt_system.model.User;
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
        String query = "SELECT updated_at FROM friends WHERE (user_id = ? AND friend_id = ? AND status = 'accepted') OR (user_id = ? AND friend_id = ? AND status = 'accepted')";

        try (PreparedStatement stmt = con.prepareStatement(query)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, friendId);
            stmt.setInt(3, friendId);
            stmt.setInt(4, userId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Timestamp friendshipStartTime = rs.getTimestamp("updated_at");
                    //System.out.println("Friendship start time found: " + friendshipStartTime);
                    return friendshipStartTime;
                } else {
                    //System.out.println("No friendship record found.");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace(); // 打印堆栈跟踪
            throw new SQLException("Failed to retrieve friendship start time from database", e);
        }

        return null;
    }


    public List<String> getChatRecords(int userId, String role) throws SQLException {
        List<String> records = new ArrayList<>();

        if ("admin".equals(role)) {
            // 管理员查询所有聊天记录
            String query = "SELECT sender_id, receiver_id, message_content, sent_at FROM chat_messages " +
                    "ORDER BY sent_at";

            try (PreparedStatement stmt = con.prepareStatement(query);
                 ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    int senderId = rs.getInt("sender_id");
                    String messageContent = rs.getString("message_content");

                    // 获取发送者的用户名
                    String senderUsername = userRepository.findById(senderId).getUsername();
                    String receiverUsername = userRepository.findById(rs.getInt("receiver_id")).getUsername();

                    // 格式化记录
                    String record = senderUsername + ": " + messageContent + ":" + receiverUsername;
                    records.add(record);
                }
            }
        } else {
            // 普通用户查询与好友的聊天记录
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
                        Timestamp sentAt = rs.getTimestamp("sent_at");

                        User user = userRepository.findById(senderId);
                        User user1 = userRepository.findById(receiverId);

                        // 获取发送者的用户名
                        String senderUsername = userRepository.findById(senderId).getUsername();
                        String receiverUsername = userRepository.findById(receiverId).getUsername();

                        boolean shouldAdd = false;
                        if (userId == senderId || userId == receiverId) {
                            // 如果是普通用户，检查与好友的聊天记录
                            Timestamp friendshipStartTime = getFriendshipStartTime(userId, senderId);
                            if (friendshipStartTime != null && friendshipStartTime.before(sentAt)|| senderId == userId||user.getRole().equals("admin")) {
                                shouldAdd = true;
                            }
                        }
                        if (shouldAdd) {
                            // 格式化记录
                            String record = senderUsername + ": " + messageContent + ":" + receiverUsername;
                            records.add(record);
                        }
                    }
                }
            }
        }

        return records;
    }
}
