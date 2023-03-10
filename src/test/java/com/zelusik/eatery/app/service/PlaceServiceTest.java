package com.zelusik.eatery.app.service;

import com.zelusik.eatery.app.domain.constant.DayOfWeek;
import com.zelusik.eatery.app.domain.place.OpeningHours;
import com.zelusik.eatery.app.domain.place.Place;
import com.zelusik.eatery.app.dto.place.OpeningHoursTimeDto;
import com.zelusik.eatery.app.dto.place.PlaceDto;
import com.zelusik.eatery.app.dto.place.request.PlaceRequest;
import com.zelusik.eatery.app.repository.OpeningHoursRepository;
import com.zelusik.eatery.app.repository.PlaceRepository;
import com.zelusik.eatery.global.exception.place.PlaceNotFoundException;
import com.zelusik.eatery.global.exception.scraping.OpeningHoursUnexpectedFormatException;
import com.zelusik.eatery.util.PlaceTestUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.util.StringUtils;

import java.time.LocalTime;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static com.zelusik.eatery.app.domain.constant.DayOfWeek.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@DisplayName("[Service] Place")
@ExtendWith(MockitoExtension.class)
class PlaceServiceTest {

    @InjectMocks
    private PlaceService sut;

    @Mock
    private PlaceRepository placeRepository;
    @Mock
    private OpeningHoursRepository openingHoursRepository;

    @DisplayName("'매일'이 적힌 영업시간이 포함된 장소 정보가 주어지면 장소를 생성 및 저장하고 저장된 장소를 반환한다.")
    @MethodSource("openingHoursEveryDaysExamples")
    @ParameterizedTest(name = "영업시간: {0}, 휴무일: {1}")
    void givenPlaceInfoWithOpeningHoursEveryDays_whenCreatePlace_thenReturnSavedPlace(
            String openingHours,
            String closingHours,
            Map<DayOfWeek, OpeningHoursTimeDto> expectedOpeningHoursResult
    ) {
        // given
        PlaceRequest placeRequest = PlaceTestUtils.createPlaceRequest();
        String homepageUrl = "www.instagram.com/toma_wv";
        Place expectedSavedPlace = PlaceTestUtils.createPlaceWithId(homepageUrl, closingHours);
        given(placeRepository.save(any(Place.class))).willReturn(expectedSavedPlace);
        given(openingHoursRepository.saveAll(any())).willReturn(any());

        // when
        Place actualSavedPlace = sut.create(placeRequest, homepageUrl, openingHours, closingHours);

        // then
        then(placeRepository).should().save(any(Place.class));
        assertThat(actualSavedPlace.getKakaoPid()).isEqualTo(placeRequest.getKakaoPid());
        actualSavedPlace.getOpeningHoursList()
                .forEach(oh -> {
                    OpeningHoursTimeDto expectedTime = expectedOpeningHoursResult.get(oh.getDayOfWeek());
                    assertThat(oh.getOpenAt()).isEqualTo(expectedTime.openAt());
                    assertThat(oh.getCloseAt()).isEqualTo(expectedTime.closeAt());
                });
        assertThat(actualSavedPlace.getClosingHours()).isEqualTo(closingHours);
    }

