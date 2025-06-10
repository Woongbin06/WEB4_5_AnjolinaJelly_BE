package com.jelly.zzirit.global.wrapper;

import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import com.jelly.zzirit.global.dto.BaseResponse;

@RestControllerAdvice
public class ResponseWrapper implements ResponseBodyAdvice<Object> {

	@Override
	public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
		return true;
	}

	@Override
	public Object beforeBodyWrite(Object body,
		MethodParameter returnType,
		MediaType selectedContentType,
		Class<? extends HttpMessageConverter<?>> selectedConverterType,
		ServerHttpRequest request,
		ServerHttpResponse response
	) {
		String path = request.getURI().getPath();
		if (path.startsWith("/actuator")) {
			return body;
		}

		if (body instanceof BaseResponse<?>) {
			return body;
		}

		if (body == null) {
			return BaseResponse.success();
		}

		return BaseResponse.success(body);
	}
}