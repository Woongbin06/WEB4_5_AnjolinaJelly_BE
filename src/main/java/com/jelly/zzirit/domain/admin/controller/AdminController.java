package com.jelly.zzirit.domain.admin.controller;

import com.jelly.zzirit.domain.admin.dto.request.ItemCreateRequest;
import com.jelly.zzirit.domain.admin.dto.request.ItemUpdateRequest;
import com.jelly.zzirit.domain.admin.dto.response.AdminItemFetchResponse;
import com.jelly.zzirit.domain.admin.dto.response.ImageUploadResponse;
import com.jelly.zzirit.domain.admin.service.CommandAdminService;
import com.jelly.zzirit.domain.admin.service.CommandS3Service;
import com.jelly.zzirit.domain.admin.service.QueryAdminService;
import com.jelly.zzirit.domain.item.dto.request.TimeDealCreateRequest;
import com.jelly.zzirit.domain.item.dto.response.TimeDealCreateResponse;
import com.jelly.zzirit.domain.item.dto.response.TimeDealFetchResponse;
import com.jelly.zzirit.domain.item.entity.timedeal.TimeDeal;
import com.jelly.zzirit.domain.item.service.CommandTimeDealService;
import com.jelly.zzirit.domain.item.service.QueryTimeDealService;
import com.jelly.zzirit.global.dto.BaseResponse;
import com.jelly.zzirit.global.dto.Empty;
import com.jelly.zzirit.global.dto.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Optional;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Tag(name = "관리자 상품 API", description = "관리자 상품 기능을 제공합니다.")
public class AdminController {
	private final QueryAdminService queryAdminService;
	private final CommandAdminService commandAdminItemService;
	private final QueryTimeDealService queryTimeDealService;
	private final CommandTimeDealService timeDealService;
	private final CommandS3Service commandS3Service;

	@Operation(summary = "관리자 상품 단건 조회", description = "관리자가 id로 상품을 단건 조회합니다.")
	@GetMapping("/items/{item-id}")
	public AdminItemFetchResponse getItem(
			@PathVariable("item-id") Long itemId
	) {
		return queryAdminService.getItemById(itemId);
	}

	@Operation(summary = "관리자 상품 이름 검색 & 목록 조회", description = "관리자가 이름으로 상품을 검색 / 상품 목록을 조회합니다.")
	@GetMapping("/items")
	public PageResponse<AdminItemFetchResponse> getItems(
			@RequestParam(required = false) String name,
			@RequestParam(defaultValue = "desc") String sort,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size
	) {
		Pageable pageable = PageRequest.of(page, size);
		return queryAdminService.getSearchItems(name, sort, pageable);
	}

	@Operation(summary = "관리자 상품 이미지 업로드", description = "상품 등록 전 이미지를 S3에 업로드하고 URL 반환")
	@PostMapping(value = "/items/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ImageUploadResponse uploadImage(
		@RequestPart("image") MultipartFile image
	) throws IOException {
		String uploadedUrl = commandS3Service.upload(image, "item-images");
		return new ImageUploadResponse(uploadedUrl);
	}

	@Operation(summary = "관리자 상품 등록", description = "관리자가 상품을 등록합니다.")
	@PostMapping("/items")
	public void createItem(@RequestBody @Valid ItemCreateRequest request) {
		commandAdminItemService.createItem(request);
	}

	@Operation(summary = "관리자 상품 수정", description = "관리자가 id로 상품(재고, 가격, 이미지)을 수정합니다.")
	@PutMapping("/items/{item-id}")
	public void updateItem(
		@PathVariable("item-id") @NotNull Long itemId,
		@RequestBody @Valid ItemUpdateRequest request
	) {
		commandAdminItemService.updateItem(itemId, request);
	}
	
	@Operation(summary = "관리자 상품 이미지 수정", description = "상품 ID로 기존 상품의 이미지를 새 이미지로 교체")
	@PutMapping(value = "/items/{item-id}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ImageUploadResponse updateImage(
		@PathVariable("item-id") Long itemId,
		@RequestPart("image") MultipartFile image
	) throws IOException {
		String uploadedUrl = commandS3Service.upload(image, "item-images");
		commandAdminItemService.updateImageUrl(itemId, uploadedUrl);
		return new ImageUploadResponse(uploadedUrl);
	}

	@Operation(summary = "관리자 상품 삭제", description = "관리자가 id로 상품을 삭제합니다.")
	@DeleteMapping("/items/{item-id}")
	public void deleteItem(@PathVariable("item-id") @NotNull Long itemId) {
		commandAdminItemService.deleteItem(itemId);
	}

	@Operation(summary = "타임딜 등록", description = "타임딜 정보와 아이템 리스트를 등록합니다.")
	@PostMapping("/time-deals")
	public TimeDealCreateResponse createTimeDeal(@RequestBody TimeDealCreateRequest request) {
		return timeDealService.createTimeDeal(request);
	}

	@GetMapping("/time-deals/search")
	@Operation(summary = "(관리자 페이지)타임딜 목록 조회", description = "관리자 페이지에서 타임딜 목록을 조회합니다.")
	public PageResponse<TimeDealFetchResponse> searchTimeDeals(
		@RequestParam(required = false) String timeDealName,
		@RequestParam(required = false) Long timeDealId,
		@RequestParam(required = false) String timeDealItemName,
		@RequestParam(required = false) Long timeDealItemId,
		@RequestParam(required = false) TimeDeal.TimeDealStatus status,
		@RequestParam(defaultValue = "0") int page,
		@RequestParam(defaultValue = "10") int size
	) {
		return queryTimeDealService.getTimeDeals(
			timeDealName, timeDealId, timeDealItemName, timeDealItemId, status, page, size
		);
	}
}