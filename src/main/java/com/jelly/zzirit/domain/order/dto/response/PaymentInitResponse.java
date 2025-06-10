package com.jelly.zzirit.domain.order.dto.response;

public record PaymentInitResponse(
	String orderId,
	long amount,
	String orderName,
	String customerName
) {
}