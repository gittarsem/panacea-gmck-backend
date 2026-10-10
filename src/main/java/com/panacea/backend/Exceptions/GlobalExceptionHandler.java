package com.panacea.backend.Exceptions;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;

import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.http.converter.HttpMessageNotReadableException;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authorization.AuthorizationDeniedException;

import org.springframework.web.HttpRequestMethodNotSupportedException;

import org.springframework.web.bind.MethodArgumentNotValidException;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import org.springframework.web.method.annotation.HandlerMethodValidationException;

import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /*
     * ============================================================
     * RESOURCE NOT FOUND
     * ============================================================
     */

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(
            ResourceNotFoundException ex,
            HttpServletRequest request
    ) {

        ErrorResponse response =
                new ErrorResponse(
                        LocalDateTime.now(),
                        HttpStatus.NOT_FOUND.value(),
                        HttpStatus.NOT_FOUND.getReasonPhrase(),
                        ex.getMessage(),
                        request.getRequestURI()
                );

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(response);
    }


    /*
     * ============================================================
     * REQUEST BODY VALIDATION
     *
     * Used with:
     * @Valid @RequestBody
     * ============================================================
     */

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorResponse> handleValidationError(
            MethodArgumentNotValidException ex,
            HttpServletRequest request
    ) {

        Map<String, String> errors =
                new LinkedHashMap<>();

        ex.getBindingResult()
                .getFieldErrors()
                .forEach(error -> {

                    String message =
                            error.getDefaultMessage();

                    if (message == null ||
                            message.isBlank()) {

                        message = "Invalid value";
                    }

                    errors.putIfAbsent(
                            error.getField(),
                            message
                    );
                });

        ValidationErrorResponse response =
                new ValidationErrorResponse(
                        LocalDateTime.now(),
                        HttpStatus.BAD_REQUEST.value(),
                        HttpStatus.BAD_REQUEST.getReasonPhrase(),
                        "Please correct the highlighted fields.",
                        request.getRequestURI(),
                        errors
                );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
    }


    /*
     * ============================================================
     * METHOD LEVEL VALIDATION
     *
     * Spring Framework 7
     *
     * Used for constraints directly on controller/service
     * method parameters.
     *
     * IMPORTANT:
     * Spring 7 uses getParameterValidationResults().
     * ============================================================
     */

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ValidationErrorResponse>
    handleMethodValidationException(
            HandlerMethodValidationException ex,
            HttpServletRequest request
    ) {

        Map<String, String> errors =
                new LinkedHashMap<>();

        ex.getParameterValidationResults()
                .forEach(result -> {

                    String field =
                            result.getMethodParameter()
                                    .getParameterName();

                    if (field == null ||
                            field.isBlank()) {

                        field = "parameter";
                    }

                    String finalField = field;

                    result.getResolvableErrors()
                            .forEach(error -> {

                                String message =
                                        error.getDefaultMessage();

                                if (message == null ||
                                        message.isBlank()) {

                                    message = "Invalid value";
                                }

                                errors.putIfAbsent(
                                        finalField,
                                        message
                                );
                            });
                });

        ValidationErrorResponse response =
                new ValidationErrorResponse(
                        LocalDateTime.now(),
                        HttpStatus.BAD_REQUEST.value(),
                        HttpStatus.BAD_REQUEST.getReasonPhrase(),
                        "Please correct the highlighted fields.",
                        request.getRequestURI(),
                        errors
                );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
    }


    /*
     * ============================================================
     * CONSTRAINT VIOLATION
     *
     * Used for:
     * @RequestParam
     * @PathVariable
     * @Validated
     * ============================================================
     */

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ValidationErrorResponse>
    handleConstraintViolation(
            ConstraintViolationException ex,
            HttpServletRequest request
    ) {

        Map<String, String> errors =
                new LinkedHashMap<>();

        ex.getConstraintViolations()
                .forEach(error -> {

                    String field =
                            error.getPropertyPath()
                                    .toString();

                    String message =
                            error.getMessage();

                    errors.put(
                            field,
                            message
                    );
                });

        ValidationErrorResponse response =
                new ValidationErrorResponse(
                        LocalDateTime.now(),
                        HttpStatus.BAD_REQUEST.value(),
                        HttpStatus.BAD_REQUEST.getReasonPhrase(),
                        "Please correct the highlighted fields.",
                        request.getRequestURI(),
                        errors
                );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
    }


    /*
     * ============================================================
     * MALFORMED JSON / INVALID REQUEST BODY
     * ============================================================
     */

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse>
    handleMessageNotReadable(
            HttpMessageNotReadableException ex,
            HttpServletRequest request
    ) {

        ErrorResponse response =
                new ErrorResponse(
                        LocalDateTime.now(),
                        HttpStatus.BAD_REQUEST.value(),
                        "Invalid Request",
                        "The request body is invalid or malformed.",
                        request.getRequestURI()
                );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
    }


    /*
     * ============================================================
     * ACCESS DENIED
     * ============================================================
     */

    @ExceptionHandler(AuthorizationDeniedException.class)
    public ResponseEntity<ErrorResponse>
    handleAuthorizationDenied(
            AuthorizationDeniedException ex,
            HttpServletRequest request
    ) {

        ErrorResponse response =
                new ErrorResponse(
                        LocalDateTime.now(),
                        HttpStatus.FORBIDDEN.value(),
                        "Forbidden",
                        "You do not have permission to perform this action.",
                        request.getRequestURI()
                );

        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(response);
    }


    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse>
    handleAccessDenied(
            AccessDeniedException ex,
            HttpServletRequest request
    ) {

        ErrorResponse response =
                new ErrorResponse(
                        LocalDateTime.now(),
                        HttpStatus.FORBIDDEN.value(),
                        "Forbidden",
                        "You do not have permission to perform this action.",
                        request.getRequestURI()
                );

        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(response);
    }


    /*
     * ============================================================
     * BAD CREDENTIALS
     * ============================================================
     */

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse>
    handleBadCredentialsException(
            BadCredentialsException ex,
            HttpServletRequest request
    ) {

        ErrorResponse response =
                new ErrorResponse(
                        LocalDateTime.now(),
                        HttpStatus.UNAUTHORIZED.value(),
                        HttpStatus.UNAUTHORIZED.getReasonPhrase(),
                        "Invalid email or password.",
                        request.getRequestURI()
                );

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(response);
    }


    /*
     * ============================================================
     * DATABASE CONSTRAINT VIOLATION
     *
     * Example:
     * - duplicate email
     * - duplicate phone
     * - unique constraint
     * ============================================================
     */

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse>
    handleDataIntegrityViolation(
            DataIntegrityViolationException ex,
            HttpServletRequest request
    ) {

        String message =
                extractDataIntegrityMessage(ex);

        ErrorResponse response =
                new ErrorResponse(
                        LocalDateTime.now(),
                        HttpStatus.CONFLICT.value(),
                        HttpStatus.CONFLICT.getReasonPhrase(),
                        message,
                        request.getRequestURI()
                );

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(response);
    }


    private String extractDataIntegrityMessage(
            DataIntegrityViolationException ex
    ) {

        Throwable cause =
                ex.getMostSpecificCause();

        String message =
                cause != null
                        ? cause.getMessage()
                        : null;

        if (message == null ||
                message.isBlank()) {

            return "The submitted information conflicts with existing data.";
        }

        String lower =
                message.toLowerCase();

        if (lower.contains("email")) {
            return "An account with this email already exists.";
        }

        if (lower.contains("phone")) {
            return "An account with this phone number already exists.";
        }

        if (lower.contains("username")) {
            return "This username is already in use.";
        }

        return "The submitted information conflicts with existing data.";
    }


    /*
     * ============================================================
     * GENERAL DATABASE ERROR
     * ============================================================
     */

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ErrorResponse>
    handleDataAccessException(
            DataAccessException ex,
            HttpServletRequest request
    ) {

        log.error(
                "Database error on {}",
                request.getRequestURI(),
                ex
        );

        ErrorResponse response =
                new ErrorResponse(
                        LocalDateTime.now(),
                        HttpStatus.SERVICE_UNAVAILABLE.value(),
                        "Database Error",
                        "The service is temporarily unable to access the database. Please try again later.",
                        request.getRequestURI()
                );

        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(response);
    }


    /*
     * ============================================================
     * ILLEGAL ARGUMENT
     * ============================================================
     */

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse>
    handleIllegalArgumentException(
            IllegalArgumentException ex,
            HttpServletRequest request
    ) {

        ErrorResponse response =
                new ErrorResponse(
                        LocalDateTime.now(),
                        HttpStatus.BAD_REQUEST.value(),
                        HttpStatus.BAD_REQUEST.getReasonPhrase(),
                        ex.getMessage(),
                        request.getRequestURI()
                );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
    }


    /*
     * ============================================================
     * BUSINESS RULE VIOLATION
     * ============================================================
     */

    @ExceptionHandler(BusinessRuleViolationException.class)
    public ResponseEntity<ErrorResponse>
    handleBusinessRuleViolation(
            BusinessRuleViolationException ex,
            HttpServletRequest request
    ) {

        ErrorResponse response =
                new ErrorResponse(
                        LocalDateTime.now(),
                        HttpStatus.CONFLICT.value(),
                        HttpStatus.CONFLICT.getReasonPhrase(),
                        ex.getMessage(),
                        request.getRequestURI()
                );

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(response);
    }


    /*
     * ============================================================
     * HTTP METHOD NOT SUPPORTED
     * ============================================================
     */

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse>
    handleMethodNotSupported(
            HttpRequestMethodNotSupportedException ex,
            HttpServletRequest request
    ) {

        String message =
                "The HTTP method "
                        + request.getMethod()
                        + " is not supported for this endpoint.";

        ErrorResponse response =
                new ErrorResponse(
                        LocalDateTime.now(),
                        HttpStatus.METHOD_NOT_ALLOWED.value(),
                        HttpStatus.METHOD_NOT_ALLOWED.getReasonPhrase(),
                        message,
                        request.getRequestURI()
                );

        return ResponseEntity
                .status(HttpStatus.METHOD_NOT_ALLOWED)
                .body(response);
    }


    /*
     * ============================================================
     * STATIC / UNKNOWN RESOURCE
     * ============================================================
     */

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse>
    handleNoResourceFound(
            NoResourceFoundException ex,
            HttpServletRequest request
    ) {

        ErrorResponse response =
                new ErrorResponse(
                        LocalDateTime.now(),
                        HttpStatus.NOT_FOUND.value(),
                        HttpStatus.NOT_FOUND.getReasonPhrase(),
                        "The requested resource was not found.",
                        request.getRequestURI()
                );

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(response);
    }


    /*
     * ============================================================
     * UNEXPECTED ERROR
     * ============================================================
     */

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse>
    handleGenericException(
            Exception ex,
            HttpServletRequest request
    ) {

        log.error(
                "Unhandled exception occurred on {}",
                request.getRequestURI(),
                ex
        );

        ErrorResponse response =
                new ErrorResponse(
                        LocalDateTime.now(),
                        HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
                        "An unexpected error occurred. Please try again later.",
                        request.getRequestURI()
                );

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(response);
    }
}