    @DisplayName("'~'이 적힌 영업시간이 포함된 장소 정보가 주어지면 장소를 생성 및 저장하고 저장된 장소를 반환한다.")
    @MethodSource("openingHoursContainsTildeExamples")
    @ParameterizedTest(name = "영업시간: {0}, 휴무일: {1}")
    void givenPlaceInfoWithOpeningHoursContainsTilde_whenCreatePlace_thenReturnSavedPlace(
            String openingHours,
            String closingHours,
            Map<DayOfWeek, OpeningHoursTimeDto> expectedOpeningHoursResult
    ) {
        // given
        PlaceRequest placeRequest = PlaceTestUtils.createPlaceRequest();
        String homepageUrl = "www.instagram.com/toma_wv";
        Place expectedSavedPlace = PlaceTestUtils.createPlaceWithId(homepageUrl, closingHours);
        given(placeRepository.save(any(Place.class))).willReturn(expectedSavedPlace);
        given(openingHoursRepository.saveAll(any())).willReturn(any());

        // when
        Place actualSavedPlace = sut.create(placeRequest, homepageUrl, openingHours, closingHours);

        // then
        int wantedNumOfInvocationsOfSaveAll = StringUtils.countOccurrencesOf(openingHours, "\n") + 1;
        then(placeRepository).should().save(any(Place.class));
        verify(openingHoursRepository, times(wantedNumOfInvocationsOfSaveAll)).saveAll(any());
        assertThat(actualSavedPlace.getKakaoPid()).isEqualTo(placeRequest.getKakaoPid());
        actualSavedPlace.getOpeningHoursList()
                .forEach(oh -> {
                    OpeningHoursTimeDto expectedTime = expectedOpeningHoursResult.get(oh.getDayOfWeek());
                    assertThat(oh.getOpenAt()).isEqualTo(expectedTime.openAt());
                    assertThat(oh.getCloseAt()).isEqualTo(expectedTime.closeAt());
                });
        assertThat(actualSavedPlace.getClosingHours()).isEqualTo(closingHours);
    }

    @DisplayName("','로 구분된 영업시간이 포함된 장소 정보가 주어지면 장소를 생성 및 저장하고 저장된 장소를 반환한다.")
    @MethodSource("openingHoursCommaSeperatedExamples")
    @ParameterizedTest(name = "영업시간: {0}, 휴무일: {1}")
    void givenPlaceInfoWithOpeningHoursCommaSeperated_whenCreatePlace_thenReturnSavedPlace(
            String openingHours,
            String closingHours,
            Map<DayOfWeek, OpeningHoursTimeDto> expectedOpeningHoursResult
    ) {
        // given
        PlaceRequest placeRequest = PlaceTestUtils.createPlaceRequest();
        String homepageUrl = "www.instagram.com/toma_wv";
        Place expectedSavedPlace = PlaceTestUtils.createPlaceWithId(homepageUrl, closingHours);
        given(placeRepository.save(any(Place.class))).willReturn(expectedSavedPlace);
        given(openingHoursRepository.saveAll(any())).willReturn(any());

        // when
        Place actualSavedPlace = sut.create(placeRequest, homepageUrl, openingHours, closingHours);

        // then
        then(placeRepository).should().save(any(Place.class));
        then(openingHoursRepository).should().saveAll(any());
        assertThat(actualSavedPlace.getKakaoPid()).isEqualTo(placeRequest.getKakaoPid());
        actualSavedPlace.getOpeningHoursList()
                .forEach(oh -> {
                    OpeningHoursTimeDto expectedTime = expectedOpeningHoursResult.get(oh.getDayOfWeek());
                    assertThat(oh.getOpenAt()).isEqualTo(expectedTime.openAt());
                    assertThat(oh.getCloseAt()).isEqualTo(expectedTime.closeAt());
                });
        assertThat(actualSavedPlace.getClosingHours()).isEqualTo(closingHours);
    }

