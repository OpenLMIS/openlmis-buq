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

package org.openlmis.buq.service.role;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.openlmis.buq.service.role.PermissionService.APPROVE_BUQ;
import static org.openlmis.buq.service.role.PermissionService.CREATE_FORECASTING;
import static org.openlmis.buq.service.role.PermissionService.MANAGE_BUQ;
import static org.openlmis.buq.service.role.PermissionService.MOH_APPROVAL;
import static org.openlmis.buq.service.role.PermissionService.PREPARE_BUQ;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openlmis.buq.builder.BottomUpQuantificationDataBuilder;
import org.openlmis.buq.builder.RightDtoDataBuilder;
import org.openlmis.buq.builder.UserDtoDataBuilder;
import org.openlmis.buq.domain.buq.BottomUpQuantification;
import org.openlmis.buq.dto.ResultDto;
import org.openlmis.buq.dto.referencedata.DetailedRoleAssignmentDto;
import org.openlmis.buq.dto.referencedata.RightDto;
import org.openlmis.buq.dto.referencedata.RoleDto;
import org.openlmis.buq.dto.referencedata.UserDto;
import org.openlmis.buq.exception.PermissionMessageException;
import org.openlmis.buq.service.referencedata.RightReferenceDataService;
import org.openlmis.buq.service.referencedata.UserReferenceDataService;
import org.openlmis.buq.service.referencedata.UserRoleAssignmentsReferenceDataService;
import org.openlmis.buq.util.AuthenticationHelper;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2Request;
import org.springframework.test.util.ReflectionTestUtils;

@SuppressWarnings("PMD.TooManyMethods")
@RunWith(MockitoJUnitRunner.class)
public class PermissionServiceTest {

  private static final String SERVICE_CLIENT_ID = "trusted-client";
  private static final String API_KEY_PREFIX = "api-key-client-";
  private static final UUID APPROVE_BUQ_RIGHT_ID = UUID.randomUUID();
  private static final UUID MANAGE_BUQ_RIGHT_ID = UUID.randomUUID();

  @Mock
  private AuthenticationHelper authenticationHelper;

  @Mock
  private UserReferenceDataService userReferenceDataService;

  @Mock
  private UserRoleAssignmentsReferenceDataService userRoleAssignmentsReferenceDataService;

  @Mock
  private RightReferenceDataService rightReferenceDataService;

  @InjectMocks
  private PermissionService permissionService;

  private final UUID programId = UUID.randomUUID();
  private final UUID facilityId = UUID.randomUUID();
  private final UserDto user = new UserDtoDataBuilder().buildAsDto();
  private final BottomUpQuantification buq = new BottomUpQuantificationDataBuilder()
      .withProgramId(programId)
      .withFacilityId(facilityId)
      .build();

  private OAuth2Authentication authentication;

  @Before
  public void setUp() {
    ReflectionTestUtils.setField(permissionService, "serviceTokenClientId", SERVICE_CLIENT_ID);
    ReflectionTestUtils.setField(permissionService, "apiKeyPrefix", API_KEY_PREFIX);

    authentication = mock(OAuth2Authentication.class);
    SecurityContext securityContext = mock(SecurityContext.class);
    when(securityContext.getAuthentication()).thenReturn(authentication);
    SecurityContextHolder.setContext(securityContext);
  }

  @After
  public void tearDown() {
    SecurityContextHolder.clearContext();
  }

  @Test
  public void shouldPassProgramAndFacilityToReferenceData() {
    RightDto right = stubUserAndRight(PREPARE_BUQ);
    stubHasRight(right, programId, facilityId, true);

    permissionService.hasPermission(PREPARE_BUQ, programId, facilityId);

    verify(userReferenceDataService).hasRight(user.getId(), right.getId(), programId, facilityId);
  }

  @Test(expected = PermissionMessageException.class)
  public void shouldDenyWhenUserHasNoRightForProgramAndFacility() {
    RightDto right = stubUserAndRight(PREPARE_BUQ);
    stubHasRight(right, programId, facilityId, false);

    permissionService.hasPermission(PREPARE_BUQ, programId, facilityId);
  }

  @Test
  public void shouldAllowServiceTokenWithoutUserLookup() {
    stubClient(SERVICE_CLIENT_ID);

    permissionService.hasPermission(PREPARE_BUQ, programId, facilityId);

    verify(authenticationHelper, never()).getCurrentUser();
  }

