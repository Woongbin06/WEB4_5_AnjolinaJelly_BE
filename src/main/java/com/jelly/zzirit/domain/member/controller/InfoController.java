package com.jelly.zzirit.domain.member.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jelly.zzirit.global.AuthMember;
import com.jelly.zzirit.global.dto.BaseResponse;
import com.jelly.zzirit.global.dto.Empty;
import com.jelly.zzirit.global.security.oauth2.service.token.OAuthTempTokenService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/info")
@RequiredArgsConstructor
@Tag(name = "로그인 및 토큰 정보", description = "인증된 사용자 정보 및 임시 회원가입 확인 API")
public class InfoController {

	private final OAuthTempTokenService oAuthTempTokenService;

	@Operation(summary = "로그인 인증 상태 확인")
	@GetMapping("/check")
	public void checkAuth() {
		log.info("유저 로그인 체크 : {}", AuthMember.getAuthUser());
	}

	@Operation(summary = "임시 회원가입 토큰 유효성 검증")
	@GetMapping("/temp-check")
	public void checkTempToken(HttpServletRequest request) {
		oAuthTempTokenService.extractTokenData(request);
	}
}