-- SmartIELTS local/dev realistic seed data.
-- Re-runnable. Intended for local manual testing only.
-- Keeps sys_user id=1 and id=2 passwords/token_version intact.

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

SET @student_password = (SELECT password FROM sys_user WHERE id = 2 LIMIT 1);
SET @admin_password = (SELECT password FROM sys_user WHERE id = 1 LIMIT 1);

START TRANSACTION;

SET FOREIGN_KEY_CHECKS = 0;

DELETE FROM speaking_talk;
DELETE FROM speaking_record;
DELETE FROM speaking_session;
DELETE FROM speaking_question;
DELETE FROM writing_record_attachment;
DELETE FROM writing_record;
DELETE FROM writing_question;
DELETE FROM listening_answer_record;
DELETE FROM listening_record;
DELETE FROM listening_audio;
DELETE FROM listening_question;
DELETE FROM listening_part_group;
DELETE FROM listening_test;
DELETE FROM reading_answer_record;
DELETE FROM reading_question_answer_rule;
DELETE FROM reading_record;
DELETE FROM reading_question;
DELETE FROM reading_passage;
DELETE FROM reading_part_group;
DELETE FROM reading_test;
DELETE FROM biz_image_resource;
DELETE FROM sys_user WHERE id NOT IN (1, 2);

SET FOREIGN_KEY_CHECKS = 1;

UPDATE sys_user
SET email = 'admin01@smartielts.com',
    role = 'ADMIN',
    is_deleted = 0,
    deleted_time = NULL,
    username = 'Admin Operator',
    profile_picture_url = NULL,
    profile_picture_object_key = NULL,
    ielts_target_scores = NULL
WHERE id = 1;

UPDATE sys_user
SET email = 'tt6k@foxmail.com',
    role = 'USER',
    is_deleted = 0,
    deleted_time = NULL,
    username = 'Alex Chen',
    profile_picture_url = 'https://static.smartielts.local/mock/users/alex-chen.png',
    profile_picture_object_key = 'dev/user-profile-picture/alex-chen.png',
    ielts_target_scores = '7,7,7,7'
WHERE id = 2;

INSERT INTO sys_user (id, email, username, password, role, is_deleted, deleted_time, created_time, last_login_time, token_version, profile_picture_url, profile_picture_object_key, ielts_target_scores)
VALUES
    (101, 'maya.li@example.com', 'Maya Li', @student_password, 'USER', 0, NULL, '2026-02-18 09:30:00', '2026-05-16 20:12:00', 0, 'https://static.smartielts.local/mock/users/maya-li.png', 'dev/user-profile-picture/maya-li.png', '7.5,7,7,7'),
    (102, 'ethan.wong@example.com', 'Ethan Wong', @student_password, 'USER', 0, NULL, '2026-02-25 11:10:00', '2026-05-15 18:22:00', 1, NULL, NULL, '6.5,6.5,6,6.5'),
    (103, 'sophia.ng@example.com', 'Sophia Ng', @student_password, 'USER', 0, NULL, '2026-03-04 14:05:00', '2026-05-14 21:05:00', 0, NULL, NULL, '8,7.5,7.5,7.5'),
    (104, 'ryan.chan@example.com', 'Ryan Chan', @student_password, 'USER', 0, NULL, '2026-03-12 10:45:00', '2026-05-12 08:40:00', 0, NULL, NULL, '6,6,6,6'),
    (105, 'nora.lam@example.com', 'Nora Lam', @student_password, 'USER', 0, NULL, '2026-03-21 16:20:00', '2026-05-11 19:30:00', 0, NULL, NULL, '7,7.5,7,7'),
    (106, 'leo.tang@example.com', 'Leo Tang', @student_password, 'USER', 0, NULL, '2026-04-02 13:15:00', '2026-05-09 22:18:00', 2, NULL, NULL, '5.5,6,5.5,6'),
    (107, 'ava.ho@example.com', 'Ava Ho', @student_password, 'USER', 0, NULL, '2026-04-10 09:55:00', '2026-05-08 12:06:00', 0, NULL, NULL, '7.5,7.5,8,7'),
    (108, 'deleted.student@example.com', 'Deleted Student', @student_password, 'USER', 1, '2026-05-10 10:00:00', '2026-04-18 10:00:00', NULL, 0, NULL, NULL, '6,6,6,6');

INSERT INTO reading_test (id, title, total_score, created_time, updated_time, is_deleted, timer_mode, prep_seconds, total_seconds, auto_submit, allow_pause)
VALUES
    (1001, 'DEV SEED Reading Test 01 - Urban Futures', 12, '2026-03-01 09:00:00', '2026-05-01 09:00:00', 0, 'TEST_LEVEL', 0, 3600, 1, 0),
    (1002, 'DEV SEED Reading Test 02 - Science and Society', 12, '2026-03-08 09:00:00', '2026-05-02 09:00:00', 0, 'TEST_LEVEL', 0, 3600, 1, 1),
    (1003, 'DEV SEED Reading Archived Mock', 12, '2026-02-01 09:00:00', '2026-05-03 09:00:00', 1, 'TEST_LEVEL', 0, 3600, 1, 0);

INSERT INTO reading_part_group (id, test_id, part_number, group_number, title, instruction_text, group_guide_text, group_requirement_text, question_type, answer_mode, options_json, accepted_answers_json, answer_rules_json, case_insensitive, ignore_whitespace, ignore_punctuation, question_no_start, question_no_end, display_order, time_limit_seconds, is_deleted)
VALUES
    (1101, 1001, 1, 1, 'Passage 1 - Compact Cities', 'Read the passage about compact urban design.', 'Complete the summary below.', 'Choose ONE WORD ONLY from the passage for each answer.', 'SUMMARY_COMPLETION', 'TEXT', NULL, NULL, NULL, 1, 1, 0, 1, 4, 1, NULL, 0),
    (1102, 1001, 2, 1, 'Passage 2 - Public Transport', 'Read the passage about transport planning.', 'Choose the correct letter.', 'Choose A, B, C or D.', 'MULTIPLE_CHOICE_SINGLE', 'SINGLE', '["A","B","C","D"]', NULL, NULL, 1, 1, 0, 5, 8, 2, NULL, 0),
    (1103, 1001, 3, 1, 'Passage 3 - Green Roofs', 'Read the passage about green roof research.', 'Answer the questions below.', 'Write NO MORE THAN TWO WORDS.', 'SHORT_ANSWER', 'TEXT', NULL, NULL, NULL, 1, 1, 0, 9, 12, 3, NULL, 0),
    (1111, 1002, 1, 1, 'Passage 1 - Memory Research', 'Read the passage about memory studies.', 'Complete the notes below.', 'Choose ONE WORD ONLY.', 'NOTE_COMPLETION', 'TEXT', NULL, NULL, NULL, 1, 1, 0, 1, 4, 1, NULL, 0),
    (1112, 1002, 2, 1, 'Passage 2 - Ocean Mapping', 'Read the passage about ocean mapping.', 'Match each statement with the correct researcher.', 'Choose the correct name.', 'MATCHING', 'TEXT', NULL, NULL, NULL, 1, 1, 0, 5, 8, 2, NULL, 0),
    (1113, 1002, 3, 1, 'Passage 3 - Work and Automation', 'Read the passage about automation.', 'Choose the correct answer.', 'Choose A, B, C or D.', 'MULTIPLE_CHOICE_SINGLE', 'SINGLE', '["A","B","C","D"]', NULL, NULL, 1, 1, 0, 9, 12, 3, NULL, 0);

INSERT INTO reading_passage (id, test_id, part_group_id, passage_no, title, content, material_type, display_order, is_deleted)
VALUES
    (1201, 1001, 1101, 1, 'Compact Cities', 'Urban planners increasingly argue that compact neighbourhoods can reduce travel time, support local shops and preserve open land. The most successful projects combine housing, services and public space within a walkable distance.', 'PASSAGE', 1, 0),
    (1202, 1001, 1102, 2, 'Public Transport That People Trust', 'Reliable public transport depends on frequency, clear information and safe interchanges. Surveys show that passengers value predictable waiting times more than high maximum speed.', 'PASSAGE', 2, 0),
    (1203, 1001, 1103, 3, 'The Quiet Benefits of Green Roofs', 'Green roofs can lower building temperatures, slow storm-water runoff and create small habitats for insects. Long-term monitoring shows that maintenance routines are crucial for plant survival.', 'PASSAGE', 3, 0),
    (1211, 1002, 1111, 1, 'Memory Research in Daily Life', 'Memory is shaped by attention, sleep and emotional context. Researchers now test learning techniques in classrooms and workplaces rather than relying only on laboratory tasks.', 'PASSAGE', 1, 0),
    (1212, 1002, 1112, 2, 'Mapping the Ocean Floor', 'Modern ocean mapping uses sonar, satellites and autonomous vehicles. Each method reveals different levels of detail and must be combined to build a reliable picture.', 'PASSAGE', 2, 0),
    (1213, 1002, 1113, 3, 'Automation and Human Work', 'Automation changes tasks rather than simply removing jobs. Organisations that redesign training early often gain more from new tools than those that focus only on cost reduction.', 'PASSAGE', 3, 0);