  @Test(expected = PermissionMessageException.class)
  public void shouldDenyApiKeyClient() {
    stubClient(API_KEY_PREFIX + "abc");

    permissionService.hasPermission(PREPARE_BUQ, programId, facilityId);
  }

  @Test
  public void shouldPassWhenUserHasOneOfRightsForProgramAndFacility() {
    RightDto prepare = stubUserAndRight(PREPARE_BUQ);
    RightDto create = stubRight(CREATE_FORECASTING);
    stubHasRight(prepare, programId, facilityId, false);
    stubHasRight(create, programId, facilityId, true);

    permissionService.hasAtLeastOnePermission(
        Arrays.asList(PREPARE_BUQ, CREATE_FORECASTING), programId, facilityId);
  }

  @Test(expected = PermissionMessageException.class)
  public void shouldDenyWhenReferenceDataReturnsNoResult() {
    RightDto prepare = stubUserAndRight(PREPARE_BUQ);
    when(userReferenceDataService.hasRight(user.getId(), prepare.getId(), programId, facilityId))
        .thenReturn(null);

    permissionService.hasAtLeastOnePermission(
        Arrays.asList(PREPARE_BUQ), programId, facilityId);
  }

  @Test
  public void shouldAllowViewingBuqWithSupervisionRightForItsProgramAndFacility() {
    stubAllRightsResolvable();
    when(userReferenceDataService.hasRight(any(), any(), any(), any()))
        .thenAnswer(invocation -> new ResultDto<>(
            APPROVE_BUQ_RIGHT_ID.equals(invocation.getArgument(1))
                && programId.equals(invocation.getArgument(2))
                && facilityId.equals(invocation.getArgument(3))));

    permissionService.canViewBottomUpQuantification(buq);
  }

  @Test
  public void shouldAllowViewingBuqWithUnscopedManageRight() {
    stubAllRightsResolvable();
    when(userReferenceDataService.hasRight(any(), any(), any(), any()))
        .thenAnswer(invocation -> new ResultDto<>(
            MANAGE_BUQ_RIGHT_ID.equals(invocation.getArgument(1))
                && invocation.getArgument(2) == null
                && invocation.getArgument(3) == null));

    permissionService.canViewBottomUpQuantification(buq);
  }

  @Test(expected = PermissionMessageException.class)
  public void shouldDenyViewingBuqWithRightsForAnotherProgramOrFacility() {
    stubAllRightsResolvable();
    when(userReferenceDataService.hasRight(any(), any(), any(), any()))
        .thenAnswer(invocation -> new ResultDto<>(
            !MANAGE_BUQ_RIGHT_ID.equals(invocation.getArgument(1))
                && !programId.equals(invocation.getArgument(2))
                && !facilityId.equals(invocation.getArgument(3))));

    permissionService.canViewBottomUpQuantification(buq);
  }

  @Test
  public void shouldCollectProgramNodePairsOfAssignmentsWithTheRight() {
    when(authenticationHelper.getCurrentUser()).thenReturn(user);
    RightDto approve = rightNamed(APPROVE_BUQ);
    when(rightReferenceDataService.findRight(APPROVE_BUQ)).thenReturn(approve);
    UUID nodeId = UUID.randomUUID();
    when(userRoleAssignmentsReferenceDataService.hasRight(user, approve)).thenReturn(Arrays.asList(
        roleAssignment(programId, nodeId),
        roleAssignment(programId, null),
        roleAssignment(null, nodeId)));

    Set<Pair<UUID, UUID>> pairs =
        permissionService.getProgramNodePairs(Collections.singletonList(APPROVE_BUQ));

    assertEquals(Collections.singleton(new ImmutablePair<>(programId, nodeId)), pairs);
  }

  @Test
  public void shouldSkipRightsMissingInReferenceData() {
    when(authenticationHelper.getCurrentUser()).thenReturn(user);

    Set<Pair<UUID, UUID>> pairs =
        permissionService.getProgramNodePairs(Collections.singletonList(MOH_APPROVAL));

    assertTrue(pairs.isEmpty());
  }

  @Test
  public void shouldPassNodeCheckWhenUserHasRightAtTheNode() {
    UUID nodeId = UUID.randomUUID();
    stubApproveAssignment(programId, nodeId);

    permissionService.hasPermissionForSupervisoryNode(
        Collections.singletonList(APPROVE_BUQ), programId, nodeId);
  }

