create database zxlt;

use zxlt;

CREATE TABLE users (
                       id INT AUTO_INCREMENT PRIMARY KEY,
                       username VARCHAR(50) NOT NULL UNIQUE,
                       password VARCHAR(255) NOT NULL,
                       email VARCHAR(100) UNIQUE,
                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE `friends` (
                           `id` int NOT NULL AUTO_INCREMENT,
                           `user_id` int NOT NULL,
                           `friend_id` int NOT NULL,
                           `status` enum('pending','accepted','rejected') DEFAULT 'pending',
                           `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
                           `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                           PRIMARY KEY (`id`)
);

CREATE TABLE chat_messages (
                               message_id INT AUTO_INCREMENT PRIMARY KEY,
                               sender_id INT NOT NULL,
                               receiver_id INT,
                               message_content TEXT NOT NULL,
                               sent_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                               message_type VARCHAR(50)
);
