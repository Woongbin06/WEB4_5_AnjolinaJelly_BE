package com.jelly.zzirit.domain.admin.controller;

import java.io.IOException;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

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
import com.jelly.zzirit.global.dto.PageResponse;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {
	private final QueryAdminService queryAdminService;
	private final CommandAdminService commandAdminItemService;
	private final QueryTimeDealService queryTimeDealService;
	private final CommandTimeDealService timeDealService;
	private final CommandS3Service commandS3Service;

	@GetMapping("/items/{item-id}")
	public AdminItemFetchResponse getItem(
			@PathVariable("item-id") Long itemId
	) {
		return queryAdminService.getItemById(itemId);
	}

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

	@PostMapping(value = "/items/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ImageUploadResponse uploadImage(
		@RequestPart("image") MultipartFile image
	) throws IOException {
		String uploadedUrl = commandS3Service.upload(image, "item-images");
		return new ImageUploadResponse(uploadedUrl);
	}

	@PostMapping("/items")
	public void createItem(@RequestBody @Valid ItemCreateRequest request) {
		commandAdminItemService.createItem(request);
	}

	@PutMapping("/items/{item-id}")
	public void updateItem(
		@PathVariable("item-id") @NotNull Long itemId,
		@RequestBody @Valid ItemUpdateRequest request
	) {
		commandAdminItemService.updateItem(itemId, request);
	}
	
	@PutMapping(value = "/items/{item-id}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ImageUploadResponse updateImage(
		@PathVariable("item-id") Long itemId,
		@RequestPart("image") MultipartFile image
	) throws IOException {
		String uploadedUrl = commandS3Service.upload(image, "item-images");
		commandAdminItemService.updateImageUrl(itemId, uploadedUrl);
		return new ImageUploadResponse(uploadedUrl);
	}

	@DeleteMapping("/items/{item-id}")
	public void deleteItem(@PathVariable("item-id") @NotNull Long itemId) {
		commandAdminItemService.deleteItem(itemId);
	}

	@PostMapping("/time-deals")
	public TimeDealCreateResponse createTimeDeal(@RequestBody TimeDealCreateRequest request) {
		return timeDealService.createTimeDeal(request);
	}

	@GetMapping("/time-deals/search")
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