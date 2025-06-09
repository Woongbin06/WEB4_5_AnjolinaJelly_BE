package com.jelly.zzirit.domain.item.dto.request;

import static com.jelly.zzirit.global.util.StringUtils.*;

import java.util.List;

public record ItemFilterRequest (
	List<String> types,
	List<String> brands,
	String keyword
) {

	public static ItemFilterRequest of(
		String types,
		String brands,
		String keyword
	) {
		return new ItemFilterRequest(
			convertStringsToCollection(types),
			convertStringsToCollection(brands),
			keyword
		);
	}
}
