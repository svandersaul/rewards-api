package com.rewards.exception;

import org.springframework.beans.TypeMismatchException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
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
     * @return HTTP 404 problem response
     */
    @ExceptionHandler(CustomerNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleCustomerNotFound(
            CustomerNotFoundException exception) {
        return problem(HttpStatus.NOT_FOUND, "Customer Not Found", exception.getMessage());
    }

    /**
     * Generates a 400 response when a customer ID path variable is not numeric.
     *
     * @param exception type mismatch exception raised by Spring when binding arguments
     * @return HTTP 400 problem response
     */
    @ExceptionHandler(TypeMismatchException.class)
    public ResponseEntity<ProblemDetail> handleTypeMismatch(TypeMismatchException exception) {
        return problem(HttpStatus.BAD_REQUEST, "Bad Request", "Customer ID must be a number");
    }

    /**
     * Generates a 404 response for requests with no matching resource.
     *
     * @param exception exception raised when no resource matches the request
     * @return HTTP 404 problem response
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ProblemDetail> handleResourceNotFound(NoResourceFoundException exception) {
        return problem(HttpStatus.NOT_FOUND, "Not Found", "Resource not found");
    }

    /**
     * Generates a 404 response when no request handler matches the path.
     *
     * @param exception exception raised when no handler matches the request
     * @return HTTP 404 problem response
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ProblemDetail> handleNoHandlerFound(NoHandlerFoundException exception) {
        return problem(HttpStatus.NOT_FOUND, "Not Found", "Resource not found");
    }

    /**
     * Generates a 405 response when a route does not support the requested method.
     *
     * @param exception exception containing the unsupported method
     * @return HTTP 405 problem response
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ProblemDetail> handleMethodNotSupported(
            HttpRequestMethodNotSupportedException exception) {
        return problem(HttpStatus.METHOD_NOT_ALLOWED, "Method Not Allowed", "HTTP method not supported");
    }

    /**
     * Generates a 500 response for any unexpected server-side exceptions.
     *
     * @param exception the unhandled exception
     * @return HTTP 500 problem response
     * 
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleUnexpectedException(Exception exception) {
        return problem(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error",
                "An unexpected error occurred");
    }

    /**
     * Creates a standard problem detail body and uses its status for the response.
     *
     * @param status HTTP status to return
     * @param title short problem title
     * @param detail human-readable explanation
     * @return response containing the problem detail
     */
    private ResponseEntity<ProblemDetail> problem(HttpStatus status, String title, String detail) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setTitle(title);
        return ResponseEntity.status(status).body(problem);
    }
}