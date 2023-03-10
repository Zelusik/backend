package com.zelusik.eatery.app.controller;

import com.zelusik.eatery.app.domain.constant.LoginType;
import com.zelusik.eatery.app.dto.auth.KakaoOAuthUserInfo;
import com.zelusik.eatery.app.dto.auth.request.KakaoLoginRequest;
import com.zelusik.eatery.app.dto.auth.request.TokenRefreshRequest;
import com.zelusik.eatery.app.dto.auth.response.LoginResponse;
import com.zelusik.eatery.app.dto.auth.response.TokenResponse;
import com.zelusik.eatery.app.dto.auth.response.TokenValidateResponse;
import com.zelusik.eatery.app.dto.member.MemberDto;
import com.zelusik.eatery.app.dto.member.response.LoggedInMemberResponse;
import com.zelusik.eatery.app.service.JwtTokenService;
import com.zelusik.eatery.app.service.KakaoOAuthService;
import com.zelusik.eatery.app.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;

@Tag(name = "로그인 등 인증 관련")
@RequiredArgsConstructor
@Validated
@RequestMapping("/api/auth")
@RestController
public class AuthController {

    private final KakaoOAuthService kakaoOAuthService;
    private final MemberService memberService;
    private final JwtTokenService jwtTokenService;

    @Operation(
            summary = "로그인",
            description = "<p>Kakao에서 전달받은 access token을 request header에 담아 로그인합니다." +
                    "<p>로그인에 성공하면 로그인 사용자 정보, access token. refresh token을 응답합니다." +
                    "<p>Access token의 만료기한은 12시간, refresh token의 만료기한은 1달입니다." +
                    "<p>사용자 정보에 포함된 약관 동의 정보(<code>termsInfo</code>)는 아직 약관 동의를 진행하지 않은 경우 <code>null</code>입니다."
    )
    @ApiResponses({
            @ApiResponse(description = "OK", responseCode = "200", content = @Content(schema = @Schema(implementation = LoginResponse.class))),
            @ApiResponse(description = "[10401] 유효하지 않은 kakao access token으로 요청한 경우.", responseCode = "401", content = @Content)
    })
    @PostMapping("/login/kakao")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody KakaoLoginRequest request) {
        KakaoOAuthUserInfo userInfo = kakaoOAuthService.getUserInfo(request.getKakaoAccessToken());

        MemberDto memberDto = memberService.findOptionalDtoBySocialUid(userInfo.getSocialUid())
                .orElseGet(() -> memberService.save(userInfo.toMemberDto()));

        TokenResponse tokenResponse = jwtTokenService.createJwtTokens(memberDto.id(), LoginType.KAKAO);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(LoginResponse.of(LoggedInMemberResponse.from(memberDto), tokenResponse));
    }

    @Operation(
            summary = "토큰 갱신하기",
            description = "<p>기존 발급받은 refresh token으로 새로운 access token과 refresh token을 발급 받습니다."
    )
    @ApiResponses({
            @ApiResponse(description = "OK", responseCode = "200", content = @Content(schema = @Schema(implementation = TokenResponse.class))),
            @ApiResponse(description = "[1502] 유효하지 않은 token으로 요청한 경우. Token 값이 잘못되었거나 만료되어 유효하지 않은 경우로 token 갱신 필요", responseCode = "401", content = @Content),
    })
    @PostMapping("/token")
    public ResponseEntity<TokenResponse> tokenRefresh(@Valid @RequestBody TokenRefreshRequest request) {
        TokenResponse tokenResponse = jwtTokenService.refresh(request.getRefreshToken());

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(tokenResponse);
    }

    @Operation(
            summary = "Refresh token 유효성 검사",
            description = "<p>Refresh token의 유효성을 확인합니다.</p>" +
                    "<p>유효하지 않은 refresh token이란 다음과 같은 경우를 말합니다.</p>" +
                    "<ul>" +
                    "<li>Refresh token의 값이 잘못된 경우</li>" +
                    "<li>Refresh token이 만료된 경우</li>" +
                    "<li>Refresh token의 발행 기록을 찾을 수 없는 경우</li>" +
                    "</ul>"
    )
    @GetMapping("/validity")
    public TokenValidateResponse validate(@RequestParam @NotBlank String refreshToken) {
        return new TokenValidateResponse(jwtTokenService.validateOfRefreshToken(refreshToken));
    }
}
