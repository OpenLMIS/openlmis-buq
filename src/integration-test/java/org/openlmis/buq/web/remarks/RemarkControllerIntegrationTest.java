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

package org.openlmis.buq.web.remarks;

import static java.util.Collections.singletonList;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willReturn;
import static org.mockito.Mockito.mock;

import com.google.common.collect.Maps;
import guru.nidi.ramltester.junit.RamlMatchers;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.Optional;
import org.apache.http.HttpStatus;
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
import org.openlmis.buq.builder.RemarkDataBuilder;
import org.openlmis.buq.domain.Remark;
import org.openlmis.buq.dto.remark.RemarkDto;
import org.openlmis.buq.i18n.MessageKeys;
import org.openlmis.buq.web.BaseWebIntegrationTest;
import org.openlmis.buq.web.RemarkController;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

@SuppressWarnings("PMD.TooManyMethods")
public class RemarkControllerIntegrationTest extends BaseWebIntegrationTest  {

  private static final String RESOURCE_URL = RemarkController.RESOURCE_PATH;
  private static final String ID_URL = RESOURCE_URL + "/{id}";
  private static final String AUDIT_LOG_URL = ID_URL + "/auditLog";

  private static final String NAME = "name";

  private final Remark remark = new RemarkDataBuilder().build();
  private final RemarkDto remarkDto = RemarkDto.newInstance(remark);

  private final DateTimeFormatter javersDateFormat =
      new DateTimeFormatterBuilder().appendPattern("yyyy-MM-dd'T'HH:mm:ss").optionalStart()
          .appendFraction(ChronoField.NANO_OF_SECOND, 1, 3, true).optionalEnd().toFormatter();

  private final LocalDateTime commitDateTime = LocalDateTime.now();
  private final GlobalId globalId = new UnboundedValueObjectId(Remark.class.getSimpleName());
  private final CommitId commitId = new CommitId(1, 0);
  private final CommitMetadata commitMetadata =
      new CommitMetadata("admin", Maps.newHashMap(), commitDateTime,
          commitDateTime.toInstant(ZoneOffset.UTC), commitId);
  private final PropertyChangeMetadata propertyChangeMetadata =
      new PropertyChangeMetadata(globalId, NAME, Optional.of(commitMetadata),
          PropertyChangeType.PROPERTY_VALUE_CHANGED);
  private final ValueChange change = new ValueChange(propertyChangeMetadata, "name1", "name2");

  @Before
  public void setUp() {
    given(remarkRepository.saveAndFlush(any(Remark.class)))
            .willAnswer(new BaseWebIntegrationTest.SaveAnswer<>());
  }

