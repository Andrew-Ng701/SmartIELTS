CREATE TABLE IF NOT EXISTS speaking_talk (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    talk_id VARCHAR(128) NOT NULL,
    user_id BIGINT NOT NULL,
    session_id VARCHAR(64) NOT NULL,
    question_id BIGINT NOT NULL,
    talk_status VARCHAR(64) NOT NULL,
    video_url TEXT NULL,
    error_message TEXT NULL,
    created_time DATETIME NOT NULL,
    updated_time DATETIME NOT NULL,
    UNIQUE KEY uk_speaking_talk_talk_id (talk_id),
    KEY idx_speaking_talk_user_id (user_id),
    KEY idx_speaking_talk_session_question (session_id, question_id)
);
