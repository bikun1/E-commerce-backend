package com.example.demo4.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

        private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

        // Handle Resource Not Found
        @ExceptionHandler(ResourceNotFoundException.class)
        public ResponseEntity<ErrorResponse> handleResourceNotFoundException(
                        ResourceNotFoundException ex, HttpServletRequest request) {
                logger.error("ResourceNotFoundException: {}", ex.getMessage());

                ErrorResponse errorResponse = new ErrorResponse(
                                LocalDateTime.now(),
                                HttpStatus.NOT_FOUND.value(),
                                "Not Found",
                                ex.getMessage(),
                                request.getRequestURI());

                return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
        }

        // Handle Bad Request
        @ExceptionHandler(BadRequestException.class)
        public ResponseEntity<ErrorResponse> handleBadRequestException(
                        BadRequestException ex, HttpServletRequest request) {
                logger.error("BadRequestException: {}", ex.getMessage());

                ErrorResponse errorResponse = new ErrorResponse(
                                LocalDateTime.now(),
                                HttpStatus.BAD_REQUEST.value(),
                                "Bad Request",
                                ex.getMessage(),
                                request.getRequestURI());

                return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }

        // Handle Validation Errors
        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<ErrorResponse> handleValidationExceptions(
                        MethodArgumentNotValidException ex, HttpServletRequest request) {
                logger.error("Validation error: {}", ex.getMessage());

                List<String> details = ex.getBindingResult()
                                .getFieldErrors()
                                .stream()
                                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                                .collect(Collectors.toList());

                ErrorResponse errorResponse = new ErrorResponse(
                                LocalDateTime.now(),
                                HttpStatus.BAD_REQUEST.value(),
                                "Validation Failed",
                                "Input validation error",
                                request.getRequestURI(),
                                details);

                return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }

        // Handle malformed/missing request body (e.g. empty body, invalid JSON)
        @ExceptionHandler(HttpMessageNotReadableException.class)
        public ResponseEntity<ErrorResponse> handleHttpMessageNotReadable(
                        HttpMessageNotReadableException ex, HttpServletRequest request) {
                logger.error("HttpMessageNotReadableException: {}", ex.getMessage());

                String message = ex.getMessage() != null && ex.getMessage().contains("Required request body is missing")
                                ? "Request body is required"
                                : "Malformed JSON or invalid request body";

                ErrorResponse errorResponse = new ErrorResponse(
                                LocalDateTime.now(),
                                HttpStatus.BAD_REQUEST.value(),
                                "Bad Request",
                                message,
                                request.getRequestURI());

                return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }

        // Handle Unauthorized
        @ExceptionHandler(UnauthorizedException.class)
        public ResponseEntity<ErrorResponse> handleUnauthorizedException(
                        UnauthorizedException ex, HttpServletRequest request) {
                logger.error("UnauthorizedException: {}", ex.getMessage());

                ErrorResponse errorResponse = new ErrorResponse(
                                LocalDateTime.now(),
                                HttpStatus.UNAUTHORIZED.value(),
                                "Unauthorized",
                                ex.getMessage(),
                                request.getRequestURI());

                return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
        }

        // Handle Forbidden
        @ExceptionHandler(AccessDeniedException.class)
        public ResponseEntity<ErrorResponse> handleForbiddenException(
                        AccessDeniedException ex, HttpServletRequest request) {
                logger.error("ForbiddenException: {}", ex.getMessage());

                ErrorResponse errorResponse = new ErrorResponse(
                                LocalDateTime.now(),
                                HttpStatus.FORBIDDEN.value(),
                                "Forbidden",
                                "You don't have permission to access this resource",
                                request.getRequestURI());

                return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
        }

        // Handle Conflict
        @ExceptionHandler(ConflictException.class)
        public ResponseEntity<ErrorResponse> handleConflictException(
                        ConflictException ex, HttpServletRequest request) {
                logger.error("ConflictException: {}", ex.getMessage());

                ErrorResponse errorResponse = new ErrorResponse(
                                LocalDateTime.now(),
                                HttpStatus.CONFLICT.value(),
                                "Conflict",
                                ex.getMessage(),
                                request.getRequestURI());

                return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
        }

        // Handle Authentication Exceptions
        @ExceptionHandler(AuthenticationException.class)
        public ResponseEntity<ErrorResponse> handleAuthenticationException(
                        AuthenticationException ex, HttpServletRequest request) {
                logger.error("AuthenticationException: {}", ex.getMessage());

                ErrorResponse errorResponse = new ErrorResponse(
                                LocalDateTime.now(),
                                HttpStatus.UNAUTHORIZED.value(),
                                "Authentication Failed",
                                ex.getMessage(),
                                request.getRequestURI());

                return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
        }

        // Handle Bad Credentials
        @ExceptionHandler(BadCredentialsException.class)
        public ResponseEntity<ErrorResponse> handleBadCredentialsException(
                        BadCredentialsException ex, HttpServletRequest request) {
                logger.error("BadCredentialsException: {}", ex.getMessage());

                ErrorResponse errorResponse = new ErrorResponse(
                                LocalDateTime.now(),
                                HttpStatus.UNAUTHORIZED.value(),
                                "Invalid Credentials",
                                "Invalid username or password",
                                request.getRequestURI());

                return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
        }

        // Handle Username Not Found
        @ExceptionHandler(UsernameNotFoundException.class)
        public ResponseEntity<ErrorResponse> handleUsernameNotFoundException(
                        UsernameNotFoundException ex, HttpServletRequest request) {
                logger.error("UsernameNotFoundException: {}", ex.getMessage());

                ErrorResponse errorResponse = new ErrorResponse(
                                LocalDateTime.now(),
                                HttpStatus.UNAUTHORIZED.value(),
                                "User Not Found",
                                "Invalid username or password",
                                request.getRequestURI());

                return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
        }

        // Handle Disabled Account
        @ExceptionHandler(DisabledException.class)
        public ResponseEntity<ErrorResponse> handleDisabledException(
                        DisabledException ex, HttpServletRequest request) {
                logger.error("DisabledException: {}", ex.getMessage());

                ErrorResponse errorResponse = new ErrorResponse(
                                LocalDateTime.now(),
                                HttpStatus.FORBIDDEN.value(),
                                "Account Disabled",
                                "Your account has been disabled",
                                request.getRequestURI());

                return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
        }

        // Handle Locked Account
        @ExceptionHandler(LockedException.class)
        public ResponseEntity<ErrorResponse> handleLockedException(
                        LockedException ex, HttpServletRequest request) {
                logger.error("LockedException: {}", ex.getMessage());

                ErrorResponse errorResponse = new ErrorResponse(
                                LocalDateTime.now(),
                                HttpStatus.FORBIDDEN.value(),
                                "Account Locked",
                                "Your account has been locked",
                                request.getRequestURI());

                return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
        }

        // Handle Token Refresh Exception
        @ExceptionHandler(TokenRefreshException.class)
        public ResponseEntity<ErrorResponse> handleTokenRefreshException(
                        TokenRefreshException ex, HttpServletRequest request) {
                logger.error("TokenRefreshException: {}", ex.getMessage());

                ErrorResponse errorResponse = new ErrorResponse(
                                LocalDateTime.now(),
                                HttpStatus.FORBIDDEN.value(),
                                "Token Refresh Failed",
                                ex.getMessage(),
                                request.getRequestURI());

                return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
        }

        // Handle Type Mismatch
        @ExceptionHandler(MethodArgumentTypeMismatchException.class)
        public ResponseEntity<ErrorResponse> handleTypeMismatch(
                        MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
                logger.error("MethodArgumentTypeMismatchException: {}", ex.getMessage());

                String message = String.format("Parameter '%s' should be of type %s",
                                ex.getName(),
                                ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown");

                ErrorResponse errorResponse = new ErrorResponse(
                                LocalDateTime.now(),
                                HttpStatus.BAD_REQUEST.value(),
                                "Type Mismatch",
                                message,
                                request.getRequestURI());

                return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }

        // Handle Illegal Argument
        @ExceptionHandler(IllegalArgumentException.class)
        public ResponseEntity<ErrorResponse> handleIllegalArgumentException(
                        IllegalArgumentException ex, HttpServletRequest request) {
                logger.error("IllegalArgumentException: {}", ex.getMessage());

                ErrorResponse errorResponse = new ErrorResponse(
                                LocalDateTime.now(),
                                HttpStatus.BAD_REQUEST.value(),
                                "Invalid Argument",
                                ex.getMessage(),
                                request.getRequestURI());

                return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }

        // Handle Null Pointer Exception
        @ExceptionHandler(NullPointerException.class)
        public ResponseEntity<ErrorResponse> handleNullPointerException(
                        NullPointerException ex, HttpServletRequest request) {
                logger.error("NullPointerException: ", ex);

                ErrorResponse errorResponse = new ErrorResponse(
                                LocalDateTime.now(),
                                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                                "Internal Server Error",
                                "An unexpected error occurred",
                                request.getRequestURI());

                return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }

        // Handle all other exceptions
        @ExceptionHandler(Exception.class)
        public ResponseEntity<ErrorResponse> handleGlobalException(
                        Exception ex, HttpServletRequest request) {
                logger.error("Unexpected error: ", ex);

                ErrorResponse errorResponse = new ErrorResponse(
                                LocalDateTime.now(),
                                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                                "Internal Server Error",
                                "An unexpected error occurred. Please try again later.",
                                request.getRequestURI());

                return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
}