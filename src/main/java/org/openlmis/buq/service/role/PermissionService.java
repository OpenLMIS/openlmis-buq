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

import static org.apache.commons.lang3.StringUtils.startsWith;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.openlmis.buq.domain.buq.BottomUpQuantification;
import org.openlmis.buq.dto.ResultDto;
import org.openlmis.buq.dto.referencedata.RightDto;
import org.openlmis.buq.dto.referencedata.UserDto;
import org.openlmis.buq.exception.PermissionMessageException;
import org.openlmis.buq.i18n.MessageKeys;
import org.openlmis.buq.service.referencedata.RightReferenceDataService;
import org.openlmis.buq.service.referencedata.UserReferenceDataService;
import org.openlmis.buq.service.referencedata.UserRoleAssignmentsReferenceDataService;
import org.openlmis.buq.util.AuthenticationHelper;
import org.openlmis.buq.util.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

@Service
@SuppressWarnings("PMD.TooManyMethods")
public class PermissionService {

  public static final String PREPARE_BUQ = "PREPARE_BUQ";
  public static final String APPROVE_BUQ = "APPROVE_BUQ";
  public static final String CREATE_FORECASTING = "CREATE_FORECASTING";
  public static final String AUTHORIZE_FORECASTING = "AUTHORIZE_FORECASTING";
  public static final String MOH_APPROVAL = "MOH_APPROVAL";
  public static final String PORALG_APPROVAL = "PORALG_APPROVAL";
  public static final String MANAGE_BUQ = "MANAGE_BUQ";

  public static final List<String> ALL_BUQ_RIGHTS = Arrays.asList(
          CREATE_FORECASTING, AUTHORIZE_FORECASTING, APPROVE_BUQ, PREPARE_BUQ,
          PORALG_APPROVAL, MOH_APPROVAL, MANAGE_BUQ);

  public static final List<String> BUQ_SUPERVISION_RIGHTS = Arrays.asList(
          CREATE_FORECASTING, AUTHORIZE_FORECASTING, APPROVE_BUQ, PREPARE_BUQ,
          PORALG_APPROVAL, MOH_APPROVAL);

  public static final List<String> RECENT_REJECTION_RIGHTS = Arrays.asList(
          CREATE_FORECASTING, APPROVE_BUQ, MOH_APPROVAL, PORALG_APPROVAL);

  public static final List<String> MOH_PORALG_RIGHTS = Arrays.asList(
          MOH_APPROVAL, PORALG_APPROVAL);

  public static final List<String> APPROVE_RIGHTS = Arrays.asList(
          MOH_APPROVAL, PORALG_APPROVAL, APPROVE_BUQ);

  @Autowired
  private AuthenticationHelper authenticationHelper;

  @Autowired
  private UserReferenceDataService userReferenceDataService;

  @Autowired
  private UserRoleAssignmentsReferenceDataService userRoleAssignmentsReferenceDataService;

  @Autowired
  private RightReferenceDataService rightReferenceDataService;

  @Value("${auth.server.clientId}")
  private String serviceTokenClientId;

  @Value("${auth.server.clientId.apiKey.prefix}")
  private String apiKeyPrefix;

  public void hasPermission(String rightName) {
    hasPermission(rightName, null, null);
  }

  /**
   * Checks if the user has the given right for the given program and facility. Referencedata
   * accepts a program only together with a facility; with neither, the right is checked
   * regardless of program and facility.
   *
   * @param rightName name of the right to check.
   * @param program   the unique identifier of the program.
   * @param facility  the unique identifier of the facility.
   */
  public void hasPermission(String rightName, UUID program, UUID facility) {
    ResultDto<Boolean> result = getRightResult(rightName, program, facility, false);
    if (null == result || !result.getResult()) {
      throw new PermissionMessageException(
              new Message(MessageKeys.ERROR_NO_FOLLOWING_PERMISSION, rightName));
    }
  }

  public void hasAtLeastOnePermission(List<String> rightNames) {
    hasAtLeastOnePermission(rightNames, null, null);
  }

