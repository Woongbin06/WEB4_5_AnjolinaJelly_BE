package com.jelly.zzirit.domain.order.infra.feign.dto;

import java.math.BigDecimal;

public record TossPaymentRefundRequest(
	String reason,
	BigDecimal amount
) {

	public static TossPaymentRefundRequest of(String reason, BigDecimal amount) {
		return new TossPaymentRefundRequest(reason, amount);
	}
}
