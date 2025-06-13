package com.jelly.zzirit.global.redis;

import java.lang.reflect.Method;

import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.util.StringUtils;

public class CustomKeyGenerator implements KeyGenerator {

	@Override
	public Object generate(Object target, Method method, Object... params) {
		return StringUtils.arrayToDelimitedString(params, "_");
	}
}
