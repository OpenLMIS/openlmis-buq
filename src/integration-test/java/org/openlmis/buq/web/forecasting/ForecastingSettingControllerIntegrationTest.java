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
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.jayway.restassured.response.ValidatableResponse;
import guru.nidi.ramltester.junit.RamlMatchers;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;
import org.apache.http.HttpStatus;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.Test;
import org.openlmis.buq.builder.ForecastingSettingDataBuilder;
import org.openlmis.buq.builder.PopulationSegmentDataBuilder;
import org.openlmis.buq.domain.forecasting.ForecastingSetting;
import org.openlmis.buq.dto.forecasting.ForecastingSettingDto;
import org.openlmis.buq.exception.NotFoundException;
import org.openlmis.buq.exception.ValidationMessageException;
import org.openlmis.buq.i18n.MessageKeys;
import org.openlmis.buq.service.role.ForecastingRights;
import org.openlmis.buq.util.Message;
import org.openlmis.buq.web.BaseWebIntegrationTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

@SuppressWarnings("PMD.TooManyMethods")
public class ForecastingSettingControllerIntegrationTest extends BaseWebIntegrationTest {

  private static final String RESOURCE_URL = ForecastingSettingController.RESOURCE_PATH;
  private static final String ID_URL = RESOURCE_URL + "/{id}";

  private static final String PROGRAM_ID = "programId";
  private static final String ORDERABLE_ID = "orderableId";
  private static final String POPULATION_SEGMENT_ID = "populationSegmentId";
  private static final String MESSAGE = "message";

  private final ForecastingSetting setting = new ForecastingSettingDataBuilder()
      .withPopulationSegment(new PopulationSegmentDataBuilder().build())
      .build();
  private final ForecastingSettingDto settingDto = ForecastingSettingDto.newInstance(setting);

