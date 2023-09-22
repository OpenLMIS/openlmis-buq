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

package org.openlmis.buq.web.buq;

import static org.hamcrest.Matchers.hasItem;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willReturn;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.openlmis.buq.web.buq.BottomUpQuantificationController.BUQ_FORM_CSV_FILENAME;
import static org.openlmis.buq.web.buq.BottomUpQuantificationController.TEXT_CSV_MEDIA_TYPE;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.jayway.restassured.response.Response;
import guru.nidi.ramltester.junit.RamlMatchers;
import java.io.IOException;
import java.util.Collections;
import java.util.Optional;
import org.apache.commons.io.FileUtils;
import org.apache.http.HttpStatus;
import org.hamcrest.Matchers;
import org.javers.core.commit.CommitId;
import org.javers.core.commit.CommitMetadata;
import org.javers.core.diff.changetype.ValueChange;
import org.javers.core.metamodel.object.GlobalId;
import org.javers.core.metamodel.object.UnboundedValueObjectId;
import org.javers.repository.jql.JqlQuery;
import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;
import org.openlmis.buq.builder.BottomUpQuantificationDataBuilder;
import org.openlmis.buq.domain.buq.BottomUpQuantification;
import org.openlmis.buq.dto.buq.BottomUpQuantificationDto;
import org.openlmis.buq.i18n.MessageKeys;
import org.openlmis.buq.repository.buq.BottomUpQuantificationSearchParams;
import org.openlmis.buq.web.BaseWebIntegrationTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

@SuppressWarnings("PMD.TooManyMethods")
public class BottomUpQuantificationControllerIntegrationTest extends BaseWebIntegrationTest {

  private static final String RESOURCE_URL = BottomUpQuantificationController.RESOURCE_PATH;
  private static final String ID_URL = RESOURCE_URL + "/{id}";
  private static final String PREPARE_URL = RESOURCE_URL + "/prepare";
  private static final String DOWNLOAD_URL = ID_URL + "/download";
  private static final String AUTHORIZE_URL = ID_URL + "/authorize";
  private static final String SUBMIT_URL = ID_URL + "/submit";
  private static final String APPROVE_URL = ID_URL + "/approve";
  private static final String AUDIT_LOG_URL = ID_URL + "/auditLog";

  private static final String STATUS = "status";

  private final BottomUpQuantification bottomUpQuantification =
      new BottomUpQuantificationDataBuilder().build();
  private final BottomUpQuantificationDto bottomUpQuantificationDto =
      BottomUpQuantificationDto.newInstance(bottomUpQuantification);

  private final GlobalId globalId = new UnboundedValueObjectId(BottomUpQuantification.class
      .getSimpleName());
  private final ValueChange change = new ValueChange(globalId, STATUS, "name1", "name2");

  private final CommitId commitId = new CommitId(1, 0);
  private final CommitMetadata commitMetadata = new CommitMetadata(
      "admin", Maps.newHashMap(), LocalDateTime.now(), commitId);

  @Before
  public void setUp() {
    given(bottomUpQuantificationRepository.saveAndFlush(any(BottomUpQuantification.class)))
        .willAnswer(new SaveAnswer<>());
    given(bottomUpQuantificationDtoBuilder.buildDto(any(BottomUpQuantification.class)))
        .willReturn(bottomUpQuantificationDto);
    change.bindToCommit(commitMetadata);
  }

