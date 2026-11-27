/*
 * This program is part of the OpenLMIS logistics management information system platform software.
 * Copyright © 2017 VillageReach
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of the GNU Affero General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details. You should have received a copy of
 * the GNU Affero General Public License along with this program. If not, see
 * http://www.gnu.org/licenses.  For additional information contact info@OpenLMIS.org.
 */

package org.openlmis.buq.web.forecasting;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.jayway.restassured.response.ValidatableResponse;
import guru.nidi.ramltester.junit.RamlMatchers;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.openlmis.buq.builder.PopulationSegmentDataBuilder;
import org.openlmis.buq.domain.forecasting.PopulationSegment;
import org.openlmis.buq.dto.forecasting.PopulationSegmentDto;
import org.openlmis.buq.i18n.MessageKeys;
import org.openlmis.buq.service.role.ForecastingRights;
import org.openlmis.buq.web.BaseWebIntegrationTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

@SuppressWarnings("PMD.TooManyMethods")
public class PopulationSegmentControllerIntegrationTest extends BaseWebIntegrationTest {

  private static final String RESOURCE_URL = PopulationSegmentController.RESOURCE_PATH;
  private static final String ID_URL = RESOURCE_URL + "/{id}";

  private static final String CODE = "code";
  private static final String MESSAGE = "message";

  private final PopulationSegment segment = new PopulationSegmentDataBuilder().build();
  private final PopulationSegmentDto segmentDto = PopulationSegmentDto.newInstance(segment);

  @Before
  public void setUp() {
    given(populationSegmentRepository.saveAndFlush(any(PopulationSegment.class)))
        .willAnswer(new SaveAnswer<>());
  }

