package org.example.zxlt_system.controller;

import org.example.zxlt_system.dao.UserRepository;
import org.example.zxlt_system.model.User;

import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@ServerEndpoint("/chat/{userId}")
public class ChatEndpoint {

    private static final Map<String, Session> userSessions = new HashMap<>();
    private static final Map<String, String> userIdToUsername = new HashMap<>();  // 用户 ID 到用户名的映射
    private static final Map<String, String> usernameToUserId = new HashMap<>();  // 用户名到用户 ID 的映射
    private static final Set<String> onlineUsers = new HashSet<>();

    @OnOpen
    public void onOpen(Session session, @PathParam("userId") String userId) throws IOException {
        userSessions.put(userId, session);
        onlineUsers.add(userId);
        String username = getUsernameFromDatabase(userId);  // 从数据库获取用户名
        userIdToUsername.put(userId, username);
        usernameToUserId.put(username, userId);  // 添加用户名到用户 ID 的映射
        broadcastOnlineUsers();
    }

    private String getUsernameFromDatabase(String userId) {
        // 实现数据库查询逻辑，使用 UserRepository 获取用户名
        UserRepository userRepository = new UserRepository();
        try {
            // 假设 userId 是用户的数据库 ID
            User user = userRepository.findById(Integer.parseInt(userId));
            if (user != null) {
                return user.getUsername();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "未知用户";  // 如果没有找到用户，返回一个默认值
    }

    @OnMessage
    public void onMessage(String message, Session session, @PathParam("userId") String userId) {
        try {
            if (message.startsWith("SEND:")) {
                String[] parts = message.split(":", 3);
                if (parts.length < 3) {
                    session.getBasicRemote().sendText("ERROR: Invalid message format");
                    return;
                }
                String targetUsername = parts[1];  // 改为使用用户名
                String msg = parts[2];
                String targetUserId = usernameToUserId.get(targetUsername);  // 根据用户名查找用户 ID

                if (targetUserId != null) {
                    Session targetSession = userSessions.get(targetUserId);
                    if (targetSession != null && targetSession.isOpen()) {
                        String senderUsername = userIdToUsername.getOrDefault(userId, userId);
                        targetSession.getBasicRemote().sendText(senderUsername + ": " + msg);
                    } else {
                        session.getBasicRemote().sendText("ERROR: Target user not found or not connected");
                    }
                } else {
                    session.getBasicRemote().sendText("ERROR: Invalid target username");
                }
            } else if ("GET_USERS".equals(message)) {
                broadcastOnlineUsers();
            } else if (message.startsWith("SYSTEM:")) {
                String systemMessage = message.substring(7);
                // 处理系统消息
                for (Session s : userSessions.values()) {
                    if (s.isOpen()) {
                        s.getBasicRemote().sendText("SYSTEM: " + systemMessage);
                    }
                }
            } else {
                // 处理未知消息类型
                session.getBasicRemote().sendText("ERROR: Unknown message type");
            }
        } catch (IOException e) {
            // 处理 I/O 异常
            try {
                session.getBasicRemote().sendText("ERROR: An error occurred while processing your message");
            } catch (IOException sendException) {
                // 处理发送错误
                sendException.printStackTrace();
            }
            e.printStackTrace();
        } catch (Exception e) {
            // 处理其他异常
            try {
                session.getBasicRemote().sendText("ERROR: An unexpected error occurred");
            } catch (IOException sendException) {
                // 处理发送错误
                sendException.printStackTrace();
            }
            e.printStackTrace();
        }
    }

    @OnClose
    public void onClose(Session session, @PathParam("userId") String userId) {
        userSessions.remove(userId);
        onlineUsers.remove(userId);
        String username = userIdToUsername.remove(userId);
        if (username != null) {
            usernameToUserId.remove(username);  // 移除用户名到用户 ID 的映射
        }
        try {
            broadcastOnlineUsers();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        // 记录错误日志
        System.err.println("Error in WebSocket session: " + throwable.getMessage());
        throwable.printStackTrace();
        // 可选：发送错误消息给客户端
        try {
            session.getBasicRemote().sendText("ERROR: " + throwable.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void broadcastOnlineUsers() throws IOException {
        // 更新为发送用户名列表而不是用户 ID 列表
        StringBuilder usersList = new StringBuilder();
        for (String userId : onlineUsers) {
            usersList.append(userIdToUsername.getOrDefault(userId, "未知用户")).append(",");
        }
        if (usersList.length() > 0) {
            usersList.setLength(usersList.length() - 1);  // 去掉最后一个逗号
        }
        for (Session session : userSessions.values()) {
            if (session.isOpen()) {
                session.getBasicRemote().sendText("ONLINE_USERS:" + usersList.toString());
            }
        }
    }
}
