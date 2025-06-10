package com.jelly.zzirit.domain.cart.dto.response;

import java.util.List;

public record CartFetchResponse(
	Long cartId,
	List<CartItemFetchResponse> items,
	Integer cartTotalQuantity,
	Integer cartTotalPrice
) {
}