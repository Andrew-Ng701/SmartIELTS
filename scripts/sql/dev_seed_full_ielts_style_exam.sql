-- SmartIELTS local/dev full IELTS-style exam seed.
-- Re-runnable. Content is original IELTS-style practice material, not copied from any official IELTS paper.

START TRANSACTION;

SET FOREIGN_KEY_CHECKS = 0;

DELETE FROM reading_question_answer_rule WHERE question_id BETWEEN 9301 AND 9340;
DELETE FROM reading_question WHERE id BETWEEN 9301 AND 9340;
DELETE FROM reading_passage WHERE id BETWEEN 9201 AND 9203;
DELETE FROM reading_part_group WHERE id BETWEEN 9101 AND 9109;
DELETE FROM reading_test WHERE id = 9001;

DELETE FROM listening_question WHERE id BETWEEN 9701 AND 9740;
DELETE FROM listening_audio WHERE id BETWEEN 9601 AND 9605;
DELETE FROM listening_part_group WHERE id BETWEEN 9501 AND 9504;
DELETE FROM listening_test WHERE id = 9401;

DELETE FROM biz_image_resource WHERE id BETWEEN 9901 AND 9902;
DELETE FROM writing_question WHERE id BETWEEN 9801 AND 9802;

SET FOREIGN_KEY_CHECKS = 1;

INSERT INTO reading_test (id, title, total_score, created_time, updated_time, is_deleted, timer_mode, prep_seconds, total_seconds, auto_submit, allow_pause)
VALUES
    (9001, 'IELTS-style Academic Reading Practice Test - Coastal Cities and Innovation', 40, NOW(), NOW(), 0, 'TEST_LEVEL', 0, 3600, 1, 0);

INSERT INTO reading_part_group (id, test_id, part_number, group_number, title, instruction_text, group_guide_text, group_requirement_text, question_type, answer_mode, options_json, accepted_answers_json, answer_rules_json, case_insensitive, ignore_whitespace, ignore_punctuation, question_no_start, question_no_end, display_order, time_limit_seconds, is_deleted)
VALUES
    (9101, 9001, 1, 1, 'Passage 1 - The Return of the Urban Waterfront', 'Read Passage 1 and answer Questions 1-13.', 'Complete the notes below.', 'Write ONE WORD ONLY from the passage for each answer.', 'NOTE_COMPLETION', 'TEXT', NULL, NULL, NULL, 1, 1, 1, 1, 6, 1, NULL, 0),
    (9102, 9001, 1, 2, 'Passage 1 - The Return of the Urban Waterfront', 'Read Passage 1 and answer Questions 1-13.', 'Do the following statements agree with the information in the passage?', 'Write TRUE, FALSE or NOT GIVEN.', 'TRUE_FALSE_NOT_GIVEN', 'SINGLE', '["TRUE","FALSE","NOT GIVEN"]', NULL, NULL, 1, 1, 0, 7, 10, 2, NULL, 0),
    (9103, 9001, 1, 3, 'Passage 1 - The Return of the Urban Waterfront', 'Read Passage 1 and answer Questions 1-13.', 'Choose the correct answer.', 'Choose A, B, C or D.', 'MULTIPLE_CHOICE_SINGLE', 'SINGLE', '["A","B","C","D"]', NULL, NULL, 1, 1, 0, 11, 13, 3, NULL, 0),
    (9104, 9001, 2, 1, 'Passage 2 - Sound Maps of the Forest', 'Read Passage 2 and answer Questions 14-26.', 'Choose the correct heading for each paragraph.', 'Choose the correct number i-viii.', 'MATCHING', 'SINGLE', '["i. A cheaper way to collect evidence","ii. Early doubts about automatic listening","iii. Why old methods missed important activity","iv. The value of local knowledge","v. A warning about using data alone","vi. New uses beyond animal surveys","vii. A debate about forest ownership","viii. Problems caused by satellite images"]', NULL, NULL, 1, 1, 0, 14, 18, 4, NULL, 0),
    (9105, 9001, 2, 2, 'Passage 2 - Sound Maps of the Forest', 'Read Passage 2 and answer Questions 14-26.', 'Complete the summary below.', 'Write ONE WORD ONLY from the passage for each answer.', 'SUMMARY_COMPLETION', 'TEXT', NULL, NULL, NULL, 1, 1, 1, 19, 23, 5, NULL, 0),
    (9106, 9001, 2, 3, 'Passage 2 - Sound Maps of the Forest', 'Read Passage 2 and answer Questions 14-26.', 'Choose the correct answer.', 'Choose A, B, C or D.', 'MULTIPLE_CHOICE_SINGLE', 'SINGLE', '["A","B","C","D"]', NULL, NULL, 1, 1, 0, 24, 26, 6, NULL, 0),
    (9107, 9001, 3, 1, 'Passage 3 - The Invention of Failure', 'Read Passage 3 and answer Questions 27-40.', 'Do the following statements agree with the claims of the writer?', 'Write YES, NO or NOT GIVEN.', 'YES_NO_NOT_GIVEN', 'SINGLE', '["YES","NO","NOT GIVEN"]', NULL, NULL, 1, 1, 0, 27, 32, 7, NULL, 0),
    (9108, 9001, 3, 2, 'Passage 3 - The Invention of Failure', 'Read Passage 3 and answer Questions 27-40.', 'Complete the sentences below.', 'Write NO MORE THAN TWO WORDS from the passage.', 'SENTENCE_COMPLETION', 'TEXT', NULL, NULL, NULL, 1, 1, 1, 33, 37, 8, NULL, 0),
    (9109, 9001, 3, 3, 'Passage 3 - The Invention of Failure', 'Read Passage 3 and answer Questions 27-40.', 'Choose the correct answer.', 'Choose A, B, C or D.', 'MULTIPLE_CHOICE_SINGLE', 'SINGLE', '["A","B","C","D"]', NULL, NULL, 1, 1, 0, 38, 40, 9, NULL, 0);

