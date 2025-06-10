package com.jelly.zzirit.domain.cart.dto.request;

import java.util.List;

public record CartItemDeleteRequest(
	List<Long> itemIds
) {
}