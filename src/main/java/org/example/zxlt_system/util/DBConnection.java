package org.example.zxlt_system.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    private static Connection connection;

    public static Connection getConnection() {
        String driver = "com.mysql.cj.jdbc.Driver";  // 使用 MySQL 8.x 驱动类名
        String url = "jdbc:mysql://localhost:3306/zxlt?useUnicode=true&characterEncoding=utf8";
        String username = "root";
        String password = "dingbao0609";

        if (connection == null) {
            try {
                // 加载 MySQL 驱动
                Class.forName(driver);
                // 获取数据库连接
                connection = DriverManager.getConnection(url, username, password);
                //System.out.println("Database connection established successfully.");
            } catch (ClassNotFoundException e) {
                System.err.println("MySQL JDBC Driver not found.");
                e.printStackTrace();
            } catch (SQLException e) {
                System.err.println("Connection failed.");
                e.printStackTrace();
            }
        }
        return connection;
    }
}