INSERT INTO reading_question (id, passage_id, part_group_id, question_number, question_type, answer_mode, question_text, correct_answer, options_json, accepted_answers_json, group_label, case_insensitive, ignore_whitespace, ignore_punctuation, display_order, score, is_deleted)
VALUES
    (1301, 1201, 1101, 1, 'SUMMARY_COMPLETION', 'TEXT', 'Compact neighbourhoods can reduce travel _____.', 'time', NULL, '["time"]', 'Compact Cities', 1, 1, 0, 1, 1, 0),
    (1302, 1201, 1101, 2, 'SUMMARY_COMPLETION', 'TEXT', 'They support local _____.', 'shops', NULL, '["shops"]', 'Compact Cities', 1, 1, 0, 2, 1, 0),
    (1303, 1201, 1101, 3, 'SUMMARY_COMPLETION', 'TEXT', 'Successful projects include public _____.', 'space', NULL, '["space"]', 'Compact Cities', 1, 1, 0, 3, 1, 0),
    (1304, 1201, 1101, 4, 'SUMMARY_COMPLETION', 'TEXT', 'Services should be within a _____ distance.', 'walkable', NULL, '["walkable"]', 'Compact Cities', 1, 1, 0, 4, 1, 0),
    (1305, 1202, 1102, 5, 'MULTIPLE_CHOICE_SINGLE', 'SINGLE', 'What do passengers value most?', 'B', '["A. Maximum speed","B. Predictable waiting times","C. Large stations","D. Low fares only"]', '["B"]', 'Public Transport', 1, 1, 0, 5, 1, 0),
    (1306, 1202, 1102, 6, 'MULTIPLE_CHOICE_SINGLE', 'SINGLE', 'Which factor supports trust?', 'A', '["A. Clear information","B. Fewer routes","C. Higher prices","D. Longer transfers"]', '["A"]', 'Public Transport', 1, 1, 0, 6, 1, 0),
    (1307, 1202, 1102, 7, 'MULTIPLE_CHOICE_SINGLE', 'SINGLE', 'Safe interchanges are described as _____.', 'C', '["A. Optional","B. Expensive","C. Important","D. Temporary"]', '["C"]', 'Public Transport', 1, 1, 0, 7, 1, 0),
    (1308, 1202, 1102, 8, 'MULTIPLE_CHOICE_SINGLE', 'SINGLE', 'Frequency mainly affects _____.', 'D', '["A. ticket colour","B. route names","C. advertising","D. waiting time"]', '["D"]', 'Public Transport', 1, 1, 0, 8, 1, 0),
    (1309, 1203, 1103, 9, 'SHORT_ANSWER', 'TEXT', 'What can green roofs lower?', 'building temperatures', NULL, '["building temperatures","temperatures"]', 'Green Roofs', 1, 1, 0, 9, 1, 0),
    (1310, 1203, 1103, 10, 'SHORT_ANSWER', 'TEXT', 'What do green roofs slow?', 'storm-water runoff', NULL, '["storm-water runoff","storm water runoff","runoff"]', 'Green Roofs', 1, 1, 1, 10, 1, 0),
    (1311, 1203, 1103, 11, 'SHORT_ANSWER', 'TEXT', 'What do they create for insects?', 'habitats', NULL, '["habitats","small habitats"]', 'Green Roofs', 1, 1, 0, 11, 1, 0),
    (1312, 1203, 1103, 12, 'SHORT_ANSWER', 'TEXT', 'What is crucial for plant survival?', 'maintenance routines', NULL, '["maintenance routines","maintenance"]', 'Green Roofs', 1, 1, 0, 12, 1, 0),
    (1321, 1211, 1111, 1, 'NOTE_COMPLETION', 'TEXT', 'Memory is shaped by _____.', 'attention', NULL, '["attention"]', 'Memory', 1, 1, 0, 1, 1, 0),
    (1322, 1211, 1111, 2, 'NOTE_COMPLETION', 'TEXT', 'Sleep affects _____.', 'memory', NULL, '["memory"]', 'Memory', 1, 1, 0, 2, 1, 0),
    (1323, 1211, 1111, 3, 'NOTE_COMPLETION', 'TEXT', 'Researchers test techniques in _____.', 'classrooms', NULL, '["classrooms","workplaces"]', 'Memory', 1, 1, 0, 3, 1, 0),
    (1324, 1211, 1111, 4, 'NOTE_COMPLETION', 'TEXT', 'Laboratory tasks are no longer the only _____.', 'method', NULL, '["method","tasks"]', 'Memory', 1, 1, 0, 4, 1, 0),
    (1325, 1212, 1112, 5, 'MATCHING', 'TEXT', 'Which technology uses sound waves?', 'sonar', NULL, '["sonar"]', 'Ocean Mapping', 1, 1, 0, 5, 1, 0),
    (1326, 1212, 1112, 6, 'MATCHING', 'TEXT', 'Which technology works from space?', 'satellites', NULL, '["satellites","satellite"]', 'Ocean Mapping', 1, 1, 0, 6, 1, 0),
    (1327, 1212, 1112, 7, 'MATCHING', 'TEXT', 'Which vehicles can travel without crew?', 'autonomous vehicles', NULL, '["autonomous vehicles","vehicles"]', 'Ocean Mapping', 1, 1, 0, 7, 1, 0),
    (1328, 1212, 1112, 8, 'MATCHING', 'TEXT', 'Methods must be combined for a reliable _____.', 'picture', NULL, '["picture"]', 'Ocean Mapping', 1, 1, 0, 8, 1, 0),
    (1329, 1213, 1113, 9, 'MULTIPLE_CHOICE_SINGLE', 'SINGLE', 'Automation mainly changes _____.', 'A', '["A. tasks","B. weather","C. office colours","D. holidays"]', '["A"]', 'Automation', 1, 1, 0, 9, 1, 0),
    (1330, 1213, 1113, 10, 'MULTIPLE_CHOICE_SINGLE', 'SINGLE', 'What should organisations redesign early?', 'B', '["A. car parks","B. training","C. logos","D. lunch menus"]', '["B"]', 'Automation', 1, 1, 0, 10, 1, 0),
    (1331, 1213, 1113, 11, 'MULTIPLE_CHOICE_SINGLE', 'SINGLE', 'The passage warns against focusing only on _____.', 'C', '["A. quality","B. learning","C. cost reduction","D. safety"]', '["C"]', 'Automation', 1, 1, 0, 11, 1, 0),
    (1332, 1213, 1113, 12, 'MULTIPLE_CHOICE_SINGLE', 'SINGLE', 'New tools produce more benefit with _____.', 'D', '["A. fewer users","B. secrecy","C. delays","D. preparation"]', '["D"]', 'Automation', 1, 1, 0, 12, 1, 0);

INSERT INTO reading_question_answer_rule (question_id, blank_no, answer_group_no, answer_text, normalized_answer, is_primary, display_order)
SELECT id, 1, 1, correct_answer, LOWER(correct_answer), 1, display_order
FROM reading_question
WHERE answer_mode = 'TEXT';

INSERT INTO reading_record (id, user_id, test_id, session_id, total_score, started_time, submitted_time, time_limit_seconds, time_spent_seconds, record_status, created_time, is_deleted)
VALUES
    (1401, 2, 1001, 'DEV-RD-ALEX-001', 9, '2026-05-04 19:00:00', '2026-05-04 19:48:00', 3600, 2880, 'SUBMITTED', '2026-05-04 19:00:00', 0),
    (1402, 2, 1002, 'DEV-RD-ALEX-002', 7, '2026-05-10 20:00:00', '2026-05-10 20:52:00', 3600, 3120, 'SUBMITTED', '2026-05-10 20:00:00', 0),
    (1403, 2, 1001, 'DEV-RD-ALEX-INPROGRESS', NULL, '2026-05-17 09:15:00', NULL, 3600, 900, 'IN_PROGRESS', '2026-05-17 09:15:00', 0),
    (1404, 2, 1002, 'DEV-RD-ALEX-PAUSED', NULL, '2026-05-17 10:05:00', NULL, 3600, 1260, 'PAUSED', '2026-05-17 10:05:00', 0),
    (1405, 2, 1001, 'DEV-RD-ALEX-DELETED', 5, '2026-04-20 18:30:00', '2026-04-20 19:20:00', 3600, 3000, 'SUBMITTED', '2026-04-20 18:30:00', 1),
    (1411, 101, 1001, 'DEV-RD-101-001', 10, '2026-05-15 18:00:00', '2026-05-15 18:50:00', 3600, 3000, 'SUBMITTED', '2026-05-15 18:00:00', 0),
    (1412, 102, 1002, 'DEV-RD-102-001', 6, '2026-05-13 20:00:00', '2026-05-13 20:54:00', 3600, 3240, 'SUBMITTED', '2026-05-13 20:00:00', 0),
    (1413, 103, 1002, 'DEV-RD-103-DELETED', 8, '2026-05-01 19:30:00', '2026-05-01 20:18:00', 3600, 2880, 'SUBMITTED', '2026-05-01 19:30:00', 1);

