package org.example.zxlt_system.controller;

import org.example.zxlt_system.dao.FriendRepository;
import org.example.zxlt_system.dao.UserRepository;
import org.example.zxlt_system.model.User;
import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import org.example.zxlt_system.service.UserService;
import org.example.zxlt_system.service.UserServiceImpl;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ServerEndpoint("/chat/{userId}")
public class ChatEndpoint {
    private UserService userService = new UserServiceImpl();
    private static final Map<String, Session> userSessions = new HashMap<>();
    private static final JedisPool jedisPool = new JedisPool(new JedisPoolConfig(), "localhost", 6379); // 创建 Redis 连接池
    private final FriendRepository friendRepository = new FriendRepository();
    private final UserRepository userRepository = new UserRepository();

    @OnOpen
    public void onOpen(Session session, @PathParam("userId") String userId) throws IOException {
        userSessions.put(userId, session);  // 先将用户加入会话列表

        // 直接从数据库获取用户名
        String username = getUsernameFromDatabase(userId);
        if (username == null) {
            username = "未知用户";  // 防止用户名为空
        }

        try (Jedis jedis = jedisPool.getResource()) {
            // 更新 Redis 哈希表
            jedis.hset("usernameToUserId", username, userId);
            jedis.hset("userIdToUsername", userId, username);

            // 将用户 ID 添加到 Redis 集合
            jedis.sadd("onlineUsers", userId);

            // 发布用户加入的消息
            jedis.publish("userChannel", "User " + username + " joined the chat");

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
            if (message.startsWith("ADD_USER:")) {
                handleAddUser(message, userId, session);
            }
            else if (message.startsWith("SEND:")) {
                handleChatMessage(message, session, userId, jedis);
            } else if (message.startsWith("ADD_FRIEND:") || message.startsWith("REMOVE_FRIEND:") ||
                    message.startsWith("ACCEPT_FRIEND:") || message.startsWith("REJECT_FRIEND:")) {
                handleFriendMessage(message, userId, session);
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
            } else if (message.startsWith("GET_FRIENDS")) {
                List<String> friendsList = Collections.singletonList(FriendRepository.getFriendsList(Integer.parseInt(userId)));
                String friendsString = String.join(",", friendsList);
                session.getBasicRemote().sendText("FRIENDS_LIST:" + friendsString);
            } else if (message.startsWith("CHANGE_PASSWORD:")) {
                handleChangePassword(message, userId, session);
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

    private void handleAddUser(String message, String userId, Session session) {
        // 处理 ADD_USER:username:password:email 格式的消息
        String[] parts = message.split(":", 4);
        if (parts.length != 4) {
            sendErrorMessage(session, "Invalid format for add user request.");
            return;
        }

        String username = parts[1];
        String password = parts[2];
        String email = parts[3];

        try {
            User newUser = new User();
            newUser.setUsername(username);
            newUser.setPassword(password);
            newUser.setEmail(email);
            userRepository.addUser(newUser);

            // 发送成功消息
            session.getBasicRemote().sendText("USER_ADDED: User " + username + " added successfully.");
        } catch (SQLException | IOException e) {
            e.printStackTrace();
            sendErrorMessage(session, "An error occurred while adding the user. Details: " + e.getMessage());
        }
    }


    private void handleChangePassword(String message, String userId, Session session) {
        String[] parts = message.split(":");
        if (parts.length != 3) {
            sendErrorMessage(session, "Invalid format for change password request.");
            return;
        }

        String oldPassword = parts[1];
        String newPassword = parts[2];

        try {
            // 从数据库获取用户信息
            UserRepository userRepository = new UserRepository();
            User user = userRepository.findById(Integer.parseInt(userId));

            if (user == null) {
                sendErrorMessage(session, "User not found.");
                return;
            }

            // 检查旧密码是否匹配
            if (!user.getPassword().equals(oldPassword)) {
                sendErrorMessage(session, "Old password is incorrect.");
                return;
            }

            // 更新密码
            boolean updated = userRepository.updatePassword(Integer.parseInt(userId), newPassword);
            if (updated) {
                session.getBasicRemote().sendText("PASSWORD_CHANGE_SUCCESS: Your password has been updated.");
            } else {
                sendErrorMessage(session, "Failed to update the password. Please try again later.");
            }
        } catch (SQLException | IOException e) {
            e.printStackTrace();
            sendErrorMessage(session, "An error occurred while changing the password. Details: " + e.getMessage());
        }
    }

    private void handleChatMessage(String message, Session session, String userId, Jedis jedis) throws IOException, SQLException {
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

        // 禁止用户向自己发送消息
        if (currentUsername.equals(targetUsername)) {
            session.getBasicRemote().sendText("ERROR: You cannot send a message to yourself.");
            return;
        }

        // 获取接收者的 userId
        int targetUserId = friendRepository.getUserIdByUsername(targetUsername);
        if (targetUserId == -1) {
            session.getBasicRemote().sendText("ERROR: User not found.");
            return;
        }

        // 检查两者是否为好友
        int currentUserId = Integer.parseInt(userId);
        if(!userRepository.isAdmin(currentUserId) && !userRepository.isAdmin(targetUserId))
        {
            session.getBasicRemote().sendText("ERROR: You can only send messages to your friends.");
            return;
        }


        // 格式化消息内容
        String formattedMessage = currentUsername + ":" + msg;

        // 将消息存储到 Redis
        jedis.rpush("chatRecords", formattedMessage);

        // 发送消息给目标用户
        Session targetSession = userSessions.get(String.valueOf(targetUserId));
        if (targetSession != null && targetSession.isOpen()) {
            targetSession.getBasicRemote().sendText(formattedMessage);
        } else {
            session.getBasicRemote().sendText("ERROR: Target user is not online.");
        }

        // 也把消息发送给自己，确认消息发送成功
        //session.getBasicRemote().sendText("我发送给" + targetUsername + ": " + msg);
    }


    private void handleFriendMessage(String message, String userId, Session session) {
        try {
            int userIdInt = Integer.parseInt(userId);
            String[] parts = message.split(":", 2);
            String action = parts[0];
            String friendUsername = parts.length > 1 ? parts[1] : "";
            int friendId = friendRepository.getUserIdByUsername(friendUsername);

            if (friendId == -1) {
                session.getBasicRemote().sendText("ERROR: User not found.");
                return;
            }

            String responseMessage = "";
            boolean success = false;

            switch (action) {
                case "ADD_FRIEND":
                    success = friendRepository.addFriend(userIdInt, friendId);
                    responseMessage = success ? friendUsername + " 好友请求已发送" : "ERROR: Operation failed.";
                    if (success) {
                        // Notify friend
                        Session friendSession = userSessions.get(String.valueOf(friendId));
                        if (friendSession != null && friendSession.isOpen()) {
                            friendSession.getBasicRemote().sendText("FRIEND_REQUEST:" + getUsernameFromDatabase(userId));
                        }
                    }
                    break;
                case "REMOVE_FRIEND":
                    success = friendRepository.removeFriend(userIdInt, friendId);
                    responseMessage = success ? "Friend removed." : "ERROR: Operation failed.";
                    break;
                case "ACCEPT_FRIEND":
                    success = friendRepository.acceptFriendRequest(friendId, userIdInt);
                    responseMessage = success ? "Friend request accepted." : "ERROR: Operation failed.";
                    if (success) {
                        // Notify requester
                        Session requesterSession = userSessions.get(String.valueOf(friendId));
                        if (requesterSession != null && requesterSession.isOpen()) {
                            requesterSession.getBasicRemote().sendText(getUsernameFromDatabase(userId) + " 好友请求已通过");
                        }
                    }
                    break;
                case "REJECT_FRIEND":
                    success = friendRepository.updateFriendRequestStatus(friendId, userIdInt, "rejected");
                    responseMessage = success ? "Friend request rejected." : "ERROR: Operation failed.";
                    break;
                case "GET_FRIENDS":
                    String friends = FriendRepository.getFriendsList(userIdInt); // 获取好友列表
                    session.getBasicRemote().sendText("FRIENDS_LIST:" + friends);
                    return; // 已经处理完响应，不需要再发送默认消息
            }

            // 发送响应消息
            session.getBasicRemote().sendText(responseMessage);

        } catch (SQLException | IOException e) {
            e.printStackTrace();
            try {
                if (session.isOpen()) {
                    session.getBasicRemote().sendText("ERROR: An error occurred while processing your request. Details: " + e.getMessage());
                }
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }


    private void notifyFriendRequest(String friendUsername, int userId) throws IOException {
        String userIdString = String.valueOf(userId);
        Session friendSession = userSessions.get(userIdString);
        if (friendSession != null && friendSession.isOpen()) {
            String requesterUsername = getUsernameFromDatabase(userIdString);
            if (requesterUsername == null) {
                requesterUsername = "未知用户";
            }
            friendSession.getBasicRemote().sendText("FRIEND_REQUEST:" + requesterUsername);
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
        try {
            if (session.isOpen()) {
                session.getBasicRemote().sendText("ERROR: An unexpected error occurred.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void broadcastOnlineUsers() throws IOException {
        try (Jedis jedis = jedisPool.getResource()) {
            StringBuilder usersList = new StringBuilder();

            for (String userId : jedis.smembers("onlineUsers")) {
                String username = jedis.hget("userIdToUsername", userId);
                if (username != null) {
                    usersList.append(username).append(",");
                }
            }

            if (usersList.length() > 0) {
                usersList.setLength(usersList.length() - 1);
            }

            for (Session session : userSessions.values()) {
                if (session.isOpen()) {
                    session.getBasicRemote().sendText("ONLINE_USERS:" + usersList.toString());
                }
            }
        }
    }
}
