package com.jelly.zzirit.domain.member.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jelly.zzirit.domain.member.dto.request.EmailAuthRequest;
import com.jelly.zzirit.domain.member.dto.request.EmailAuthVerifyRequest;
import com.jelly.zzirit.domain.member.dto.request.SignupRequest;
import com.jelly.zzirit.domain.member.dto.request.SocialSignupRequest;
import com.jelly.zzirit.domain.member.service.auth.CommandAuthService;
import com.jelly.zzirit.domain.member.service.email.CommandEmailService;
import com.jelly.zzirit.global.dto.BaseResponse;
import com.jelly.zzirit.global.dto.Empty;
import com.jelly.zzirit.global.security.oauth2.service.signup.FirstOAuthSignUpService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "인증", description = "자체 회원가입 및 소셜 회원가입 관련 API")
public class AuthController {

	private final CommandAuthService commandAuthService;
	private final FirstOAuthSignUpService firstOAuthSignUpService;
	private final CommandEmailService commandEmailService;

	@Operation(summary = "이메일 인증 코드 전송")
	@PostMapping("/send-email-code")
	public void sendEmailVerificationCode(@RequestBody @Valid EmailAuthRequest emailAuthRequest) {
		commandEmailService.sendEmailVerificationCode(emailAuthRequest.getEmail());
	}

	@Operation(summary = "이메일 인증 코드 검증")
	@PostMapping("/verify-email")
	public void verifyEmailCode(@RequestBody @Valid EmailAuthVerifyRequest emailAuthVerifyRequest) {
		commandEmailService.verifyEmailCode(emailAuthVerifyRequest.getEmail(), emailAuthVerifyRequest.getCode());
	}

	@Operation(summary = "자체 회원가입")
	@PostMapping("/signup")
	public void signup(@RequestBody @Valid SignupRequest signupRequest) {
		commandAuthService.signup(signupRequest);
	}

	@Operation(summary = "소셜 회원가입 최종 처리")
	@PostMapping("/social-signup")
	public void completeSignup(
		HttpServletRequest request,
		HttpServletResponse response,
		@RequestBody @Valid SocialSignupRequest socialSignupRequest
	) {
		firstOAuthSignUpService.finalizeSocialSignup(request, response, socialSignupRequest);
	}
}