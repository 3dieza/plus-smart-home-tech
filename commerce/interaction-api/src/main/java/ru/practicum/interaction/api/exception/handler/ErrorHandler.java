package ru.practicum.interaction.api.exception.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.practicum.interaction.api.exception.BadRequestException;
import ru.practicum.interaction.api.exception.NoSpecifiedProductInWarehouseException;
import ru.practicum.interaction.api.exception.NotAuthorizedUserException;
import ru.practicum.interaction.api.exception.NotFoundException;
import ru.practicum.interaction.api.exception.ProductNotFoundException;
import ru.practicum.interaction.api.exception.SpecifiedProductAlreadyInWarehouseException;

@Slf4j
@RestControllerAdvice
public class ErrorHandler {

    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<ApiError> handleProductNotFoundException(Exception ex) {
        log.error("ProductNotFoundException: {}", ex.getMessage(), ex);
        return buildResponse("Товар не найден. Пожалуйста, проверьте запрос.", HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(NotAuthorizedUserException.class)
    public ResponseEntity<ApiError> handleNotAuthorizedUserException(Exception ex) {
        log.error("NotAuthorizedUserException: {}", ex.getMessage(), ex);
        return buildResponse("Пользователь не указан. Пожалуйста, проверьте запрос.", HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ApiError> handleNotFoundException(Exception ex) {
        log.error("NotFoundException: {}", ex.getMessage(), ex);
        return buildResponse("Ресурс не найден.", HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(SpecifiedProductAlreadyInWarehouseException.class)
    public ResponseEntity<ApiError> handleSpecifiedProductAlreadyInWarehouseException(Exception ex) {
        log.error("SpecifiedProductAlreadyInWarehouseException: {}", ex.getMessage(), ex);
        return buildResponse("Товар с таким описанием уже зарегистрирован на складе", HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(NoSpecifiedProductInWarehouseException.class)
    public ResponseEntity<ApiError> handleNoSpecifiedProductInWarehouseException(Exception ex) {
        log.error("NoSpecifiedProductInWarehouseException: {}", ex.getMessage(), ex);
        return buildResponse("Нет информации о товаре на складе", HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ApiError> handleBadRequestException(Exception ex) {
        log.error("BadRequestException: {}", ex.getMessage(), ex);
        return buildResponse("Некорректный запрос.", HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleAll(Exception ex) {
        log.error("Необработанное исключение: {}", ex.getMessage(), ex);
        return buildResponse("Внутренняя ошибка сервера.", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private ResponseEntity<ApiError> buildResponse(String userMessage, HttpStatus status) {
        ApiError response = ApiError.builder()
                .userMessage(userMessage)
                .httpStatus(status.toString())
                .build();
        return new ResponseEntity<>(response, status);
    }
}