-- Apply local assets and fuller reading passages to the full IELTS-style dev seed.
-- Run after scripts/sql/dev_seed_full_ielts_style_exam.sql.

START TRANSACTION;

UPDATE reading_passage
SET content = 'For much of the twentieth century, many city waterfronts were treated as working edges rather than public places. Warehouses, rail lines and fenced docks separated residents from the water, and in several ports the river or harbour became associated with noise, risk and pollution. When manufacturing moved away, large areas were left unused. At first, city leaders saw these sites mainly as real estate opportunities, but early projects often produced isolated apartment blocks with little connection to the rest of the city.

More recent waterfront plans have taken a different approach. In Portland, Rotterdam and a number of Asian port cities, planners have linked new housing to public transport, flood protection and everyday services. Instead of building a single attraction, they have tried to create continuous routes for walking and cycling. A former dock road may become a promenade, while old cranes or warehouses are retained as reminders of industrial history. Supporters argue that such features give redeveloped areas a memory that new construction alone cannot provide.

The most difficult issue is water management. Climate change has made storm surges and heavy rain more common, so waterfront districts must be designed to flood safely. Some schemes use raised ground floors, parks that can temporarily store water, and planted edges that reduce the force of waves. These measures are more expensive than simple walls, but they can create spaces that people enjoy in normal weather. Researchers also note that successful waterfronts require maintenance budgets; without them, public spaces decline quickly and private owners begin to control access.

The social effects of renewal are less visible than new bridges and parks, but they are just as important. A waterfront that attracts restaurants, offices and weekend visitors can raise nearby rents within a few years. Long-term residents may find that the shops they used have been replaced by businesses aimed at tourists. For this reason, several cities now require affordable housing, public seating and local business space as conditions for development approval. These rules are sometimes criticised by developers, but they help prevent the waterfront from becoming an exclusive leisure zone.

Another lesson from recent projects is that small connections matter. A beautiful river path is of limited value if people must cross six lanes of traffic to reach it. Good schemes link the water''s edge with bus stops, schools, markets and residential streets. They also provide lighting, toilets, shaded seating and clear signs, because daily use depends on ordinary comfort rather than spectacular design. In some cities, local groups have been invited to help choose activities for restored warehouses, including workshops, libraries and low-cost studios.

The most successful waterfronts therefore combine engineering, memory and social planning. They do not simply decorate land that industry has abandoned. They turn a former boundary into a shared part of the city, while recognising that access has to be protected long after the opening ceremony.',
    title = 'The Return of the Urban Waterfront'
WHERE id = 9201;

UPDATE reading_passage
SET content = 'A. For decades, conservationists counted wildlife by walking fixed routes and recording what they saw. This method produced valuable records, but it also missed animals that were active at night or hidden high in the canopy. In dense forests, visibility can be so poor that sound becomes a better guide than sight. A forest that appears empty to a visitor at midday may become highly active after sunset, when insects, frogs and birds create a complex acoustic record of life.

B. Early experiments with automatic recorders were not immediately persuasive. The machines collected hundreds of hours of noise, and researchers struggled to separate birds, insects, rain and distant engines. Some biologists argued that the work created more data than insight. They were also concerned that software trained in one region would misidentify species in another, especially when calls varied between local populations.

C. Improvements in battery life, storage and machine learning have changed that view. Small recorders can now remain in the field for weeks, and software can identify repeated patterns in calls. Because the equipment is relatively cheap, researchers can place many devices across a landscape and compare activity between protected and disturbed areas. A single survey team might visit a valley once a month, but a network of recorders can listen through storms, holidays and nights when people are not present.

D. Local knowledge remains essential. In one community forest project, residents recognised seasonal calls that the software labelled as unknown. Their observations helped researchers connect recordings with fruiting trees, hunting pressure and changes in river levels. People who collected forest products also knew when certain paths became noisy with insects or unusually quiet after logging activity. This information turned isolated sound files into evidence that could be interpreted in context.

E. Sound maps are also being used outside traditional wildlife surveys. Ecologists have monitored illegal logging, measured the recovery of burned areas and even studied how traffic noise affects pollinators. A forest that sounds rich is not automatically healthy, however, and scientists caution that acoustic data should be combined with field visits, satellite images and interviews. Sound can reveal activity, but it cannot show whether a river is polluted or whether young trees are surviving.

F. The new tools have made conservation more transparent. Instead of relying only on expert reports, communities can hear changes in their environment and challenge claims that a damaged area is recovering. Recordings can be shared in public meetings, where people who do not read technical documents can still understand that a place has become quieter or more disturbed. The technology is not a replacement for protection, but it gives people a cheaper way to notice when protection is failing.

G. The next challenge is ethical as well as technical. Recorders placed in forests may capture human voices, religious ceremonies or the locations of valuable resources. Conservation groups therefore need clear agreements about where devices are placed, who owns the recordings and how long files are stored. If those questions are ignored, a tool designed to support local stewardship may create suspicion. Used carefully, however, sound maps can make hidden ecological change audible.',
    title = 'Sound Maps of the Forest'
WHERE id = 9202;

UPDATE reading_passage
SET content = 'In business and education, failure is often described as a necessary stage on the path to success. This idea is attractive because it turns disappointment into evidence of courage. Yet the modern celebration of failure is more complicated than it first appears. Not every unsuccessful attempt teaches a useful lesson, and not every learner has the same freedom to recover from mistakes.

