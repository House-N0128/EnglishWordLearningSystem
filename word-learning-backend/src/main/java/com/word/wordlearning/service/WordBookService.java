package com.word.wordlearning.service;

import com.word.wordlearning.entity.WordBook;
import com.word.wordlearning.mapper.WordBookMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WordBookService {

    private final WordBookMapper wordBookMapper;

    public WordBookService(WordBookMapper wordBookMapper) {
        this.wordBookMapper = wordBookMapper;
    }

    public List<WordBook> listAvailable() {
        List<WordBook> books = wordBookMapper.findAll();
        for (WordBook book : books) {
            book.setWordCount(wordBookMapper.countWordsInBook(book.getWordBookId()));
        }
        return books;
    }

    public List<WordBook> listAll() {
        // Admin: all books including 未上架
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
        wordBookMapper.delete(wordBookId);
    }
}
