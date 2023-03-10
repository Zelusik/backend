package com.zelusik.eatery.app.domain;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Entity
public class TermsInfo extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "terms_info_id")
    private Long id;

    @Column(nullable = false)
    private Boolean isMinor;

    @Column(nullable = false)
    private Boolean service;
    @Column(nullable = false)
    private LocalDateTime serviceUpdatedAt;

    @Column(nullable = false)
    private Boolean userInfo;
    @Column(nullable = false)
    private LocalDateTime userInfoUpdatedAt;

    @Column(nullable = false)
    private Boolean locationInfo;
    @Column(nullable = false)
    private LocalDateTime locationInfoUpdatedAt;

    @Column(nullable = false)
    private Boolean marketingReception;
    @Column(nullable = false)
    private LocalDateTime marketingReceptionUpdatedAt;

    public static TermsInfo of(Boolean isMinor, Boolean service, LocalDateTime serviceUpdatedAt, Boolean userInfo, LocalDateTime userInfoUpdatedAt, Boolean locationInfo, LocalDateTime locationInfoUpdatedAt, Boolean marketingReception, LocalDateTime marketingReceptionUpdatedAt) {
        return new TermsInfo(isMinor, service, serviceUpdatedAt, userInfo, userInfoUpdatedAt, locationInfo, locationInfoUpdatedAt, marketingReception, marketingReceptionUpdatedAt);
    }

    private TermsInfo(Boolean isMinor, Boolean service, LocalDateTime serviceUpdatedAt, Boolean userInfo, LocalDateTime userInfoUpdatedAt, Boolean locationInfo, LocalDateTime locationInfoUpdatedAt, Boolean marketingReception, LocalDateTime marketingReceptionUpdatedAt) {
        this.isMinor = isMinor;
        this.service = service;
        this.serviceUpdatedAt = serviceUpdatedAt;
        this.userInfo = userInfo;
        this.userInfoUpdatedAt = userInfoUpdatedAt;
        this.locationInfo = locationInfo;
        this.locationInfoUpdatedAt = locationInfoUpdatedAt;
        this.marketingReception = marketingReception;
        this.marketingReceptionUpdatedAt = marketingReceptionUpdatedAt;
    }
}