    @DisplayName("','로 구분된 영업시간과 단일 요일 정보가 포함된 장소 정보가 주어지면 장소를 생성 및 저장하고 저장된 장소를 반환한다.")
    @Test
    void givenPlaceInfoWithOpeningHoursCommaSeperatedWithSingleDay_whenCreatePlace_thenReturnSavedPlace() {
        // given
        PlaceRequest placeRequest = PlaceTestUtils.createPlaceRequest();
        String homepageUrl = "www.instagram.com/toma_wv";
        String openingHours = "월,화,수,토,일 11:00 ~ 19:00\n목 09:00 ~ 18:00";
        String closingHours = "금요일";
        Map<DayOfWeek, OpeningHoursTimeDto> expectedOpeningHoursResult = Map.of(
                MON, new OpeningHoursTimeDto(LocalTime.of(11, 0), LocalTime.of(19, 0)),
                TUE, new OpeningHoursTimeDto(LocalTime.of(11, 0), LocalTime.of(19, 0)),
                WED, new OpeningHoursTimeDto(LocalTime.of(11, 0), LocalTime.of(19, 0)),
                THU, new OpeningHoursTimeDto(LocalTime.of(9, 0), LocalTime.of(18, 0)),
                SAT, new OpeningHoursTimeDto(LocalTime.of(11, 0), LocalTime.of(19, 0)),
                SUN, new OpeningHoursTimeDto(LocalTime.of(11, 0), LocalTime.of(19, 0))
        );
        Place expectedSavedPlace = PlaceTestUtils.createPlaceWithId(homepageUrl, closingHours);
        given(placeRepository.save(any(Place.class))).willReturn(expectedSavedPlace);
        given(openingHoursRepository.save(any(OpeningHours.class)))
                .willReturn(OpeningHours.of(
                        expectedSavedPlace,
                        THU,
                        LocalTime.of(9, 0),
                        LocalTime.of(18, 0)
                ));
        given(openingHoursRepository.saveAll(any())).willReturn(any());

        // when
        Place actualSavedPlace = sut.create(placeRequest, homepageUrl, openingHours, closingHours);

        // then
        then(placeRepository).should().save(any(Place.class));
        then(openingHoursRepository).should().save(any());
        then(openingHoursRepository).should().saveAll(any());
        assertThat(actualSavedPlace.getKakaoPid()).isEqualTo(placeRequest.getKakaoPid());
        actualSavedPlace.getOpeningHoursList()
                .forEach(oh -> {
                    OpeningHoursTimeDto expectedTime = expectedOpeningHoursResult.get(oh.getDayOfWeek());
                    assertThat(oh.getOpenAt()).isEqualTo(expectedTime.openAt());
                    assertThat(oh.getCloseAt()).isEqualTo(expectedTime.closeAt());
                });
        assertThat(actualSavedPlace.getClosingHours()).isEqualTo(closingHours);
    }

    @DisplayName("처리할 수 없는 형태의 영업시간 정보가 주어지고, 장소를 생성하면, 예외가 발생한다.")
    @Test
    void givenUnexpectedFormatOpeningHoursInfo_whenCreatePlace_thenThrowException() {
        // given
        PlaceRequest placeRequest = PlaceTestUtils.createPlaceRequest();
        String homepageUrl = "www.instagram.com/toma_wv";
        String closingHours = null;

        // when
        Throwable t = catchThrowable(() -> sut.create(placeRequest, homepageUrl, "처리할 수 없는 값", closingHours));

        // then
        then(placeRepository).shouldHaveNoInteractions();
        then(openingHoursRepository).shouldHaveNoInteractions();
        assertThat(t).isInstanceOf(OpeningHoursUnexpectedFormatException.class);
    }
    
    @DisplayName("Id(PK)가 주어지고, 일치하는 장소를 찾으면, 장소 정보를 반환한다.")
    @Test
    void givenId_whenFindExistentPlace_thenReturnPlaceDto() {
        // given
        long placeId = 1L;
        given(placeRepository.findById(placeId)).willReturn(Optional.of(PlaceTestUtils.createPlaceWithId()));
        
        // when
        PlaceDto findDto = sut.findDtoById(placeId);

        // then
        then(placeRepository).should().findById(placeId);
        assertThat(findDto.id()).isEqualTo(placeId);
    }
    
    @DisplayName("Id(PK)가 주어지고, 존재하지 않는 장소를 찾으면, 예외가 발생한다.")
    @Test
    void givenId_whenFindNotExistentPlace_thenReturnThrowException() {
        // given
        long placeId = 1L;
        given(placeRepository.findById(placeId)).willReturn(Optional.empty());
        
        // when
        Throwable t = catchThrowable(() -> sut.findDtoById(placeId));

        // then
        then(placeRepository).should().findById(placeId);
        assertThat(t).isInstanceOf(PlaceNotFoundException.class);
    }