INSERT INTO reading_answer_record (record_id, question_id, part_group_id, user_answer, normalized_answer, raw_answers_json, is_correct, score)
VALUES
    (1401, 1301, 1101, 'time', 'time', '["time"]', 1, 1),
    (1401, 1302, 1101, 'shops', 'shops', '["shops"]', 1, 1),
    (1401, 1303, 1101, 'parks', 'parks', '["parks"]', 0, 0),
    (1401, 1304, 1101, 'walkable', 'walkable', '["walkable"]', 1, 1),
    (1401, 1305, 1102, 'B', 'b', '["B"]', 1, 1),
    (1401, 1306, 1102, 'A', 'a', '["A"]', 1, 1),
    (1401, 1307, 1102, 'C', 'c', '["C"]', 1, 1),
    (1401, 1308, 1102, 'A', 'a', '["A"]', 0, 0),
    (1401, 1309, 1103, 'temperatures', 'temperatures', '["temperatures"]', 1, 1),
    (1401, 1310, 1103, 'storm water runoff', 'storm water runoff', '["storm water runoff"]', 1, 1),
    (1401, 1311, 1103, '', '', '[""]', 0, 0),
    (1401, 1312, 1103, 'maintenance routines', 'maintenance routines', '["maintenance routines"]', 1, 1),
    (1402, 1321, 1111, 'attention', 'attention', '["attention"]', 1, 1),
    (1402, 1322, 1111, 'sleep', 'sleep', '["sleep"]', 0, 0),
    (1402, 1323, 1111, 'classrooms', 'classrooms', '["classrooms"]', 1, 1),
    (1402, 1324, 1111, '', '', '[""]', 0, 0),
    (1402, 1325, 1112, 'sonar', 'sonar', '["sonar"]', 1, 1),
    (1402, 1326, 1112, 'satellites', 'satellites', '["satellites"]', 1, 1),
    (1402, 1327, 1112, 'autonomous vehicles', 'autonomous vehicles', '["autonomous vehicles"]', 1, 1),
    (1402, 1328, 1112, 'picture', 'picture', '["picture"]', 1, 1),
    (1405, 1301, 1101, 'time', 'time', '["time"]', 1, 1),
    (1405, 1302, 1101, 'stores', 'stores', '["stores"]', 0, 0),
    (1411, 1301, 1101, 'time', 'time', '["time"]', 1, 1),
    (1411, 1302, 1101, 'shops', 'shops', '["shops"]', 1, 1),
    (1412, 1321, 1111, 'attention', 'attention', '["attention"]', 1, 1);

INSERT INTO listening_test (id, title, total_score, created_time, updated_time, is_deleted, timer_mode, prep_seconds, total_seconds, auto_submit, allow_pause, allow_audio_seek)
VALUES
    (2001, 'DEV SEED Listening Test 01 - Campus Services', 8, '2026-03-02 09:00:00', '2026-05-01 10:00:00', 0, 'TEST_LEVEL', 30, 2400, 1, 0, 0),
    (2002, 'DEV SEED Listening Test 02 - Community Projects', 8, '2026-03-09 09:00:00', '2026-05-02 10:00:00', 0, 'TEST_LEVEL', 60, 2400, 1, 1, 1);

INSERT INTO listening_part_group (id, test_id, part_number, group_number, title, instruction_text, group_guide_text, group_requirement_text, question_type, answer_mode, options_json, accepted_answers_json, answer_rules_json, case_insensitive, ignore_whitespace, ignore_punctuation, question_no_start, question_no_end, display_order, time_limit_seconds, is_deleted)
VALUES
    (2101, 2001, 1, 1, 'Section 1 - Library Membership', 'Listen to a conversation at a library desk.', 'Complete the form.', 'Write ONE WORD AND/OR A NUMBER.', 'FORM_COMPLETION', 'TEXT', NULL, NULL, NULL, 1, 1, 0, 1, 4, 1, NULL, 0),
    (2102, 2001, 2, 1, 'Section 2 - Campus Map', 'Listen to an orientation talk.', 'Choose the correct letter.', 'Choose A, B or C.', 'MULTIPLE_CHOICE_SINGLE', 'SINGLE', '["A","B","C"]', NULL, NULL, 1, 1, 0, 5, 8, 2, NULL, 0),
    (2111, 2002, 1, 1, 'Section 1 - Volunteer Sign-up', 'Listen to a phone call about volunteering.', 'Complete the notes.', 'Write ONE WORD ONLY.', 'NOTE_COMPLETION', 'TEXT', NULL, NULL, NULL, 1, 1, 0, 1, 4, 1, NULL, 0),
    (2112, 2002, 2, 1, 'Section 2 - Riverside Project', 'Listen to a community presentation.', 'Choose the correct answer.', 'Choose A, B or C.', 'MULTIPLE_CHOICE_SINGLE', 'SINGLE', '["A","B","C"]', NULL, NULL, 1, 1, 0, 5, 8, 2, NULL, 0);

INSERT INTO listening_audio (id, test_id, part_group_id, audio_scope, title, audio_url, audio_object_key, transcript_text, is_deleted, created_time, updated_time)
VALUES
    (2201, 2001, NULL, 'test', 'Full audio - Campus Services', 'https://static.smartielts.local/mock/audio/listening-2001-full.mp3', 'dev/listening/audio/2001-full.mp3', 'Full test audio for campus services practice.', 0, '2026-03-02 09:05:00', '2026-05-01 10:05:00'),
    (2202, 2001, 2101, 'part_group', 'Library membership conversation', 'https://static.smartielts.local/mock/audio/listening-2001-section1.mp3', 'dev/listening/audio/2001-section1.mp3', 'Receptionist: The annual membership fee is twenty pounds. Student: Can I pay by card?', 0, '2026-03-02 09:06:00', '2026-05-01 10:06:00'),
    (2203, 2001, 2102, 'part_group', 'Campus map orientation', 'https://static.smartielts.local/mock/audio/listening-2001-section2.mp3', 'dev/listening/audio/2001-section2.mp3', 'Guide: The science building is next to the cafe, opposite the sports centre.', 0, '2026-03-02 09:07:00', '2026-05-01 10:07:00'),
    (2211, 2002, NULL, 'test', 'Full audio - Community Projects', 'https://static.smartielts.local/mock/audio/listening-2002-full.mp3', 'dev/listening/audio/2002-full.mp3', 'Full test audio for community projects practice.', 0, '2026-03-09 09:05:00', '2026-05-02 10:05:00'),
    (2212, 2002, 2111, 'part_group', 'Volunteer sign-up call', 'https://static.smartielts.local/mock/audio/listening-2002-section1.mp3', 'dev/listening/audio/2002-section1.mp3', 'Caller: We need volunteers on Saturday morning at the riverside garden.', 0, '2026-03-09 09:06:00', '2026-05-02 10:06:00'),
    (2213, 2002, 2112, 'part_group', 'Riverside project presentation', 'https://static.smartielts.local/mock/audio/listening-2002-section2.mp3', 'dev/listening/audio/2002-section2.mp3', 'Speaker: The first stage improves lighting and the second stage adds seating.', 0, '2026-03-09 09:07:00', '2026-05-02 10:07:00');

INSERT INTO listening_question (id, test_id, part_group_id, section_number, question_number, question_type, answer_mode, question_text, correct_answer, options_json, accepted_answers_json, case_insensitive, ignore_whitespace, ignore_punctuation, display_order, score, is_deleted)
VALUES
    (2301, 2001, 2101, 1, 1, 'FORM_COMPLETION', 'TEXT', 'Membership type: _____.', 'student', NULL, '["student"]', 1, 1, 0, 1, 1, 0),
    (2302, 2001, 2101, 1, 2, 'FORM_COMPLETION', 'TEXT', 'Annual fee: _____ pounds.', '20', NULL, '["20","twenty"]', 1, 1, 0, 2, 1, 0),
    (2303, 2001, 2101, 1, 3, 'FORM_COMPLETION', 'TEXT', 'Payment method: _____.', 'card', NULL, '["card"]', 1, 1, 0, 3, 1, 0),
    (2304, 2001, 2101, 1, 4, 'FORM_COMPLETION', 'TEXT', 'Card issued in: _____.', 'May', NULL, '["May"]', 1, 1, 0, 4, 1, 0),
    (2305, 2001, 2102, 2, 5, 'MULTIPLE_CHOICE_SINGLE', 'SINGLE', 'Where is the science building?', 'B', '["A. Behind the library","B. Next to the cafe","C. Near the station"]', '["B"]', 1, 1, 0, 5, 1, 0),
    (2306, 2001, 2102, 2, 6, 'MULTIPLE_CHOICE_SINGLE', 'SINGLE', 'What is opposite the sports centre?', 'A', '["A. Science building","B. Bookshop","C. Main hall"]', '["A"]', 1, 1, 0, 6, 1, 0),
    (2307, 2001, 2102, 2, 7, 'MULTIPLE_CHOICE_SINGLE', 'SINGLE', 'Students meet at the _____.', 'C', '["A. cafe","B. gate","C. main hall"]', '["C"]', 1, 1, 0, 7, 1, 0),
    (2308, 2001, 2102, 2, 8, 'MULTIPLE_CHOICE_SINGLE', 'SINGLE', 'The tour lasts _____.', 'A', '["A. 30 minutes","B. 45 minutes","C. 60 minutes"]', '["A"]', 1, 1, 0, 8, 1, 0),
    (2321, 2002, 2111, 1, 1, 'NOTE_COMPLETION', 'TEXT', 'Volunteers meet on _____.', 'Saturday', NULL, '["Saturday"]', 1, 1, 0, 1, 1, 0),
    (2322, 2002, 2111, 1, 2, 'NOTE_COMPLETION', 'TEXT', 'Work starts in the _____.', 'morning', NULL, '["morning"]', 1, 1, 0, 2, 1, 0),
    (2323, 2002, 2111, 1, 3, 'NOTE_COMPLETION', 'TEXT', 'Location: riverside _____.', 'garden', NULL, '["garden"]', 1, 1, 0, 3, 1, 0),
    (2324, 2002, 2111, 1, 4, 'NOTE_COMPLETION', 'TEXT', 'Bring protective _____.', 'gloves', NULL, '["gloves"]', 1, 1, 0, 4, 1, 0),
    (2325, 2002, 2112, 2, 5, 'MULTIPLE_CHOICE_SINGLE', 'SINGLE', 'The first stage improves _____.', 'A', '["A. lighting","B. parking","C. cafes"]', '["A"]', 1, 1, 0, 5, 1, 0),
    (2326, 2002, 2112, 2, 6, 'MULTIPLE_CHOICE_SINGLE', 'SINGLE', 'The second stage adds _____.', 'B', '["A. shops","B. seating","C. fountains"]', '["B"]', 1, 1, 0, 6, 1, 0),
    (2327, 2002, 2112, 2, 7, 'MULTIPLE_CHOICE_SINGLE', 'SINGLE', 'Residents wanted better _____.', 'C', '["A. signs","B. tickets","C. safety"]', '["C"]', 1, 1, 0, 7, 1, 0),
    (2328, 2002, 2112, 2, 8, 'MULTIPLE_CHOICE_SINGLE', 'SINGLE', 'The project finishes in _____.', 'B', '["A. June","B. July","C. August"]', '["B"]', 1, 1, 0, 8, 1, 0);

