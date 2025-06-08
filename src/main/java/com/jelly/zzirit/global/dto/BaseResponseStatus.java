package com.jelly.zzirit.global.dto;

import org.springframework.http.HttpStatus;

import lombok.Getter;

@Getter
public enum BaseResponseStatus {

	// 요청 성공
	SUCCESS(true, 1000, "요청이 성공하였습니다.", HttpStatus.OK),

	// 이메일 인증 관련
	EMAIL_REQUEST_LOCKED(false, 2001, "인증 코드는 30초 후에 다시 요청할 수 있습니다.", HttpStatus.BAD_REQUEST),
	EMAIL_ALREADY_VERIFIED(false, 2002, "이메일 인증이 이미 완료되었습니다.", HttpStatus.BAD_REQUEST),
	EMAIL_VERIFICATION_EXPIRED(false, 2003, "이메일 인증 시간이 만료되었습니다. 다시 인증을 진행해 주세요.", HttpStatus.BAD_REQUEST),
	EMAIL_VERIFICATION_REQUIRED(false, 2004, "이메일 인증이 필요합니다.", HttpStatus.BAD_REQUEST),
	EMAIL_INVALID_CODE(false, 2005, "유효하지 않은 인증 코드입니다.", HttpStatus.BAD_REQUEST),
	EMAIL_SEND_FAILED(false, 2006, "이메일 전송에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),

	// 회원가입 관련
	USER_ALREADY_EXISTS(false, 2101, "이미 가입된 이메일입니다. 다른 방법으로 로그인하세요.", HttpStatus.CONFLICT),
	USER_PASSWORD_INVALID(false, 2102, "비밀번호는 8~15자 이내로 숫자와 소문자를 포함해야 합니다.", HttpStatus.BAD_REQUEST),
	USER_NICKNAME_DUPLICATE(false, 2103, "이미 사용 중인 닉네임입니다.", HttpStatus.CONFLICT),
	USER_RECOVERY_REQUIRED(false, 2104, "이 계정은 삭제되었습니다. 로그인 하셔서 계정 복구하시길 바랍니다", HttpStatus.FORBIDDEN),
	USER_NOT_FOUND(false, 2105, "사용자를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
	TEMP_USER_NOT_FOUND(false, 2106, "임시 사용자 정보를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
	USER_NICKNAME_NOT_VERIFIED(false, 2107, "닉네임 중복 검사를 먼저 진행하세요.", HttpStatus.BAD_REQUEST),
	USER_TEMP_SESSION_EXPIRED(false, 2108, "임시 세션이 만료되었습니다. 다시 소셜 로그인을 진행하세요.", HttpStatus.BAD_REQUEST),
	USER_SOCIAL_SIGNUP_REQUIRED(false, 2109, "추가 정보 입력이 필요합니다. 닉네임과 비밀번호를 설정해주세요.", HttpStatus.UNAUTHORIZED),
	USER_PASSWORD_NOT_VALID(false, 2110, "비밀번호 형식이 올바르지 않습니다.", HttpStatus.BAD_REQUEST),

	// TEMP_AUTHORIZATION_HEADER 관련
	TEMP_AUTHORIZATION_HEADER_MISSING(false, 2201, "임시 인증 토큰이 없습니다.", HttpStatus.UNAUTHORIZED),
	TEMP_AUTHORIZATION_HEADER_INVALID(false, 2202, "유효하지 않은 임시 인증 토큰입니다.", HttpStatus.UNAUTHORIZED),
	TEMP_AUTHORIZATION_HEADER_EXPIRED(false, 2203, "임시 인증 토큰이 만료되었습니다.", HttpStatus.UNAUTHORIZED),

	// OAuth 관련
	OAUTH_USER_NOT_FOUND(false, 2204, "소셜 로그인 사용자 정보를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
	OAUTH_REDIRECT_FAILED(false, 2205, "소셜 로그인 후 리다이렉트에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
	OAUTH_EMAIL_NOT_FOUND(false, 2206, "이메일 정보를 가져올 수 없습니다.", HttpStatus.BAD_REQUEST),

	// 회원 관련
	INVALID_PASSWORD(false, 2301, "비밀번호가 일치하지 않습니다", HttpStatus.BAD_REQUEST),

	// 주문 관련 에러 코드
	TOSS_PAYMENT_REQUEST_FAILED(false, 3000, "토스 결제 요청에 실패했습니다.", HttpStatus.BAD_GATEWAY),
	ITEM_NOT_FOUND(false, 3001, "해당 상품이 존재하지 않습니다.", HttpStatus.BAD_REQUEST),
	ORDER_NOT_FOUND(false, 3002, "주문 정보를 찾을 수 없습니다.", HttpStatus.BAD_REQUEST),
	INVALID_TIMEDEAL_ITEM(false, 3012, "유효한 타임딜 상품이 아닙니다.", HttpStatus.BAD_REQUEST),
	PAYMENT_AMOUNT_MISMATCH(false, 3003, "결제 금액이 일치하지 않습니다.", HttpStatus.BAD_REQUEST),
	ORDER_ID_MISMATCH(false, 3004, "주문 번호가 일치하지 않습니다.", HttpStatus.BAD_REQUEST),
	ALREADY_PROCESSED(false, 3005, "이미 처리된 주문입니다.", HttpStatus.BAD_REQUEST),
	TOSS_PAYMENT_VERIFY_FAILED(false, 3006, "토스 결제 정보 검증에 실패했습니다.", HttpStatus.BAD_GATEWAY),
	TOSS_CONFIRM_FAILED(false, 3007, "토스 결제 최종 승인에 실패했습니다.", HttpStatus.BAD_GATEWAY),
	NOT_PAID_ORDER(false, 3008, "결제 완료된 주문만 취소할 수 있습니다.", HttpStatus.BAD_REQUEST),
	EXPIRED_CANCEL_TIME(false, 3009, "24시간 이내의 주문만 취소할 수 있습니다.", HttpStatus.BAD_REQUEST),
	UNSUPPORTED_PAYMENT_PROVIDER(false, 3010, "지원하지 않는 결제 수단입니다.", HttpStatus.BAD_REQUEST),
	UNREGISTERED_PAYMENT_GATEWAY(false, 3011, "PG사 구현체가 등록되어 있지 않습니다.", HttpStatus.INTERNAL_SERVER_ERROR),

	// 환불 및 상태 변경 관련 추가
	TOSS_REFUND_FAILED(false, 3020, "토스 결제 취소 API 실패입니다.", HttpStatus.BAD_GATEWAY),
	ORDER_REFUND_FAILED(false, 3021, "환불 처리에 실패했습니다. 관리자에게 문의하세요.", HttpStatus.INTERNAL_SERVER_ERROR),
	STOCK_RESTORE_FAILED(false, 3022, "재고 복원에 실패했습니다.", HttpStatus.BAD_REQUEST),
	TOSS_PAYMENT_NOT_DONE(false, 3023, "토스 결제가 완료되지 않았습니다.", HttpStatus.BAD_REQUEST),
	TOSS_PAYMENT_AMOUNT_MISMATCH(false, 3024, "토스 결제 금액이 일치하지 않습니다.", HttpStatus.BAD_REQUEST),
	STOCK_REDUCE_FAILED(false, 3025, "재고 차감에 실패했습니다.", HttpStatus.BAD_REQUEST),
	PAYMENT_NOT_FOUND(false, 3026, "결제 정보를 찾을 수 없습니다.", HttpStatus.BAD_REQUEST),
	STOCK_NOT_FOUND(false, 3027, "재고 정보를 찾을 수 없습니다.", HttpStatus.BAD_REQUEST),
	OUT_OF_STOCK(false, 3028, "상품 재고가 부족합니다.", HttpStatus.BAD_REQUEST),
	LOCK_FAILED(false, 3029, "다른 요청과 충돌이 발생했습니다. 다시 시도해주세요.", HttpStatus.CONFLICT),
	LOCK_INTERRUPTED(false, 3030, "시스템 인터럽트가 발생했습니다. 다시 시도해주세요.", HttpStatus.INTERNAL_SERVER_ERROR),

	// 실패
	VALIDATION_FAILED(false, 40000, "입력 값이 유효하지 않습니다", HttpStatus.BAD_REQUEST),
	AUTH_REQUEST_BODY_INVALID(false, 40001, "잘못된 요청 본문입니다.", HttpStatus.BAD_REQUEST),

	// 인증 & 인가
	UNAUTHORIZED(false, 40002, "인증되지 않은 요청입니다.", HttpStatus.UNAUTHORIZED),
	ACCESS_DENIED(false, 40003, "접근 권한이 없습니다.", HttpStatus.FORBIDDEN),
	AUTH_CHECK_FAILED(false, 40004, "로그인이 필요합니다.", HttpStatus.UNAUTHORIZED),
	NO_AUTHORITY(false, 40005, "수행할 권한이 없습니다.", HttpStatus.FORBIDDEN),
	ACCOUNT_LOCKED(false, 40006, "로그인 시도가 너무 많습니다. 잠시 후 다시 시도해주세요.", HttpStatus.UNAUTHORIZED),

	// JWT 관련
	JWT_BLACKLISTED(false, 40010, "블랙리스트에 등록된 토큰입니다.", HttpStatus.UNAUTHORIZED),
	JWT_INVALID(false, 40011, "잘못된 토큰입니다.", HttpStatus.UNAUTHORIZED),
	JWT_MISSING(false, 40012, "토큰이 존재하지 않습니다", HttpStatus.UNAUTHORIZED),

	// RefreshToken 관련
	REFRESH_TOKEN_NULL(false, 40020, "리프레시 토큰이 없습니다.", HttpStatus.UNAUTHORIZED),
	REFRESH_TOKEN_EXPIRED(false, 40021, "리프레시 토큰이 만료되었습니다.", HttpStatus.UNAUTHORIZED),
	REFRESH_TOKEN_INVALID(false, 40022, "유효하지 않은 리프레시 토큰입니다.", HttpStatus.UNAUTHORIZED),
	REFRESH_TOKEN_NOT_FOUND(false, 40023, "저장된 리프레시 토큰이 없습니다.", HttpStatus.UNAUTHORIZED),

	// 기타
	NOT_SUPPORTED(false, 50001, "지원하지 않는 형식입니다.", HttpStatus.INTERNAL_SERVER_ERROR),

	// 상품 관련
	TYPE_NOT_FOUND(false, 60001, "타입이 없습니다.", HttpStatus.NOT_FOUND),
	BRAND_NOT_FOUND(false, 60002, "브랜드가 없습니다.", HttpStatus.NOT_FOUND),
	ITEM_STOCK_NOT_FOUND(false, 60003, "상품 재고가 없습니다.", HttpStatus.NOT_FOUND),
	TYPE_BRAND_NOT_FOUND(false, 60004, "상품 종류-브랜드가 없습니다.", HttpStatus.NOT_FOUND),
	ITEM_NOT_FOUND_IN_CART(false, 60005, "장바구니에 해당 상품이 존재하지 않습니다.", HttpStatus.NOT_FOUND),
	IMAGE_REQUIRED(false, 60006, "상품 이미지가 없습니다.", HttpStatus.NOT_FOUND),
	INVALID_IMAGE_URL(false, 60007, "잘못된 이미지 URL 형식입니다.", HttpStatus.BAD_REQUEST),
	INVALID_PRICE(false, 60008, "상품 가격은 0 이상이어야 합니다.", HttpStatus.BAD_REQUEST),
	INVALID_STOCK(false, 60009, "상품 재고는 0 이상이어야 합니다.", HttpStatus.BAD_REQUEST),
	ITEM_CONCURRENT_UPDATE(false, 60010, "다른 관리자가 먼저 수정했습니다. 다시 시도해 주세요.", HttpStatus.CONFLICT),

	// 장바구니 수량 조정 관련
	INVALID_CART_QUANTITY(false, 60010, "장바구니 수량은 1개 이상이어야 합니다.", HttpStatus.BAD_REQUEST),
	CART_QUANTITY_EXCEEDS_STOCK(false, 60011, "장바구니 수량이 재고를 초과할 수 없습니다.", HttpStatus.BAD_REQUEST),

	// 타임딜 관련
	TIME_DEAL_TIME_OVERLAP(false, 70001, "해당 기간에 이미 존재하는 타임딜이 있습니다.", HttpStatus.CONFLICT),
	TIME_DEAL_START_TIME_PAST(false, 70002, "타임딜 시작 시간은 현재보다 이전일 수 없습니다.", HttpStatus.BAD_REQUEST),

	// 레디스 관련
	REDIS_ACCESS_ERROR(false, 80000, "내부 시스템 오류(Redis 처리 실패)", HttpStatus.INTERNAL_SERVER_ERROR),
	IP_BLOCKED(false, 40104, "해당 IP 에서 로그인 시도가 너무 많습니다. 잠시 후 다시 시도해주세요.", HttpStatus.UNAUTHORIZED);

	private final boolean isSuccess;
	private final int code;
	private final String message;
	private final int httpStatusCode;

	BaseResponseStatus(boolean isSuccess, int code, String message, HttpStatus httpStatus) {
		this.isSuccess = isSuccess;
		this.code = code;
		this.message = message;
		this.httpStatusCode = httpStatus.value();
	}

	public boolean isSuccess() {
		return isSuccess;
	}
}