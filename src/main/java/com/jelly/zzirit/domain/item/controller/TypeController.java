package com.jelly.zzirit.domain.item.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jelly.zzirit.domain.item.dto.response.TypeFetchResponse;
import com.jelly.zzirit.domain.item.service.QueryTypeService;
import com.jelly.zzirit.global.dto.BaseResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/types")
@RequiredArgsConstructor
@Tag(name = "상품 종류 API", description = "상품 종류와 관련된 API를 설명합니다.")
public class TypeController {

	private final QueryTypeService queryTypeService;

	@GetMapping
	@Operation(summary = "상품 종류 전체 조회", description = "상품 종류를 전체 조회 합니다.")
	public List<TypeFetchResponse> findType() {
		return queryTypeService.getAll();
	}
}
