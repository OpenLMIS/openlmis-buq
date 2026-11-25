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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.openlmis.buq.service.role.PermissionService.APPROVE_BUQ;
import static org.openlmis.buq.service.role.PermissionService.CREATE_FORECASTING;
import static org.openlmis.buq.service.role.PermissionService.MANAGE_BUQ;
import static org.openlmis.buq.service.role.PermissionService.PREPARE_BUQ;

import java.util.Arrays;
import java.util.UUID;
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
import org.openlmis.buq.dto.referencedata.RightDto;
import org.openlmis.buq.dto.referencedata.UserDto;
import org.openlmis.buq.exception.PermissionMessageException;
import org.openlmis.buq.service.referencedata.UserReferenceDataService;
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

  private RightDto rightNamed(String name) {
    RightDto right = new RightDtoDataBuilder().buildAsDto();
    right.setName(name);
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
