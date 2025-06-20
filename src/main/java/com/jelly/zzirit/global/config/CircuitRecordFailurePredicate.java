package com.jelly.zzirit.global.config;

import java.util.function.Predicate;

import com.jelly.zzirit.global.exception.custom.InvalidCustomException;

public class CircuitRecordFailurePredicate implements Predicate<Throwable> {

	@Override
	public boolean test(Throwable throwable) {
		if (throwable instanceof InvalidCustomException) {
			return false;
		}
		return true;
	}
}