INSERT INTO reading_passage (id, test_id, part_group_id, passage_no, title, content, material_type, display_order, is_deleted)
VALUES
    (9201, 9001, NULL, 1, 'The Return of the Urban Waterfront',
'For much of the twentieth century, many city waterfronts were treated as working edges rather than public places. Warehouses, rail lines and fenced docks separated residents from the water, and in several ports the river or harbour became associated with noise, risk and pollution. When manufacturing moved away, large areas were left unused. At first, city leaders saw these sites mainly as real estate opportunities, but early projects often produced isolated apartment blocks with little connection to the rest of the city.

More recent waterfront plans have taken a different approach. In Portland, Rotterdam and a number of Asian port cities, planners have linked new housing to public transport, flood protection and everyday services. Instead of building a single attraction, they have tried to create continuous routes for walking and cycling. A former dock road may become a promenade, while old cranes or warehouses are retained as reminders of industrial history. Supporters argue that such features give redeveloped areas a memory that new construction alone cannot provide.

The most difficult issue is water management. Climate change has made storm surges and heavy rain more common, so waterfront districts must be designed to flood safely. Some schemes use raised ground floors, parks that can temporarily store water, and planted edges that reduce the force of waves. These measures are more expensive than simple walls, but they can create spaces that people enjoy in normal weather. Researchers also note that successful waterfronts require maintenance budgets; without them, public spaces decline quickly and private owners begin to control access.

Critics warn that attractive waterfronts can raise rents and push out long-term residents. Several cities now require affordable housing, public seating and local business space as conditions for development approval. The aim is not only to beautify the water''s edge but to make it part of daily urban life.', 'PASSAGE', 1, 0),
    (9202, 9001, NULL, 2, 'Sound Maps of the Forest',
'A. For decades, conservationists counted wildlife by walking fixed routes and recording what they saw. This method produced valuable records, but it also missed animals that were active at night or hidden high in the canopy. In dense forests, visibility can be so poor that sound becomes a better guide than sight.

B. Early experiments with automatic recorders were not immediately persuasive. The machines collected hundreds of hours of noise, and researchers struggled to separate birds, insects, rain and distant engines. Some biologists argued that the work created more data than insight.

C. Improvements in battery life, storage and machine learning have changed that view. Small recorders can now remain in the field for weeks, and software can identify repeated patterns in calls. Because the equipment is relatively cheap, researchers can place many devices across a landscape and compare activity between protected and disturbed areas.

D. Local knowledge remains essential. In one community forest project, residents recognised seasonal calls that the software labelled as unknown. Their observations helped researchers connect recordings with fruiting trees, hunting pressure and changes in river levels.

E. Sound maps are also being used outside traditional wildlife surveys. Ecologists have monitored illegal logging, measured the recovery of burned areas and even studied how traffic noise affects pollinators. A forest that sounds rich is not automatically healthy, however, and scientists caution that acoustic data should be combined with field visits, satellite images and interviews.

F. The new tools have made conservation more transparent. Instead of relying only on expert reports, communities can hear changes in their environment and challenge claims that a damaged area is recovering. The technology is not a replacement for protection, but it gives people a cheaper way to notice when protection is failing.', 'PASSAGE', 2, 0),
    (9203, 9001, NULL, 3, 'The Invention of Failure',
'In business and education, failure is often described as a necessary stage on the path to success. This idea is attractive because it turns disappointment into evidence of courage. Yet the modern celebration of failure is more complicated than it first appears. Not every unsuccessful attempt teaches a useful lesson, and not every learner has the same freedom to recover from mistakes.

The language of productive failure began in engineering, where prototypes are expected to break. A bridge model that collapses in a laboratory may reveal a weakness before real people are at risk. In this context, failure is controlled, observed and documented. The lesson is not simply that the model failed, but why it failed and how the design should change.

Problems arise when the same language is applied loosely to schools and workplaces. A student who receives poor feedback without guidance may become less willing to experiment. An employee told to be bold may still be punished if a project loses money. In these cases, organisations praise risk in public while rewarding caution in private.

Psychologists distinguish between errors that expose a missing skill and errors caused by careless preparation. The first type can be valuable if followed by targeted practice; the second often signals that basic routines were ignored. Treating both as equally admirable can make failure sound romantic rather than analytical.

Some companies have tried to make learning from failure more systematic. After a project ends, teams hold reviews that focus on decisions, assumptions and evidence rather than blame. The best reviews produce changes in process, such as earlier testing or clearer responsibility. Without such changes, the meeting becomes a ritual and the same mistake returns under a new name.

The challenge, then, is to design environments where small mistakes are visible, recoverable and informative. Failure should not be worshipped. It should be made useful.', 'PASSAGE', 3, 0);

INSERT INTO reading_question (id, passage_id, part_group_id, question_number, question_type, answer_mode, question_text, correct_answer, options_json, accepted_answers_json, group_label, case_insensitive, ignore_whitespace, ignore_punctuation, display_order, score, is_deleted)
VALUES
    (9301, 9201, 9101, 1, 'NOTE_COMPLETION', 'TEXT', 'Old waterfronts were separated from residents by rail lines, warehouses and fenced _____.', 'docks', NULL, '["docks"]', 'Waterfront notes', 1, 1, 1, 1, 1, 0),
    (9302, 9201, 9101, 2, 'NOTE_COMPLETION', 'TEXT', 'Early redevelopment sometimes created isolated apartment _____.', 'blocks', NULL, '["blocks"]', 'Waterfront notes', 1, 1, 1, 2, 1, 0),
    (9303, 9201, 9101, 3, 'NOTE_COMPLETION', 'TEXT', 'Newer plans connect housing with transport, flood protection and everyday _____.', 'services', NULL, '["services"]', 'Waterfront notes', 1, 1, 1, 3, 1, 0),
    (9304, 9201, 9101, 4, 'NOTE_COMPLETION', 'TEXT', 'A former dock road may be turned into a _____.', 'promenade', NULL, '["promenade"]', 'Waterfront notes', 1, 1, 1, 4, 1, 0),
    (9305, 9201, 9101, 5, 'NOTE_COMPLETION', 'TEXT', 'Old cranes and warehouses help give new districts a sense of _____.', 'memory', NULL, '["memory"]', 'Waterfront notes', 1, 1, 1, 5, 1, 0),
    (9306, 9201, 9101, 6, 'NOTE_COMPLETION', 'TEXT', 'Planted edges can reduce the force of _____.', 'waves', NULL, '["waves"]', 'Waterfront notes', 1, 1, 1, 6, 1, 0),
    (9307, 9201, 9102, 7, 'TRUE_FALSE_NOT_GIVEN', 'SINGLE', 'Early waterfront projects always included strong links to the rest of the city.', 'FALSE', '["TRUE","FALSE","NOT GIVEN"]', '["FALSE"]', 'Waterfront statements', 1, 1, 0, 7, 1, 0),
    (9308, 9201, 9102, 8, 'TRUE_FALSE_NOT_GIVEN', 'SINGLE', 'Some parks in waterfront districts are designed to store water temporarily.', 'TRUE', '["TRUE","FALSE","NOT GIVEN"]', '["TRUE"]', 'Waterfront statements', 1, 1, 0, 8, 1, 0),
    (9309, 9201, 9102, 9, 'TRUE_FALSE_NOT_GIVEN', 'SINGLE', 'The passage says Rotterdam has the most successful waterfront in Europe.', 'NOT GIVEN', '["TRUE","FALSE","NOT GIVEN"]', '["NOT GIVEN"]', 'Waterfront statements', 1, 1, 0, 9, 1, 0),
    (9310, 9201, 9102, 10, 'TRUE_FALSE_NOT_GIVEN', 'SINGLE', 'Some cities make affordable housing a condition of development approval.', 'TRUE', '["TRUE","FALSE","NOT GIVEN"]', '["TRUE"]', 'Waterfront statements', 1, 1, 0, 10, 1, 0),
    (9311, 9201, 9103, 11, 'MULTIPLE_CHOICE_SINGLE', 'SINGLE', 'What does the writer identify as the hardest waterfront planning issue?', 'C', '["A. attracting tourists","B. preserving every warehouse","C. managing water safely","D. increasing private ownership"]', '["C"]', 'Waterfront choices', 1, 1, 0, 11, 1, 0),
    (9312, 9201, 9103, 12, 'MULTIPLE_CHOICE_SINGLE', 'SINGLE', 'Why are old industrial features sometimes kept?', 'B', '["A. They reduce construction costs.","B. They preserve historical character.","C. They stop flooding completely.","D. They prevent all rent rises."]', '["B"]', 'Waterfront choices', 1, 1, 0, 12, 1, 0),
    (9313, 9201, 9103, 13, 'MULTIPLE_CHOICE_SINGLE', 'SINGLE', 'Critics are mainly concerned that waterfront renewal may _____.', 'A', '["A. displace existing residents","B. make rivers too clean","C. remove public transport","D. lower property values"]', '["A"]', 'Waterfront choices', 1, 1, 0, 13, 1, 0),
    (9314, 9202, 9104, 14, 'MATCHING', 'SINGLE', 'Paragraph A', 'iii', '["i","ii","iii","iv","v","vi","vii","viii"]', '["iii"]', 'Forest headings', 1, 1, 0, 14, 1, 0),
    (9315, 9202, 9104, 15, 'MATCHING', 'SINGLE', 'Paragraph B', 'ii', '["i","ii","iii","iv","v","vi","vii","viii"]', '["ii"]', 'Forest headings', 1, 1, 0, 15, 1, 0),
    (9316, 9202, 9104, 16, 'MATCHING', 'SINGLE', 'Paragraph C', 'i', '["i","ii","iii","iv","v","vi","vii","viii"]', '["i"]', 'Forest headings', 1, 1, 0, 16, 1, 0),
    (9317, 9202, 9104, 17, 'MATCHING', 'SINGLE', 'Paragraph D', 'iv', '["i","ii","iii","iv","v","vi","vii","viii"]', '["iv"]', 'Forest headings', 1, 1, 0, 17, 1, 0),
    (9318, 9202, 9104, 18, 'MATCHING', 'SINGLE', 'Paragraph E', 'v', '["i","ii","iii","iv","v","vi","vii","viii"]', '["v"]', 'Forest headings', 1, 1, 0, 18, 1, 0),
    (9319, 9202, 9105, 19, 'SUMMARY_COMPLETION', 'TEXT', 'Traditional surveys depended on what researchers could _____.', 'see', NULL, '["see"]', 'Forest summary', 1, 1, 1, 19, 1, 0),
    (9320, 9202, 9105, 20, 'SUMMARY_COMPLETION', 'TEXT', 'Automatic recorders collect large amounts of _____.', 'noise', NULL, '["noise"]', 'Forest summary', 1, 1, 1, 20, 1, 0),
    (9321, 9202, 9105, 21, 'SUMMARY_COMPLETION', 'TEXT', 'Software can now identify repeated patterns in _____.', 'calls', NULL, '["calls"]', 'Forest summary', 1, 1, 1, 21, 1, 0),
    (9322, 9202, 9105, 22, 'SUMMARY_COMPLETION', 'TEXT', 'Residents connected recordings with changes in river _____.', 'levels', NULL, '["levels"]', 'Forest summary', 1, 1, 1, 22, 1, 0),
    (9323, 9202, 9105, 23, 'SUMMARY_COMPLETION', 'TEXT', 'Communities can challenge claims that a damaged area is _____.', 'recovering', NULL, '["recovering"]', 'Forest summary', 1, 1, 1, 23, 1, 0),
    (9324, 9202, 9106, 24, 'MULTIPLE_CHOICE_SINGLE', 'SINGLE', 'Why did some researchers doubt early automatic recorders?', 'D', '["A. They were illegal in protected areas.","B. They recorded only birds.","C. They were too large to move.","D. They produced more data than researchers could interpret."]', '["D"]', 'Forest choices', 1, 1, 0, 24, 1, 0),
    (9325, 9202, 9106, 25, 'MULTIPLE_CHOICE_SINGLE', 'SINGLE', 'Which activity is mentioned as a new use of sound maps?', 'B', '["A. predicting earthquakes","B. monitoring illegal logging","C. measuring ocean temperature","D. designing tourist trails"]', '["B"]', 'Forest choices', 1, 1, 0, 25, 1, 0),
    (9326, 9202, 9106, 26, 'MULTIPLE_CHOICE_SINGLE', 'SINGLE', 'What is the writer''s view of acoustic technology?', 'A', '["A. Useful when combined with other evidence","B. A full replacement for conservation rules","C. Too expensive for community use","D. Reliable only in cities"]', '["A"]', 'Forest choices', 1, 1, 0, 26, 1, 0),
    (9327, 9203, 9107, 27, 'YES_NO_NOT_GIVEN', 'SINGLE', 'The writer believes all failure produces useful lessons.', 'NO', '["YES","NO","NOT GIVEN"]', '["NO"]', 'Failure claims', 1, 1, 0, 27, 1, 0),
    (9328, 9203, 9107, 28, 'YES_NO_NOT_GIVEN', 'SINGLE', 'In engineering, failed prototypes can protect people from later danger.', 'YES', '["YES","NO","NOT GIVEN"]', '["YES"]', 'Failure claims', 1, 1, 0, 28, 1, 0),
    (9329, 9203, 9107, 29, 'YES_NO_NOT_GIVEN', 'SINGLE', 'Most schools now teach students how to analyse unsuccessful work.', 'NOT GIVEN', '["YES","NO","NOT GIVEN"]', '["NOT GIVEN"]', 'Failure claims', 1, 1, 0, 29, 1, 0),
    (9330, 9203, 9107, 30, 'YES_NO_NOT_GIVEN', 'SINGLE', 'Organisations may publicly praise risk while privately rewarding caution.', 'YES', '["YES","NO","NOT GIVEN"]', '["YES"]', 'Failure claims', 1, 1, 0, 30, 1, 0),
    (9331, 9203, 9107, 31, 'YES_NO_NOT_GIVEN', 'SINGLE', 'Careless preparation and missing skills should be treated in the same way.', 'NO', '["YES","NO","NOT GIVEN"]', '["NO"]', 'Failure claims', 1, 1, 0, 31, 1, 0),
    (9332, 9203, 9107, 32, 'YES_NO_NOT_GIVEN', 'SINGLE', 'The writer thinks failure should be made useful rather than worshipped.', 'YES', '["YES","NO","NOT GIVEN"]', '["YES"]', 'Failure claims', 1, 1, 0, 32, 1, 0),
    (9333, 9203, 9108, 33, 'SENTENCE_COMPLETION', 'TEXT', 'The celebration of failure turns disappointment into evidence of _____.', 'courage', NULL, '["courage"]', 'Failure sentences', 1, 1, 1, 33, 1, 0),
    (9334, 9203, 9108, 34, 'SENTENCE_COMPLETION', 'TEXT', 'A bridge model may reveal a weakness in a _____.', 'laboratory', NULL, '["laboratory"]', 'Failure sentences', 1, 1, 1, 34, 1, 0),
    (9335, 9203, 9108, 35, 'SENTENCE_COMPLETION', 'TEXT', 'Poor feedback without guidance may reduce a student''s willingness to _____.', 'experiment', NULL, '["experiment"]', 'Failure sentences', 1, 1, 1, 35, 1, 0),
    (9336, 9203, 9108, 36, 'SENTENCE_COMPLETION', 'TEXT', 'Useful project reviews focus on decisions, assumptions and _____.', 'evidence', NULL, '["evidence"]', 'Failure sentences', 1, 1, 1, 36, 1, 0),
    (9337, 9203, 9108, 37, 'SENTENCE_COMPLETION', 'TEXT', 'The best reviews produce changes in _____.', 'process', NULL, '["process"]', 'Failure sentences', 1, 1, 1, 37, 1, 0),
    (9338, 9203, 9109, 38, 'MULTIPLE_CHOICE_SINGLE', 'SINGLE', 'Where does the passage say the language of productive failure began?', 'C', '["A. medicine","B. advertising","C. engineering","D. sports coaching"]', '["C"]', 'Failure choices', 1, 1, 0, 38, 1, 0),
    (9339, 9203, 9109, 39, 'MULTIPLE_CHOICE_SINGLE', 'SINGLE', 'What does the writer say makes some failure valuable?', 'B', '["A. It is expensive.","B. It is followed by targeted practice.","C. It is kept secret.","D. It happens repeatedly."]', '["B"]', 'Failure choices', 1, 1, 0, 39, 1, 0),
    (9340, 9203, 9109, 40, 'MULTIPLE_CHOICE_SINGLE', 'SINGLE', 'What is the main purpose of the passage?', 'D', '["A. To encourage people to avoid all mistakes","B. To prove failure is always better than success","C. To compare schools with engineering firms","D. To argue for a more analytical view of failure"]', '["D"]', 'Failure choices', 1, 1, 0, 40, 1, 0);

INSERT INTO reading_question_answer_rule (question_id, blank_no, answer_group_no, answer_text, normalized_answer, is_primary, display_order)
SELECT id, 1, 1, correct_answer, LOWER(correct_answer), 1, display_order
FROM reading_question
WHERE id BETWEEN 9301 AND 9340
  AND answer_mode = 'TEXT';

INSERT INTO listening_test (id, title, total_score, created_time, updated_time, is_deleted, timer_mode, prep_seconds, total_seconds, auto_submit, allow_pause, allow_audio_seek)
VALUES
    (9401, 'IELTS-style Academic Listening Practice Test - Services, Tour, Seminar and Lecture', 40, NOW(), NOW(), 0, 'TEST_LEVEL', 30, 2400, 1, 0, 0);

INSERT INTO listening_part_group (id, test_id, part_number, group_number, title, instruction_text, group_guide_text, group_requirement_text, question_type, answer_mode, options_json, accepted_answers_json, answer_rules_json, case_insensitive, ignore_whitespace, ignore_punctuation, question_no_start, question_no_end, display_order, time_limit_seconds, is_deleted)
VALUES
    (9501, 9401, 1, 1, 'Section 1 - Joining a Community Workshop', 'Listen to a conversation between a resident and a workshop coordinator.', 'Complete the form.', 'Write ONE WORD AND/OR A NUMBER.', 'FORM_COMPLETION', 'TEXT', NULL, NULL, NULL, 1, 1, 1, 1, 10, 1, 600, 0),
    (9502, 9401, 2, 1, 'Section 2 - Museum Tour', 'Listen to a guide giving information about a museum visit.', 'Choose the correct answer and label the locations.', 'Choose A, B or C, or write the correct letter.', 'MAP_LABELING', 'SINGLE', '["A","B","C","D","E","F"]', NULL, NULL, 1, 1, 0, 11, 20, 2, 600, 0),
    (9503, 9401, 3, 1, 'Section 3 - Student Seminar Project', 'Listen to two students discussing a seminar project.', 'Choose the correct answers and complete the notes.', 'Choose the correct letter or write ONE WORD ONLY.', 'MULTIPLE_CHOICE_SINGLE', 'TEXT', NULL, NULL, NULL, 1, 1, 1, 21, 30, 3, 600, 0),
    (9504, 9401, 4, 1, 'Section 4 - Urban Heat Lecture', 'Listen to part of a university lecture.', 'Complete the lecture notes.', 'Write ONE WORD ONLY.', 'NOTE_COMPLETION', 'TEXT', NULL, NULL, NULL, 1, 1, 1, 31, 40, 4, 600, 0);

INSERT INTO listening_audio (id, test_id, part_group_id, audio_scope, title, audio_url, audio_object_key, transcript_text, is_deleted, created_time, updated_time)
VALUES
    (9601, 9401, NULL, 'test', 'Full audio - IELTS-style Academic Listening Practice Test', 'https://static.smartielts.local/mock/audio/ielts-style-full-practice-9401.mp3', 'dev/listening/audio/ielts-style-full-practice-9401.mp3', 'Full original IELTS-style listening practice audio transcript covering a workshop booking, museum tour, student seminar discussion and urban heat lecture.', 0, NOW(), NOW()),
    (9602, 9401, 9501, 'part_group', 'Joining a Community Workshop', 'https://static.smartielts.local/mock/audio/ielts-style-9401-section1.mp3', 'dev/listening/audio/ielts-style-9401-section1.mp3', 'Coordinator: The workshop is called Practical Furniture Repair. It runs on Tuesday evenings at 6:30 in Room 14. The fee is 28 pounds, and participants should bring an apron. Resident: My surname is Harris and my phone number ends 7821.', 0, NOW(), NOW()),
    (9603, 9401, 9502, 'part_group', 'Museum Tour', 'https://static.smartielts.local/mock/audio/ielts-style-9401-section2.mp3', 'dev/listening/audio/ielts-style-9401-section2.mp3', 'Guide: The sculpture court is beside the cafe. The education room is on the first floor. The temporary exhibition is near the west entrance. Please meet again in the atrium at 3:15.', 0, NOW(), NOW()),
    (9604, 9401, 9503, 'part_group', 'Student Seminar Project', 'https://static.smartielts.local/mock/audio/ielts-style-9401-section3.mp3', 'dev/listening/audio/ielts-style-9401-section3.mp3', 'Student A: Our seminar should focus on commuter behaviour. Student B: We need a survey and interviews. The tutor liked the comparison with cycling data but asked us to reduce the theory section.', 0, NOW(), NOW()),
    (9605, 9401, 9504, 'part_group', 'Urban Heat Lecture', 'https://static.smartielts.local/mock/audio/ielts-style-9401-section4.mp3', 'dev/listening/audio/ielts-style-9401-section4.mp3', 'Lecturer: Urban heat islands are intensified by dark surfaces, traffic and limited vegetation. Reflective roofs, shade trees and permeable paving can reduce temperatures. Cities also need monitoring stations and public warnings for vulnerable residents.', 0, NOW(), NOW());

INSERT INTO listening_question (id, test_id, part_group_id, section_number, question_number, question_type, answer_mode, question_text, correct_answer, options_json, accepted_answers_json, case_insensitive, ignore_whitespace, ignore_punctuation, display_order, score, is_deleted)
VALUES
    (9701, 9401, 9501, 1, 1, 'FORM_COMPLETION', 'TEXT', 'Workshop title: Practical Furniture _____.', 'Repair', NULL, '["Repair"]', 1, 1, 1, 1, 1, 0),
    (9702, 9401, 9501, 1, 2, 'FORM_COMPLETION', 'TEXT', 'Day: _____.', 'Tuesday', NULL, '["Tuesday"]', 1, 1, 1, 2, 1, 0),
    (9703, 9401, 9501, 1, 3, 'FORM_COMPLETION', 'TEXT', 'Start time: _____.', '6:30', NULL, '["6:30","6.30","six thirty"]', 1, 1, 1, 3, 1, 0),
    (9704, 9401, 9501, 1, 4, 'FORM_COMPLETION', 'TEXT', 'Room number: _____.', '14', NULL, '["14","fourteen"]', 1, 1, 1, 4, 1, 0),
    (9705, 9401, 9501, 1, 5, 'FORM_COMPLETION', 'TEXT', 'Fee: GBP _____.', '28', NULL, '["28","twenty eight","twenty-eight"]', 1, 1, 1, 5, 1, 0),
    (9706, 9401, 9501, 1, 6, 'FORM_COMPLETION', 'TEXT', 'Bring an _____.', 'apron', NULL, '["apron"]', 1, 1, 1, 6, 1, 0),
    (9707, 9401, 9501, 1, 7, 'FORM_COMPLETION', 'TEXT', 'Surname: _____.', 'Harris', NULL, '["Harris"]', 1, 1, 1, 7, 1, 0),
    (9708, 9401, 9501, 1, 8, 'FORM_COMPLETION', 'TEXT', 'Phone ending: _____.', '7821', NULL, '["7821"]', 1, 1, 1, 8, 1, 0),
    (9709, 9401, 9501, 1, 9, 'FORM_COMPLETION', 'TEXT', 'Payment method chosen: _____.', 'card', NULL, '["card"]', 1, 1, 1, 9, 1, 0),
    (9710, 9401, 9501, 1, 10, 'FORM_COMPLETION', 'TEXT', 'Confirmation will be sent by _____.', 'email', NULL, '["email"]', 1, 1, 1, 10, 1, 0),
    (9711, 9401, 9502, 2, 11, 'MULTIPLE_CHOICE_SINGLE', 'SINGLE', 'Where is the sculpture court?', 'B', '["A. opposite the shop","B. beside the cafe","C. behind the theatre"]', '["B"]', 1, 1, 0, 11, 1, 0),
    (9712, 9401, 9502, 2, 12, 'MULTIPLE_CHOICE_SINGLE', 'SINGLE', 'Where is the education room?', 'A', '["A. on the first floor","B. near the river door","C. below the library"]', '["A"]', 1, 1, 0, 12, 1, 0),
    (9713, 9401, 9502, 2, 13, 'MAP_LABELING', 'SINGLE', 'Temporary exhibition location', 'D', '["A","B","C","D","E","F"]', '["D"]', 1, 1, 0, 13, 1, 0),
    (9714, 9401, 9502, 2, 14, 'MAP_LABELING', 'SINGLE', 'Cafe location', 'B', '["A","B","C","D","E","F"]', '["B"]', 1, 1, 0, 14, 1, 0),
    (9715, 9401, 9502, 2, 15, 'MAP_LABELING', 'SINGLE', 'West entrance location', 'F', '["A","B","C","D","E","F"]', '["F"]', 1, 1, 0, 15, 1, 0),
    (9716, 9401, 9502, 2, 16, 'NOTE_COMPLETION', 'TEXT', 'Visitors meet again in the _____.', 'atrium', NULL, '["atrium"]', 1, 1, 1, 16, 1, 0),
    (9717, 9401, 9502, 2, 17, 'NOTE_COMPLETION', 'TEXT', 'Meeting time: _____.', '3:15', NULL, '["3:15","3.15","three fifteen"]', 1, 1, 1, 17, 1, 0),
    (9718, 9401, 9502, 2, 18, 'MULTIPLE_CHOICE_SINGLE', 'SINGLE', 'What does the guide warn visitors not to do?', 'C', '["A. use the lifts","B. visit the cafe","C. touch fragile objects"]', '["C"]', 1, 1, 0, 18, 1, 0),
    (9719, 9401, 9502, 2, 19, 'NOTE_COMPLETION', 'TEXT', 'The museum shop has a discount on _____.', 'posters', NULL, '["posters"]', 1, 1, 1, 19, 1, 0),
    (9720, 9401, 9502, 2, 20, 'NOTE_COMPLETION', 'TEXT', 'The tour ends at the main _____.', 'desk', NULL, '["desk"]', 1, 1, 1, 20, 1, 0),
    (9721, 9401, 9503, 3, 21, 'MULTIPLE_CHOICE_SINGLE', 'SINGLE', 'What topic do the students choose?', 'A', '["A. commuter behaviour","B. museum learning","C. housing design","D. forest recovery"]', '["A"]', 1, 1, 0, 21, 1, 0),
    (9722, 9401, 9503, 3, 22, 'MULTIPLE_CHOICE_SINGLE', 'SINGLE', 'Which method will they definitely use?', 'C', '["A. laboratory testing","B. satellite images","C. a survey","D. archive film"]', '["C"]', 1, 1, 0, 22, 1, 0),
    (9723, 9401, 9503, 3, 23, 'NOTE_COMPLETION', 'TEXT', 'They will also conduct _____.', 'interviews', NULL, '["interviews"]', 1, 1, 1, 23, 1, 0),
    (9724, 9401, 9503, 3, 24, 'NOTE_COMPLETION', 'TEXT', 'The tutor liked the comparison with cycling _____.', 'data', NULL, '["data"]', 1, 1, 1, 24, 1, 0),
    (9725, 9401, 9503, 3, 25, 'NOTE_COMPLETION', 'TEXT', 'They need to reduce the theory _____.', 'section', NULL, '["section"]', 1, 1, 1, 25, 1, 0),
    (9726, 9401, 9503, 3, 26, 'MULTIPLE_CHOICE_SINGLE', 'SINGLE', 'What problem do they mention?', 'B', '["A. too few books","B. a short deadline","C. no supervisor","D. expensive equipment"]', '["B"]', 1, 1, 0, 26, 1, 0),
    (9727, 9401, 9503, 3, 27, 'NOTE_COMPLETION', 'TEXT', 'Their presentation should include a simple _____.', 'chart', NULL, '["chart"]', 1, 1, 1, 27, 1, 0),
    (9728, 9401, 9503, 3, 28, 'NOTE_COMPLETION', 'TEXT', 'They will send the first draft on _____.', 'Friday', NULL, '["Friday"]', 1, 1, 1, 28, 1, 0),
    (9729, 9401, 9503, 3, 29, 'MULTIPLE_CHOICE_SINGLE', 'SINGLE', 'Who will write the introduction?', 'D', '["A. the tutor","B. the whole class","C. neither student","D. Maya"]', '["D"]', 1, 1, 0, 29, 1, 0),
    (9730, 9401, 9503, 3, 30, 'NOTE_COMPLETION', 'TEXT', 'The conclusion should mention policy _____.', 'implications', NULL, '["implications"]', 1, 1, 1, 30, 1, 0),
    (9731, 9401, 9504, 4, 31, 'NOTE_COMPLETION', 'TEXT', 'Urban heat islands are intensified by dark _____.', 'surfaces', NULL, '["surfaces"]', 1, 1, 1, 31, 1, 0),
    (9732, 9401, 9504, 4, 32, 'NOTE_COMPLETION', 'TEXT', 'Traffic is another source of urban _____.', 'heat', NULL, '["heat"]', 1, 1, 1, 32, 1, 0),
    (9733, 9401, 9504, 4, 33, 'NOTE_COMPLETION', 'TEXT', 'Limited _____ makes heat worse.', 'vegetation', NULL, '["vegetation"]', 1, 1, 1, 33, 1, 0),
    (9734, 9401, 9504, 4, 34, 'NOTE_COMPLETION', 'TEXT', 'Reflective roofs can reduce _____.', 'temperatures', NULL, '["temperatures"]', 1, 1, 1, 34, 1, 0),
    (9735, 9401, 9504, 4, 35, 'NOTE_COMPLETION', 'TEXT', 'Shade trees are useful along _____.', 'streets', NULL, '["streets"]', 1, 1, 1, 35, 1, 0),
    (9736, 9401, 9504, 4, 36, 'NOTE_COMPLETION', 'TEXT', 'Permeable paving helps water enter the _____.', 'ground', NULL, '["ground"]', 1, 1, 1, 36, 1, 0),
    (9737, 9401, 9504, 4, 37, 'NOTE_COMPLETION', 'TEXT', 'Cities need monitoring _____.', 'stations', NULL, '["stations"]', 1, 1, 1, 37, 1, 0),
    (9738, 9401, 9504, 4, 38, 'NOTE_COMPLETION', 'TEXT', 'Public warnings should protect vulnerable _____.', 'residents', NULL, '["residents"]', 1, 1, 1, 38, 1, 0),
    (9739, 9401, 9504, 4, 39, 'NOTE_COMPLETION', 'TEXT', 'Cool roofs are most effective on large commercial _____.', 'buildings', NULL, '["buildings"]', 1, 1, 1, 39, 1, 0),
    (9740, 9401, 9504, 4, 40, 'NOTE_COMPLETION', 'TEXT', 'Heat planning should be included in city _____.', 'policy', NULL, '["policy"]', 1, 1, 1, 40, 1, 0);

INSERT INTO writing_question (id, task_type, chart_type, title, description, image_detail_description, prep_seconds, total_seconds, is_deleted, deleted_time, created_time)
VALUES
    (9801, 'TASK1', 'Table', 'IELTS-style Academic Writing Task 1 - City Transport Use', 'The table below shows the percentage of commuters using five transport types in three cities in 2010 and 2025. Summarise the information by selecting and reporting the main features, and make comparisons where relevant. Data: Northport bus 38% to 31%, metro 22% to 34%, car 30% to 24%, bicycle 6% to 8%, walking 4% to 3%; Eastford bus 26% to 28%, metro 18% to 25%, car 42% to 32%, bicycle 9% to 11%, walking 5% to 4%; Lakeside bus 21% to 19%, metro 0% to 16%, car 51% to 40%, bicycle 14% to 18%, walking 14% to 7%. Write at least 150 words.', NULL, 0, 1200, 0, NULL, NOW()),
    (9802, 'TASK2', NULL, 'IELTS-style Academic Writing Task 2 - Public Green Space', 'Some people believe that cities should spend more money creating public green spaces, while others think this money should be used to improve roads and public transport. Discuss both views and give your own opinion. Write at least 250 words.', NULL, 0, 2400, 0, NULL, NOW());

INSERT INTO biz_image_resource (id, target_type, target_id, bucket_type, biz_path, file_url, object_key, original_name, content_type, file_size, width, height, sort_order, created_time, is_deleted)
VALUES
    (9901, 'WRITING_QUESTION', 9801, 'WRITING_QUESTION', 'writing-question-image', 'https://static.smartielts.local/mock/images/ielts-style-task1-city-transport-table.png', 'dev/writing-question/ielts-style-task1-city-transport-table.png', 'ielts-style-task1-city-transport-table.png', 'image/png', 142000, 1200, 720, 1, NOW(), 0),
    (9902, 'LISTENING_PART_GROUP', 9502, 'QUESTION_GROUP_IMAGE', 'question-group-image', 'https://static.smartielts.local/mock/images/ielts-style-museum-map.png', 'dev/question-group-image/ielts-style-museum-map.png', 'ielts-style-museum-map.png', 'image/png', 128000, 1000, 700, 1, NOW(), 0);

COMMIT;

SELECT 'full_ielts_style_seed_complete' AS status,
       (SELECT COUNT(*) FROM reading_question WHERE id BETWEEN 9301 AND 9340) AS reading_questions,
       (SELECT COUNT(*) FROM listening_question WHERE id BETWEEN 9701 AND 9740) AS listening_questions,
       (SELECT COUNT(*) FROM writing_question WHERE id BETWEEN 9801 AND 9802) AS writing_tasks;
