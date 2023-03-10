package com.zelusik.eatery.app.dto.place.response;

import com.zelusik.eatery.app.dto.place.OpeningHoursDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalTime;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class OpeningHoursResponse {

    @Schema(description = "영업시간 id(PK)", example = "1")
    private Long id;

    @Schema(description = "요일", example = "월")
    private String dayOfWeek;

    @Schema(description = "영업시작 시간", example = "12:00")
    private String openAt;

    @Schema(description = "영업종료 시간", example = "22:30")
    private String closeAt;

    public static OpeningHoursResponse from(OpeningHoursDto openingHoursDto) {
        return new OpeningHoursResponse(
                openingHoursDto.id(),
                openingHoursDto.dayOfWeek().getDescription(),
                parseTimeResponseOf(openingHoursDto.openAt()),
                parseTimeResponseOf(openingHoursDto.closeAt())
        );
    }

    /**
     * 12:30:00 => 12:30
     *
     * @param time
     * @return
     */
    private static String parseTimeResponseOf(LocalTime time) {
        return time.toString().substring(0, 5);
    }
}
