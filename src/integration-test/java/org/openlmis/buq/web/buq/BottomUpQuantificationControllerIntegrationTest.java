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

import static java.util.Collections.singletonList;
import static org.hamcrest.Matchers.anEmptyMap;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.openlmis.buq.web.buq.BottomUpQuantificationController.BUQ_FORM_CSV_FILENAME;
import static org.openlmis.buq.web.buq.BottomUpQuantificationController.GEOGRAPHIC_ZONE_ID;
import static org.openlmis.buq.web.buq.BottomUpQuantificationController.TEXT_CSV_MEDIA_TYPE;

import com.google.common.collect.Maps;
import com.jayway.restassured.response.Response;
import guru.nidi.ramltester.junit.RamlMatchers;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.apache.commons.io.FileUtils;
import org.apache.http.HttpStatus;
import org.hamcrest.Matchers;
import org.javers.common.string.PrettyValuePrinter;
import org.javers.core.Changes;
import org.javers.core.commit.CommitId;
import org.javers.core.commit.CommitMetadata;
import org.javers.core.diff.changetype.PropertyChangeMetadata;
import org.javers.core.diff.changetype.PropertyChangeType;
import org.javers.core.diff.changetype.ValueChange;
import org.javers.core.metamodel.object.GlobalId;
import org.javers.core.metamodel.object.UnboundedValueObjectId;
import org.javers.repository.jql.JqlQuery;
import org.junit.Before;
import org.junit.Test;
import org.openlmis.buq.ApproveFacilityForecastingStats;
import org.openlmis.buq.builder.BottomUpQuantificationDataBuilder;
import org.openlmis.buq.builder.ProgramDtoDataBuilder;
import org.openlmis.buq.domain.buq.BottomUpQuantification;
import org.openlmis.buq.dto.BottomUpQuantificationGroupCostsData;
import org.openlmis.buq.dto.buq.BottomUpQuantificationDto;
import org.openlmis.buq.dto.referencedata.ProgramDto;
import org.openlmis.buq.i18n.MessageKeys;
import org.openlmis.buq.repository.buq.BottomUpQuantificationSearchParams;
import org.openlmis.buq.service.role.PermissionService;
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
  private static final String APPROVE_FACILITY_FORECASTING_STATUS_URL = RESOURCE_URL
      + "/approveFacilityForecastingStats";
  private static final String SUPERVISED_GEOGRAPHIC_ZONES_URL = RESOURCE_URL
      + "/supervisedGeographicZones";
  private static final String FOR_FINAL_APPROVAL_URL = RESOURCE_URL + "/forFinalApproval";
  private static final String AUDIT_LOG_URL = ID_URL + "/auditLog";

  private static final String STATUS = "status";
  private static final String PROGRAM_ID = "programId";
  private static final String FACILITY_ID = "facilityId";
  private static final String PROCESSING_PERIOD_ID = "processingPeriodId";

  private final BottomUpQuantification bottomUpQuantification =
      new BottomUpQuantificationDataBuilder().build();
  private final BottomUpQuantificationDto bottomUpQuantificationDto =
      BottomUpQuantificationDto.newInstance(bottomUpQuantification);

  private final LocalDateTime commitDateTime = java.time.LocalDateTime.now();
  private final GlobalId globalId =
      new UnboundedValueObjectId(BottomUpQuantification.class.getSimpleName());
  private final CommitId commitId = new CommitId(1, 0);
  private final CommitMetadata commitMetadata =
      new CommitMetadata("admin", Maps.newHashMap(), commitDateTime,
          commitDateTime.toInstant(ZoneOffset.UTC), commitId);
  private final PropertyChangeMetadata propertyChangeMetadata =
      new PropertyChangeMetadata(globalId, STATUS, Optional.of(commitMetadata),
          PropertyChangeType.PROPERTY_VALUE_CHANGED);
  private final ValueChange change = new ValueChange(propertyChangeMetadata, "name1", "name2");

  @Before
  public void setUp() {
    given(bottomUpQuantificationRepository.saveAndFlush(any(BottomUpQuantification.class)))
        .willAnswer(new SaveAnswer<>());
    given(bottomUpQuantificationDtoBuilder.buildDto(any(BottomUpQuantification.class)))
        .willReturn(bottomUpQuantificationDto);
  }

  @Test
  public void shouldReturnPageOfBottomUpQuantifications() {
    mockUserHasAtLeastOneOfFollowingRights(PermissionService.ALL_BUQ_RIGHTS);
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
        .queryParam(FACILITY_ID, bottomUpQuantificationDto.getFacilityId())
        .queryParam(PROGRAM_ID, bottomUpQuantificationDto.getProgramId())
        .queryParam(PROCESSING_PERIOD_ID, bottomUpQuantificationDto.getProcessingPeriodId())
        .when()
        .post(PREPARE_URL)
        .then()
        .statusCode(HttpStatus.SC_UNAUTHORIZED);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnGivenBottomUpQuantification() {
    mockUserHasAtLeastOneOfFollowingRights(PermissionService.ALL_BUQ_RIGHTS);
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
    mockUserHasRight(PermissionService.PREPARE_BUQ);
    given(bottomUpQuantificationService.prepare(bottomUpQuantification.getFacilityId(),
        bottomUpQuantification.getProgramId(), bottomUpQuantification.getProcessingPeriodId()))
        .willReturn(bottomUpQuantification);

    restAssured.given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .queryParam(FACILITY_ID, bottomUpQuantification.getFacilityId())
        .queryParam(PROGRAM_ID, bottomUpQuantification.getProgramId())
        .queryParam(PROCESSING_PERIOD_ID, bottomUpQuantification.getProcessingPeriodId())
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
    mockUserHasAtLeastOneOfFollowingRights(PermissionService.ALL_BUQ_RIGHTS);
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
    mockUserHasAtLeastOneOfFollowingRights(Arrays.asList(
            PermissionService.CREATE_FORECASTING,
            PermissionService.AUTHORIZE_FORECASTING));
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
    mockUserHasRight(PermissionService.PREPARE_BUQ);
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
    mockUserHasRight(PermissionService.PREPARE_BUQ);
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
    mockUserHasRight(PermissionService.PREPARE_BUQ);
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
    mockUserHasRight(PermissionService.AUTHORIZE_FORECASTING);
    mockUserHasRight(PermissionService.CREATE_FORECASTING);
    ProgramDto programDto = new ProgramDtoDataBuilder().buildAsDto();
    given(programReferenceDataService.findOne(bottomUpQuantificationDto.getProgramId()))
            .willReturn(programDto);
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
    mockUserHasRight(PermissionService.APPROVE_BUQ);
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
    mockUserHasRight(PermissionService.CREATE_FORECASTING);
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
    mockUserHasRight(PermissionService.PREPARE_BUQ);
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
    mockUserHasRight(PermissionService.PREPARE_BUQ);
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
  public void shouldReturnApproveFacilityForecastingStats() {
    mockUserHasRight(PermissionService.APPROVE_BUQ);
    final UUID programId = UUID.randomUUID();
    ApproveFacilityForecastingStats stats = new ApproveFacilityForecastingStats(10, 3, 30);
    given(bottomUpQuantificationService.getApproveFacilityForecastingStats(programId))
        .willReturn(stats);

    restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .queryParam(PROGRAM_ID, programId)
        .when()
        .get(APPROVE_FACILITY_FORECASTING_STATUS_URL)
        .then()
        .statusCode(HttpStatus.SC_OK)
        .body("totalFacilities", Matchers.is(10))
        .body("totalSubmitted", Matchers.is(3))
        .body("percentageSubmitted", Matchers.is(30));

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnSupervisedGeographicZones() {
    final UUID programId = UUID.randomUUID();
    final UUID geoZoneId = UUID.randomUUID();
    Map<UUID, Map<UUID, Map<UUID, Set<UUID>>>> zones = new HashMap<>();
    zones.put(geoZoneId, new HashMap<>());
    mockUserHasAtLeastOneOfFollowingRights(PermissionService.MOH_PORALG_RIGHTS);
    given(bottomUpQuantificationService.getSupervisedGeographicZones(programId))
        .willReturn(zones);

    restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .queryParam(PROGRAM_ID, programId)
        .when()
        .get(SUPERVISED_GEOGRAPHIC_ZONES_URL)
        .then()
        .statusCode(HttpStatus.SC_OK)
        .body("$", hasKey(geoZoneId.toString()))
        .body(geoZoneId.toString(), is(anEmptyMap()));

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldRetrieveAuditLogs() {
    mockUserHasRight(PermissionService.MANAGE_BUQ);
    given(bottomUpQuantificationRepository.existsById(bottomUpQuantificationDto.getId()))
        .willReturn(true);
    willReturn(new Changes(singletonList(change), mock(PrettyValuePrinter.class)))
        .given(javers).findChanges(any(JqlQuery.class));

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
    mockUserHasRight(PermissionService.MANAGE_BUQ);
    given(bottomUpQuantificationRepository.existsById(bottomUpQuantificationDto.getId()))
        .willReturn(true);
    willReturn(new Changes(singletonList(change), mock(PrettyValuePrinter.class)))
        .given(javers).findChanges(any(JqlQuery.class));

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
    mockUserHasRight(PermissionService.MANAGE_BUQ);
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

  @Test
  public void shouldReturnBottomUpQuantificationsForFinalApprovalWithGroupCosts() {
    mockUserHasAtLeastOneOfFollowingRights(PermissionService.MOH_PORALG_RIGHTS);
    given(bottomUpQuantificationService.getBottomUpQuantificationsForFinalApprovalWithGroupCosts(
        any(UUID.class),
        any(UUID.class),
        any(UUID.class),
        any(Pageable.class)))
        .willReturn(Collections.singletonList(new BottomUpQuantificationGroupCostsData()));

    restAssured.given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .queryParam(PROGRAM_ID, bottomUpQuantificationDto.getProgramId())
        .queryParam(PROCESSING_PERIOD_ID, bottomUpQuantificationDto.getProcessingPeriodId())
        .queryParam(GEOGRAPHIC_ZONE_ID, UUID.randomUUID())
        .when()
        .get(FOR_FINAL_APPROVAL_URL)
        .then()
        .statusCode(HttpStatus.SC_OK)
        .body("content", Matchers.hasSize(1));

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

}
