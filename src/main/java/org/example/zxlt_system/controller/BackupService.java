package org.example.zxlt_system.controller;

import org.example.zxlt_system.util.DBConnection;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

// EJB 定时任务服务
@Singleton
@Startup
public class BackupService {
    private Connection connection;

    private static final JedisPool jedisPool = new JedisPool(new JedisPoolConfig(), "localhost", 6379); // 创建 Redis 连接池

    public BackupService() {
        this.connection = DBConnection.getConnection();
    }
    @Schedule(hour = "*", minute = "*", second = "10", persistent = false) // 每小时执行一次
    public void backupChatRecords() {
        try (Jedis jedis = jedisPool.getResource()) { // 从连接池获取 Jedis 实例
            List<String> records = getChatRecords(jedis);
            backupChatRecords(records);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private List<String> getChatRecords(Jedis jedis) {
        // 假设聊天记录存储在一个列表中
        return jedis.lrange("chat_records", 0, -1);
    }

    private void backupChatRecords(List<String> records) {
        String sql = "INSERT INTO chat_backup (message) VALUES (?)";

        try (
             PreparedStatement stmt = connection.prepareStatement(sql)) {

            for (String record : records) {
                stmt.setString(1, record);
                stmt.addBatch();
            }
            stmt.executeBatch();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