INSERT INTO listening_record (id, user_id, test_id, session_id, total_score, started_time, submitted_time, time_limit_seconds, time_spent_seconds, record_status, created_time, is_deleted)
VALUES
    (2401, 2, 2001, 'DEV-LS-ALEX-001', 6, '2026-05-05 19:00:00', '2026-05-05 19:34:00', 2400, 2040, 'SUBMITTED', '2026-05-05 19:00:00', 0),
    (2402, 2, 2002, 'DEV-LS-ALEX-002', 7, '2026-05-11 20:00:00', '2026-05-11 20:36:00', 2400, 2160, 'SUBMITTED', '2026-05-11 20:00:00', 0),
    (2403, 2, 2001, 'DEV-LS-ALEX-INPROGRESS', NULL, '2026-05-17 11:00:00', NULL, 2400, 480, 'IN_PROGRESS', '2026-05-17 11:00:00', 0),
    (2404, 2, 2002, 'DEV-LS-ALEX-DELETED', 4, '2026-04-18 18:10:00', '2026-04-18 18:48:00', 2400, 2280, 'SUBMITTED', '2026-04-18 18:10:00', 1),
    (2411, 101, 2001, 'DEV-LS-101-001', 7, '2026-05-14 19:00:00', '2026-05-14 19:35:00', 2400, 2100, 'SUBMITTED', '2026-05-14 19:00:00', 0),
    (2412, 104, 2002, 'DEV-LS-104-001', 5, '2026-05-12 19:20:00', '2026-05-12 19:58:00', 2400, 2280, 'SUBMITTED', '2026-05-12 19:20:00', 0);

INSERT INTO listening_answer_record (record_id, question_id, part_group_id, user_answer, normalized_answer, raw_answers_json, is_correct, score)
VALUES
    (2401, 2301, 2101, 'student', 'student', '["student"]', 1, 1),
    (2401, 2302, 2101, '20', '20', '["20"]', 1, 1),
    (2401, 2303, 2101, 'cash', 'cash', '["cash"]', 0, 0),
    (2401, 2304, 2101, 'May', 'may', '["May"]', 1, 1),
    (2401, 2305, 2102, 'B', 'b', '["B"]', 1, 1),
    (2401, 2306, 2102, 'A', 'a', '["A"]', 1, 1),
    (2401, 2307, 2102, '', '', '[""]', 0, 0),
    (2401, 2308, 2102, 'A', 'a', '["A"]', 1, 1),
    (2402, 2321, 2111, 'Saturday', 'saturday', '["Saturday"]', 1, 1),
    (2402, 2322, 2111, 'morning', 'morning', '["morning"]', 1, 1),
    (2402, 2323, 2111, 'garden', 'garden', '["garden"]', 1, 1),
    (2402, 2324, 2111, 'gloves', 'gloves', '["gloves"]', 1, 1),
    (2402, 2325, 2112, 'A', 'a', '["A"]', 1, 1),
    (2402, 2326, 2112, 'B', 'b', '["B"]', 1, 1),
    (2402, 2327, 2112, 'A', 'a', '["A"]', 0, 0),
    (2402, 2328, 2112, 'B', 'b', '["B"]', 1, 1),
    (2404, 2321, 2111, 'Sunday', 'sunday', '["Sunday"]', 0, 0),
    (2411, 2301, 2101, 'student', 'student', '["student"]', 1, 1);

INSERT INTO writing_question (id, task_type, chart_type, title, description, image_detail_description, prep_seconds, total_seconds, is_deleted, deleted_time, created_time)
VALUES
    (3001, 'TASK1', 'Line graph', 'DEV SEED Task 1 - Library Visits Chart', 'The chart shows monthly visits to three city libraries in 2025. Summarise the information by selecting and reporting the main features, and make comparisons where relevant.', NULL, 0, 1200, 0, NULL, '2026-03-03 09:00:00'),
    (3002, 'TASK2', NULL, 'DEV SEED Task 2 - Remote Work and Cities', 'Some people believe remote work will make large cities less important. To what extent do you agree or disagree?', NULL, 0, 2400, 0, NULL, '2026-03-10 09:00:00'),
    (3003, 'TASK2', NULL, 'DEV SEED Task 2 - Public Transport Funding', 'Governments should spend more money on public transport than roads. Discuss both views and give your own opinion.', NULL, 0, 2400, 0, NULL, '2026-03-17 09:00:00'),
    (3004, 'TASK1', 'Mixed charts', 'DEV SEED Archived Task 1', 'Archived prompt for deleted-state admin testing.', NULL, 0, 1200, 1, '2026-05-01 10:00:00', '2026-02-15 09:00:00');

INSERT INTO writing_record (id, user_id, question_id, input_type, text_content, extracted_text, target_score, ai_score, ai_feedback, ai_raw_response, ai_status, ai_provider, ai_model, created_time, is_deleted, deleted_time)
VALUES
    (3101, 2, 3001, 'TEXT', 'The line chart compares monthly visits to three city libraries. Overall, the central library remained the most popular, while the riverside branch grew steadily after spring. The east branch fluctuated but ended slightly higher than it began.', NULL, 7.0, 7.0, 'Clear overview and relevant comparisons. Improve precision by grouping months more naturally and avoiding minor detail.', '{"overall":7.0,"taskAchievement":7.0}', 'SUCCESS', 'ALIYUN_BAILIAN', 'qwen3.6-plus', '2026-05-06 21:00:00', 0, NULL),
    (3102, 2, 3002, 'TEXT', 'Remote work will change large cities, but it is unlikely to make them unimportant. Cities still provide specialised jobs, universities, hospitals and cultural networks. However, workers may visit offices less often and choose more affordable suburbs.', NULL, 7.5, 7.5, 'Strong position and coherent progression. Add one more concrete example for a higher band.', '{"overall":7.5,"coherence":7.5}', 'SUCCESS', 'ALIYUN_BAILIAN', 'qwen3.6-plus', '2026-05-12 20:30:00', 0, NULL),
    (3103, 2, 3001, 'IMAGE', NULL, 'The chart shows library visits increasing from January to June. Central branch is highest, riverside rises sharply, east branch is stable.', 7.0, 6.5, 'The answer has a clear overview but lacks enough data selection. Include more comparisons and approximate figures.', '{"overall":6.5,"source":"ocr"}', 'SUCCESS', 'ALIYUN_BAILIAN', 'qwen3.6-plus', '2026-05-14 19:15:00', 0, NULL),
    (3104, 2, 3003, 'PDF', NULL, 'Governments face difficult choices about transport funding. Public transport can reduce congestion and pollution, while roads remain necessary for rural areas and freight.', 7.0, NULL, 'AI scoring failed because the submitted PDF text was incomplete.', '{"error":"mock scoring timeout"}', 'FAILED', 'ALIYUN_BAILIAN', 'qwen3.6-plus', '2026-05-16 22:10:00', 0, NULL),
    (3105, 2, 3002, 'TEXT', 'Draft essay waiting for scoring. Cities will remain important because they concentrate services and relationships.', NULL, 7.0, NULL, NULL, NULL, 'PENDING', 'ALIYUN_BAILIAN', 'qwen3.6-plus', '2026-05-17 12:05:00', 0, NULL),
    (3106, 2, 3001, 'TEXT', 'Deleted old response for recycle-bin testing.', NULL, 6.5, 6.0, 'Older response with limited overview.', '{"overall":6.0}', 'SUCCESS', 'ALIYUN_BAILIAN', 'qwen3.6-plus', '2026-04-16 18:20:00', 1, '2026-05-13 09:00:00'),
    (3111, 101, 3002, 'TEXT', 'Remote work gives employees flexibility, but cities continue to provide opportunity.', NULL, 7.0, 6.5, 'Solid answer with limited development.', '{"overall":6.5}', 'SUCCESS', 'ALIYUN_BAILIAN', 'qwen3.6-plus', '2026-05-15 21:00:00', 0, NULL),
    (3112, 102, 3003, 'TEXT', 'Public transport is important because many people cannot drive.', NULL, 6.0, 5.5, 'Position is clear but ideas need extension.', '{"overall":5.5}', 'SUCCESS', 'ALIYUN_BAILIAN', 'qwen3.6-plus', '2026-05-13 21:00:00', 0, NULL),
    (3113, 105, 3001, 'IMAGE', NULL, 'OCR extracted a partial chart description.', 7.5, NULL, 'Mock OCR/scoring failure for admin AI failure testing.', '{"error":"ocr text too short"}', 'FAILED', 'ALIYUN_BAILIAN', 'qwen3.6-plus', '2026-05-11 21:00:00', 0, NULL);

