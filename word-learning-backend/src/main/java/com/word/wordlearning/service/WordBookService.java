package com.word.wordlearning.service;

import com.word.wordlearning.entity.WordBook;
import com.word.wordlearning.mapper.WordBookMapper;
import com.word.wordlearning.mapper.WordMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WordBookService {

    private final WordBookMapper wordBookMapper;
    private final WordMapper wordMapper;

    public WordBookService(WordBookMapper wordBookMapper, WordMapper wordMapper) {
        this.wordBookMapper = wordBookMapper;
        this.wordMapper = wordMapper;
    }

    public List<WordBook> listAvailable() {
        List<WordBook> books = wordBookMapper.findAll();
        for (WordBook book : books) {
            book.setWordCount(wordBookMapper.countWordsInBook(book.getWordBookId()));
        }
        return books;
    }

    public List<WordBook> listAll() {
        List<WordBook> books = wordBookMapper.findAll();
        for (WordBook book : books) {
            book.setWordCount(wordBookMapper.countWordsInBook(book.getWordBookId()));
        }
        return books;
    }

    public void add(WordBook book) {
        wordBookMapper.insert(book);
    }

    public void update(WordBook book) {
        wordBookMapper.update(book);
    }

    public void delete(String wordBookId) {
        wordMapper.deleteAllBookRefsByBookId(wordBookId);
        wordBookMapper.hardDelete(wordBookId);
    }
}
