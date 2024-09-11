package org.example.zxlt_system.controller;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import javax.annotation.Resource;
import javax.ejb.Schedule;
import javax.ejb.Stateless;
import javax.sql.DataSource;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

@Stateless
public class RedisToMySQLBackup {

    @Resource(lookup = "java:/jdbc/myDataSource")
    private DataSource dataSource;

    private static final JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
    static {
        jedisPoolConfig.setMaxTotal(128);
        jedisPoolConfig.setMinIdle(16);
        jedisPoolConfig.setMaxIdle(64);
    }
    private static final JedisPool jedisPool = new JedisPool(jedisPoolConfig, "localhost", 6379);

    @Schedule(hour = "*", minute = "*/10", persistent = false)
    public void backupData() {
        System.out.println("Starting backup process...");

        try (Jedis jedis = jedisPool.getResource()) {
            List<String> chatRecords = jedis.lrange("chatRecords", 0, -1);
            System.out.println("Chat records retrieved from Redis: " + chatRecords.size());

            if (chatRecords.isEmpty()) {
                System.out.println("No chat records to backup.");
                return;
            }

            try (Connection conn = dataSource.getConnection()) {
                String sql = "INSERT INTO chat_backup (message) VALUES (?)";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    for (String record : chatRecords) {
                        stmt.setString(1, record);
                        stmt.addBatch();
                    }
                    int[] result = stmt.executeBatch();
                    System.out.println("Number of records inserted: " + result.length);
                    conn.commit();  // 显式提交事务
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

            // 清空 Redis 中的聊天记录
            jedis.del("chatRecords");
            System.out.println("Chat records cleared from Redis after backup.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