The language of productive failure began in engineering, where prototypes are expected to break. A bridge model that collapses in a laboratory may reveal a weakness before real people are at risk. In this context, failure is controlled, observed and documented. The lesson is not simply that the model failed, but why it failed and how the design should change. Engineers do not celebrate the collapse itself; they value the information that careful testing provides.

Problems arise when the same language is applied loosely to schools and workplaces. A student who receives poor feedback without guidance may become less willing to experiment. An employee told to be bold may still be punished if a project loses money. In these cases, organisations praise risk in public while rewarding caution in private. The result is confusion: people are asked to innovate, but they learn that unsuccessful innovation may damage their reputation.

Psychologists distinguish between errors that expose a missing skill and errors caused by careless preparation. The first type can be valuable if followed by targeted practice; the second often signals that basic routines were ignored. Treating both as equally admirable can make failure sound romantic rather than analytical. A musician who plays a difficult passage badly may need focused practice. A musician who forgets the instrument has learned a different lesson about preparation.

Some companies have tried to make learning from failure more systematic. After a project ends, teams hold reviews that focus on decisions, assumptions and evidence rather than blame. The best reviews produce changes in process, such as earlier testing or clearer responsibility. Without such changes, the meeting becomes a ritual and the same mistake returns under a new name. A useful review asks which warning signs were missed, which evidence was weak and which decision rules should change.

Schools face a related challenge. Teachers may want students to experiment, but assessment systems often reward only correct answers. Some educators respond by separating early drafts from final grades. Students can test ideas, receive comments and revise before their work is formally judged. This does not remove standards; it changes the timing of judgement so that mistakes become part of learning rather than proof of inability.

The challenge, then, is to design environments where small mistakes are visible, recoverable and informative. Failure should not be worshipped. It should be made useful. That requires time, honest feedback and a clear difference between intelligent risk and avoidable carelessness.',
    title = 'The Invention of Failure'
WHERE id = 9203;

UPDATE writing_question
SET title = 'IELTS-style Academic Writing Task 1 - Computer Ownership by Education Level',
    chart_type = 'Bar chart',
    description = 'The chart below shows computer ownership by education level in 2002 and 2010. Summarise the information by selecting and reporting the main features, and make comparisons where relevant. Write at least 150 words.'
WHERE id = 9801;

UPDATE biz_image_resource
SET file_url = 'https://smartielts-bucket-writing-task1.oss-cn-hongkong.aliyuncs.com/writing-question-image/29a76c20-7c00-406c-b63b-6cefea3ebae6.png',
    object_key = 'writing-question-image/29a76c20-7c00-406c-b63b-6cefea3ebae6.png',
    original_name = 'comp-ownership-graph.png',
    content_type = 'image/png',
    file_size = 33436,
    width = 658,
    height = 378
WHERE id = 9901
  AND target_type = 'WRITING_QUESTION'
  AND target_id = 9801;

UPDATE listening_audio
SET title = 'Full audio - Provided note test MP3',
    audio_url = 'http://localhost:8080/mock/audio/note_test.mp3',
    audio_object_key = 'dev/listening/audio/note_test.mp3',
    transcript_text = 'Provided local MP3: note_test.mp3'
WHERE id = 9601;

UPDATE listening_audio
SET title = 'Section 1 audio - Provided note intro MP3',
    audio_url = 'http://localhost:8080/mock/audio/note_intro.mp3',
    audio_object_key = 'dev/listening/audio/note_intro.mp3',
    transcript_text = 'Provided local MP3: note_intro.mp3'
WHERE id = 9602;

UPDATE listening_audio
SET title = 'Section 2 audio - Provided maps intro MP3',
    audio_url = 'http://localhost:8080/mock/audio/maps_intro.mp3',
    audio_object_key = 'dev/listening/audio/maps_intro.mp3',
    transcript_text = 'Provided local MP3: maps_intro.mp3'
WHERE id = 9603;

UPDATE listening_audio
SET title = 'Section 3 audio - Provided note test MP3',
    audio_url = 'http://localhost:8080/mock/audio/note_test.mp3',
    audio_object_key = 'dev/listening/audio/note_test.mp3',
    transcript_text = 'Provided local MP3 reused for Section 3: note_test.mp3'
WHERE id = 9604;

UPDATE listening_audio
SET title = 'Section 4 audio - Provided note test MP3',
    audio_url = 'http://localhost:8080/mock/audio/note_test.mp3',
    audio_object_key = 'dev/listening/audio/note_test.mp3',
    transcript_text = 'Provided local MP3 reused for Section 4: note_test.mp3'
WHERE id = 9605;

COMMIT;

SELECT 'full_ielts_style_assets_update_complete' AS status,
       (SELECT file_url FROM biz_image_resource WHERE id = 9901) AS writing_task1_image_url,
       (SELECT COUNT(*) FROM listening_audio WHERE test_id = 9401 AND audio_url LIKE 'http://localhost:8080/mock/audio/%') AS local_audio_rows,
       (SELECT COUNT(*) FROM reading_passage WHERE id BETWEEN 9201 AND 9203 AND CHAR_LENGTH(content) > 2000) AS fuller_reading_passages;