  @Test
  public void shouldReturnPageOfSegmentsSortedByCodeByDefault() {
    mockUserHasRight(ForecastingRights.MANAGE);
    given(populationSegmentRepository.findAll(any(Pageable.class)))
        .willReturn(new PageImpl<>(Collections.singletonList(segment)));

    restAssured.given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .queryParam("page", 0)
        .queryParam("size", 10)
        .when()
        .get(RESOURCE_URL)
        .then()
        .statusCode(HttpStatus.SC_OK)
        .body("content", hasSize(1))
        .body("content[0].id", is(segment.getId().toString()))
        .body("content[0].code", is(segment.getCode()));

    ArgumentCaptor<Pageable> pageable = ArgumentCaptor.forClass(Pageable.class);
    verify(populationSegmentRepository).findAll(pageable.capture());
    assertEquals(Arrays.asList(Sort.Order.asc(CODE), Sort.Order.asc(ID)),
        pageable.getValue().getSort().toList());
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldBreakClientSortTiesById() {
    mockUserHasRight(ForecastingRights.MANAGE);
    given(populationSegmentRepository.findAll(any(Pageable.class)))
        .willReturn(new PageImpl<>(Collections.singletonList(segment)));

    restAssured.given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .queryParam("page", 1)
        .queryParam("size", 1)
        .queryParam("sort", "populationPercentage,desc")
        .when()
        .get(RESOURCE_URL)
        .then()
        .statusCode(HttpStatus.SC_OK);

    ArgumentCaptor<Pageable> pageable = ArgumentCaptor.forClass(Pageable.class);
    verify(populationSegmentRepository).findAll(pageable.capture());
    assertEquals(1, pageable.getValue().getPageNumber());
    assertEquals(1, pageable.getValue().getPageSize());
    assertEquals(Arrays.asList(Sort.Order.desc("populationPercentage"), Sort.Order.asc(ID)),
        pageable.getValue().getSort().toList());
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnForbiddenForSegmentsPageWithoutRight() {
    mockUserHasNoRight(ForecastingRights.MANAGE);

    restAssured.given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .when()
        .get(RESOURCE_URL)
        .then()
        .statusCode(HttpStatus.SC_FORBIDDEN);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnUnauthorizedForSegmentsPageWithoutToken() {
    restAssured.given()
        .when()
        .get(RESOURCE_URL)
        .then()
        .statusCode(HttpStatus.SC_UNAUTHORIZED);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldCreateSegment() {
    mockUserHasRight(ForecastingRights.MANAGE);

    post(segmentDto)
        .statusCode(HttpStatus.SC_CREATED)
        .body(ID, is(notNullValue()))
        .body(CODE, is(segmentDto.getCode()));

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnBadRequestNamingCodeWhenCodeIsTooLong() {
    mockUserHasRight(ForecastingRights.MANAGE);
    segmentDto.setCode(StringUtils.repeat('C', 51));

    post(segmentDto)
        .statusCode(HttpStatus.SC_BAD_REQUEST)
        .body(MESSAGE_KEY, is(MessageKeys.ERROR_VALIDATION_FIELD_INVALID))
        .body(MESSAGE, containsString(CODE));

    verify(populationSegmentRepository, never()).saveAndFlush(any(PopulationSegment.class));
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnBadRequestWhenCodeIsDuplicated() {
    mockUserHasRight(ForecastingRights.MANAGE);
    given(populationSegmentRepository.saveAndFlush(any(PopulationSegment.class)))
        .willThrow(new DataIntegrityViolationException(null, new ConstraintViolationException(
            null, null, "population_segments_code_unique")));

    post(segmentDto)
        .statusCode(HttpStatus.SC_BAD_REQUEST)
        .body(MESSAGE_KEY, is(MessageKeys.ERROR_POPULATION_SEGMENT_CODE_DUPLICATED));

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnForbiddenForCreateWithoutRight() {
    mockUserHasNoRight(ForecastingRights.MANAGE);

    post(segmentDto)
        .statusCode(HttpStatus.SC_FORBIDDEN);

    verify(populationSegmentRepository, never()).saveAndFlush(any(PopulationSegment.class));
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnUnauthorizedForCreateWithoutToken() {
    restAssured.given()
        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        .body(segmentDto)
        .when()
        .post(RESOURCE_URL)
        .then()
        .statusCode(HttpStatus.SC_UNAUTHORIZED);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnGivenSegment() {
    mockUserHasRight(ForecastingRights.MANAGE);
    given(populationSegmentRepository.findById(segment.getId()))
        .willReturn(Optional.of(segment));

    get(segment.getId())
        .statusCode(HttpStatus.SC_OK)
        .body(ID, is(segment.getId().toString()))
        .body(CODE, is(segment.getCode()));

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnNotFoundForMissingSegment() {
    mockUserHasRight(ForecastingRights.MANAGE);
    given(populationSegmentRepository.findById(segment.getId())).willReturn(Optional.empty());

    get(segment.getId())
        .statusCode(HttpStatus.SC_NOT_FOUND)
        .body(MESSAGE_KEY, is(MessageKeys.ERROR_POPULATION_SEGMENT_NOT_FOUND));

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnForbiddenRatherThanNotFoundForMissingSegmentWithoutRight() {
    mockUserHasNoRight(ForecastingRights.MANAGE);
    given(populationSegmentRepository.findById(segment.getId())).willReturn(Optional.empty());

    get(segment.getId())
        .statusCode(HttpStatus.SC_FORBIDDEN);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnUnauthorizedForGetSegmentWithoutToken() {
    restAssured.given()
        .pathParam(ID, segment.getId().toString())
        .when()
        .get(ID_URL)
        .then()
        .statusCode(HttpStatus.SC_UNAUTHORIZED);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldUpdateSegment() {
    mockUserHasRight(ForecastingRights.MANAGE);
    given(populationSegmentRepository.findById(segment.getId()))
        .willReturn(Optional.of(segment));
    segmentDto.setName("Pregnant women");

    put(segment.getId(), segmentDto)
        .statusCode(HttpStatus.SC_OK)
        .body(ID, is(segment.getId().toString()))
        .body("name", is("Pregnant women"));

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnNotFoundWhenUpdatingMissingSegment() {
    mockUserHasRight(ForecastingRights.MANAGE);
    given(populationSegmentRepository.findById(segment.getId())).willReturn(Optional.empty());

    put(segment.getId(), segmentDto)
        .statusCode(HttpStatus.SC_NOT_FOUND)
        .body(MESSAGE_KEY, is(MessageKeys.ERROR_POPULATION_SEGMENT_NOT_FOUND));

    verify(populationSegmentRepository, never()).saveAndFlush(any(PopulationSegment.class));
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnBadRequestWhenUpdatedIdDiffersFromPath() {
    mockUserHasRight(ForecastingRights.MANAGE);

    put(UUID.randomUUID(), segmentDto)
        .statusCode(HttpStatus.SC_BAD_REQUEST)
        .body(MESSAGE_KEY, is(MessageKeys.ERROR_POPULATION_SEGMENT_ID_MISMATCH));

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnForbiddenForUpdateWithoutRight() {
    mockUserHasNoRight(ForecastingRights.MANAGE);

    put(segment.getId(), segmentDto)
        .statusCode(HttpStatus.SC_FORBIDDEN);

    verify(populationSegmentRepository, never()).saveAndFlush(any(PopulationSegment.class));
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnUnauthorizedForUpdateWithoutToken() {
    restAssured.given()
        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        .pathParam(ID, segment.getId().toString())
        .body(segmentDto)
        .when()
        .put(ID_URL)
        .then()
        .statusCode(HttpStatus.SC_UNAUTHORIZED);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldDeleteUnusedSegment() {
    mockUserHasRight(ForecastingRights.MANAGE);
    given(populationSegmentRepository.existsById(segment.getId())).willReturn(true);

    delete(segment.getId())
        .statusCode(HttpStatus.SC_NO_CONTENT);

    verify(populationSegmentRepository).deleteById(segment.getId());
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnNotFoundWhenDeletingMissingSegment() {
    mockUserHasRight(ForecastingRights.MANAGE);
    given(populationSegmentRepository.existsById(segment.getId())).willReturn(false);

    delete(segment.getId())
        .statusCode(HttpStatus.SC_NOT_FOUND)
        .body(MESSAGE_KEY, is(MessageKeys.ERROR_POPULATION_SEGMENT_NOT_FOUND));

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnBadRequestWhenDeletingSegmentUsedBySetting() {
    mockUserHasRight(ForecastingRights.MANAGE);
    given(populationSegmentRepository.existsById(segment.getId())).willReturn(true);
    given(forecastingSettingRepository.existsByPopulationSegmentId(segment.getId()))
        .willReturn(true);

    delete(segment.getId())
        .statusCode(HttpStatus.SC_BAD_REQUEST)
        .body(MESSAGE_KEY, is(MessageKeys.ERROR_POPULATION_SEGMENT_IN_USE));

    verify(populationSegmentRepository, never()).deleteById(any(UUID.class));
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnBadRequestWhenDeletingSegmentUsedOnlyByForecastLineItem() {
    mockUserHasRight(ForecastingRights.MANAGE);
    given(populationSegmentRepository.existsById(segment.getId())).willReturn(true);
    given(populationSegmentRepository.isReferencedByForecastLineItem(segment.getId()))
        .willReturn(true);

    delete(segment.getId())
        .statusCode(HttpStatus.SC_BAD_REQUEST)
        .body(MESSAGE_KEY, is(MessageKeys.ERROR_POPULATION_SEGMENT_IN_USE));

    verify(populationSegmentRepository, never()).deleteById(any(UUID.class));
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnForbiddenForDeleteWithoutRight() {
    mockUserHasNoRight(ForecastingRights.MANAGE);

    delete(segment.getId())
        .statusCode(HttpStatus.SC_FORBIDDEN);

    verify(populationSegmentRepository, never()).deleteById(any(UUID.class));
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnUnauthorizedForDeleteWithoutToken() {
    restAssured.given()
        .pathParam(ID, segment.getId().toString())
        .when()
        .delete(ID_URL)
        .then()
        .statusCode(HttpStatus.SC_UNAUTHORIZED);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  private ValidatableResponse post(PopulationSegmentDto body) {
    return restAssured.given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        .body(body)
        .when()
        .post(RESOURCE_URL)
        .then();
  }

  private ValidatableResponse get(UUID id) {
    return restAssured.given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .pathParam(ID, id.toString())
        .when()
        .get(ID_URL)
        .then();
  }

  private ValidatableResponse put(UUID id, PopulationSegmentDto body) {
    return restAssured.given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        .pathParam(ID, id.toString())
        .body(body)
        .when()
        .put(ID_URL)
        .then();
  }

  private ValidatableResponse delete(UUID id) {
    return restAssured.given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .pathParam(ID, id.toString())
        .when()
        .delete(ID_URL)
        .then();
  }

}
