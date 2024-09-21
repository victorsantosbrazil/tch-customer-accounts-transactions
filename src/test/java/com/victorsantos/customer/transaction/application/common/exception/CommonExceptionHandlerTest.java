package com.victorsantos.customer.transaction.application.common.exception;

import static com.victorsantos.customer.transaction.application.common.exception.CommonExceptionHandlerTestController.TEST_EXCEPTIONS_PATH;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.victorsantos.customer.transaction.application.common.dto.ErrorResponse;
import com.victorsantos.customer.transaction.application.common.dto.ValidationErrorResponse;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@WebMvcTest(CommonExceptionHandlerTestController.class)
@ContextConfiguration(classes = {CommonExceptionHandlerTestController.class, CommonExceptionHandler.class})
class CommonExceptionHandlerTest {

    private MockMvc mockMvc;

    @Autowired
    private CommonExceptionHandlerTestController exceptionHandlerTestController;

    @Autowired
    private CommonExceptionHandler commonExceptionHandler;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    public void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(exceptionHandlerTestController)
                .setControllerAdvice(commonExceptionHandler)
                .build();
    }

    @Test
    @DisplayName("should handle api exceptions")
    void testHandleApiException() throws Exception {
        int statusCode = 400;
        String message = "Internal server error";
        ErrorResponse response = new ErrorResponse("error", message, message);
        var responseJson = objectMapper.writeValueAsString(response);

        var path = String.format("%s/%s/%s", TEST_EXCEPTIONS_PATH, "/api-exception", statusCode);

        mockMvc.perform(post(path).content(responseJson).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is(statusCode))
                .andExpect(content().json(responseJson));
    }

    @Test
    @DisplayName("should handle method argument not valid exceptions")
    void testHandleMethodArgumentNotValidException() throws Exception {
        String type = "validation_error";
        String title = "Validation error";
        String detail = "One or more fields are invalid";
        var errors = Map.of("name", "must not be empty");

        ValidationErrorResponse response = new ValidationErrorResponse(type, title, detail, errors);
        var responseJson = objectMapper.writeValueAsString(response);

        var path = String.format("%s/%s", TEST_EXCEPTIONS_PATH, "/method-argument-not-valid");
        mockMvc.perform(post(path)
                        .content(objectMapper.writeValueAsString(errors))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is(400))
                .andExpect(content().json(responseJson));
    }

    @Test
    @DisplayName("should handle unexpected exceptions")
    void testHandleUnexpectedException() throws Exception {
        String message = "Internal server error";
        ErrorResponse response = new ErrorResponse("internal_server_error", message, message);
        var responseJson = objectMapper.writeValueAsString(response);

        mockMvc.perform(get(TEST_EXCEPTIONS_PATH + "/unexpected"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().json(responseJson));
    }
}