  /**
   * Checks if the user has at least one of the specified
   * permissions for a given program and facility.
   *
   * @param rightNames   A list of permission names to check.
   * @param program      The unique identifier of the program.
   * @param facility     The unique identifier of the facility.
   */
  public void hasAtLeastOnePermission(List<String> rightNames, UUID program, UUID facility) {
    if (!hasAnyRight(rightNames, program, facility)) {
      throw new PermissionMessageException(
              new Message(MessageKeys.ERROR_NO_SINGLE_FOLLOWING_PERMISSION, rightNames));
    }
  }

  /**
   * Checks if the user can view the given bottom-up quantification: any BUQ supervision right
   * for its program and facility, or MANAGE_BUQ, which is a general admin right without a
   * program or facility.
   *
   * @param bottomUpQuantification the bottom-up quantification to view.
   */
  public void canViewBottomUpQuantification(BottomUpQuantification bottomUpQuantification) {
    boolean canView = hasAnyRight(BUQ_SUPERVISION_RIGHTS,
            bottomUpQuantification.getProgramId(), bottomUpQuantification.getFacilityId())
        || hasAnyRight(Collections.singletonList(MANAGE_BUQ), null, null);

    if (!canView) {
      throw new PermissionMessageException(
              new Message(MessageKeys.ERROR_NO_SINGLE_FOLLOWING_PERMISSION, ALL_BUQ_RIGHTS));
    }
  }

  private boolean hasAnyRight(List<String> rightNames, UUID program, UUID facility) {
    for (String name : rightNames) {
      ResultDto<Boolean> result = getRightResult(name, program, facility, false);

      if (null != result && Boolean.TRUE.equals(result.getResult())) {
        return true;
      }
    }

    return false;
  }

  /**
   * Returns the (program, supervisory node) pairs of the current user's role assignments that
   * hold any of the given rights. Rights missing in referencedata are skipped.
   *
   * @param rightNames names of the rights to look for.
   * @return pairs of program id (left) and supervisory node id (right).
   */
  public Set<Pair<UUID, UUID>> getProgramNodePairs(List<String> rightNames) {
    UserDto user = authenticationHelper.getCurrentUser();

    return rightNames.stream()
        .map(rightReferenceDataService::findRight)
        .filter(Objects::nonNull)
        .flatMap(right -> userRoleAssignmentsReferenceDataService.hasRight(user, right).stream())
        .filter(item -> Objects.nonNull(item.getRole().getId()))
        .filter(item -> Objects.nonNull(item.getSupervisoryNodeId()))
        .filter(item -> Objects.nonNull(item.getProgramId()))
        .map(item -> new ImmutablePair<>(item.getProgramId(), item.getSupervisoryNodeId()))
        .collect(Collectors.toSet());
  }

  /**
   * Checks if the user has any of the given rights through a role assigned at the given
   * supervisory node for the given program. A right at an ancestor node does not count: a node's
   * rights reach the facilities of its child nodes, but approval moves one node at a time.
   *
   * @param rightNames      names of the rights to check.
   * @param program         the unique identifier of the program.
   * @param supervisoryNode the unique identifier of the supervisory node.
   */
  public void hasPermissionForSupervisoryNode(List<String> rightNames, UUID program,
      UUID supervisoryNode) {
    hasPermissionForSupervisoryNodes(rightNames,
        Collections.singleton(new ImmutablePair<>(program, supervisoryNode)));
  }

  /**
   * Checks {@link #hasPermissionForSupervisoryNode} for every given (program, supervisory node)
   * pair, looking up the user's role assignments once per right, however many pairs are given.
   *
   * @param rightNames       names of the rights to check.
   * @param programNodePairs pairs of program id (left) and supervisory node id (right).
   */
  public void hasPermissionForSupervisoryNodes(List<String> rightNames,
      Set<Pair<UUID, UUID>> programNodePairs) {
    OAuth2Authentication authentication = (OAuth2Authentication) SecurityContextHolder
            .getContext()
            .getAuthentication();

    boolean hasPermission = authentication.isClientOnly()
        ? checkServiceToken(false, authentication).getResult()
        : getProgramNodePairs(rightNames).containsAll(programNodePairs);

    if (!hasPermission) {
      throw new PermissionMessageException(
              new Message(MessageKeys.ERROR_NO_SINGLE_FOLLOWING_PERMISSION, rightNames));
    }
  }

