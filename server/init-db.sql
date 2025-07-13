-- Database initialization script for local development
-- This script creates the necessary tables if they don't exist

-- Create users table
CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    username VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    first_name VARCHAR(255),
    last_name VARCHAR(255),
    profile_picture VARCHAR(500),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create chats table
CREATE TABLE IF NOT EXISTS chats (
    id BIGSERIAL PRIMARY KEY,
    user1_id BIGINT NOT NULL,
    user2_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user1_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (user2_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE(user1_id, user2_id)
);

-- Create messages table
CREATE TABLE IF NOT EXISTS messages (
    id BIGSERIAL PRIMARY KEY,
    chat_id BIGINT NOT NULL,
    sender_id BIGINT NOT NULL,
    content TEXT NOT NULL,
    message_type VARCHAR(50) DEFAULT 'text',
    file_url VARCHAR(500),
    is_read BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (chat_id) REFERENCES chats(id) ON DELETE CASCADE,
    FOREIGN KEY (sender_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Create contacts table
CREATE TABLE IF NOT EXISTS contacts (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    contact_id BIGINT NOT NULL,
    status VARCHAR(50) DEFAULT 'pending', -- pending, accepted, rejected, blocked
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (contact_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE(user_id, contact_id)
);

-- Create notifications table
CREATE TABLE IF NOT EXISTS notifications (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    type VARCHAR(100) NOT NULL, -- contact_request, message, system
    title VARCHAR(255) NOT NULL,
    content TEXT,
    is_read BOOLEAN DEFAULT FALSE,
    metadata JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Create indexes for better performance
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_users_username ON users(username);
CREATE INDEX IF NOT EXISTS idx_chats_users ON chats(user1_id, user2_id);
CREATE INDEX IF NOT EXISTS idx_messages_chat ON messages(chat_id);
CREATE INDEX IF NOT EXISTS idx_messages_sender ON messages(sender_id);
CREATE INDEX IF NOT EXISTS idx_messages_created ON messages(created_at);
CREATE INDEX IF NOT EXISTS idx_contacts_user ON contacts(user_id);
CREATE INDEX IF NOT EXISTS idx_notifications_user ON notifications(user_id);
CREATE INDEX IF NOT EXISTS idx_notifications_read ON notifications(is_read);

-- Insert some sample data for testing
INSERT INTO users (email, username, password, first_name, last_name) VALUES 
('john.doe@example.com', 'johndoe', '$2a$10$KIjYCwJpfWnUJ9fPdFRVcOBHkZyaZ1/nxRHgWGLiJzgNJy8nBQVLG', 'John', 'Doe'),
('jane.smith@example.com', 'janesmith', '$2a$10$KIjYCwJpfWnUJ9fPdFRVcOBHkZyaZ1/nxRHgWGLiJzgNJy8nBQVLG', 'Jane', 'Smith'),
('alice.johnson@example.com', 'alicejohnson', '$2a$10$KIjYCwJpfWnUJ9fPdFRVcOBHkZyaZ1/nxRHgWGLiJzgNJy8nBQVLG', 'Alice', 'Johnson')
ON CONFLICT (email) DO NOTHING;

-- Create a sample chat
INSERT INTO chats (user1_id, user2_id) VALUES 
(1, 2),
(1, 3)
ON CONFLICT (user1_id, user2_id) DO NOTHING;

-- Create sample messages
INSERT INTO messages (chat_id, sender_id, content, message_type) VALUES 
(1, 1, 'Hello Jane! How are you?', 'text'),
(1, 2, 'Hi John! I''m doing great, thanks for asking!', 'text'),
(2, 1, 'Hey Alice! Welcome to Chatty!', 'text'),
(2, 3, 'Thanks John! This app looks amazing!', 'text')
ON CONFLICT DO NOTHING;

-- Create sample contact relationships
INSERT INTO contacts (user_id, contact_id, status) VALUES 
(1, 2, 'accepted'),
(2, 1, 'accepted'),
(1, 3, 'accepted'),
(3, 1, 'accepted')
ON CONFLICT (user_id, contact_id) DO NOTHING;

-- Create sample notifications
INSERT INTO notifications (user_id, type, title, content) VALUES 
(1, 'system', 'Welcome to Chatty!', 'Welcome to Chatty Microservices! Start chatting with your friends.'),
(2, 'system', 'Welcome to Chatty!', 'Welcome to Chatty Microservices! Start chatting with your friends.'),
(3, 'system', 'Welcome to Chatty!', 'Welcome to Chatty Microservices! Start chatting with your friends.')
ON CONFLICT DO NOTHING;
