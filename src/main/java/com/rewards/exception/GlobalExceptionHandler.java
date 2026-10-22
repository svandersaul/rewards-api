package com.rewards.exception;

import org.springframework.beans.TypeMismatchException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * Centralizes exception handling across the REST API and converts domain errors into consistent HTTP responses.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Generates a 404 response for missing customers.
     *
     * @param exception exception containing the missing customer ID
     * @return HTTP 404 response with an error message
     */
    @ExceptionHandler(CustomerNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleCustomerNotFound(
            CustomerNotFoundException exception) {
        ErrorResponse error = new ErrorResponse(HttpStatus.NOT_FOUND, "Customer Not Found", exception.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    /**
     * Generates a 400 response when a customer ID path variable is not numeric.
     *
     * @param exception type mismatch exception raised by Spring when binding arguments
     * @return HTTP 400 response with an error message
     */
    @ExceptionHandler(TypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(TypeMismatchException exception) {
        ErrorResponse error = new ErrorResponse(HttpStatus.BAD_REQUEST, "Bad Request", "Customer ID must be a number");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(NoResourceFoundException exception) {
        ErrorResponse error = new ErrorResponse(HttpStatus.NOT_FOUND, "Not Found", "Resource not found");
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoHandlerFound(NoHandlerFoundException exception) {
        ErrorResponse error = new ErrorResponse(HttpStatus.NOT_FOUND, "Not Found", "Resource not found");
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMethodNotSupported(
            HttpRequestMethodNotSupportedException exception) {
        ErrorResponse error = new ErrorResponse(
                HttpStatus.METHOD_NOT_ALLOWED, "Method Not Allowed", "HTTP method not supported");
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(error);
    }

    /**
     * Generates a 500 response for any unexpected server-side exceptions.
     *
     * @param exception the unhandled exception
     * @return HTTP 500 response with a generic message
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpectedException(Exception exception) {
        ErrorResponse error = new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", "An unexpected error occurred");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}