  /**
   * Checks if the user has any of the given rights for the given program at any facility. Used
   * where a request names a program but no facility, which referencedata's right check refuses.
   *
   * @param rightNames names of the rights to check.
   * @param program    the unique identifier of the program.
   */
  public void hasPermissionForProgram(List<String> rightNames, UUID program) {
    OAuth2Authentication authentication = (OAuth2Authentication) SecurityContextHolder
            .getContext()
            .getAuthentication();

    boolean hasPermission;
    if (authentication.isClientOnly()) {
      hasPermission = checkServiceToken(false, authentication).getResult();
    } else {
      List<String[]> permissions = getCurrentUserFacilityProgramPermissions();
      hasPermission = rightNames.stream().anyMatch(rightName ->
          !getFacilityIds(permissions, rightName, program).isEmpty());
    }

    if (!hasPermission) {
      throw new PermissionMessageException(
              new Message(MessageKeys.ERROR_NO_SINGLE_FOLLOWING_PERMISSION, rightNames));
    }
  }

  /**
   * Returns the facilities at which the current user has the given right for the given program.
   *
   * @param rightName name of the right.
   * @param program   the unique identifier of the program.
   * @return facility ids.
   */
  public List<UUID> getSupervisedFacilityIds(String rightName, UUID program) {
    return getFacilityIds(getCurrentUserFacilityProgramPermissions(), rightName, program);
  }

  private List<String[]> getCurrentUserFacilityProgramPermissions() {
    UserDto user = authenticationHelper.getCurrentUser();

    return userReferenceDataService.getPermissionStrings(user.getId())
        .stream()
        .map(permission -> permission.split("\\|"))
        .filter(parts -> parts.length == 3)
        .collect(Collectors.toList());
  }

  private List<UUID> getFacilityIds(List<String[]> permissions, String rightName,
      UUID program) {
    return permissions.stream()
        .filter(parts -> rightName.equals(parts[0]))
        .filter(parts -> program.equals(UUID.fromString(parts[2])))
        .map(parts -> UUID.fromString(parts[1]))
        .collect(Collectors.toList());
  }

  private ResultDto<Boolean> getRightResult(String rightName, UUID program, UUID facility,
                                            boolean allowApiKey) {
    OAuth2Authentication authentication = (OAuth2Authentication) SecurityContextHolder
            .getContext()
            .getAuthentication();

    return authentication.isClientOnly()
            ? checkServiceToken(allowApiKey, authentication)
            : checkUserToken(rightName, program, facility);
  }

  private ResultDto<Boolean> checkUserToken(String rightName, UUID program, UUID facility) {
    UserDto user = authenticationHelper.getCurrentUser();
    RightDto right = authenticationHelper.getRight(rightName);

    try {
      return userReferenceDataService.hasRight(
              user.getId(), right.getId(), program, facility);
    } catch (HttpClientErrorException httpException) {
      throw new PermissionMessageException(new Message(MessageKeys.ERROR_PERMISSION_CHECK_FAILED,
              httpException.getMessage()), httpException);
    }
  }

  private ResultDto<Boolean> checkServiceToken(boolean allowApiKey,
                                               OAuth2Authentication authentication) {
    String clientId = authentication.getOAuth2Request().getClientId();

    if (serviceTokenClientId.equals(clientId)) {
      return new ResultDto<>(true);
    }

    if (startsWith(clientId, apiKeyPrefix)) {
      return new ResultDto<>(allowApiKey);
    }

    return new ResultDto<>(false);
  }

}