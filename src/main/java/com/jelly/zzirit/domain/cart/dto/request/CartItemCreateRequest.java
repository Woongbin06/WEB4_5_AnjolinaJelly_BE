package com.jelly.zzirit.domain.cart.dto.request;

public record CartItemCreateRequest(
	Long itemId,
	Integer quantity
) {
}