  @Test
  public void shouldSearchSettingsByProgramAndOrderable() {
    mockUserHasRight(ForecastingRights.MANAGE);
    given(forecastingSettingRepository.search(eq(setting.getProgramId()),
        eq(setting.getOrderableId()), any(Pageable.class)))
        .willReturn(new PageImpl<>(Collections.singletonList(setting)));

    restAssured.given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .queryParam(PROGRAM_ID, setting.getProgramId().toString())
        .queryParam(ORDERABLE_ID, setting.getOrderableId().toString())
        .when()
        .get(RESOURCE_URL)
        .then()
        .statusCode(HttpStatus.SC_OK)
        .body("content", hasSize(1))
        .body("content[0].id", is(setting.getId().toString()))
        .body("content[0].populationSegmentId",
            is(setting.getPopulationSegment().getId().toString()));

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnForbiddenForSearchWithoutRight() {
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
  public void shouldReturnUnauthorizedForSearchWithoutToken() {
    restAssured.given()
        .when()
        .get(RESOURCE_URL)
        .then()
        .statusCode(HttpStatus.SC_UNAUTHORIZED);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldCreateSetting() {
    mockUserHasRight(ForecastingRights.MANAGE);
    given(forecastingSettingService.create(any(ForecastingSettingDto.class))).willReturn(setting);

    post(settingDto)
        .statusCode(HttpStatus.SC_CREATED)
        .body(ID, is(setting.getId().toString()))
        .body(POPULATION_SEGMENT_ID, is(setting.getPopulationSegment().getId().toString()))
        .body("bufferMonths", is(1));

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnBadRequestNamingWastageWhenWastageIs100() {
    mockUserHasRight(ForecastingRights.MANAGE);
    settingDto.setWastagePercentage(new BigDecimal("100"));

    post(settingDto)
        .statusCode(HttpStatus.SC_BAD_REQUEST)
        .body(MESSAGE_KEY, is(MessageKeys.ERROR_VALIDATION_FIELD_INVALID))
        .body(MESSAGE, containsString("wastagePercentage"));

    verify(forecastingSettingService, never()).create(any(ForecastingSettingDto.class));
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnBadRequestWhenCommodityTypeIsTaken() {
    mockUserHasRight(ForecastingRights.MANAGE);
    given(forecastingSettingService.create(any(ForecastingSettingDto.class)))
        .willThrow(new ValidationMessageException(new Message(
            MessageKeys.ERROR_FORECASTING_SETTING_COMMODITY_TYPE_TAKEN, "P1", "bcg", "P2")));

    post(settingDto)
        .statusCode(HttpStatus.SC_BAD_REQUEST)
        .body(MESSAGE_KEY, is(MessageKeys.ERROR_FORECASTING_SETTING_COMMODITY_TYPE_TAKEN));

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnBadRequestWhenSettingIsDuplicated() {
    mockUserHasRight(ForecastingRights.MANAGE);
    given(forecastingSettingService.create(any(ForecastingSettingDto.class)))
        .willThrow(new DataIntegrityViolationException(null, new ConstraintViolationException(
            null, null, "forecasting_settings_program_orderable_unique")));

    post(settingDto)
        .statusCode(HttpStatus.SC_BAD_REQUEST)
        .body(MESSAGE_KEY, is(MessageKeys.ERROR_FORECASTING_SETTING_DUPLICATED));

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnForbiddenForCreateWithoutRight() {
    mockUserHasNoRight(ForecastingRights.MANAGE);

    post(settingDto)
        .statusCode(HttpStatus.SC_FORBIDDEN);

    verify(forecastingSettingService, never()).create(any(ForecastingSettingDto.class));
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnUnauthorizedForCreateWithoutToken() {
    restAssured.given()
        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        .body(settingDto)
        .when()
        .post(RESOURCE_URL)
        .then()
        .statusCode(HttpStatus.SC_UNAUTHORIZED);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnGivenSetting() {
    mockUserHasRight(ForecastingRights.MANAGE);
    given(forecastingSettingRepository.findById(setting.getId()))
        .willReturn(Optional.of(setting));

    get(setting.getId())
        .statusCode(HttpStatus.SC_OK)
        .body(ID, is(setting.getId().toString()))
        .body(PROGRAM_ID, is(setting.getProgramId().toString()));

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnNotFoundForMissingSetting() {
    mockUserHasRight(ForecastingRights.MANAGE);
    given(forecastingSettingRepository.findById(setting.getId())).willReturn(Optional.empty());

    get(setting.getId())
        .statusCode(HttpStatus.SC_NOT_FOUND)
        .body(MESSAGE_KEY, is(MessageKeys.ERROR_FORECASTING_SETTING_NOT_FOUND));

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnForbiddenRatherThanNotFoundForMissingSettingWithoutRight() {
    mockUserHasNoRight(ForecastingRights.MANAGE);
    given(forecastingSettingRepository.findById(setting.getId())).willReturn(Optional.empty());

    get(setting.getId())
        .statusCode(HttpStatus.SC_FORBIDDEN);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnUnauthorizedForGetSettingWithoutToken() {
    restAssured.given()
        .pathParam(ID, setting.getId().toString())
        .when()
        .get(ID_URL)
        .then()
        .statusCode(HttpStatus.SC_UNAUTHORIZED);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldUpdateSetting() {
    mockUserHasRight(ForecastingRights.MANAGE);
    given(forecastingSettingService.update(eq(setting.getId()), any(ForecastingSettingDto.class)))
        .willReturn(setting);

    put(setting.getId(), settingDto)
        .statusCode(HttpStatus.SC_OK)
        .body(ID, is(setting.getId().toString()));

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnNotFoundWhenUpdatingMissingSetting() {
    mockUserHasRight(ForecastingRights.MANAGE);
    given(forecastingSettingService.update(eq(setting.getId()), any(ForecastingSettingDto.class)))
        .willThrow(new NotFoundException(MessageKeys.ERROR_FORECASTING_SETTING_NOT_FOUND));

    put(setting.getId(), settingDto)
        .statusCode(HttpStatus.SC_NOT_FOUND)
        .body(MESSAGE_KEY, is(MessageKeys.ERROR_FORECASTING_SETTING_NOT_FOUND));

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnBadRequestWhenUpdatedIdDiffersFromPath() {
    mockUserHasRight(ForecastingRights.MANAGE);

    put(UUID.randomUUID(), settingDto)
        .statusCode(HttpStatus.SC_BAD_REQUEST)
        .body(MESSAGE_KEY, is(MessageKeys.ERROR_FORECASTING_SETTING_ID_MISMATCH));

    verify(forecastingSettingService, never())
        .update(any(UUID.class), any(ForecastingSettingDto.class));
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnForbiddenForUpdateWithoutRight() {
    mockUserHasNoRight(ForecastingRights.MANAGE);

    put(setting.getId(), settingDto)
        .statusCode(HttpStatus.SC_FORBIDDEN);

    verify(forecastingSettingService, never())
        .update(any(UUID.class), any(ForecastingSettingDto.class));
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnUnauthorizedForUpdateWithoutToken() {
    restAssured.given()
        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        .pathParam(ID, setting.getId().toString())
        .body(settingDto)
        .when()
        .put(ID_URL)
        .then()
        .statusCode(HttpStatus.SC_UNAUTHORIZED);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldDeleteSetting() {
    mockUserHasRight(ForecastingRights.MANAGE);
    given(forecastingSettingRepository.existsById(setting.getId())).willReturn(true);

    delete(setting.getId())
        .statusCode(HttpStatus.SC_NO_CONTENT);

    verify(forecastingSettingRepository).deleteById(setting.getId());
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnNotFoundWhenDeletingMissingSetting() {
    mockUserHasRight(ForecastingRights.MANAGE);
    given(forecastingSettingRepository.existsById(setting.getId())).willReturn(false);

    delete(setting.getId())
        .statusCode(HttpStatus.SC_NOT_FOUND)
        .body(MESSAGE_KEY, is(MessageKeys.ERROR_FORECASTING_SETTING_NOT_FOUND));

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnForbiddenForDeleteWithoutRight() {
    mockUserHasNoRight(ForecastingRights.MANAGE);

    delete(setting.getId())
        .statusCode(HttpStatus.SC_FORBIDDEN);

    verify(forecastingSettingRepository, never()).deleteById(any(UUID.class));
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnUnauthorizedForDeleteWithoutToken() {
    restAssured.given()
        .pathParam(ID, setting.getId().toString())
        .when()
        .delete(ID_URL)
        .then()
        .statusCode(HttpStatus.SC_UNAUTHORIZED);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  private ValidatableResponse post(ForecastingSettingDto body) {
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

  private ValidatableResponse put(UUID id, ForecastingSettingDto body) {
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
