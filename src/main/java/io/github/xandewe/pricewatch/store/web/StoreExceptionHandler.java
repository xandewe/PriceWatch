package io.github.xandewe.pricewatch.store.web;

import io.github.xandewe.pricewatch.store.DuplicateStoreException;
import io.github.xandewe.pricewatch.store.InvalidStoreQueryException;
import io.github.xandewe.pricewatch.store.InvalidStoreWebsiteUrlException;
import io.github.xandewe.pricewatch.store.StoreNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.Clock;
import java.time.Instant;

@RestControllerAdvice(assignableTypes = StoreController.class)
public class StoreExceptionHandler {

	private final Clock clock;

	public StoreExceptionHandler(Clock clock) {
		this.clock = clock;
	}

	@ExceptionHandler(StoreNotFoundException.class)
	ResponseEntity<ApiErrorResponse> handleNotFound(
			StoreNotFoundException exception,
			HttpServletRequest request) {
		return response(HttpStatus.NOT_FOUND, "STORE_NOT_FOUND", exception.getMessage(), request);
	}

	@ExceptionHandler(DuplicateStoreException.class)
	ResponseEntity<ApiErrorResponse> handleConflict(
			DuplicateStoreException exception,
			HttpServletRequest request) {
		return response(HttpStatus.CONFLICT, "STORE_NAME_CONFLICT", exception.getMessage(), request);
	}

	@ExceptionHandler(InvalidStoreWebsiteUrlException.class)
	ResponseEntity<ApiErrorResponse> handleInvalidWebsiteUrl(
			InvalidStoreWebsiteUrlException exception,
			HttpServletRequest request) {
		return response(HttpStatus.BAD_REQUEST, "INVALID_WEBSITE_URL", exception.getMessage(), request);
	}

	@ExceptionHandler(InvalidStoreQueryException.class)
	ResponseEntity<ApiErrorResponse> handleInvalidQuery(
			InvalidStoreQueryException exception,
			HttpServletRequest request) {
		return response(HttpStatus.BAD_REQUEST, "INVALID_QUERY", exception.getMessage(), request);
	}

	@ExceptionHandler({
			MethodArgumentNotValidException.class,
			HttpMessageNotReadableException.class
	})
	ResponseEntity<ApiErrorResponse> handleInvalidRequest(
			Exception exception,
			HttpServletRequest request) {
		return response(
				HttpStatus.BAD_REQUEST,
				"INVALID_REQUEST",
				"Request body is invalid",
				request);
	}

	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
	ResponseEntity<ApiErrorResponse> handleInvalidParameter(
			MethodArgumentTypeMismatchException exception,
			HttpServletRequest request) {
		return response(
				HttpStatus.BAD_REQUEST,
				"INVALID_QUERY",
				"Query parameter is invalid: " + exception.getName(),
				request);
	}

	private ResponseEntity<ApiErrorResponse> response(
			HttpStatus status,
			String code,
			String message,
			HttpServletRequest request) {
		var body = new ApiErrorResponse(
				Instant.now(clock),
				status.value(),
				status.getReasonPhrase(),
				code,
				message,
				request.getRequestURI());
		return ResponseEntity.status(status).body(body);
	}
}
