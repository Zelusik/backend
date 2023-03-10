package com.zelusik.eatery.app.dto.terms_info.response;

import com.zelusik.eatery.app.dto.terms_info.TermsInfoDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class TermsInfoResponse {

    @Schema(description = "약관 동의 entity의 PK", example = "1")
    private Long id;

    @Schema(description = "미성년자(14세 미만) 여부", example = "false")
    private Boolean isMinor;

    @Schema(description = "서비스 약관 동의 여부", example = "true")
    private Boolean service;

    @Schema(description = "서비스 약관 동의 갱신 일시", example = "2023-02-27T08:29:11.091Z")
    private LocalDateTime serviceUpdatedAt;

    @Schema(description = "사용자 정보 약관 동의 여부", example = "true")
    private Boolean userInfo;

    @Schema(description = "사용자 정보 약관 동의 갱신 일시", example = "2023-02-27T08:29:11.091Z")
    private LocalDateTime userInfoUpdatedAt;

    @Schema(description = "위치 정보 수집 약관 동의 갱신 일시", example = "true")
    private Boolean locationInfo;

    @Schema(description = "위치 정보 수집 약관 동의 갱신 일시", example = "2023-02-27T08:29:11.091Z")
    private LocalDateTime locationInfoUpdatedAt;

    @Schema(description = "마케팅 수신 약관 동의 여부", example = "false")
    private Boolean marketingReception;

    @Schema(description = "마케팅 수신 약관 동의 갱신 일시", example = "2023-02-27T08:29:11.091Z")
    private LocalDateTime marketingReceptionUpdatedAt;

    public static TermsInfoResponse from(TermsInfoDto termsInfoDto) {
        if (termsInfoDto == null) {
            return null;
        }

        return new TermsInfoResponse(
                termsInfoDto.id(),
                termsInfoDto.isMinor(),
                termsInfoDto.service(), termsInfoDto.serviceUpdatedAt(),
                termsInfoDto.userInfo(), termsInfoDto.userInfoUpdatedAt(),
                termsInfoDto.locationInfo(), termsInfoDto.locationInfoUpdatedAt(),
                termsInfoDto.marketingReception(), termsInfoDto.marketingReceptionUpdatedAt()
        );
    }
}
