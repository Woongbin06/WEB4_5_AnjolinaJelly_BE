package com.jelly.zzirit.domain.item.dto.response;

import java.util.List;

public record SimpleItemsFetchResponse(
	List<SimpleItemFetchResponse> items
) {

	public static SimpleItemsFetchResponse from(List<ItemFetchQueryResponse> items) {
		return new SimpleItemsFetchResponse(
			items.stream()
				.map(SimpleItemFetchResponse::from)
				.toList()
		);
	}
}
