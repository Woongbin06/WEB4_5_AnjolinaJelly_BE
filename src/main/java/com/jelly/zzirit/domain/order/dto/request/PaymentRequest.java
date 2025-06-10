package com.jelly.zzirit.domain.order.dto.request;

import java.math.BigDecimal;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record PaymentRequest(
	@NotEmpty(message = "주문 항목은 비어 있을 수 없습니다.")
	List<@Valid OrderItemCreateRequest> orderItems,
	@NotNull(message = "결제 금액은 필수입니다.")
	@DecimalMin(value = "0.01", inclusive = true, message = "결제 금액은 0보다 커야 합니다.")
	BigDecimal totalAmount,
	String shippingRequest,
	@NotBlank(message = "주소는 필수입니다.")
	String address,
	String addressDetail
) {
}