INSERT INTO writing_record_attachment (id, record_id, file_type, file_url, file_key, sort_order, created_time, ocr_text)
VALUES
    (3201, 3103, 'IMAGE', 'https://static.smartielts.local/mock/uploads/alex-writing-chart-photo.png', 'dev/writing-record/alex-chart-photo.png', 1, '2026-05-14 19:15:30', 'The chart shows library visits increasing from January to June.'),
    (3202, 3104, 'PDF', 'https://static.smartielts.local/mock/uploads/alex-transport-essay.pdf', 'dev/writing-record/alex-transport-essay.pdf', 1, '2026-05-16 22:10:30', 'Governments face difficult choices about transport funding.'),
    (3203, 3113, 'IMAGE', 'https://static.smartielts.local/mock/uploads/nora-task1-photo.png', 'dev/writing-record/nora-task1-photo.png', 1, '2026-05-11 21:00:30', 'Partial chart description.');

INSERT INTO speaking_question (id, part, sub_type, topic_key, question_text, cue_card, follow_up_questions_json, prep_seconds, answer_seconds, display_order, active, is_deleted, deleted_time, created_time)
VALUES
    (4001, 'PART1', 'NORMAL', 'work_study', 'Do you work or are you a student?', NULL, NULL, 0, 30, 1, 1, 0, NULL, '2026-03-04 09:00:00'),
    (4002, 'PART1', 'NORMAL', 'work_study', 'What subject are you most interested in?', NULL, NULL, 0, 30, 2, 1, 0, NULL, '2026-03-04 09:01:00'),
    (4003, 'PART2', 'CUE_CARD', 'useful_skill', 'Describe a useful skill you learned recently.', 'You should say:\nwhat the skill is\nhow you learned it\nwhy you wanted to learn it\nand explain how this skill helps you now.', '["Do people in your country enjoy learning practical skills?","Should schools teach more life skills?"]', 60, 120, 3, 1, 0, NULL, '2026-03-04 09:02:00'),
    (4004, 'PART3', 'FOLLOW_UP', 'useful_skill', 'How has technology changed the way people learn new skills?', NULL, '["What skills will be important in the future?"]', 0, 45, 4, 1, 0, NULL, '2026-03-04 09:03:00'),
    (4005, 'PART1', 'NORMAL', 'hometown', 'What is your hometown like?', NULL, NULL, 0, 30, 5, 1, 0, NULL, '2026-03-04 09:04:00'),
    (4006, 'PART2', 'CUE_CARD', 'memorable_journey', 'Describe a memorable journey you took.', 'You should say:\nwhere you went\nwho you travelled with\nwhat happened\nand explain why you remember it.', '["Do people travel more now than in the past?","Why do some people prefer travelling alone?"]', 60, 120, 6, 1, 0, NULL, '2026-03-04 09:05:00'),
    (4007, 'PART3', 'FOLLOW_UP', 'memorable_journey', 'Do you think tourism always benefits local communities?', NULL, '["How can tourists behave responsibly?"]', 0, 45, 7, 1, 0, NULL, '2026-03-04 09:06:00'),
    (4008, 'PART1', 'NORMAL', 'inactive_topic', 'Inactive seed question for admin testing.', NULL, NULL, 0, 30, 8, 0, 0, NULL, '2026-03-04 09:07:00'),
    (4009, 'PART1', 'NORMAL', 'deleted_topic', 'Deleted seed question for restore testing.', NULL, NULL, 0, 30, 9, 0, 1, '2026-05-01 09:00:00', '2026-03-04 09:08:00');

