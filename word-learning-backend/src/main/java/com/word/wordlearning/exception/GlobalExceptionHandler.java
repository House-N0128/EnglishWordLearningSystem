// 文件路径: d:\Projects\EnglishWordLearningSystem\word-learning-backend\src\main\java\com\word\wordlearning\exception\GlobalExceptionHandler.java

package com.word.wordlearning.exception;

import com.word.wordlearning.dto.Result;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Result<String>> handleException(Exception e) {
        // 打印详细错误日志到控制台
        e.printStackTrace();
        // 返回详细错误信息给前端
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Result.error(500, e.getMessage()));
    }
}