  @Test(expected = PermissionMessageException.class)
  public void shouldDenyNodeCheckWhenUserHasRightAtAnotherNodeOnly() {
    stubApproveAssignment(programId, UUID.randomUUID());

    permissionService.hasPermissionForSupervisoryNode(
        Collections.singletonList(APPROVE_BUQ), programId, UUID.randomUUID());
  }

  @Test(expected = PermissionMessageException.class)
  public void shouldDenyNodeCheckWhenUserHasRightForAnotherProgramOnly() {
    UUID nodeId = UUID.randomUUID();
    stubApproveAssignment(UUID.randomUUID(), nodeId);

    permissionService.hasPermissionForSupervisoryNode(
        Collections.singletonList(APPROVE_BUQ), programId, nodeId);
  }

  @Test
  public void shouldLookUpAssignmentsOnceForSeveralProgramNodePairs() {
    UUID nodeId = UUID.randomUUID();
    UUID otherNodeId = UUID.randomUUID();
    when(authenticationHelper.getCurrentUser()).thenReturn(user);
    RightDto approve = rightNamed(APPROVE_BUQ);
    when(rightReferenceDataService.findRight(APPROVE_BUQ)).thenReturn(approve);
    when(userRoleAssignmentsReferenceDataService.hasRight(user, approve)).thenReturn(Arrays.asList(
        roleAssignment(programId, nodeId), roleAssignment(programId, otherNodeId)));

    permissionService.hasPermissionForSupervisoryNodes(Collections.singletonList(APPROVE_BUQ),
        new HashSet<>(Arrays.asList(new ImmutablePair<>(programId, nodeId),
            new ImmutablePair<>(programId, otherNodeId))));

    verify(authenticationHelper, times(1)).getCurrentUser();
    verify(userRoleAssignmentsReferenceDataService, times(1)).hasRight(user, approve);
  }

  @Test(expected = PermissionMessageException.class)
  public void shouldDenySeveralProgramNodePairsIfOneIsMissing() {
    UUID nodeId = UUID.randomUUID();
    stubApproveAssignment(programId, nodeId);

    permissionService.hasPermissionForSupervisoryNodes(Collections.singletonList(APPROVE_BUQ),
        new HashSet<>(Arrays.asList(new ImmutablePair<>(programId, nodeId),
            new ImmutablePair<>(programId, UUID.randomUUID()))));
  }

  @Test
  public void shouldAllowServiceTokenNodeCheckWithoutUserLookup() {
    stubClient(SERVICE_CLIENT_ID);

    permissionService.hasPermissionForSupervisoryNode(
        Collections.singletonList(APPROVE_BUQ), programId, UUID.randomUUID());

    verify(authenticationHelper, never()).getCurrentUser();
  }

  @Test
  public void shouldPassProgramCheckWhenUserHasRightAtAnyFacilityOfTheProgram() {
    stubPermissionStrings(APPROVE_BUQ + "|" + facilityId + "|" + programId);

    permissionService.hasPermissionForProgram(Arrays.asList(MOH_APPROVAL, APPROVE_BUQ), programId);
  }

  @Test(expected = PermissionMessageException.class)
  public void shouldDenyProgramCheckWhenUserHasRightForAnotherProgramOnly() {
    stubPermissionStrings(APPROVE_BUQ + "|" + facilityId + "|" + UUID.randomUUID(),
        MANAGE_BUQ, MOH_APPROVAL + "|" + facilityId + "|" + programId);

    permissionService.hasPermissionForProgram(Collections.singletonList(APPROVE_BUQ), programId);
  }

  @Test
  public void shouldAllowServiceTokenProgramCheckWithoutUserLookup() {
    stubClient(SERVICE_CLIENT_ID);

    permissionService.hasPermissionForProgram(Collections.singletonList(APPROVE_BUQ), programId);

    verify(authenticationHelper, never()).getCurrentUser();
  }

  @Test
  public void shouldReturnFacilitiesWhereUserHasRightForTheProgram() {
    UUID otherFacilityId = UUID.randomUUID();
    stubPermissionStrings(APPROVE_BUQ + "|" + facilityId + "|" + programId,
        APPROVE_BUQ + "|" + otherFacilityId + "|" + UUID.randomUUID(),
        MOH_APPROVAL + "|" + otherFacilityId + "|" + programId,
        APPROVE_BUQ + "|" + otherFacilityId,
        MANAGE_BUQ);

    assertEquals(Collections.singletonList(facilityId),
        permissionService.getSupervisedFacilityIds(APPROVE_BUQ, programId));
  }

