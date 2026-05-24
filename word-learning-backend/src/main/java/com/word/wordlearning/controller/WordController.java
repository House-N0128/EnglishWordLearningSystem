package com.word.wordlearning.controller;

import com.word.wordlearning.dto.Result;
import com.word.wordlearning.entity.Word;
import com.word.wordlearning.mapper.WordBookMapper;
import com.word.wordlearning.mapper.WordMapper;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.io.InputStream;
import org.springframework.web.multipart.MultipartFile;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.WorkbookFactory;

@RestController
@RequestMapping("/api/words")
public class WordController {

    private final WordMapper wordMapper;
    private final WordBookMapper wordBookMapper;

    public WordController(WordMapper wordMapper, WordBookMapper wordBookMapper) {
        this.wordMapper = wordMapper;
        this.wordBookMapper = wordBookMapper;
    }

    @GetMapping
    public Result<List<Word>> list(@RequestParam("wordBookId") String wordBookId) {
        return Result.success(wordMapper.findByWordBookId(wordBookId));
    }

    @GetMapping("/{wordId}")
    public Result<Word> detail(@PathVariable String wordId) {
        Word word = wordMapper.findById(wordId);
        if (word == null) {
            return Result.error(404, "单词不存在");
        }
        return Result.success(word);
    }

    @GetMapping("/search")
    public Result<List<Word>> search(@RequestParam String keyword) {
        return Result.success(wordMapper.search(keyword));
    }

    @PostMapping
    public Result<String> add(@RequestBody Map<String, Object> body) {
        String wordId = (String) body.get("wordId");
        String bookId = (String) body.get("wordBookId");
        String spelling = (String) body.get("englishSpelling");
        String definition = (String) body.get("chineseDefinition");
        String phonetic = body.get("phoneticSymbol") != null ? (String) body.get("phoneticSymbol") : "";
        String partOfSpeech = body.get("partOfSpeech") != null ? (String) body.get("partOfSpeech") : "";
        String example = body.get("exampleSentence") != null ? (String) body.get("exampleSentence") : "";
        String audio = body.get("wordPronunciation") != null ? (String) body.get("wordPronunciation") : "";
        String image = body.get("wordImage") != null ? (String) body.get("wordImage") : "";

        // 自动生成单词ID
        if (wordId == null || wordId.trim().isEmpty()) {
            wordId = generateWordId();
        }
        if (spelling == null || spelling.trim().isEmpty()) {
            return Result.error(400, "缺少英文拼写");
        }
        if (definition == null || definition.trim().isEmpty()) {
            return Result.error(400, "缺少中文释义");
        }

        Word exist = wordMapper.findById(wordId);
        if (exist == null) {
            Word w = new Word();
            w.setWordId(wordId);
            w.setEnglishSpelling(spelling);
            w.setChineseDefinition(definition);
            w.setPhoneticSymbol(phonetic);
            w.setPartOfSpeech(partOfSpeech);
            w.setExampleSentence(example);
            w.setWordPronunciation(audio);
            w.setWordImage(image);
            wordMapper.insert(w);
        }
        
        // 如果提供了词书ID，则建立关联
        if (bookId != null && !bookId.trim().isEmpty()) {
            if (wordMapper.existsBookRef(bookId, wordId) > 0) {
                return Result.error(400, "该单词已在此词书中，无需重复添加");
            }
            wordMapper.insertBookRef(bookId, wordId);
            wordBookMapper.syncWordCount(bookId);
        }
        return Result.success("单词添加成功");
    }

    private String generateWordId() {
        Integer max = wordMapper.maxWordIdNum();
        int next = (max == null) ? 1 : max + 1;
        return "WD" + String.format("%05d", next);
    }

