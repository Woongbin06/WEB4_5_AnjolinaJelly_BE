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
import com.jelly.zzirit.global.security.oauth2.service.signup.FirstOAuthSignUpService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

	private final CommandAuthService commandAuthService;
	private final FirstOAuthSignUpService firstOAuthSignUpService;
	private final CommandEmailService commandEmailService;

	@PostMapping("/send-email-code")
	public void sendEmailVerificationCode(@RequestBody @Valid EmailAuthRequest emailAuthRequest) {
		commandEmailService.sendEmailVerificationCode(emailAuthRequest.getEmail());
	}

	@PostMapping("/verify-email")
	public void verifyEmailCode(@RequestBody @Valid EmailAuthVerifyRequest emailAuthVerifyRequest) {
		commandEmailService.verifyEmailCode(emailAuthVerifyRequest.getEmail(), emailAuthVerifyRequest.getCode());
	}

	@PostMapping("/signup")
	public void signup(@RequestBody @Valid SignupRequest signupRequest) {
		commandAuthService.signup(signupRequest);
	}

	@PostMapping("/social-signup")
	public void completeSignup(
		HttpServletRequest request,
		HttpServletResponse response,
		@RequestBody @Valid SocialSignupRequest socialSignupRequest
	) {
		firstOAuthSignUpService.finalizeSocialSignup(request, response, socialSignupRequest);
	}
}