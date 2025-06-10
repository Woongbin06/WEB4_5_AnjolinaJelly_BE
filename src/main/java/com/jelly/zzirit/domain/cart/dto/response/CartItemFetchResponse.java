package com.jelly.zzirit.domain.cart.dto.response;

public record CartItemFetchResponse(
	Long cartItemId,
	Long itemId,
	String itemName,
	String type,
	String brand,
	Integer quantity,
	String imageUrl,
	Integer originalPrice,
	Integer discountedPrice,
	Integer totalPrice,
	Boolean isTimeDeal,
	Integer discountRatio,
	Boolean isSoldOut
) {
}