  @Test
  public void shouldReturnUnauthorizedForAllRemarksEndpointIfUserIsNotAuthorized() {
    restAssured.given()
            .when()
            .get(RESOURCE_URL)
            .then()
            .statusCode(HttpStatus.SC_UNAUTHORIZED);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnUnauthorizedForRemarkEndpointIfUserIsNotAuthorized() {
    restAssured
            .given()
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .body(remarkDto)
            .when()
            .post(RESOURCE_URL)
            .then()
            .statusCode(HttpStatus.SC_UNAUTHORIZED);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnGivenRemark() {
    given(remarkRepository.findById(remarkDto.getId()))
            .willReturn(Optional.of(remark));

    restAssured
            .given()
            .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
            .pathParam(ID, remarkDto.getId().toString())
            .when()
            .get(ID_URL)
            .then()
            .statusCode(HttpStatus.SC_OK)
            .body(ID, is(remarkDto.getId().toString()))
            .body(NAME, is(remarkDto.getName()));

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnNotFoundMessageIfRemarkDoesNotExistForGivenRemarkEndpoint() {
    given(remarkRepository.findById(remarkDto.getId())).willReturn(Optional.empty());

    restAssured
            .given()
            .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
            .pathParam(ID, remarkDto.getId().toString())
            .when()
            .get(ID_URL)
            .then()
            .statusCode(HttpStatus.SC_NOT_FOUND)
            .body(MESSAGE_KEY, is(MessageKeys.ERROR_REMARK_NOT_FOUND));

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnUnauthorizedForGetRemarkEndpointIfUserIsNotAuthorized() {
    restAssured
            .given()
            .pathParam(ID, remarkDto.getId().toString())
            .when()
            .get(ID_URL)
            .then()
            .statusCode(HttpStatus.SC_UNAUTHORIZED);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldUpdateRemark() {
    given(remarkRepository.findById(remarkDto.getId()))
            .willReturn(Optional.of(remark));

    restAssured
            .given()
            .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .pathParam(ID, remarkDto.getId().toString())
            .body(remarkDto)
            .when()
            .put(ID_URL)
            .then()
            .statusCode(HttpStatus.SC_OK)
            .body(ID, is(remarkDto.getId().toString()))
            .body(NAME, is(remarkDto.getName()));

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnUnauthorizedForUpdateRemarkEndpointIfUserIsNotAuthorized() {
    restAssured
            .given()
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .pathParam(ID, remarkDto.getId().toString())
            .body(remarkDto)
            .when()
            .put(ID_URL)
            .then()
            .statusCode(HttpStatus.SC_UNAUTHORIZED);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldDeleteRemark() {
    given(remarkRepository.existsById(remarkDto.getId())).willReturn(true);

    restAssured
            .given()
            .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
            .pathParam(ID, remarkDto.getId().toString())
            .when()
            .delete(ID_URL)
            .then()
            .statusCode(HttpStatus.SC_NO_CONTENT);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnNoContentStatusForDelete() {
    restAssured
            .given()
            .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
            .pathParam(ID, remarkDto.getId().toString())
            .when()
            .delete(ID_URL)
            .then()
            .statusCode(HttpStatus.SC_NO_CONTENT);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnUnauthorizedForDeleteRemarkEndpointIfUserIsNotAuthorized() {
    restAssured
            .given()
            .pathParam(ID, remarkDto.getId().toString())
            .when()
            .delete(ID_URL)
            .then()
            .statusCode(HttpStatus.SC_UNAUTHORIZED);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldRetrieveAuditLogs() {
    given(remarkRepository.existsById(remarkDto.getId())).willReturn(true);
    willReturn(new Changes(singletonList(change), mock(PrettyValuePrinter.class)))
        .given(javers).findChanges(any(JqlQuery.class));

    restAssured
            .given()
            .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
            .pathParam(ID, remarkDto.getId().toString())
            .when()
            .get(AUDIT_LOG_URL)
            .then()
            .statusCode(HttpStatus.SC_OK)
            .body("", hasSize(1))
            .body("changeType", hasItem(change.getClass().getSimpleName()))
            .body("globalId.valueObject", hasItem(Remark.class.getSimpleName()))
            .body("commitMetadata.author", hasItem(commitMetadata.getAuthor()))
            .body("commitMetadata.properties", hasItem(hasSize(0)))
            .body("commitMetadata.commitDate", hasItem(commitMetadata.getCommitDate().toString()))
            .body("commitMetadata.id", hasItem(commitId.valueAsNumber().floatValue()))
            .body("property", hasItem(change.getPropertyName()))
            .body("left", hasItem(change.getLeft().toString()))
            .body("right", hasItem(change.getRight().toString()));

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldRetrieveAuditLogsWithParameters() {
    given(remarkRepository.existsById(remarkDto.getId())).willReturn(true);
    willReturn(new Changes(singletonList(change), mock(PrettyValuePrinter.class)))
        .given(javers).findChanges(any(JqlQuery.class));

    restAssured
            .given()
            .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
            .pathParam(ID, remarkDto.getId().toString())
            .queryParam("author", commitMetadata.getAuthor())
            .queryParam("changedPropertyName", change.getPropertyName())
            .when()
            .get(AUDIT_LOG_URL)
            .then()
            .statusCode(HttpStatus.SC_OK)
            .body("", hasSize(1))
            .body("changeType", hasItem(change.getClass().getSimpleName()))
            .body("globalId.valueObject", hasItem(Remark.class.getSimpleName()))
            .body("commitMetadata.author", hasItem(commitMetadata.getAuthor()))
            .body("commitMetadata.properties", hasItem(hasSize(0)))
            .body("commitMetadata.commitDate",
                hasItem(commitMetadata.getCommitDate().format(javersDateFormat)))
            .body("commitMetadata.id", hasItem(commitId.valueAsNumber().floatValue()))
            .body("property", hasItem(change.getPropertyName()))
            .body("left", hasItem(change.getLeft().toString()))
            .body("right", hasItem(change.getRight().toString()));

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnNotFoundMessageIfRemarkDoesNotExistForAuditLogEndpoint() {
    given(remarkRepository.existsById(remarkDto.getId())).willReturn(false);

    restAssured
            .given()
            .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
            .pathParam(ID, remarkDto.getId().toString())
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
            .pathParam(ID, remarkDto.getId().toString())
            .when()
            .get(AUDIT_LOG_URL)
            .then()
            .statusCode(HttpStatus.SC_UNAUTHORIZED);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }
}
