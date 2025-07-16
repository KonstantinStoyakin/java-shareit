package ru.practicum.shareit.exception;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ErrorHandlerTest {

    @InjectMocks
    private ErrorHandler errorHandler;

    @Test
    void handleNotFoundException_shouldReturn404() {
        NotFoundException exception = new NotFoundException("Not found");
        ErrorResponse response = errorHandler.handleNotFoundException(exception);

        assertEquals("Not found", response.getError());
    }

    @Test
    void handleConflictException_shouldReturn409() {
        ConflictException exception = new ConflictException("Conflict");
        ErrorResponse response = errorHandler.handleConflictException(exception);

        assertEquals("Conflict", response.getError());
    }

    @Test
    void handleValidationException_shouldReturn400() {
        ValidationException exception = new ValidationException("Bad request");
        ErrorResponse response = errorHandler.handleValidationException(exception);

        assertEquals("Bad request", response.getError());
    }

    @Test
    void handleMethodArgumentNotValidException_shouldReturn400() {
        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);

        when(exception.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(
                new FieldError("object", "field", "error message")
        ));

        ErrorResponse response = errorHandler.handleMethodArgumentNotValidException(exception);

        assertNotNull(response);
        assertTrue(response.getError().contains("Validation error"));
        assertTrue(response.getError().contains("field: error message"));
    }

    @Test
    void handleMissingRequestHeaderException_shouldReturn400() {
        MissingRequestHeaderException exception = mock(MissingRequestHeaderException.class);
        when(exception.getHeaderName()).thenReturn("X-Header");

        ErrorResponse response = errorHandler.handleMissingRequestHeaderException(exception);

        assertEquals("Required header is missing: X-Header", response.getError());
    }

    @Test
    void handleNullPointerException_shouldReturn500() {
        NullPointerException exception = new NullPointerException("Null reference");
        ErrorResponse response = errorHandler.handleNullPointerException(exception);

        assertTrue(response.getError().contains("Null reference"));
    }

    @Test
    void handleForbiddenException_shouldReturn403() {
        ForbiddenException exception = new ForbiddenException("Forbidden");
        ErrorResponse response = errorHandler.handleForbiddenException(exception);

        assertEquals("Forbidden", response.getError());
    }
}