  @Test
  public void shouldReturnProgramFacilityPairsOfBuqSupervisionRights() {
    UUID otherFacilityId = UUID.randomUUID();
    UUID otherProgramId = UUID.randomUUID();
    stubPermissionStrings(PREPARE_BUQ + "|" + facilityId + "|" + programId,
        APPROVE_BUQ + "|" + otherFacilityId + "|" + programId,
        MOH_APPROVAL + "|" + facilityId + "|" + otherProgramId,
        "REQUISITION_VIEW|" + otherFacilityId + "|" + otherProgramId,
        "FACILITIES_MANAGE");

    Set<Pair<UUID, UUID>> expected = new HashSet<>(Arrays.asList(
        new ImmutablePair<>(programId, facilityId),
        new ImmutablePair<>(programId, otherFacilityId),
        new ImmutablePair<>(otherProgramId, facilityId)));
    assertEquals(Optional.of(expected), permissionService.getViewableProgramFacilityPairs());
  }

  @Test
  public void shouldNotRestrictViewableBuqsForUnscopedManageRight() {
    stubPermissionStrings(MANAGE_BUQ, PREPARE_BUQ + "|" + facilityId + "|" + programId);

    assertEquals(Optional.empty(), permissionService.getViewableProgramFacilityPairs());
  }

  @Test
  public void shouldNotRestrictViewableBuqsForServiceToken() {
    when(authentication.isClientOnly()).thenReturn(true);

    assertEquals(Optional.empty(), permissionService.getViewableProgramFacilityPairs());
    verify(authenticationHelper, never()).getCurrentUser();
  }

  private void stubPermissionStrings(String... permissionStrings) {
    when(authenticationHelper.getCurrentUser()).thenReturn(user);
    when(userReferenceDataService.getPermissionStrings(user.getId()))
        .thenReturn(Arrays.asList(permissionStrings));
  }

  private void stubApproveAssignment(UUID assignmentProgramId, UUID nodeId) {
    when(authenticationHelper.getCurrentUser()).thenReturn(user);
    RightDto approve = rightNamed(APPROVE_BUQ);
    when(rightReferenceDataService.findRight(APPROVE_BUQ)).thenReturn(approve);
    when(userRoleAssignmentsReferenceDataService.hasRight(user, approve))
        .thenReturn(Collections.singletonList(roleAssignment(assignmentProgramId, nodeId)));
  }

  private DetailedRoleAssignmentDto roleAssignment(UUID assignmentProgramId, UUID nodeId) {
    RoleDto role = new RoleDto();
    role.setId(UUID.randomUUID());
    DetailedRoleAssignmentDto assignment = new DetailedRoleAssignmentDto();
    assignment.setRole(role);
    assignment.setProgramId(assignmentProgramId);
    assignment.setSupervisoryNodeId(nodeId);
    return assignment;
  }

  private RightDto rightNamed(String name) {
    RightDto right = new RightDtoDataBuilder().buildAsDto();
    right.setName(name);
    right.setId(UUID.randomUUID());
    if (APPROVE_BUQ.equals(name)) {
      right.setId(APPROVE_BUQ_RIGHT_ID);
    } else if (MANAGE_BUQ.equals(name)) {
      right.setId(MANAGE_BUQ_RIGHT_ID);
    }
    return right;
  }

  private void stubAllRightsResolvable() {
    when(authenticationHelper.getCurrentUser()).thenReturn(user);
    when(authenticationHelper.getRight(any())).thenAnswer(invocation ->
        rightNamed(invocation.getArgument(0)));
  }

  private RightDto stubUserAndRight(String rightName) {
    when(authenticationHelper.getCurrentUser()).thenReturn(user);
    return stubRight(rightName);
  }

  private RightDto stubRight(String rightName) {
    RightDto right = rightNamed(rightName);
    when(authenticationHelper.getRight(rightName)).thenReturn(right);
    return right;
  }

  private void stubHasRight(RightDto right, UUID program, UUID facility, boolean result) {
    when(userReferenceDataService.hasRight(user.getId(), right.getId(), program, facility))
        .thenReturn(new ResultDto<>(result));
  }

  private void stubClient(String clientId) {
    when(authentication.isClientOnly()).thenReturn(true);
    OAuth2Request request = mock(OAuth2Request.class);
    when(request.getClientId()).thenReturn(clientId);
    when(authentication.getOAuth2Request()).thenReturn(request);
  }
}
