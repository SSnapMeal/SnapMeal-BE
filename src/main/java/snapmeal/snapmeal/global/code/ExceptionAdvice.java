package snapmeal.snapmeal.global.code;


import snapmeal.snapmeal.global.handler.GeneralException;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import snapmeal.snapmeal.global.ApiResponse;
import snapmeal.snapmeal.web.controller.UserController;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@RestControllerAdvice(annotations = {RestController.class}, basePackageClasses = {UserController.class})
public class ExceptionAdvice extends ResponseEntityExceptionHandler {

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Object> validation(ConstraintViolationException e, WebRequest request) {
        String errorMessage = e.getConstraintViolations().stream()
                .map(violation -> violation.getMessage())
                .findFirst()
                .orElse("UNKNOWN_ERROR");

        ErrorCode errorCode = ErrorCode.valueOf(errorMessage);
        return handleExceptionInternalConstraint(e, errorCode, HttpHeaders.EMPTY, request);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException e,
                                                                  HttpHeaders headers,
                                                                  HttpStatusCode status,
                                                                  WebRequest request) {
        Map<String, String> errors = new LinkedHashMap<>();

        e.getBindingResult().getFieldErrors().forEach(fieldError -> {
            String fieldName = fieldError.getField();
            String errorMessage = Optional.ofNullable(fieldError.getDefaultMessage()).orElse("");
            errors.merge(fieldName, errorMessage, (existing, newMsg) -> existing + ", " + newMsg);
        });

        ErrorCode errorCode = ErrorCode.INVALID_INPUT_VALUE;
        return handleExceptionInternalArgs(e, headers, errorCode, request, errors);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> exception(Exception e, WebRequest request) {
        e.printStackTrace();
        ErrorCode errorCode = ErrorCode.INTERNAL_SERVER_ERROR;
        return handleExceptionInternalFalse(e, errorCode, HttpHeaders.EMPTY, errorCode.getHttpStatus(), request, e.getMessage());
    }

    @ExceptionHandler(GeneralException.class)
    public ResponseEntity<Object> onThrowException(GeneralException generalException, WebRequest request) {
        ErrorResponseDto errorResponse = generalException.getErrorResponseHttpStatus();
        return handleExceptionInternal(generalException, errorResponse, HttpHeaders.EMPTY, request); // ✅ 수정
    }

    private ResponseEntity<Object> handleExceptionInternal(Exception e, ErrorResponseDto reason,
                                                           HttpHeaders headers, WebRequest request) {
        ApiResponse<Object> body = ApiResponse.onFailure(reason.getErrorCode(), reason.getErrorMessage(), null);
        return super.handleExceptionInternal(e, body, headers, reason.getHttpStatus(), request);
    }


    private ResponseEntity<Object> handleExceptionInternalFalse(Exception e, ErrorCode errorCode,
                                                                HttpHeaders headers, HttpStatus status,
                                                                WebRequest request, String errorPoint) {
        ApiResponse<Object> body = ApiResponse.onFailure(errorCode.getErrorCode(), errorCode.getErrorMessage(), errorPoint);
        return super.handleExceptionInternal(e, body, headers, status, request);
    }

    private ResponseEntity<Object> handleExceptionInternalArgs(Exception e, HttpHeaders headers, ErrorCode errorCode,
                                                               WebRequest request, Map<String, String> errorArgs) {
        ApiResponse<Object> body = ApiResponse.onFailure(errorCode.getErrorCode(), errorCode.getErrorMessage(), errorArgs);
        return super.handleExceptionInternal(e, body, headers, errorCode.getHttpStatus(), request);
    }

    private ResponseEntity<Object> handleExceptionInternalConstraint(Exception e, ErrorCode errorCode,
                                                                     HttpHeaders headers, WebRequest request) {
        ApiResponse<Object> body = ApiResponse.onFailure(errorCode.getErrorCode(), errorCode.getErrorMessage(), null);
        return super.handleExceptionInternal(e, body, headers, errorCode.getHttpStatus(), request);
    }
}
