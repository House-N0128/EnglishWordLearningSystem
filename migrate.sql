-- 0. 先删外键约束
ALTER TABLE t_collection DROP FOREIGN KEY t_collection_ibfk_2;
ALTER TABLE t_word_learning_record DROP FOREIGN KEY t_word_learning_record_ibfk_3;

-- 1. 创建新单词表（拼写唯一）
CREATE TABLE t_word_new (
    word_id VARCHAR(50) PRIMARY KEY,
    spelling VARCHAR(100) NOT NULL UNIQUE,
    definition TEXT NOT NULL,
    phonetic VARCHAR(100) NOT NULL,
    pronunciation_url VARCHAR(255) DEFAULT '',
    image_url VARCHAR(255) DEFAULT '',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
) DEFAULT CHARSET=utf8mb4;

-- 2. 创建词书-单词关联表
CREATE TABLE t_word_book_ref (
    id INT PRIMARY KEY AUTO_INCREMENT,
    word_book_id VARCHAR(50) NOT NULL,
    word_id VARCHAR(50) NOT NULL,
    sort_order INT DEFAULT 0,
    UNIQUE KEY uk_book_word (word_book_id, word_id),
    INDEX idx_word (word_id)
) DEFAULT CHARSET=utf8mb4;

-- 3. 迁移单词本体（按拼写去重，取最早创建的ID）
INSERT INTO t_word_new (word_id, spelling, definition, phonetic, pronunciation_url, image_url, created_at)
SELECT MIN(wordId), englishSpelling, MAX(chineseDefinition), MAX(phoneticSymbol),
       MAX(WordPronunciation), MAX(wordImage), MIN(createTime)
FROM t_word
GROUP BY englishSpelling;

-- 4. 迁移词书关联
INSERT INTO t_word_book_ref (word_book_id, word_id)
SELECT w.wordBookId, nw.word_id
FROM t_word w
JOIN t_word_new nw ON w.englishSpelling = nw.spelling;

-- 5. 更新引用表：t_collection 和 t_learning_record 中的旧wordId映射为新word_id
UPDATE t_collection c
JOIN t_word w ON c.wordId = w.wordId
JOIN t_word_new nw ON w.englishSpelling = nw.spelling
SET c.wordId = nw.word_id;

UPDATE t_word_learning_record r
JOIN t_word w ON r.wordId = w.wordId
JOIN t_word_new nw ON w.englishSpelling = nw.spelling
SET r.wordId = nw.word_id;

-- 6. 删除旧表并重命名
DROP TABLE t_word;
RENAME TABLE t_word_new TO t_word;