  @Test
  public void shouldReturnPageOfBottomUpQuantifications() {
    given(bottomUpQuantificationRepository.search(
        any(BottomUpQuantificationSearchParams.class),
        any(Pageable.class)))
        .willReturn(new PageImpl<>(Collections.singletonList(bottomUpQuantification)));

    restAssured.given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .queryParam("page", pageable.getPageNumber())
        .queryParam("size", pageable.getPageSize())
        .when()
        .get(RESOURCE_URL)
        .then()
        .statusCode(HttpStatus.SC_OK)
        .body("content", Matchers.hasSize(1))
        .body("content[0].id", Matchers.is(bottomUpQuantification.getId().toString()))
        .body("content[0].status", Matchers.is(bottomUpQuantification.getStatus().toString()));

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnUnauthorizedForAllBottomUpQuantificationEndpointIfUserIsNotAuthorized() {
    restAssured.given()
        .when()
        .get(RESOURCE_URL)
        .then()
        .statusCode(HttpStatus.SC_UNAUTHORIZED);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnUnauthorizedForPrepareBuqEndpointIfUserIsNotAuthorized() {
    restAssured
        .given()
        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        .queryParam("facilityId", bottomUpQuantificationDto.getFacilityId())
        .queryParam("programId", bottomUpQuantificationDto.getProgramId())
        .queryParam("processingPeriodId", bottomUpQuantificationDto.getProcessingPeriodId())
        .when()
        .post(PREPARE_URL)
        .then()
        .statusCode(HttpStatus.SC_UNAUTHORIZED);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnGivenBottomUpQuantification() {
    given(bottomUpQuantificationRepository.findById(bottomUpQuantificationDto.getId()))
        .willReturn(Optional.of(bottomUpQuantification));

    restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .pathParam(ID, bottomUpQuantificationDto.getId().toString())
        .when()
        .get(ID_URL)
        .then()
        .statusCode(HttpStatus.SC_OK)
        .body(ID, Matchers.is(bottomUpQuantificationDto.getId().toString()))
        .body(STATUS, Matchers.is(bottomUpQuantificationDto.getStatus().toString()));

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldCreateBottomUpQuantification() {
    given(bottomUpQuantificationService.prepare(bottomUpQuantification.getFacilityId(),
        bottomUpQuantification.getProgramId(), bottomUpQuantification.getProcessingPeriodId()))
        .willReturn(bottomUpQuantification);

    restAssured.given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .queryParam("facilityId", bottomUpQuantification.getFacilityId())
        .queryParam("programId", bottomUpQuantification.getProgramId())
        .queryParam("processingPeriodId", bottomUpQuantification.getProcessingPeriodId())
        .when()
        .post(PREPARE_URL)
        .then()
        .statusCode(HttpStatus.SC_CREATED)
        .body(ID, Matchers.is(bottomUpQuantification.getId().toString()))
        .body(STATUS, Matchers.is(bottomUpQuantification.getStatus().toString()));

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnNotFoundMessageIfBuqDoesNotExistForGivenBuqEndpoint() {
    given(bottomUpQuantificationRepository.findById(bottomUpQuantificationDto.getId()))
        .willReturn(Optional.empty());

    restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .pathParam(ID, bottomUpQuantificationDto.getId().toString())
        .when()
        .get(ID_URL)
        .then()
        .statusCode(HttpStatus.SC_NOT_FOUND)
        .body(MESSAGE_KEY, Matchers.is(MessageKeys.ERROR_BOTTOM_UP_QUANTIFICATION_NOT_FOUND));

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnUnauthorizedForGetBottomUpQuantificationEndpointIfUserIsNotAuthorized() {
    restAssured
        .given()
        .pathParam(ID, bottomUpQuantificationDto.getId().toString())
        .when()
        .get(ID_URL)
        .then()
        .statusCode(HttpStatus.SC_UNAUTHORIZED);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldUpdateBottomUpQuantification() {
    given(bottomUpQuantificationRepository.existsById(bottomUpQuantificationDto.getId()))
        .willReturn(true);
    given(bottomUpQuantificationService.save(any(BottomUpQuantificationDto.class),
        eq(bottomUpQuantificationDto.getId())))
        .willReturn(bottomUpQuantification);

    restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        .pathParam(ID, bottomUpQuantificationDto.getId().toString())
        .body(bottomUpQuantificationDto)
        .when()
        .put(ID_URL)
        .then()
        .statusCode(HttpStatus.SC_OK)
        .body(ID, Matchers.is(bottomUpQuantification.getId().toString()))
        .body(STATUS, Matchers.is(bottomUpQuantification.getStatus().toString()));

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnUnauthorizedForUpdateBuqEndpointIfUserIsNotAuthorized() {
    restAssured
        .given()
        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        .pathParam(ID, bottomUpQuantificationDto.getId().toString())
        .body(bottomUpQuantificationDto)
        .when()
        .put(ID_URL)
        .then()
        .statusCode(HttpStatus.SC_UNAUTHORIZED);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldDeleteBottomUpQuantification() {
    given(bottomUpQuantificationRepository.findById(bottomUpQuantificationDto.getId()))
        .willReturn(Optional.of(bottomUpQuantification));
    willDoNothing().given(bottomUpQuantificationService).delete(bottomUpQuantification);

    restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .pathParam(ID, bottomUpQuantificationDto.getId().toString())
        .when()
        .delete(ID_URL)
        .then()
        .statusCode(HttpStatus.SC_NO_CONTENT);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnNotFoundMessageIfBuqDoesNotExistForDeleteBuqOfFundEndpoint() {
    given(bottomUpQuantificationRepository.findById(any()))
        .willReturn(Optional.empty());

    restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .pathParam(ID, bottomUpQuantificationDto.getId().toString())
        .when()
        .delete(ID_URL)
        .then()
        .statusCode(HttpStatus.SC_NOT_FOUND)
        .body(MESSAGE_KEY, Matchers.is(MessageKeys.ERROR_BOTTOM_UP_QUANTIFICATION_NOT_FOUND));

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnUnauthorizedForDeleteBuqEndpointIfUserIsNotAuthorized() {
    restAssured
        .given()
        .pathParam(ID, bottomUpQuantificationDto.getId().toString())
        .when()
        .delete(ID_URL)
        .then()
        .statusCode(HttpStatus.SC_UNAUTHORIZED);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldAuthorizeBottomUpQuantification() {
    given(bottomUpQuantificationRepository.existsById(bottomUpQuantificationDto.getId()))
        .willReturn(true);
    given(bottomUpQuantificationService.authorize(any(BottomUpQuantificationDto.class),
        eq(bottomUpQuantificationDto.getId())))
        .willReturn(bottomUpQuantification);

    restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        .pathParam(ID, bottomUpQuantificationDto.getId().toString())
        .body(bottomUpQuantificationDto)
        .when()
        .post(AUTHORIZE_URL)
        .then()
        .statusCode(HttpStatus.SC_OK)
        .body(ID, Matchers.is(bottomUpQuantification.getId().toString()))
        .body(STATUS, Matchers.is(bottomUpQuantification.getStatus().toString()));

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldApproveBottomUpQuantification() {
    given(bottomUpQuantificationRepository.existsById(bottomUpQuantificationDto.getId()))
        .willReturn(true);
    given(bottomUpQuantificationService.approve(any(BottomUpQuantificationDto.class),
        eq(bottomUpQuantificationDto.getId())))
        .willReturn(bottomUpQuantification);

    restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        .pathParam(ID, bottomUpQuantificationDto.getId().toString())
        .body(bottomUpQuantificationDto)
        .when()
        .post(APPROVE_URL)
        .then()
        .statusCode(HttpStatus.SC_OK)
        .body(ID, Matchers.is(bottomUpQuantification.getId().toString()))
        .body(STATUS, Matchers.is(bottomUpQuantification.getStatus().toString()));

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldSubmitBottomUpQuantification() {
    given(bottomUpQuantificationService
            .submitBottomUpQuantification(any(BottomUpQuantificationDto.class),
            eq(bottomUpQuantificationDto.getId())))
            .willReturn(bottomUpQuantificationDto);

    restAssured
            .given()
            .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .pathParam(ID, bottomUpQuantificationDto.getId().toString())
            .body(bottomUpQuantificationDto)
            .when()
            .post(SUBMIT_URL)
            .then()
            .statusCode(HttpStatus.SC_OK)
            .body(ID, Matchers.is(bottomUpQuantification.getId().toString()))
            .body(STATUS, Matchers.is(bottomUpQuantification.getStatus().toString()));

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldDownload() throws IOException {
    given(bottomUpQuantificationRepository.findById(bottomUpQuantificationDto.getId()))
        .willReturn(Optional.of(bottomUpQuantification));
    ClassPathResource file = new ClassPathResource("csv/" + BUQ_FORM_CSV_FILENAME + ".csv");
    byte[] buqDataBytes = FileUtils.readFileToByteArray(file.getFile());
    given(bottomUpQuantificationService.getPreparationFormData(bottomUpQuantification))
        .willReturn(buqDataBytes);

    Response response = restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(TEXT_CSV_MEDIA_TYPE)
        .pathParam(ID, bottomUpQuantificationDto.getId().toString())
        .when()
        .get(DOWNLOAD_URL)
        .then()
        .statusCode(200)
        .extract().response();

    verify(bottomUpQuantificationService).getPreparationFormData(bottomUpQuantification);
    assertArrayEquals(response.getBody().asByteArray(), buqDataBytes);
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnNotFoundMessageIfBuqDoesNotExistForGivenBuqDownloadEndpoint() {
    given(bottomUpQuantificationRepository.findById(bottomUpQuantificationDto.getId()))
        .willReturn(Optional.empty());

    restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(TEXT_CSV_MEDIA_TYPE)
        .pathParam(ID, bottomUpQuantificationDto.getId().toString())
        .when()
        .get(DOWNLOAD_URL)
        .then()
        .statusCode(HttpStatus.SC_NOT_FOUND)
        .body(MESSAGE_KEY, Matchers.is(MessageKeys.ERROR_BOTTOM_UP_QUANTIFICATION_NOT_FOUND));

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldRetrieveAuditLogs() {
    given(bottomUpQuantificationRepository.existsById(bottomUpQuantificationDto.getId()))
        .willReturn(true);
    willReturn(Lists.newArrayList(change)).given(javers).findChanges(any(JqlQuery.class));

    restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .pathParam(ID, bottomUpQuantificationDto.getId().toString())
        .when()
        .get(AUDIT_LOG_URL)
        .then()
        .statusCode(HttpStatus.SC_OK)
        .body("", Matchers.hasSize(1))
        .body("changeType", hasItem(change.getClass().getSimpleName()))
        .body("globalId.valueObject", hasItem(BottomUpQuantification.class.getSimpleName()))
        .body("commitMetadata.author", hasItem(commitMetadata.getAuthor()))
        .body("commitMetadata.properties", hasItem(Matchers.hasSize(0)))
        .body("commitMetadata.commitDate", hasItem(commitMetadata.getCommitDate().toString()))
        .body("commitMetadata.id", hasItem(commitId.valueAsNumber().floatValue()))
        .body("property", hasItem(change.getPropertyName()))
        .body("left", hasItem(change.getLeft().toString()))
        .body("right", hasItem(change.getRight().toString()));

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldRetrieveAuditLogsWithParameters() {
    given(bottomUpQuantificationRepository.existsById(bottomUpQuantificationDto.getId()))
        .willReturn(true);
    willReturn(Lists.newArrayList(change)).given(javers).findChanges(any(JqlQuery.class));

    restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .pathParam(ID, bottomUpQuantificationDto.getId().toString())
        .queryParam("author", commitMetadata.getAuthor())
        .queryParam("changedPropertyName", change.getPropertyName())
        .when()
        .get(AUDIT_LOG_URL)
        .then()
        .statusCode(HttpStatus.SC_OK)
        .body("", Matchers.hasSize(1))
        .body("changeType", hasItem(change.getClass().getSimpleName()))
        .body("globalId.valueObject", hasItem(BottomUpQuantification.class.getSimpleName()))
        .body("commitMetadata.author", hasItem(commitMetadata.getAuthor()))
        .body("commitMetadata.properties", hasItem(Matchers.hasSize(0)))
        .body("commitMetadata.commitDate", hasItem(commitMetadata.getCommitDate().toString()))
        .body("commitMetadata.id", hasItem(commitId.valueAsNumber().floatValue()))
        .body("property", hasItem(change.getPropertyName()))
        .body("left", hasItem(change.getLeft().toString()))
        .body("right", hasItem(change.getRight().toString()));

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnNotFoundMessageIfBuqDoesNotExistForAuditLogEndpoint() {
    given(sourceOfFundRepository.existsById(bottomUpQuantificationDto.getId())).willReturn(false);

    restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .pathParam(ID, bottomUpQuantificationDto.getId().toString())
        .when()
        .get(AUDIT_LOG_URL)
        .then()
        .statusCode(HttpStatus.SC_NOT_FOUND);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnUnauthorizedForAuditLogEndpointIfUserIsNotAuthorized() {
    restAssured
        .given()
        .pathParam(ID, bottomUpQuantificationDto.getId().toString())
        .when()
        .get(AUDIT_LOG_URL)
        .then()
        .statusCode(HttpStatus.SC_UNAUTHORIZED);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

}
