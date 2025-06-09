package com.jelly.zzirit.domain.item.dto.response;

import java.util.List;

public record SimpleItemsFetchResponse(
	Long totalCount,
	List<SimpleItemFetchResponse> items
) {

	public static SimpleItemsFetchResponse from(Long totalCount, List<ItemFetchQueryResponse> items) {
		List<SimpleItemFetchResponse> itemResponses = items.stream()
			.map(SimpleItemFetchResponse::from)
			.toList();

		return new SimpleItemsFetchResponse(totalCount, itemResponses);
	}
}
