package org.example.zxlt_system.controller;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@ServerEndpoint("/chat/{userId}")
public class ChatEndpoint {

    private static final Map<String, Session> userSessions = new HashMap<>();
    private static final Map<String, String> userIdToUsername = new HashMap<>();  // 用户 ID 到用户名的映射
    private static final Set<String> onlineUsers = new HashSet<>();

    @OnOpen
    public void onOpen(Session session, @PathParam("userId") String userId) throws IOException {
        userSessions.put(userId, session);
        onlineUsers.add(userId);
        userIdToUsername.put(userId, "User" + userId);  // 示例：将用户 ID 映射到用户名
        broadcastOnlineUsers();
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
                String targetUserId = parts[1];
                String msg = parts[2];
                Session targetSession = userSessions.get(targetUserId);
                if (targetSession != null && targetSession.isOpen()) {
                    String senderUsername = userIdToUsername.getOrDefault(userId, userId);
                    targetSession.getBasicRemote().sendText(senderUsername + ": " + msg);
                } else {
                    session.getBasicRemote().sendText("ERROR: Target user not found or not connected");
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
        userIdToUsername.remove(userId);
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
        String usersList = String.join(",", onlineUsers);
        for (Session session : userSessions.values()) {
            if (session.isOpen()) {
                session.getBasicRemote().sendText("ONLINE_USERS:" + usersList);
            }
        }
    }
}
