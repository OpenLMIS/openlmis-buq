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
import org.openlmis.buq.builder.SupplyRatioDataBuilder;
import org.openlmis.buq.domain.forecasting.SupplyRatio;
import org.openlmis.buq.dto.forecasting.SupplyRatioDto;
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
public class SupplyRatioControllerIntegrationTest extends BaseWebIntegrationTest {

  private static final String RESOURCE_URL = SupplyRatioController.RESOURCE_PATH;
  private static final String ID_URL = RESOURCE_URL + "/{id}";

  private static final String VACCINE_ORDERABLE_ID = "vaccineOrderableId";
  private static final String SUPPLY_ORDERABLE_ID = "supplyOrderableId";
  private static final String MESSAGE = "message";

  private final SupplyRatio ratio = new SupplyRatioDataBuilder().build();
  private final SupplyRatioDto ratioDto = SupplyRatioDto.newInstance(ratio);

  @Test
  public void shouldSearchRatiosByVaccineAndSupply() {
    mockUserHasRight(ForecastingRights.MANAGE);
    given(supplyRatioRepository.search(eq(ratio.getVaccineOrderableId()),
        eq(ratio.getSupplyOrderableId()), any(Pageable.class)))
        .willReturn(new PageImpl<>(Collections.singletonList(ratio)));

    restAssured.given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .queryParam(VACCINE_ORDERABLE_ID, ratio.getVaccineOrderableId().toString())
        .queryParam(SUPPLY_ORDERABLE_ID, ratio.getSupplyOrderableId().toString())
        .when()
        .get(RESOURCE_URL)
        .then()
        .statusCode(HttpStatus.SC_OK)
        .body("content", hasSize(1))
        .body("content[0].id", is(ratio.getId().toString()))
        .body("content[0].type", is(ratio.getType().name()));

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldSearchAllRatiosWithoutParameters() {
    mockUserHasRight(ForecastingRights.MANAGE);
    given(supplyRatioRepository.search(eq(null), eq(null), any(Pageable.class)))
        .willReturn(new PageImpl<>(Collections.singletonList(ratio)));

    restAssured.given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .when()
        .get(RESOURCE_URL)
        .then()
        .statusCode(HttpStatus.SC_OK)
        .body("content", hasSize(1));

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
  public void shouldCreateRatio() {
    mockUserHasRight(ForecastingRights.MANAGE);
    given(supplyRatioService.create(any(SupplyRatioDto.class))).willReturn(ratio);

    post(ratioDto)
        .statusCode(HttpStatus.SC_CREATED)
        .body(ID, is(ratio.getId().toString()))
        .body(VACCINE_ORDERABLE_ID, is(ratioDto.getVaccineOrderableId().toString()))
        .body("ratio", is(1.11f));

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnBadRequestNamingRatioWhenRatioIsZero() {
    mockUserHasRight(ForecastingRights.MANAGE);
    ratioDto.setRatio(BigDecimal.ZERO);

    post(ratioDto)
        .statusCode(HttpStatus.SC_BAD_REQUEST)
        .body(MESSAGE_KEY, is(MessageKeys.ERROR_VALIDATION_FIELD_INVALID))
        .body(MESSAGE, containsString("ratio"));

    verify(supplyRatioService, never()).create(any(SupplyRatioDto.class));
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnBadRequestWhenVaccineAndSupplyAreTheSameProduct() {
    mockUserHasRight(ForecastingRights.MANAGE);
    given(supplyRatioService.create(any(SupplyRatioDto.class)))
        .willThrow(new ValidationMessageException(MessageKeys.ERROR_SUPPLY_RATIO_SAME_ORDERABLE));

    post(ratioDto)
        .statusCode(HttpStatus.SC_BAD_REQUEST)
        .body(MESSAGE_KEY, is(MessageKeys.ERROR_SUPPLY_RATIO_SAME_ORDERABLE));

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnBadRequestWhenSupplyHasNoCommodityType() {
    mockUserHasRight(ForecastingRights.MANAGE);
    given(supplyRatioService.create(any(SupplyRatioDto.class)))
        .willThrow(new ValidationMessageException(new Message(
            MessageKeys.ERROR_SUPPLY_RATIO_COMMODITY_TYPE_MISSING, "C100")));

    post(ratioDto)
        .statusCode(HttpStatus.SC_BAD_REQUEST)
        .body(MESSAGE_KEY, is(MessageKeys.ERROR_SUPPLY_RATIO_COMMODITY_TYPE_MISSING));

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnBadRequestForUnknownType() {
    mockUserHasRight(ForecastingRights.MANAGE);
    String body = "{\"vaccineOrderableId\":\"" + ratioDto.getVaccineOrderableId()
        + "\",\"supplyOrderableId\":\"" + ratioDto.getSupplyOrderableId()
        + "\",\"type\":\"CRATE\",\"ratio\":1.11}";

    restAssured.given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        .body(body)
        .when()
        .post(RESOURCE_URL)
        .then()
        .statusCode(HttpStatus.SC_BAD_REQUEST)
        .body(MESSAGE_KEY, is(MessageKeys.ERROR_REQUEST_BODY_UNREADABLE))
        .body(MESSAGE, containsString("CRATE"));

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnBadRequestWhenRatioIsDuplicated() {
    mockUserHasRight(ForecastingRights.MANAGE);
    given(supplyRatioService.create(any(SupplyRatioDto.class)))
        .willThrow(new DataIntegrityViolationException(null, new ConstraintViolationException(
            null, null, "supply_ratios_vaccine_supply_unique")));

    post(ratioDto)
        .statusCode(HttpStatus.SC_BAD_REQUEST)
        .body(MESSAGE_KEY, is(MessageKeys.ERROR_SUPPLY_RATIO_DUPLICATED));

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnForbiddenForCreateWithoutRight() {
    mockUserHasNoRight(ForecastingRights.MANAGE);

    post(ratioDto)
        .statusCode(HttpStatus.SC_FORBIDDEN);

    verify(supplyRatioService, never()).create(any(SupplyRatioDto.class));
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnUnauthorizedForCreateWithoutToken() {
    restAssured.given()
        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        .body(ratioDto)
        .when()
        .post(RESOURCE_URL)
        .then()
        .statusCode(HttpStatus.SC_UNAUTHORIZED);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnGivenRatio() {
    mockUserHasRight(ForecastingRights.MANAGE);
    given(supplyRatioRepository.findById(ratio.getId())).willReturn(Optional.of(ratio));

    get(ratio.getId())
        .statusCode(HttpStatus.SC_OK)
        .body(ID, is(ratio.getId().toString()))
        .body(SUPPLY_ORDERABLE_ID, is(ratio.getSupplyOrderableId().toString()));

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnNotFoundForMissingRatio() {
    mockUserHasRight(ForecastingRights.MANAGE);
    given(supplyRatioRepository.findById(ratio.getId())).willReturn(Optional.empty());

    get(ratio.getId())
        .statusCode(HttpStatus.SC_NOT_FOUND)
        .body(MESSAGE_KEY, is(MessageKeys.ERROR_SUPPLY_RATIO_NOT_FOUND));

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnForbiddenRatherThanNotFoundForMissingRatioWithoutRight() {
    mockUserHasNoRight(ForecastingRights.MANAGE);
    given(supplyRatioRepository.findById(ratio.getId())).willReturn(Optional.empty());

    get(ratio.getId())
        .statusCode(HttpStatus.SC_FORBIDDEN);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnUnauthorizedForGetRatioWithoutToken() {
    restAssured.given()
        .pathParam(ID, ratio.getId().toString())
        .when()
        .get(ID_URL)
        .then()
        .statusCode(HttpStatus.SC_UNAUTHORIZED);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldUpdateRatio() {
    mockUserHasRight(ForecastingRights.MANAGE);
    ratio.setRatio(new BigDecimal("2.5"));
    given(supplyRatioService.update(eq(ratio.getId()), any(SupplyRatioDto.class)))
        .willReturn(ratio);

    put(ratio.getId(), ratioDto)
        .statusCode(HttpStatus.SC_OK)
        .body(ID, is(ratio.getId().toString()))
        .body("ratio", is(2.5f));

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnBadRequestWhenUpdatingToSameProduct() {
    mockUserHasRight(ForecastingRights.MANAGE);
    given(supplyRatioService.update(eq(ratio.getId()), any(SupplyRatioDto.class)))
        .willThrow(new ValidationMessageException(MessageKeys.ERROR_SUPPLY_RATIO_SAME_ORDERABLE));

    put(ratio.getId(), ratioDto)
        .statusCode(HttpStatus.SC_BAD_REQUEST)
        .body(MESSAGE_KEY, is(MessageKeys.ERROR_SUPPLY_RATIO_SAME_ORDERABLE));

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnNotFoundWhenUpdatingMissingRatio() {
    mockUserHasRight(ForecastingRights.MANAGE);
    given(supplyRatioService.update(eq(ratio.getId()), any(SupplyRatioDto.class)))
        .willThrow(new NotFoundException(MessageKeys.ERROR_SUPPLY_RATIO_NOT_FOUND));

    put(ratio.getId(), ratioDto)
        .statusCode(HttpStatus.SC_NOT_FOUND)
        .body(MESSAGE_KEY, is(MessageKeys.ERROR_SUPPLY_RATIO_NOT_FOUND));

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnBadRequestWhenUpdatedIdDiffersFromPath() {
    mockUserHasRight(ForecastingRights.MANAGE);

    put(UUID.randomUUID(), ratioDto)
        .statusCode(HttpStatus.SC_BAD_REQUEST)
        .body(MESSAGE_KEY, is(MessageKeys.ERROR_SUPPLY_RATIO_ID_MISMATCH));

    verify(supplyRatioService, never()).update(any(UUID.class), any(SupplyRatioDto.class));
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnForbiddenForUpdateWithoutRight() {
    mockUserHasNoRight(ForecastingRights.MANAGE);

    put(ratio.getId(), ratioDto)
        .statusCode(HttpStatus.SC_FORBIDDEN);

    verify(supplyRatioService, never()).update(any(UUID.class), any(SupplyRatioDto.class));
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnUnauthorizedForUpdateWithoutToken() {
    restAssured.given()
        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        .pathParam(ID, ratio.getId().toString())
        .body(ratioDto)
        .when()
        .put(ID_URL)
        .then()
        .statusCode(HttpStatus.SC_UNAUTHORIZED);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldDeleteRatio() {
    mockUserHasRight(ForecastingRights.MANAGE);
    given(supplyRatioRepository.existsById(ratio.getId())).willReturn(true);

    delete(ratio.getId())
        .statusCode(HttpStatus.SC_NO_CONTENT);

    verify(supplyRatioRepository).deleteById(ratio.getId());
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnNotFoundWhenDeletingMissingRatio() {
    mockUserHasRight(ForecastingRights.MANAGE);
    given(supplyRatioRepository.existsById(ratio.getId())).willReturn(false);

    delete(ratio.getId())
        .statusCode(HttpStatus.SC_NOT_FOUND)
        .body(MESSAGE_KEY, is(MessageKeys.ERROR_SUPPLY_RATIO_NOT_FOUND));

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnForbiddenForDeleteWithoutRight() {
    mockUserHasNoRight(ForecastingRights.MANAGE);

    delete(ratio.getId())
        .statusCode(HttpStatus.SC_FORBIDDEN);

    verify(supplyRatioRepository, never()).deleteById(any(UUID.class));
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnUnauthorizedForDeleteWithoutToken() {
    restAssured.given()
        .pathParam(ID, ratio.getId().toString())
        .when()
        .delete(ID_URL)
        .then()
        .statusCode(HttpStatus.SC_UNAUTHORIZED);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  private ValidatableResponse post(SupplyRatioDto body) {
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

  private ValidatableResponse put(UUID id, SupplyRatioDto body) {
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