INSERT INTO speaking_session (id, session_id, user_id, exam_type, total_questions, current_index, exam_status, exam_plan_json, fluency_and_coherence, lexical_resource, grammatical_range_and_accuracy, pronunciation, overall_score, final_feedback, started_time, completed_time, created_time, updated_time)
VALUES
    (4101, 'DEV-SPK-ALEX-COMPLETE-001', 2, 'FULL', 4, 4, 'COMPLETED', '[{"questionId":4001},{"questionId":4002},{"questionId":4003},{"questionId":4004}]', 7.0, 7.0, 6.5, 7.0, 7.0, 'You answered naturally and organised longer ideas well. Keep improving grammatical accuracy in complex sentences.', '2026-05-07 20:00:00', '2026-05-07 20:18:00', '2026-05-07 20:00:00', '2026-05-07 20:18:00'),
    (4102, 'DEV-SPK-ALEX-INPROGRESS-001', 2, 'FULL', 4, 2, 'IN_PROGRESS', '[{"questionId":4005},{"questionId":4006},{"questionId":4007},{"questionId":4004}]', NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-17 12:30:00', NULL, '2026-05-17 12:30:00', '2026-05-17 12:42:00'),
    (4103, 'DEV-SPK-ALEX-DELETED-001', 2, 'FULL', 1, 1, 'COMPLETED', '[{"questionId":4008}]', 5.5, 6.0, 5.5, 6.0, 6.0, 'Deleted speaking practice for recycle-bin testing.', '2026-04-15 18:00:00', '2026-04-15 18:05:00', '2026-04-15 18:00:00', '2026-04-15 18:05:00'),
    (4111, 'DEV-SPK-101-COMPLETE-001', 101, 'FULL', 3, 3, 'COMPLETED', '[{"questionId":4001},{"questionId":4003},{"questionId":4004}]', 7.5, 7.0, 7.0, 7.5, 7.5, 'Confident delivery with clear examples.', '2026-05-15 20:00:00', '2026-05-15 20:15:00', '2026-05-15 20:00:00', '2026-05-15 20:15:00'),
    (4112, 'DEV-SPK-106-FAILED-001', 106, 'FULL', 2, 2, 'FAILED', '[{"questionId":4005},{"questionId":4006}]', NULL, NULL, NULL, NULL, NULL, 'Mock failed final evaluation.', '2026-05-09 20:00:00', NULL, '2026-05-09 20:00:00', '2026-05-09 20:08:00');

INSERT INTO speaking_record (id, user_id, session_id, question_id, audio_url, transcript, fluency_and_coherence, lexical_resource, grammatical_range_and_accuracy, pronunciation, overall_score, feedback, answer_status, is_deleted, deleted_time, ai_status, ai_provider, ai_model, ai_error_message, relevance_comment, quality_comment, created_time, updated_time)
VALUES
    (4201, 2, 'DEV-SPK-ALEX-COMPLETE-001', 4001, 'https://static.smartielts.local/mock/audio/speaking-alex-4201.mp3', 'I am currently a student, and I am preparing for graduate study overseas. I also work part time on weekends.', 7.0, 7.0, 6.5, 7.0, 7.0, 'Natural answer with enough detail for Part 1.', 'SCORED', 0, NULL, 'SUCCESS', 'ALIYUN_BAILIAN', 'qwen3-omni-flash', NULL, 'Directly answers the question.', 'Clear pronunciation with minor grammar slips.', '2026-05-07 20:02:00', '2026-05-07 20:03:00'),
    (4202, 2, 'DEV-SPK-ALEX-COMPLETE-001', 4002, 'https://static.smartielts.local/mock/audio/speaking-alex-4202.mp3', 'I am most interested in environmental economics because it connects business decisions with real social problems.', 7.0, 7.5, 7.0, 7.0, 7.0, 'Good topic vocabulary and clear reason.', 'SCORED', 0, NULL, 'SUCCESS', 'ALIYUN_BAILIAN', 'qwen3-omni-flash', NULL, 'Relevant and developed.', 'Good control of vocabulary.', '2026-05-07 20:05:00', '2026-05-07 20:06:00'),
    (4203, 2, 'DEV-SPK-ALEX-COMPLETE-001', 4003, 'https://static.smartielts.local/mock/audio/speaking-alex-4203.mp3', 'A useful skill I learned recently is making simple data dashboards. I learned it through online tutorials and by applying it to my study schedule.', 7.0, 7.0, 6.5, 7.0, 7.0, 'Well organised Part 2 response. Extend the final explanation for stronger development.', 'SCORED', 0, NULL, 'SUCCESS', 'ALIYUN_BAILIAN', 'qwen3-omni-flash', NULL, 'Covers the cue card.', 'Generally fluent with occasional hesitation.', '2026-05-07 20:09:00', '2026-05-07 20:11:00'),
    (4204, 2, 'DEV-SPK-ALEX-COMPLETE-001', 4004, 'https://static.smartielts.local/mock/audio/speaking-alex-4204.mp3', 'Technology has made learning more flexible because people can watch demonstrations and receive feedback quickly, but it also requires discipline.', 7.0, 7.0, 6.5, 7.0, 7.0, 'Balanced answer with a clear contrast.', 'SCORED', 0, NULL, 'SUCCESS', 'ALIYUN_BAILIAN', 'qwen3-omni-flash', NULL, 'Addresses abstract Part 3 question.', 'Clear but could use more complex grammar.', '2026-05-07 20:14:00', '2026-05-07 20:15:00'),
    (4205, 2, 'DEV-SPK-ALEX-INPROGRESS-001', 4005, 'https://static.smartielts.local/mock/audio/speaking-alex-4205.mp3', 'My hometown is busy but convenient. It has a good transport system and many small restaurants.', 6.5, 6.5, 6.0, 6.5, 6.5, 'Relevant answer with simple but effective language.', 'SCORED', 0, NULL, 'SUCCESS', 'ALIYUN_BAILIAN', 'qwen3-omni-flash', NULL, 'Relevant.', 'Understandable and stable.', '2026-05-17 12:34:00', '2026-05-17 12:35:00'),
    (4206, 2, 'DEV-SPK-ALEX-INPROGRESS-001', 4006, 'https://static.smartielts.local/mock/audio/speaking-alex-4206.mp3', 'I travelled to Kyoto with two friends last year. The journey was memorable because we missed a train and had to change our plan.', NULL, NULL, NULL, NULL, NULL, NULL, 'PROCESSING', 0, NULL, 'PENDING', 'ALIYUN_BAILIAN', 'qwen3-omni-flash', NULL, NULL, NULL, '2026-05-17 12:40:00', '2026-05-17 12:40:00'),
    (4207, 2, 'DEV-SPK-ALEX-DELETED-001', 4008, 'https://static.smartielts.local/mock/audio/speaking-alex-4207.mp3', 'This is an older deleted speaking answer.', 5.5, 6.0, 5.5, 6.0, 6.0, 'Deleted record for recycle-bin testing.', 'SCORED', 1, '2026-05-13 09:15:00', 'SUCCESS', 'ALIYUN_BAILIAN', 'qwen3-omni-flash', NULL, 'Older answer.', 'Limited range.', '2026-04-15 18:03:00', '2026-05-13 09:15:00'),
    (4208, 2, 'DEV-SPK-ALEX-INPROGRESS-001', 4007, 'https://static.smartielts.local/mock/audio/speaking-alex-4208.mp3', 'Tourism can help local businesses, but sometimes it makes housing expensive.', NULL, NULL, NULL, NULL, NULL, NULL, 'FAILED', 0, NULL, 'FAILED', 'ALIYUN_BAILIAN', 'qwen3-omni-flash', 'Mock ASR confidence too low.', 'Relevant but incomplete.', 'Audio quality too low for full scoring.', '2026-05-17 12:42:00', '2026-05-17 12:43:00'),
    (4211, 101, 'DEV-SPK-101-COMPLETE-001', 4001, 'https://static.smartielts.local/mock/audio/speaking-101-4211.mp3', 'I work as a marketing assistant and study English in the evenings.', 7.5, 7.0, 7.0, 7.5, 7.5, 'Confident answer.', 'SCORED', 0, NULL, 'SUCCESS', 'ALIYUN_BAILIAN', 'qwen3-omni-flash', NULL, 'Relevant.', 'Clear.', '2026-05-15 20:02:00', '2026-05-15 20:03:00'),
    (4212, 106, 'DEV-SPK-106-FAILED-001', 4005, 'https://static.smartielts.local/mock/audio/speaking-106-4212.mp3', 'My hometown is small.', NULL, NULL, NULL, NULL, NULL, NULL, 'FAILED', 0, NULL, 'FAILED', 'ALIYUN_BAILIAN', 'qwen3-omni-flash', 'Mock scoring service unavailable.', 'Very brief.', 'Insufficient sample.', '2026-05-09 20:04:00', '2026-05-09 20:05:00');

INSERT INTO speaking_talk (id, talk_id, user_id, session_id, question_id, talk_status, video_url, error_message, created_time, updated_time)
VALUES
    (4301, 'dev-talk-alex-4001', 2, 'DEV-SPK-ALEX-COMPLETE-001', 4001, 'DONE', 'https://static.smartielts.local/mock/video/dev-talk-alex-4001.mp4', NULL, '2026-05-07 20:01:00', '2026-05-07 20:01:30'),
    (4302, 'dev-talk-alex-4006', 2, 'DEV-SPK-ALEX-INPROGRESS-001', 4006, 'CREATED', NULL, NULL, '2026-05-17 12:39:00', '2026-05-17 12:39:00'),
    (4303, 'dev-talk-106-failed', 106, 'DEV-SPK-106-FAILED-001', 4005, 'FAILED', NULL, 'Mock D-ID generation failed.', '2026-05-09 20:03:00', '2026-05-09 20:04:00');

INSERT INTO biz_image_resource (id, target_type, target_id, bucket_type, biz_path, file_url, object_key, original_name, content_type, file_size, width, height, sort_order, created_time, is_deleted)
VALUES
    (5001, 'WRITING_QUESTION', 3001, 'WRITING_QUESTION', 'writing-question-image', 'https://static.smartielts.local/mock/images/writing-task1-library-visits.png', 'dev/writing-question/library-visits.png', 'library-visits.png', 'image/png', 184233, 1200, 720, 1, '2026-03-03 09:05:00', 0),
    (5002, 'READING_PART_GROUP', 1101, 'QUESTION_GROUP_IMAGE', 'question-group-image', 'https://static.smartielts.local/mock/images/reading-compact-cities-map.png', 'dev/question-group-image/reading-compact-cities-map.png', 'compact-cities-map.png', 'image/png', 98221, 900, 540, 1, '2026-03-01 09:05:00', 0),
    (5003, 'LISTENING_PART_GROUP', 2102, 'QUESTION_GROUP_IMAGE', 'question-group-image', 'https://static.smartielts.local/mock/images/listening-campus-map.png', 'dev/question-group-image/listening-campus-map.png', 'campus-map.png', 'image/png', 112442, 1000, 700, 1, '2026-03-02 09:08:00', 0);

-- Extra broad coverage for id=2 manual testing: more question types, image targets and mixed records.
INSERT INTO reading_test (id, title, total_score, created_time, updated_time, is_deleted, timer_mode, prep_seconds, total_seconds, auto_submit, allow_pause)
VALUES
    (1004, 'DEV SEED Reading Test 03 - Culture and Technology', 15, '2026-03-15 09:00:00', '2026-05-04 09:00:00', 0, 'TEST_LEVEL', 120, 3600, 1, 1);

INSERT INTO reading_part_group (id, test_id, part_number, group_number, title, instruction_text, group_guide_text, group_requirement_text, question_type, answer_mode, options_json, accepted_answers_json, answer_rules_json, case_insensitive, ignore_whitespace, ignore_punctuation, question_no_start, question_no_end, display_order, time_limit_seconds, is_deleted)
VALUES
    (1121, 1004, 1, 1, 'Passage 1 - Digital Museums', 'Read the passage about digital museum collections.', 'Do the following statements agree with the information in the passage?', 'Write TRUE, FALSE or NOT GIVEN.', 'TRUE_FALSE_NOT_GIVEN', 'SINGLE', '["TRUE","FALSE","NOT GIVEN"]', NULL, NULL, 1, 1, 0, 1, 5, 1, 1200, 0),
    (1122, 1004, 2, 1, 'Passage 2 - Language Learning Apps', 'Read the passage about mobile language learning.', 'Choose the correct heading for each section.', 'Choose the correct number i-viii.', 'MATCHING', 'SINGLE', '["i. A problem of motivation","ii. Learning through reminders","iii. The role of feedback","iv. A market in decline","v. Teachers and apps together","vi. Costs of development","vii. A single perfect method","viii. Data privacy concerns"]', NULL, NULL, 1, 1, 0, 6, 10, 2, 1200, 0),
    (1123, 1004, 3, 1, 'Passage 3 - Renewable Materials', 'Read the passage about renewable materials in construction.', 'Complete the sentences below.', 'Write NO MORE THAN TWO WORDS.', 'SENTENCE_COMPLETION', 'TEXT', NULL, NULL, NULL, 1, 1, 0, 11, 15, 3, 1200, 0);

INSERT INTO reading_passage (id, test_id, part_group_id, passage_no, title, content, material_type, display_order, is_deleted)
VALUES
    (1221, 1004, 1121, 1, 'Digital Museums', 'Museums have digitised collections to reach visitors who cannot travel. Curators say online exhibitions can widen access, although physical galleries still provide a unique sense of scale and atmosphere.', 'PASSAGE', 1, 0),
    (1222, 1004, 1122, 2, 'Language Learning Apps', 'Language apps use reminders, immediate feedback and short lessons to keep learners engaged. Researchers argue that apps work best when they support teachers rather than replace them completely.', 'PASSAGE', 2, 0),
    (1223, 1004, 1123, 3, 'Renewable Materials in Construction', 'Builders are experimenting with bamboo, engineered timber and recycled fibres. These materials can reduce embodied carbon, but reliable certification is essential before wider adoption.', 'TABLE', 3, 0);

INSERT INTO reading_question (id, passage_id, part_group_id, question_number, question_type, answer_mode, question_text, correct_answer, options_json, accepted_answers_json, group_label, case_insensitive, ignore_whitespace, ignore_punctuation, display_order, score, is_deleted)
VALUES
    (1341, 1221, 1121, 1, 'TRUE_FALSE_NOT_GIVEN', 'SINGLE', 'Online exhibitions can help people who cannot visit in person.', 'TRUE', '["TRUE","FALSE","NOT GIVEN"]', '["TRUE"]', 'Digital Museums', 1, 1, 0, 1, 1, 0),
    (1342, 1221, 1121, 2, 'TRUE_FALSE_NOT_GIVEN', 'SINGLE', 'Curators believe physical galleries are no longer useful.', 'FALSE', '["TRUE","FALSE","NOT GIVEN"]', '["FALSE"]', 'Digital Museums', 1, 1, 0, 2, 1, 0),
    (1343, 1221, 1121, 3, 'TRUE_FALSE_NOT_GIVEN', 'SINGLE', 'The passage says most museums charge for online visits.', 'NOT GIVEN', '["TRUE","FALSE","NOT GIVEN"]', '["NOT GIVEN"]', 'Digital Museums', 1, 1, 0, 3, 1, 0),
    (1344, 1221, 1121, 4, 'TRUE_FALSE_NOT_GIVEN', 'SINGLE', 'Physical galleries can show scale in a special way.', 'TRUE', '["TRUE","FALSE","NOT GIVEN"]', '["TRUE"]', 'Digital Museums', 1, 1, 0, 4, 1, 0),
    (1345, 1221, 1121, 5, 'TRUE_FALSE_NOT_GIVEN', 'SINGLE', 'All visitors prefer digital exhibitions.', 'NOT GIVEN', '["TRUE","FALSE","NOT GIVEN"]', '["NOT GIVEN"]', 'Digital Museums', 1, 1, 0, 5, 1, 0),
    (1346, 1222, 1122, 6, 'MATCHING', 'SINGLE', 'Paragraph A', 'ii', '["i","ii","iii","iv","v","vi","vii","viii"]', '["ii"]', 'Language Apps', 1, 1, 0, 6, 1, 0),
    (1347, 1222, 1122, 7, 'MATCHING', 'SINGLE', 'Paragraph B', 'iii', '["i","ii","iii","iv","v","vi","vii","viii"]', '["iii"]', 'Language Apps', 1, 1, 0, 7, 1, 0),
    (1348, 1222, 1122, 8, 'MATCHING', 'SINGLE', 'Paragraph C', 'v', '["i","ii","iii","iv","v","vi","vii","viii"]', '["v"]', 'Language Apps', 1, 1, 0, 8, 1, 0),
    (1349, 1222, 1122, 9, 'MATCHING', 'SINGLE', 'Paragraph D', 'viii', '["i","ii","iii","iv","v","vi","vii","viii"]', '["viii"]', 'Language Apps', 1, 1, 0, 9, 1, 0),
    (1350, 1222, 1122, 10, 'MATCHING', 'SINGLE', 'Paragraph E', 'i', '["i","ii","iii","iv","v","vi","vii","viii"]', '["i"]', 'Language Apps', 1, 1, 0, 10, 1, 0),
    (1351, 1223, 1123, 11, 'SENTENCE_COMPLETION', 'TEXT', 'Bamboo is one renewable material used by _____.', 'builders', NULL, '["builders"]', 'Renewable Materials', 1, 1, 0, 11, 1, 0),
    (1352, 1223, 1123, 12, 'SENTENCE_COMPLETION', 'TEXT', 'Engineered timber can lower embodied _____.', 'carbon', NULL, '["carbon"]', 'Renewable Materials', 1, 1, 0, 12, 1, 0),
    (1353, 1223, 1123, 13, 'SENTENCE_COMPLETION', 'TEXT', 'Some projects use recycled _____.', 'fibres', NULL, '["fibres","fibers"]', 'Renewable Materials', 1, 1, 0, 13, 1, 0),
    (1354, 1223, 1123, 14, 'SENTENCE_COMPLETION', 'TEXT', 'Wider adoption requires reliable _____.', 'certification', NULL, '["certification"]', 'Renewable Materials', 1, 1, 0, 14, 1, 0),
    (1355, 1223, 1123, 15, 'SENTENCE_COMPLETION', 'TEXT', 'Renewable materials are being tested in _____.', 'construction', NULL, '["construction"]', 'Renewable Materials', 1, 1, 0, 15, 1, 0);

INSERT INTO reading_question_answer_rule (question_id, blank_no, answer_group_no, answer_text, normalized_answer, is_primary, display_order)
SELECT id, 1, 1, correct_answer, LOWER(correct_answer), 1, display_order
FROM reading_question
WHERE id BETWEEN 1351 AND 1355;

INSERT INTO reading_record (id, user_id, test_id, session_id, total_score, started_time, submitted_time, time_limit_seconds, time_spent_seconds, record_status, created_time, is_deleted)
VALUES
    (1406, 2, 1004, 'DEV-RD-ALEX-MIXED-TYPES', 11, '2026-05-18 19:00:00', '2026-05-18 19:55:00', 3600, 3300, 'SUBMITTED', '2026-05-18 19:00:00', 0),
    (1414, 105, 1004, 'DEV-RD-105-MIXED-TYPES', 13, '2026-05-16 18:40:00', '2026-05-16 19:31:00', 3600, 3060, 'SUBMITTED', '2026-05-16 18:40:00', 0);

INSERT INTO reading_answer_record (record_id, question_id, part_group_id, user_answer, normalized_answer, raw_answers_json, is_correct, score)
VALUES
    (1406, 1341, 1121, 'TRUE', 'true', '["TRUE"]', 1, 1),
    (1406, 1342, 1121, 'FALSE', 'false', '["FALSE"]', 1, 1),
    (1406, 1343, 1121, 'FALSE', 'false', '["FALSE"]', 0, 0),
    (1406, 1344, 1121, 'TRUE', 'true', '["TRUE"]', 1, 1),
    (1406, 1345, 1121, 'NOT GIVEN', 'not given', '["NOT GIVEN"]', 1, 1),
    (1406, 1346, 1122, 'ii', 'ii', '["ii"]', 1, 1),
    (1406, 1347, 1122, 'iii', 'iii', '["iii"]', 1, 1),
    (1406, 1348, 1122, 'iv', 'iv', '["iv"]', 0, 0),
    (1406, 1349, 1122, 'viii', 'viii', '["viii"]', 1, 1),
    (1406, 1350, 1122, 'i', 'i', '["i"]', 1, 1),
    (1406, 1351, 1123, 'builders', 'builders', '["builders"]', 1, 1),
    (1406, 1352, 1123, 'carbon', 'carbon', '["carbon"]', 1, 1),
    (1406, 1353, 1123, '', '', '[""]', 0, 0),
    (1406, 1354, 1123, 'certification', 'certification', '["certification"]', 1, 1),
    (1406, 1355, 1123, 'construction', 'construction', '["construction"]', 1, 1),
    (1414, 1341, 1121, 'TRUE', 'true', '["TRUE"]', 1, 1),
    (1414, 1342, 1121, 'FALSE', 'false', '["FALSE"]', 1, 1);

INSERT INTO listening_test (id, title, total_score, created_time, updated_time, is_deleted, timer_mode, prep_seconds, total_seconds, auto_submit, allow_pause, allow_audio_seek)
VALUES
    (2003, 'DEV SEED Listening Test 03 - Travel and Research', 12, '2026-03-16 09:00:00', '2026-05-04 10:00:00', 0, 'TEST_LEVEL', 45, 2400, 1, 1, 1);

INSERT INTO listening_part_group (id, test_id, part_number, group_number, title, instruction_text, group_guide_text, group_requirement_text, question_type, answer_mode, options_json, accepted_answers_json, answer_rules_json, case_insensitive, ignore_whitespace, ignore_punctuation, question_no_start, question_no_end, display_order, time_limit_seconds, is_deleted)
VALUES
    (2121, 2003, 1, 1, 'Section 1 - Hostel Booking', 'Listen to a hostel booking conversation.', 'Complete the table below.', 'Write ONE WORD AND/OR A NUMBER.', 'TABLE_COMPLETION', 'TEXT', NULL, NULL, NULL, 1, 1, 0, 1, 4, 1, 600, 0),
    (2122, 2003, 2, 1, 'Section 2 - City Walking Tour', 'Listen to directions on a city map.', 'Label the map.', 'Choose the correct letter A-F.', 'MAP_LABELING', 'SINGLE', '["A","B","C","D","E","F"]', NULL, NULL, 1, 1, 0, 5, 8, 2, 600, 0),
    (2123, 2003, 3, 1, 'Section 3 - Research Discussion', 'Listen to two students discussing a research project.', 'Choose TWO answers.', 'Choose TWO letters A-E.', 'MULTIPLE_CHOICE_MULTI', 'MULTI', '["A. interview commuters","B. collect weather data","C. analyse ticket prices","D. review old maps","E. compare bus routes"]', NULL, NULL, 1, 1, 0, 9, 12, 3, 900, 0);

INSERT INTO listening_audio (id, test_id, part_group_id, audio_scope, title, audio_url, audio_object_key, transcript_text, is_deleted, created_time, updated_time)
VALUES
    (2221, 2003, NULL, 'test', 'Full audio - Travel and Research', 'https://static.smartielts.local/mock/audio/listening-2003-full.mp3', 'dev/listening/audio/2003-full.mp3', 'Full test audio for travel booking and research discussion.', 0, '2026-03-16 09:05:00', '2026-05-04 10:05:00'),
    (2222, 2003, 2121, 'part_group', 'Hostel booking conversation', 'https://static.smartielts.local/mock/audio/listening-2003-section1.mp3', 'dev/listening/audio/2003-section1.mp3', 'Receptionist: The hostel is on Market Street. Guest: I need three nights from the fifteenth.', 0, '2026-03-16 09:06:00', '2026-05-04 10:06:00'),
    (2223, 2003, 2122, 'part_group', 'City walking tour directions', 'https://static.smartielts.local/mock/audio/listening-2003-section2.mp3', 'dev/listening/audio/2003-section2.mp3', 'Guide: The museum is beside the river entrance, and the market is opposite the old clock tower.', 0, '2026-03-16 09:07:00', '2026-05-04 10:07:00'),
    (2224, 2003, 2123, 'part_group', 'Research project discussion', 'https://static.smartielts.local/mock/audio/listening-2003-section3.mp3', 'dev/listening/audio/2003-section3.mp3', 'Student A: We should interview commuters and compare bus routes. Student B: Ticket prices may be useful later.', 0, '2026-03-16 09:08:00', '2026-05-04 10:08:00');

INSERT INTO listening_question (id, test_id, part_group_id, section_number, question_number, question_type, answer_mode, question_text, correct_answer, options_json, accepted_answers_json, case_insensitive, ignore_whitespace, ignore_punctuation, display_order, score, is_deleted)
VALUES
    (2341, 2003, 2121, 1, 1, 'TABLE_COMPLETION', 'TEXT', 'Hostel location: _____ Street.', 'Market', NULL, '["Market"]', 1, 1, 0, 1, 1, 0),
    (2342, 2003, 2121, 1, 2, 'TABLE_COMPLETION', 'TEXT', 'Number of nights: _____.', '3', NULL, '["3","three"]', 1, 1, 0, 2, 1, 0),
    (2343, 2003, 2121, 1, 3, 'TABLE_COMPLETION', 'TEXT', 'Arrival date: _____ May.', '15', NULL, '["15","fifteenth"]', 1, 1, 0, 3, 1, 0),
    (2344, 2003, 2121, 1, 4, 'TABLE_COMPLETION', 'TEXT', 'Breakfast is served in the _____.', 'courtyard', NULL, '["courtyard"]', 1, 1, 0, 4, 1, 0),
    (2345, 2003, 2122, 2, 5, 'MAP_LABELING', 'SINGLE', 'Museum', 'C', '["A","B","C","D","E","F"]', '["C"]', 1, 1, 0, 5, 1, 0),
    (2346, 2003, 2122, 2, 6, 'MAP_LABELING', 'SINGLE', 'Market', 'F', '["A","B","C","D","E","F"]', '["F"]', 1, 1, 0, 6, 1, 0),
    (2347, 2003, 2122, 2, 7, 'MAP_LABELING', 'SINGLE', 'Information desk', 'A', '["A","B","C","D","E","F"]', '["A"]', 1, 1, 0, 7, 1, 0),
    (2348, 2003, 2122, 2, 8, 'MAP_LABELING', 'SINGLE', 'Old clock tower', 'D', '["A","B","C","D","E","F"]', '["D"]', 1, 1, 0, 8, 1, 0),
    (2349, 2003, 2123, 3, 9, 'MULTIPLE_CHOICE_MULTI', 'MULTI', 'Which TWO methods do the students choose first?', 'A,E', '["A. interview commuters","B. collect weather data","C. analyse ticket prices","D. review old maps","E. compare bus routes"]', '["A","E"]', 1, 1, 0, 9, 2, 0),
    (2350, 2003, 2123, 3, 10, 'MULTIPLE_CHOICE_MULTI', 'MULTI', 'Which TWO sources will they use later?', 'C,D', '["A. interview commuters","B. collect weather data","C. analyse ticket prices","D. review old maps","E. compare bus routes"]', '["C","D"]', 1, 1, 0, 10, 2, 0),
    (2351, 2003, 2123, 3, 11, 'SHORT_ANSWER', 'TEXT', 'What group will they interview?', 'commuters', NULL, '["commuters"]', 1, 1, 0, 11, 1, 0),
    (2352, 2003, 2123, 3, 12, 'SHORT_ANSWER', 'TEXT', 'Which routes will they compare?', 'bus routes', NULL, '["bus routes"]', 1, 1, 0, 12, 1, 0);

INSERT INTO listening_record (id, user_id, test_id, session_id, total_score, started_time, submitted_time, time_limit_seconds, time_spent_seconds, record_status, created_time, is_deleted)
VALUES
    (2405, 2, 2003, 'DEV-LS-ALEX-MAP-MULTI', 10, '2026-05-18 20:00:00', '2026-05-18 20:35:00', 2400, 2100, 'SUBMITTED', '2026-05-18 20:00:00', 0),
    (2406, 2, 2003, 'DEV-LS-ALEX-PAUSED', NULL, '2026-05-18 21:00:00', NULL, 2400, 720, 'PAUSED', '2026-05-18 21:00:00', 0),
    (2413, 107, 2003, 'DEV-LS-107-MAP-MULTI', 11, '2026-05-16 20:00:00', '2026-05-16 20:36:00', 2400, 2160, 'SUBMITTED', '2026-05-16 20:00:00', 0);

INSERT INTO listening_answer_record (record_id, question_id, part_group_id, user_answer, normalized_answer, raw_answers_json, is_correct, score)
VALUES
    (2405, 2341, 2121, 'Market', 'market', '["Market"]', 1, 1),
    (2405, 2342, 2121, 'three', 'three', '["three"]', 1, 1),
    (2405, 2343, 2121, '15', '15', '["15"]', 1, 1),
    (2405, 2344, 2121, 'canteen', 'canteen', '["canteen"]', 0, 0),
    (2405, 2345, 2122, 'C', 'c', '["C"]', 1, 1),
    (2405, 2346, 2122, 'F', 'f', '["F"]', 1, 1),
    (2405, 2347, 2122, 'B', 'b', '["B"]', 0, 0),
    (2405, 2348, 2122, 'D', 'd', '["D"]', 1, 1),
    (2405, 2349, 2123, 'A,E', 'a,e', '["A","E"]', 1, 2),
    (2405, 2350, 2123, 'C,E', 'c,e', '["C","E"]', 0, 1),
    (2405, 2351, 2123, 'commuters', 'commuters', '["commuters"]', 1, 1),
    (2405, 2352, 2123, 'bus routes', 'bus routes', '["bus routes"]', 1, 1),
    (2413, 2341, 2121, 'Market', 'market', '["Market"]', 1, 1),
    (2413, 2349, 2123, 'A,E', 'a,e', '["A","E"]', 1, 2);

INSERT INTO biz_image_resource (id, target_type, target_id, bucket_type, biz_path, file_url, object_key, original_name, content_type, file_size, width, height, sort_order, created_time, is_deleted)
VALUES
    (5004, 'READING_QUESTION', 1346, 'QUESTION_GROUP_IMAGE', 'question-group-image', 'https://static.smartielts.local/mock/images/reading-language-apps-headings.png', 'dev/question-group-image/reading-language-apps-headings.png', 'language-apps-headings.png', 'image/png', 76012, 900, 640, 1, '2026-03-15 09:10:00', 0),
    (5005, 'LISTENING_QUESTION', 2345, 'QUESTION_GROUP_IMAGE', 'question-group-image', 'https://static.smartielts.local/mock/images/listening-city-map-question.png', 'dev/question-group-image/listening-city-map-question.png', 'city-map-question.png', 'image/png', 134110, 1100, 760, 1, '2026-03-16 09:10:00', 0),
    (5006, 'WRITING_QUESTION', 3001, 'WRITING_QUESTION', 'writing-question-image', 'https://static.smartielts.local/mock/images/writing-task1-library-visits-table.png', 'dev/writing-question/library-visits-table.png', 'library-visits-table.png', 'image/png', 104233, 1000, 620, 2, '2026-03-03 09:06:00', 0);

COMMIT;

SELECT 'seed_complete' AS status,
       (SELECT COUNT(*) FROM sys_user WHERE role = 'USER') AS user_count,
       (SELECT COUNT(*) FROM reading_record WHERE user_id = 2) AS alex_reading_records,
       (SELECT COUNT(*) FROM listening_record WHERE user_id = 2) AS alex_listening_records,
       (SELECT COUNT(*) FROM writing_record WHERE user_id = 2) AS alex_writing_records,
       (SELECT COUNT(*) FROM speaking_record WHERE user_id = 2) AS alex_speaking_records;
