package org.example.zxlt_system.controller;

import org.example.zxlt_system.dao.UserRepository;
import org.example.zxlt_system.model.User;
import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ServerEndpoint("/chat/{userId}")
public class ChatEndpoint {

    private static final Map<String, Session> userSessions = new HashMap<>();
    private static final JedisPool jedisPool = new JedisPool(new JedisPoolConfig(), "localhost", 6379);// 创建 Redis 连接池

    @OnOpen
    public void onOpen(Session session, @PathParam("userId") String userId) throws IOException {
        try (Jedis jedis = jedisPool.getResource()) {
            userSessions.put(userId, session);

            // 从数据库获取用户名
            String username = getUsernameFromDatabase(userId);
            if (username == null) {
                username = "未知用户"; // 防止用户名为空
            }

            // 更新 Redis 哈希表
            jedis.hset("usernameToUserId", username, userId);
            jedis.hset("userIdToUsername", userId, username);

            // 发布用户加入的消息
            jedis.publish("userChannel", "User " + username + " joined the chat");
            //System.out.println("User joined: " + username + " (" + userId + ")");

            // 将用户 ID 添加到 Redis 集合
            jedis.sadd("onlineUsers", userId);

            // 广播在线用户列表
            broadcastOnlineUsers();
        }
    }

    private String getUsernameFromDatabase(String userId) {
        UserRepository userRepository = new UserRepository();
        try {
            User user = userRepository.findById(Integer.parseInt(userId));
            if (user != null) {
                return user.getUsername();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "未知用户";
    }

    @OnMessage
    public void onMessage(String message, Session session, @PathParam("userId") String userId) {
        try (Jedis jedis = jedisPool.getResource()) {
            if (message.startsWith("SEND:")) {
                // 处理发送的消息
                String[] parts = message.split(":", 3);
                if (parts.length < 3) {
                    session.getBasicRemote().sendText("ERROR: Invalid message format");
                    return;
                }

                String targetUsername = parts[1];
                String msg = parts[2];

                // 从 Redis 获取当前用户名
                String currentUsername = jedis.hget("userIdToUsername", userId);
                if (currentUsername == null) {
                    currentUsername = "未知用户"; // 防止用户名为空
                }

                // 格式化消息内容
                String formattedMessage = currentUsername + ":" + msg;

                // 将消息存储到 Redis
                jedis.rpush("chatRecords", formattedMessage);

                // 广播消息给所有用户
                for (Session s : userSessions.values()) {
                    if (s.isOpen()) {
                        s.getBasicRemote().sendText(formattedMessage);
                    }
                }
            } else if ("GET_USERS".equals(message)) {
                broadcastOnlineUsers();
            } else if ("GET_HISTORY".equals(message)) {
                // 处理获取聊天记录的请求
                List<String> chatRecords = jedis.lrange("chatRecords", 0, -1);
                StringBuilder history = new StringBuilder("CHAT_HISTORY:");
                for (String record : chatRecords) {
                    history.append(record).append("\n");
                }
                session.getBasicRemote().sendText(history.toString());
            } else if (message.startsWith("SYSTEM:")) {
                String systemMessage = message.substring(7);
                jedis.publish("systemChannel", systemMessage);
            } else {
                session.getBasicRemote().sendText("ERROR: Unknown message type");
            }
        } catch (IOException e) {
            e.printStackTrace();
            sendErrorMessage(session, "An error occurred while processing your message");
        } catch (Exception e) {
            e.printStackTrace();
            sendErrorMessage(session, "An unexpected error occurred");
        }
    }





    private void sendErrorMessage(Session session, String errorMessage) {
        try {
            session.getBasicRemote().sendText("ERROR: " + errorMessage);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @OnClose
    public void onClose(Session session, @PathParam("userId") String userId) {
        try (Jedis jedis = jedisPool.getResource()) {
            userSessions.remove(userId);
            jedis.srem("onlineUsers", userId); // 确保用户 ID 从 onlineUsers 集合中删除
            jedis.hdel("userIdToUsername", userId);
            jedis.publish("userChannel", "User " + userId + " left the chat");
            broadcastOnlineUsers();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @OnError
    public void onError(Session session, Throwable throwable) {
        System.err.println("Error in WebSocket session: " + throwable.getMessage());
        throwable.printStackTrace();
        sendErrorMessage(session, "An error occurred");
    }

    private void broadcastOnlineUsers() throws IOException {
        try (Jedis jedis = jedisPool.getResource()) {
            StringBuilder usersList = new StringBuilder();

            // 打印当前在线用户集合
            //System.out.println("Online users in Redis: " + jedis.smembers("onlineUsers"));

            for (String userId : jedis.smembers("onlineUsers")) {
                String username = jedis.hget("userIdToUsername", userId);
                if (username != null) {
                    usersList.append(username).append(",");
                }
            }

            // 去掉最后的逗号
            if (usersList.length() > 0) {
                usersList.setLength(usersList.length() - 1);
            }

            // 打印广播的用户列表
            //System.out.println("Broadcasting online users: " + usersList.toString());

            // 发送给所有连接的用户
            for (Session session : userSessions.values()) {
                if (session.isOpen()) {
                    session.getBasicRemote().sendText("ONLINE_USERS:" + usersList.toString());
                }
            }
        }
    }

}