    @DisplayName("kakaoPid가 주어지고, 일치하는 장소를 찾으면, 장소를 반환한다.")
    @Test
    void givenKakaoPid_whenFindExistentPlace_thenReturnOptionalPlace() {
        // given
        String kakaoPid = "test";
        Place expectedPlace = PlaceTestUtils.createPlaceWithId();
        given(placeRepository.findByKakaoPid(kakaoPid)).willReturn(Optional.of(expectedPlace));

        // when
        Optional<Place> actualPlace = sut.findOptEntityByKakaoPid(kakaoPid);

        // then
        then(placeRepository).should().findByKakaoPid(kakaoPid);
        assertThat(actualPlace.get().getId()).isEqualTo(expectedPlace.getId());
    }

    @DisplayName("kakaoPid가 주어지고, kakaoPid에 일치하는 장소가 없댜면, 비어있는 Optional을 반환한다.")
    @Test
    void givenKakaoPid_whenFindNotExistentPlace_thenReturnEmptyOptional() {
        // given
        String kakaoPid = "test";
        given(placeRepository.findByKakaoPid(kakaoPid)).willReturn(Optional.empty());

        // when
        Optional<Place> actualPlace = sut.findOptEntityByKakaoPid(kakaoPid);

        // then
        then(placeRepository).should().findByKakaoPid(kakaoPid);
        assertThat(actualPlace).isEmpty();
    }

    static Stream<Arguments> openingHoursEveryDaysExamples() {
        return Stream.of(
                arguments("매일 11:30 ~ 22:00", null, Map.of(
                        MON, new OpeningHoursTimeDto(LocalTime.of(11, 30), LocalTime.of(22, 0)),
                        TUE, new OpeningHoursTimeDto(LocalTime.of(11, 30), LocalTime.of(22, 0)),
                        WED, new OpeningHoursTimeDto(LocalTime.of(11, 30), LocalTime.of(22, 0)),
                        THU, new OpeningHoursTimeDto(LocalTime.of(11, 30), LocalTime.of(22, 0)),
                        FRI, new OpeningHoursTimeDto(LocalTime.of(11, 30), LocalTime.of(22, 0)),
                        SAT, new OpeningHoursTimeDto(LocalTime.of(11, 30), LocalTime.of(22, 0)),
                        SUN, new OpeningHoursTimeDto(LocalTime.of(11, 30), LocalTime.of(22, 0))
                )),
                arguments("매일 07:30 ~ 24:00", null, Map.of(
                        MON, new OpeningHoursTimeDto(LocalTime.of(7, 30), LocalTime.of(23, 59)),
                        TUE, new OpeningHoursTimeDto(LocalTime.of(7, 30), LocalTime.of(23, 59)),
                        WED, new OpeningHoursTimeDto(LocalTime.of(7, 30), LocalTime.of(23, 59)),
                        THU, new OpeningHoursTimeDto(LocalTime.of(7, 30), LocalTime.of(23, 59)),
                        FRI, new OpeningHoursTimeDto(LocalTime.of(7, 30), LocalTime.of(23, 59)),
                        SAT, new OpeningHoursTimeDto(LocalTime.of(7, 30), LocalTime.of(23, 59)),
                        SUN, new OpeningHoursTimeDto(LocalTime.of(7, 30), LocalTime.of(23, 59))
                )),
                arguments(" 매일 12:00 ~ 20:00", null, Map.of(
                        MON, new OpeningHoursTimeDto(LocalTime.of(12, 0), LocalTime.of(20, 0)),
                        TUE, new OpeningHoursTimeDto(LocalTime.of(12, 0), LocalTime.of(20, 0)),
                        WED, new OpeningHoursTimeDto(LocalTime.of(12, 0), LocalTime.of(20, 0)),
                        THU, new OpeningHoursTimeDto(LocalTime.of(12, 0), LocalTime.of(20, 0)),
                        FRI, new OpeningHoursTimeDto(LocalTime.of(12, 0), LocalTime.of(20, 0)),
                        SAT, new OpeningHoursTimeDto(LocalTime.of(12, 0), LocalTime.of(20, 0)),
                        SUN, new OpeningHoursTimeDto(LocalTime.of(12, 0), LocalTime.of(20, 0))
                ))
        );
    }

