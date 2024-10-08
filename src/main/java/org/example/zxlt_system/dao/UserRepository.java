package org.example.zxlt_system.dao;

import org.example.zxlt_system.model.User;
import org.example.zxlt_system.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserRepository {


    private Connection connection;

    public UserRepository() {
        this.connection = DBConnection.getConnection();
    }

    public boolean isUsernameExists(String username) throws SQLException {
        String query = "select * from users where username=?";
        try(PreparedStatement stmt = connection.prepareStatement(query)){
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return true;
            }
            return false;
        }
    }

    //判断邮箱是否已存在
    public boolean isEmailExists (String email) throws SQLException {
        String query = "select * from users where email = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return true;
            }
            return false;
        }
    }
    // 添加用户
    public void addUser(User user) throws SQLException {
        String query = "INSERT INTO users (username, password, email) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getPassword());
            stmt.setString(3, user.getEmail());
            stmt.executeUpdate();
        }
    }

    // 更新用户信息
    public boolean updateUser(User user) throws SQLException {
        String sql = "UPDATE users SET username = ?, password = ?, email = ? WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, user.getUsername());
            statement.setString(2, user.getPassword());
            statement.setString(3, user.getEmail());
            statement.setInt(4, user.getId());
            int rowsUpdated = statement.executeUpdate();
            return rowsUpdated > 0;
        }
    }

    // 删除用户
    public boolean deleteUser(String username) throws SQLException {
        String sql = "DELETE FROM users WHERE username = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username);
            int rowsDeleted = statement.executeUpdate();
            return rowsDeleted > 0;
        }
    }

    // 获取所有用户
    public List<User> getAllUsers() throws SQLException {
        List<User> users = new ArrayList<>();
        String query = "SELECT * FROM users";
        try (PreparedStatement stmt = connection.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                users.add(mapUserFromResultSet(rs));
            }
        }
        return users;
    }

    public boolean updatePassword(int userId, String newPassword) throws SQLException {
        String sql = "UPDATE users SET password = ? WHERE id = ?";
        try (
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, newPassword);
            statement.setInt(2, userId);
            int rowsUpdated = statement.executeUpdate();
            return rowsUpdated > 0;
        }
    }

    public boolean isAdmin(int userId) {
        String query = "SELECT role FROM users WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {  // 检查是否有查询结果
                    String role = rs.getString("role");  // 获取role字段的值
                    if ("admin".equals(role)) {  // 比较字符串内容
                        return true;  // 用户是管理员
                    } else {
                        return false;  // 用户不是管理员
                    }
                } else {
                    return false;  // 如果没有查询结果，用户不是管理员
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public User login(String username, String password) throws SQLException {
        String query = "SELECT * FROM users WHERE username = ? AND password = ?";
        User user = null;

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, username);
            stmt.setString(2, password);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    user = new User();
                    user.setId(rs.getInt("id"));
                    user.setUsername(rs.getString("username"));
                    user.setPassword(rs.getString("password"));
                    user.setEmail(rs.getString("email"));
                    user.setRole(rs.getString("role"));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database error occurred", e);
        }

        return user;
    }

    public User findByUsername(String a) throws SQLException {
        String query = "SELECT * FROM users WHERE username = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, a);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    User user = new User();
                    user.setId(rs.getInt("id"));
                    user.setUsername(rs.getString("username"));
                    user.setPassword(rs.getString("password"));
                    user.setEmail(rs.getString("email"));
                    user.setRole(rs.getString("role")); // 设置用户角色
                    return user;
                }
            }
        }
        return null;
    }

    public User findById(int userId) throws SQLException {
        // Example: Ensure this method fetches the role along with other user details
        String query = "SELECT * FROM users WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, userId);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                User user = new User();
                user.setId(resultSet.getInt("id"));
                user.setUsername(resultSet.getString("username"));
                user.setPassword(resultSet.getString("password"));
                user.setRole(resultSet.getString("role")); // Ensure role is being fetched
                return user;
            }
        }
        return null;
    }


    private User mapUserFromResultSet(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getInt("id"));
        user.setUsername(rs.getString("username"));
        user.setPassword(rs.getString("password"));
        user.setEmail(rs.getString("email"));
        return user;
    }

    public String isUsernameAndEmailValid(String username, String email) {
        String query = "SELECT role FROM users WHERE username = ? AND email = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, username);
            stmt.setString(2, email);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                // 返回用户的角色
                return rs.getString("role");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null; // 用户名和邮箱无效，返回 null
    }


    public boolean resetPassword(String username,  String email,String newPassword) {
        // 调用 isUsernameAndEmailValid 获取角色
        String role = isUsernameAndEmailValid(username, email);

        // 如果用户名和邮箱无效，返回 false
        if (role == null) {
            System.out.println("Invalid username or email.");
            return false;
        }

        // 如果角色不是 admin，拒绝重置密码
        if ("admin".equals(role)) {
            System.out.println("Only admin users can reset passwords.");
            return false;
        }

        // 如果是管理员，允许重置密码
        String updateQuery = "UPDATE users SET password = ? WHERE username = ? AND email = ?";
        try (PreparedStatement stmt = connection.prepareStatement(updateQuery)) {
            stmt.setString(1, newPassword);
            stmt.setString(2, username);
            stmt.setString(3, email);
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean update(User user) {
        String sql = "UPDATE users SET username = ?, email = ?, password = ?, role = ? WHERE id = ?";
        boolean a = false;
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getEmail());
            stmt.setString(3, user.getPassword()); // 确保密码是加密后的
            stmt.setString(4, user.getRole());
            stmt.setInt(5, user.getId());


            int rowsUpdated = stmt.executeUpdate();
            if (rowsUpdated != 0) {
                //System.out.println("No user found with ID: " + user.getId());
                a = true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Error updating user: " + e.getMessage());
        }
        return a;
    }


    // 关闭数据库连接
    public void close() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
