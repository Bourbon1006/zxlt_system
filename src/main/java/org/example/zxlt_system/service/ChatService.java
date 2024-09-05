package org.example.zxlt_system.service;

import org.example.zxlt_system.dao.ChatRecordDAO;

import java.sql.SQLException;

public class ChatService {

    private final ChatRecordDAO chatRecordDAO = new ChatRecordDAO();

    public void saveChatRecord(String message) {
        try {
            chatRecordDAO.saveChatRecord(message);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