    public Result<String> addLegacy(@RequestBody Map<String, String> body) {
        String spelling = body.get("englishSpelling");
        String definition = body.get("chineseDefinition");
        String bookId = body.get("wordBookId");
        if (spelling == null || definition == null) {
            return Result.error(400, "缺少必填字段");
        }

        // Find existing word by spelling
        Word existBySpelling = wordMapper.findBySpelling(spelling);

        // If adding to a specific book
        if (bookId != null && !bookId.isEmpty()) {
            if (existBySpelling != null) {
                // Word exists - check if already in this book
                if (wordMapper.existsBookRef(bookId, existBySpelling.getWordId()) > 0) {
                    return Result.error(400, "该单词已在此词书中");
                }
                // Word exists but not in this book - add ref
                wordMapper.insertBookRef(bookId, existBySpelling.getWordId());
                wordBookMapper.syncWordCount(bookId);
                return Result.success("已将单词添加到词书");
            }
            // Word doesn't exist - create it and add ref
            String newId = body.get("wordId");
            if (newId == null || newId.isEmpty()) {
                newId = generateWordId();
            }
            Word w = new Word();
            w.setWordId(newId);
            w.setEnglishSpelling(spelling);
            w.setChineseDefinition(definition);
            w.setPhoneticSymbol(body.get("phoneticSymbol") != null ? body.get("phoneticSymbol") : "");
            w.setPartOfSpeech(body.get("partOfSpeech") != null ? body.get("partOfSpeech") : "");
            w.setExampleSentence(body.get("exampleSentence") != null ? body.get("exampleSentence") : "");
            w.setWordPronunciation(body.get("wordPronunciation") != null ? body.get("wordPronunciation") : "");
            w.setWordImage(body.get("wordImage") != null ? body.get("wordImage") : "");
            wordMapper.insert(w);
            wordMapper.insertBookRef(bookId, newId);
            wordBookMapper.syncWordCount(bookId);
            return Result.success("单词已创建并添加到词书");
        }

        // No book specified - just create word (prevent duplicate)
        if (existBySpelling != null) {
            return Result.error(400, "单词已存在: " + spelling);
        }

        String wordId = body.get("wordId");
        if (wordId == null || wordId.isEmpty()) {
            wordId = generateWordId();
        }
        String phonetic = body.get("phoneticSymbol") != null ? body.get("phoneticSymbol") : "";
        String partOfSpeech = body.get("partOfSpeech") != null ? body.get("partOfSpeech") : "";
        String example = body.get("exampleSentence") != null ? body.get("exampleSentence") : "";
        String audio = body.get("wordPronunciation") != null ? body.get("wordPronunciation") : "";
        String image = body.get("wordImage") != null ? body.get("wordImage") : "";

        Word w = new Word();
        w.setWordId(wordId);
        w.setEnglishSpelling(spelling);
        w.setChineseDefinition(definition);
        w.setPhoneticSymbol(phonetic);
        w.setPartOfSpeech(partOfSpeech);
        w.setExampleSentence(example);
        w.setWordPronunciation(audio);
        w.setWordImage(image);
        wordMapper.insert(w);
        return Result.success("单词添加成功");
    }

    @PutMapping("/{wordId}")
    public Result<String> update(@PathVariable String wordId, @RequestBody Word word) {
        word.setWordId(wordId);
        wordMapper.update(word);
        return Result.success("单词修改成功");
    }

    @GetMapping("/all")
    public Result<List<Word>> listAll() {
        return Result.success(wordMapper.findAllWords());
    }

    @PostMapping("/batch")
    public Result<Map<String, Object>> batchAdd(@RequestBody Map<String, Object> body) {
        String bookId = (String) body.get("wordBookId");
        boolean hasBook = bookId != null && !bookId.trim().isEmpty();

        @SuppressWarnings("unchecked")
        List<Map<String, String>> words = (List<Map<String, String>>) body.get("words");
        if (words == null || words.isEmpty()) return Result.error(400, "单词列表为空");

        int success = 0, fail = 0, skipped = 0;
        List<String> skippedWords = new ArrayList<>();
        Integer maxNum = wordMapper.maxWordIdNum();
        int idSeq = (maxNum == null) ? 1 : maxNum + 1;
        for (Map<String, String> w : words) {
            try {
                String spelling = w.get("englishSpelling");
                String definition = w.get("chineseDefinition");
                if (spelling == null || definition == null) { fail++; continue; }

                Word exist = wordMapper.findBySpelling(spelling);
                if (exist != null) { skipped++; skippedWords.add(spelling); continue; }

                String wordId = w.get("wordId");
                if (wordId == null || wordId.isEmpty()) {
                    wordId = "WD" + String.format("%05d", idSeq++);
                }

                Word nw = new Word();
                nw.setWordId(wordId);
                nw.setEnglishSpelling(spelling);
                nw.setChineseDefinition(definition);
                nw.setPhoneticSymbol(w.getOrDefault("phoneticSymbol", ""));
                nw.setExampleSentence(w.getOrDefault("exampleSentence", ""));
                nw.setWordPronunciation(w.getOrDefault("wordPronunciation", ""));
                nw.setWordImage(w.getOrDefault("wordImage", ""));
                wordMapper.insert(nw);
                if (hasBook) wordMapper.insertBookRef(bookId, wordId);
                success++;
            } catch (Exception e) { fail++; }
        }
        if (hasBook) wordBookMapper.syncWordCount(bookId);
        Map<String, Object> result = new HashMap<>();
        result.put("newCount", success);
        result.put("skippedCount", skipped);
        result.put("failCount", fail);
        result.put("skippedWords", skippedWords);
        return Result.success(result);
    }

