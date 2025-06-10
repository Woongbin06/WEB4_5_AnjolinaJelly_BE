package com.jelly.zzirit.domain.item.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jelly.zzirit.domain.item.dto.response.BrandFetchResponse;
import com.jelly.zzirit.domain.item.service.QueryBrandService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/brands")
@RequiredArgsConstructor
public class BrandController {

	private final QueryBrandService queryBrandService;

	@GetMapping("/{type-id}")
	public List<BrandFetchResponse> findBrandByType(@PathVariable(name = "type-id") Long typeId) {
		return queryBrandService.getByType(typeId);
	}
}