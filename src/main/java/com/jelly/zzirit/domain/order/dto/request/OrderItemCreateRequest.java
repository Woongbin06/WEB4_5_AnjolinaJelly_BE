package com.jelly.zzirit.domain.order.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record OrderItemCreateRequest(
	@NotNull(message = "상품 ID는 필수입니다.")
	Long itemId,
	@NotBlank(message = "상품 이름은 필수입니다.")
	String itemName,
	@Min(value = 1, message = "수량은 1개 이상이어야 합니다.")
	int quantity
) {
}