    static Stream<Arguments> openingHoursContainsTildeExamples() {
        return Stream.of(
                arguments("화~일 10:30 ~ 15:30", "월요일", Map.of(
                        TUE, new OpeningHoursTimeDto(LocalTime.of(10, 30), LocalTime.of(15, 30)),
                        WED, new OpeningHoursTimeDto(LocalTime.of(10, 30), LocalTime.of(15, 30)),
                        THU, new OpeningHoursTimeDto(LocalTime.of(10, 30), LocalTime.of(15, 30)),
                        FRI, new OpeningHoursTimeDto(LocalTime.of(10, 30), LocalTime.of(15, 30)),
                        SAT, new OpeningHoursTimeDto(LocalTime.of(10, 30), LocalTime.of(15, 30)),
                        SUN, new OpeningHoursTimeDto(LocalTime.of(10, 30), LocalTime.of(15, 30))
                )),
                arguments("월~토 18:00 ~ 02:00", "일요일", Map.of(
                        MON, new OpeningHoursTimeDto(LocalTime.of(18, 0), LocalTime.of(2, 0)),
                        TUE, new OpeningHoursTimeDto(LocalTime.of(18, 0), LocalTime.of(2, 0)),
                        WED, new OpeningHoursTimeDto(LocalTime.of(18, 0), LocalTime.of(2, 0)),
                        THU, new OpeningHoursTimeDto(LocalTime.of(18, 0), LocalTime.of(2, 0)),
                        FRI, new OpeningHoursTimeDto(LocalTime.of(18, 0), LocalTime.of(2, 0)),
                        SAT, new OpeningHoursTimeDto(LocalTime.of(18, 0), LocalTime.of(2, 0))
                )),
                arguments("월~수 12:00 ~ 18:00\n목~토 18:00 ~ 22:00", "일요일", Map.of(
                        MON, new OpeningHoursTimeDto(LocalTime.of(12, 0), LocalTime.of(18, 0)),
                        TUE, new OpeningHoursTimeDto(LocalTime.of(12, 0), LocalTime.of(18, 0)),
                        WED, new OpeningHoursTimeDto(LocalTime.of(12, 0), LocalTime.of(18, 0)),
                        THU, new OpeningHoursTimeDto(LocalTime.of(18, 0), LocalTime.of(22, 0)),
                        FRI, new OpeningHoursTimeDto(LocalTime.of(18, 0), LocalTime.of(22, 0)),
                        SAT, new OpeningHoursTimeDto(LocalTime.of(18, 0), LocalTime.of(22, 0))
                ))
        );
    }

    static Stream<Arguments> openingHoursCommaSeperatedExamples() {
        return Stream.of(
                arguments("월,화,목,금,토,일 10:00 ~ 19:30", "수요일", Map.of(
                        MON, new OpeningHoursTimeDto(LocalTime.of(10, 0), LocalTime.of(19, 30)),
                        TUE, new OpeningHoursTimeDto(LocalTime.of(10, 0), LocalTime.of(19, 30)),
                        THU, new OpeningHoursTimeDto(LocalTime.of(10, 0), LocalTime.of(19, 30)),
                        FRI, new OpeningHoursTimeDto(LocalTime.of(10, 0), LocalTime.of(19, 30)),
                        SAT, new OpeningHoursTimeDto(LocalTime.of(10, 0), LocalTime.of(19, 30)),
                        SUN, new OpeningHoursTimeDto(LocalTime.of(10, 0), LocalTime.of(19, 30))
                ))
        );
    }
}