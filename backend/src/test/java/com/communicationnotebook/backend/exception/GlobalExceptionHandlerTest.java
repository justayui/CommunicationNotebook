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

    @Test
    void handleValidationException_returnsBadRequest() {
        mockMvc.post()
                .uri("/test/validate")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"value\":\"\"}")
                .assertThat()
                .hasStatus(400);
    }

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

    @RestController
    @RequestMapping("/test")
    public static class TestController {

        @org.springframework.web.bind.annotation.GetMapping("/response-status/{status}")
        public void responseStatus(@org.springframework.web.bind.annotation.PathVariable int status) {
            throw new ResponseStatusException(HttpStatus.valueOf(status), "test reason");
        }

        @PostMapping("/validate")
        public void validate(@jakarta.validation.Valid @RequestBody ValidationRequest request) {}

        @org.springframework.web.bind.annotation.GetMapping("/unexpected")
        public void unexpected() {
            throw new RuntimeException("internal secret detail");
        }
    }

    record ValidationRequest(@NotBlank String value) {}
}
