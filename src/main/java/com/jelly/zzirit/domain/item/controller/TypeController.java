package com.jelly.zzirit.domain.item.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jelly.zzirit.domain.item.dto.response.TypeFetchResponse;
import com.jelly.zzirit.domain.item.service.QueryTypeService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/types")
@RequiredArgsConstructor
public class TypeController {

	private final QueryTypeService queryTypeService;

	@GetMapping
	public List<TypeFetchResponse> findType() {
		return queryTypeService.getAll();
	}
}
