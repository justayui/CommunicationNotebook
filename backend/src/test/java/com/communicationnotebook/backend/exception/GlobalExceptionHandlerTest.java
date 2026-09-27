package com.communicationnotebook.backend.exception;

import jakarta.validation.constraints.NotBlank;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@WebMvcTest(GlobalExceptionHandlerTest.TestController.class)
@Import({GlobalExceptionHandler.class, GlobalExceptionHandlerTest.TestController.class})
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvcTester mockMvc;

    //アプリケーションが意図的にthrowする例外処理に関するテスト
    @Test
    void handleResponseStatusException_returnsSameStatus() {
        mockMvc.get()
                .uri("/test/response-status/404")
                .assertThat()
                .hasStatus(404)
                .bodyJson()
                .extractingPath("$.status")
                .isEqualTo(404);
    }

    @Test
    void handleResponseStatusException_returnsSameStatus_forForbidden() {
        mockMvc.get().uri("/test/response-status/403").assertThat().hasStatus(403);
    }

    //入力チェック関連の例外処理に関するテスト
    @Test
    void handleValidationException_returnsBadRequest() {
        mockMvc.post()
                .uri("/test/validate")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"value\":\"\"}")
                .assertThat()
                .hasStatus(400);
    }

    //リクエストの型変換エラー関連の例外処理に関するテスト
    @Test
    void handleTypeMismatchException_returnsBadRequest() {
        mockMvc.get()
                .uri("/test/type-mismatch?value=not-a-number")
                .assertThat()
                .hasStatus(400)
                .bodyJson()
                .extractingPath("$.message")
                .isEqualTo("パラメータの型が不正です");
    }

    //JSONエラーによる例外処理に関するテスト
    @Test
    void handleMessageNotReadableException_returnsBadRequest() {
        mockMvc.post()
                .uri("/test/validate")
                .contentType(MediaType.APPLICATION_JSON)
                .content("not json")
                .assertThat()
                .hasStatus(400)
                .bodyJson()
                .extractingPath("$.message")
                .isEqualTo("リクエストの形式が不正です");
    }

    //その他の予期しない例外の処理に関するテスト
    @Test
    void handleUnexpectedException_returnsInternalServerError_andDoesNotLeakMessage() {
        mockMvc.get()
                .uri("/test/unexpected")
                .assertThat()
                .hasStatus(500)
                .bodyJson()
                .extractingPath("$.message")
                .isEqualTo("サーバーエラーが発生しました");
    }

    //意図的に例外を起こすテスト専用のコントローラーです
    @RestController
    @RequestMapping("/test")
    public static class TestController {

        @org.springframework.web.bind.annotation.GetMapping("/response-status/{status}")
        public void responseStatus(@org.springframework.web.bind.annotation.PathVariable int status) {
            throw new ResponseStatusException(HttpStatus.valueOf(status), "test reason");
        }

        @PostMapping("/validate")
        public void validate(@jakarta.validation.Valid @RequestBody ValidationRequest request) {}

        @org.springframework.web.bind.annotation.GetMapping("/type-mismatch")
        public void typeMismatch(
                @org.springframework.web.bind.annotation.RequestParam int value) {}

        @org.springframework.web.bind.annotation.GetMapping("/unexpected")
        public void unexpected() {
            throw new RuntimeException("internal secret detail");
        }
    }

    //入力チェックエラーを起こすためのテスト用リクエストです
    record ValidationRequest(@NotBlank String value) {}
}
