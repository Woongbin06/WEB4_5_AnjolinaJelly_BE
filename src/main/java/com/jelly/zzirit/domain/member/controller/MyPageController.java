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

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user/mypage")
public class MyPageController {

	private final QueryMyPageService queryMyPageService;
	private final CommandMyPageService commandMyPageService;

	@GetMapping("/info")
	public MyPageInfoResponse getMyPageInfo() {
		return queryMyPageService.getMyPageInfo();
	}

	@PatchMapping("/address")
	public void updateAddress(@RequestBody @Valid AddressUpdateRequest request) {
		commandMyPageService.updateAddress(request);
	}
}