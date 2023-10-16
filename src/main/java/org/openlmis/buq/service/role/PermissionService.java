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
import java.util.List;
import java.util.UUID;
import org.openlmis.buq.dto.ResultDto;
import org.openlmis.buq.dto.referencedata.RightDto;
import org.openlmis.buq.dto.referencedata.UserDto;
import org.openlmis.buq.exception.PermissionMessageException;
import org.openlmis.buq.i18n.MessageKeys;
import org.openlmis.buq.service.referencedata.UserReferenceDataService;
import org.openlmis.buq.util.AuthenticationHelper;
import org.openlmis.buq.util.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

@Service
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
          PORALG_APPROVAL, PORALG_APPROVAL, MANAGE_BUQ);

  public static final List<String> RECENT_REJECTION_RIGHTS = Arrays.asList(
          CREATE_FORECASTING, AUTHORIZE_FORECASTING, MOH_APPROVAL, PORALG_APPROVAL);

  @Autowired
  private AuthenticationHelper authenticationHelper;

  @Autowired
  private UserReferenceDataService userReferenceDataService;

  @Value("${auth.server.clientId}")
  private String serviceTokenClientId;

  @Value("${auth.server.clientId.apiKey.prefix}")
  private String apiKeyPrefix;

  public void hasPermission(String rightName) {
    hasPermission(rightName, null, null);
  }

  private void hasPermission(String rightName, UUID program, UUID facility) {
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
  private void hasAtLeastOnePermission(List<String> rightNames, UUID program, UUID facility) {
    boolean hasAnyPermission = false;

    for (String name : rightNames) {
      ResultDto<Boolean> result = getRightResult(name, program, facility, false);

      if (result.getResult()) {
        hasAnyPermission = true;
        break;
      }
    }

    if (!hasAnyPermission) {
      throw new PermissionMessageException(
              new Message(MessageKeys.ERROR_NO_SINGLE_FOLLOWING_PERMISSION, rightNames));
    }
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