    // 支持Excel文件上传的批量导入接口
    // - 带 wordBookId：关联到指定词书（三种场景：新增+关联 / 仅关联 / 跳过）
    // - 不带 wordBookId：只导入单词到单词库（已存在的跳过）
    @PostMapping(value = "/batch/import", consumes = "multipart/form-data")
    public Result<Map<String, Object>> batchImport(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "wordBookId", required = false) String wordBookId) {
        if (file.isEmpty()) {
            return Result.error(400, "请选择要导入的文件");
        }

        boolean hasBook = wordBookId != null && !wordBookId.trim().isEmpty();
        int newCount = 0;
        int linkedCount = 0;
        int skippedCount = 0;
        int failCount = 0;
        List<String> skippedWords = new ArrayList<>();

        try (InputStream is = file.getInputStream()) {
            Workbook workbook = WorkbookFactory.create(is);
            Sheet sheet = workbook.getSheetAt(0);

            Integer maxNum = wordMapper.maxWordIdNum();
            int idSeq = (maxNum == null) ? 1 : maxNum + 1;

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                try {
                    String spelling = getCellValueAsString(row.getCell(0));
                    if (spelling == null || spelling.trim().isEmpty()) { failCount++; continue; }
                    spelling = spelling.trim();

                    // part_of_speech (B列)
                    String partOfSpeech = getCellValueAsString(row.getCell(1));

                    // definition (C列)
                    String definition = getCellValueAsString(row.getCell(2));
                    if (definition == null || definition.trim().isEmpty()) { failCount++; continue; }
                    definition = definition.trim();

                    // example_sentence (D列)
                    String exampleSentence = getCellValueAsString(row.getCell(3));

                    // phonetic (E列)
                    String phonetic = getCellValueAsString(row.getCell(4));

                    // pronunciation_url (F列)
                    String pronunciationUrl = getCellValueAsString(row.getCell(5));

                    // image_url (G列)
                    String imageUrl = getCellValueAsString(row.getCell(6));

                    Word exist = wordMapper.findBySpelling(spelling);

                    if (exist == null) {
                        // 单词不存在 → 创建
                        String wordId = "WD" + String.format("%05d", idSeq++);
                        Word w = new Word();
                        w.setWordId(wordId);
                        w.setEnglishSpelling(spelling);
                        w.setChineseDefinition(definition);
                        w.setPartOfSpeech(partOfSpeech != null ? partOfSpeech.trim() : "");
                        w.setExampleSentence(exampleSentence != null ? exampleSentence.trim() : "");
                        w.setPhoneticSymbol(phonetic != null ? phonetic.trim() : "");
                        w.setWordPronunciation(pronunciationUrl != null ? pronunciationUrl.trim() : "");
                        w.setWordImage(imageUrl != null ? imageUrl.trim() : "");
                        wordMapper.insert(w);
                        if (hasBook) wordMapper.insertBookRef(wordBookId, wordId);
                        newCount++;
                    } else if (hasBook) {
                        // 单词存在且有词书 → 检查关联
                        if (wordMapper.existsBookRef(wordBookId, exist.getWordId()) > 0) {
                            skippedCount++;
                            skippedWords.add(spelling);
                        } else {
                            wordMapper.insertBookRef(wordBookId, exist.getWordId());
                            linkedCount++;
                        }
                    } else {
                        // 单词存在且无词书 → 纯导入模式，跳过
                        skippedCount++;
                        skippedWords.add(spelling);
                    }
                } catch (Exception e) {
                    failCount++;
                }
            }

            workbook.close();
            if (hasBook) wordBookMapper.syncWordCount(wordBookId);

        } catch (Exception e) {
            return Result.error(500, "文件解析失败：" + e.getMessage());
        }

        Map<String, Object> result = new HashMap<>();
        result.put("newCount", newCount);
        if (hasBook) result.put("linkedCount", linkedCount);
        result.put("skippedCount", skippedCount);
        result.put("failCount", failCount);
        result.put("skippedWords", skippedWords);

        return Result.success(result);
    }

    // 辅助方法：获取单元格值为字符串
    private String getCellValueAsString(Cell cell) {
        if (cell == null) return null;
        
        CellType cellType = cell.getCellType();
        
        switch (cellType) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                // 尝试获取日期值，如果不是日期则获取数字值
                try {
                    // 先尝试作为日期处理
                    if (cell.getDateCellValue() != null) {
                        return cell.getDateCellValue().toString();
                    }
                } catch (Exception ignored) {
                    // 如果不是日期格式，会抛出异常，忽略即可
                }
                // 处理数字，避免科学计数法
                double numValue = cell.getNumericCellValue();
                if (numValue == Math.floor(numValue) && !Double.isInfinite(numValue)) {
                    return String.valueOf((long) numValue);
                }
                return String.valueOf(numValue);
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                try {
                    return cell.getStringCellValue();
                } catch (Exception e) {
                    try {
                        return String.valueOf(cell.getNumericCellValue());
                    } catch (Exception ex) {
                        return cell.getCellFormula();
                    }
                }
            case BLANK:
                return "";
            default:
                return null;
        }
    }

    @DeleteMapping("/{wordId}")
    public Result<String> delete(@PathVariable String wordId, @RequestParam(required = false) String wordBookId) {
        if (wordBookId != null) {
            // Delete only from this book
            int n = wordMapper.deleteBookRef(wordBookId, wordId);
            if (n > 0) {
                // If no more refs, delete the word itself
                if (wordMapper.countBookRefs(wordId) == 0) {
                    wordMapper.delete(wordId);
                }
                wordBookMapper.syncWordCount(wordBookId);
                return Result.success("已从词书移除");
            }
            return Result.error(404, "未找到关联");
        }
        // Full delete
        wordMapper.delete(wordId);
        return Result.success("单词已删除");
    }
}