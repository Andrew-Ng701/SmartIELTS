UPDATE reading_part_group
SET question_type = 'MATCHING'
WHERE question_type IN ('HEADING_MATCHING', 'MATCHING_HEADINGS');

UPDATE reading_question
SET question_type = 'MATCHING'
WHERE question_type IN ('HEADING_MATCHING', 'MATCHING_HEADINGS');
