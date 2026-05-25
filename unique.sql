SELECT 
    CONSTRAINT_NAME, 
    DELETE_RULE 
FROM information_schema.REFERENTIAL_CONSTRAINTS 
WHERE TABLE_NAME = 't_word_book_ref' 
  AND CONSTRAINT_SCHEMA = DATABASE();
  
DELETE FROM t_word_book_ref 
WHERE word_id NOT IN (SELECT word_id FROM t_word);

-- ALTER TABLE t_word_book_ref DROP FOREIGN KEY fk_existing_name;

ALTER TABLE t_word_book_ref 
ADD CONSTRAINT fk_word_ref_word 
FOREIGN KEY (word_id) REFERENCES t_word(word_id) 
ON DELETE CASCADE;
