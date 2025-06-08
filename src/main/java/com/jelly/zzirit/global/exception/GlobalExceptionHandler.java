package com.jelly.zzirit.global.exception;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.jelly.zzirit.global.dto.BaseResponse;
import com.jelly.zzirit.global.dto.BaseResponseStatus;
import com.jelly.zzirit.global.dto.Empty;
import com.jelly.zzirit.global.exception.custom.InvalidCustomException;
import com.jelly.zzirit.global.exception.custom.InvalidTimeDealException;

import jakarta.servlet.http.HttpServletResponse;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(InvalidCustomException.class)
	public BaseResponse<Empty> handleGlobalException(InvalidCustomException e, HttpServletResponse response) {
		response.setStatus(e.getStatus().getHttpStatusCode());
		return BaseResponse.error(e.getStatus());
	}

	@ExceptionHandler(InvalidTimeDealException.class)
	public BaseResponse<String> handleTimeDealException(InvalidCustomException e, HttpServletResponse response) {
		response.setStatus(e.getStatus().getHttpStatusCode());
		return BaseResponse.error(e.getStatus(), e.getMessage());
	}

	@ResponseStatus(HttpStatus.BAD_REQUEST)
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public BaseResponse<List<String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
		List<String> errors = ex.getBindingResult().getFieldErrors().stream()
			.map(DefaultMessageSourceResolvable::getDefaultMessage)
			.collect(Collectors.toList());

		return BaseResponse.error(BaseResponseStatus.VALIDATION_FAILED, errors);
	} // 요청 DTO 검증 실패 [Validation 실패]

	@ResponseStatus(HttpStatus.CONFLICT)
	@ExceptionHandler(ObjectOptimisticLockingFailureException.class) // 낙관적 락에 의한 충돌
	public BaseResponse<Empty> handleLockingFailureException() {
		return BaseResponse.error(BaseResponseStatus.ITEM_CONCURRENT_UPDATE);
	}
}