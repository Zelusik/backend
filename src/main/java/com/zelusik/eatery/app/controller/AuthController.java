package com.zelusik.eatery.app.controller;

import com.zelusik.eatery.app.domain.constant.LoginType;
import com.zelusik.eatery.app.dto.auth.KakaoOAuthUserInfo;
import com.zelusik.eatery.app.dto.auth.response.LoginResponse;
import com.zelusik.eatery.app.dto.auth.response.TokenResponse;
import com.zelusik.eatery.app.dto.member.MemberDto;
import com.zelusik.eatery.app.dto.member.response.LoggedInMemberResponse;
import com.zelusik.eatery.app.service.MemberService;
import com.zelusik.eatery.app.service.RefreshTokenService;
import com.zelusik.eatery.global.security.JwtTokenInfoDto;
import com.zelusik.eatery.global.security.JwtTokenProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "로그인 등 인증 관련")
@RequiredArgsConstructor
@RequestMapping("/api/auth")
@RestController
public class AuthController {

    private final KakaoOAuthController kakaoOAuthController;
    private final MemberService memberService;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenService refreshTokenService;

    @Operation(
            summary = "로그인",
            description = "<p>Kakao에서 전달받은 access token을 request header에 담아 로그인합니다.</p>" +
                    "<p>로그인에 성공하면 로그인 사용자 정보, access token. refresh token을 응답합니다.</p>" +
                    "<p>사용자 정보에 포함된 약관 동의 정보(<code>termsInfo</code>)는 아직 약관 동의를 진행하지 않은 경우 <code>null</code>입니다.</p>"
    )
    @PostMapping("/login/kakao")
    public ResponseEntity<LoginResponse> login(
            @Parameter(
                    description = "카카오 서버에서 받은 access Token (<strong>Bearer</strong> type)",
                    example = "Bearer 4rzV6xwsGVjz8im0ZHAJJc9GISbC-8hiMszZcHtKCj1y6wAAAYaHOixY"
            ) @RequestHeader("kakao-access-token") String kakaoAccessToken
    ) {
        KakaoOAuthUserInfo userInfo = kakaoOAuthController.getUserInfo(kakaoAccessToken);

        MemberDto memberDto = memberService.findOptionalMemberBySocialUid(userInfo.getSocialUid())
                .orElseGet(() -> memberService.signUp(userInfo.toMemberDto()));

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(createLoginResponseWithJwtTokens(memberDto, LoginType.KAKAO));
    }

    /**
     * Access token과 refresh token을 생성한 후,
     * <code>LoginResponse</code> 객체에 로그인 사용자 정보와 두 개의 token을 담아 반환한다.
     *
     * @param memberDto 로그인 사용자
     * @param loginType 로그인 유형
     * @return 로그인 사용자 정보와 access token, refresh token 정보가 담긴 <code>LoginResponse</code> 객체
     */
    private LoginResponse createLoginResponseWithJwtTokens(MemberDto memberDto, LoginType loginType) {
        JwtTokenInfoDto accessTokenInfo = jwtTokenProvider.createAccessToken(memberDto.id(), loginType);
        JwtTokenInfoDto refreshTokenInfo = jwtTokenProvider.createRefreshToken(memberDto.id(), loginType);
        refreshTokenService.save(memberDto.id(), refreshTokenInfo.token());

        return LoginResponse.of(
                LoggedInMemberResponse.from(memberDto),
                TokenResponse.of(
                        accessTokenInfo.token(), accessTokenInfo.expiresAt(),
                        refreshTokenInfo.token(), refreshTokenInfo.expiresAt()
                )
        );
    }
}
