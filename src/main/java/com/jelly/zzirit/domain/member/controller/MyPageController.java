package com.jelly.zzirit.domain.member.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jelly.zzirit.domain.member.dto.request.AddressUpdateRequest;
import com.jelly.zzirit.domain.member.dto.response.MyPageInfoResponse;
import com.jelly.zzirit.domain.member.service.userinfo.CommandMyPageService;
import com.jelly.zzirit.domain.member.service.userinfo.QueryMyPageService;
import com.jelly.zzirit.global.dto.BaseResponse;
import com.jelly.zzirit.global.dto.Empty;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user/mypage")
@Tag(name = "마이페이지", description = "회원 마이페이지 API")
@SecurityRequirement(name = "accessToken")
public class MyPageController {

	private final QueryMyPageService queryMyPageService;
	private final CommandMyPageService commandMyPageService;

	@Operation(summary = "내 정보 조회", description = "회원의 이름 및 주소 정보를 조회합니다.")
	@GetMapping("/info")
	public MyPageInfoResponse getMyPageInfo() {
		return queryMyPageService.getMyPageInfo();
	}

	@Operation(summary = "주소 수정", description = "회원의 주소 및 상세주소를 수정합니다.")
	@PatchMapping("/address")
	public void updateAddress(@RequestBody @Valid AddressUpdateRequest request) {
		commandMyPageService.updateAddress(request);
	}
}