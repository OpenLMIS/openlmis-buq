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

package org.openlmis.buq.i18n;

import java.util.Arrays;

public abstract class MessageKeys {
  private static final String DELIMITER = ".";

  private static final String SERVICE_PREFIX = "buq";
  private static final String ERROR = "error";

  private static final String BOTTOM_UP_QUANTIFICATION = "bottomUpQuantification";
  private static final String BOTTOM_UP_QUANTIFICATION_STATUS = BOTTOM_UP_QUANTIFICATION + "Status";
  private static final String SOURCE_OF_FUND = "sourceOfFund";
  private static final String JAVERS = "javers";

  private static final String REMARK = "remark";

  private static final String REJECTION = "rejection";

  private static final String ID = "id";
  private static final String NAME = "name";
  private static final String SERVICE = "service";
  private static final String FACILITY = "facility";
  private static final String PROGRAM = "program";
  private static final String PROCESSING_PERIOD = "processingPeriod";
  private static final String ORDERABLE = "orderable";
  private static final String LINE_ITEM = "lineItem";
  private static final String DATE = "date";
  private static final String UUID = "uuid";
  private static final String PARAMETER = "parameter";
  private static final String FIELD = "field";
  private static final String BOOLEAN = "boolean";
  private static final String USER = "user";
  private static final String PRODUCT_GROUP = "productGroup";

  private static final String MISMATCH = "mismatch";
  private static final String NOT_FOUND = "notFound";
  private static final String DUPLICATED = "duplicated";
  private static final String INVALID = "invalid";
  private static final String REQUIRED = "required";
  private static final String AUTHENTICATION = "authentication";
  private static final String FORMAT = "format";
  private static final String PREPARE = "prepare";
  private static final String SUBMIT = "submit";
  private static final String AUTHORIZE = "authorize";
  private static final String REJECT = "reject";
  private static final String APPROVE = "approve";
  private static final String PERIOD_FACILITY_UNIQUE = "periodAndFacilityUnique";
  private static final String AND = "and";
  private static final String RIGHT = "right";

  private static final String ERROR_PREFIX = join(SERVICE_PREFIX, ERROR);

  public static final String ERROR_BOTTOM_UP_QUANTIFICATION_NOT_FOUND = join(ERROR_PREFIX,
      BOTTOM_UP_QUANTIFICATION, NOT_FOUND);
  public static final String ERROR_SOURCE_OF_FUND_NOT_FOUND = join(ERROR_PREFIX, SOURCE_OF_FUND,
      NOT_FOUND);
  public static final String ERROR_SOURCE_OF_FUND_ID_MISMATCH = join(ERROR_PREFIX, SOURCE_OF_FUND,
      ID, MISMATCH);
  public static final String ERROR_SOURCE_OF_FUND_NAME_DUPLICATED =
      join(ERROR_PREFIX, SOURCE_OF_FUND, NAME, DUPLICATED);

  public static final String ERROR_REMARK_NAME_DUPLICATED =
      join(ERROR_PREFIX, REMARK, NAME, DUPLICATED);
  public static final String ERROR_REMARK_NOT_FOUND = join(ERROR_PREFIX, REMARK, NOT_FOUND);
  public static final String ERROR_REJECTION_NOT_FOUND = join(ERROR_PREFIX, REJECTION, NOT_FOUND);
  public static final String ERROR_USER_NOT_FOUND = join(ERROR_PREFIX, AUTHENTICATION, USER,
      "canNotBeFound");
  public static final String ERROR_RIGHT_NOT_FOUND = join(ERROR_PREFIX, AUTHENTICATION, RIGHT,
      "canNotBeFound");
  public static final String ERROR_SERVICE_REQUIRED = join(ERROR_PREFIX, SERVICE, REQUIRED);
  public static final String ERROR_SERVICE_OCCURRED = join(ERROR_PREFIX, SERVICE, "errorOccurred");
  public static final String ERROR_PREPARE_MISSING_PARAMETERS = join(ERROR_PREFIX, PREPARE,
      "missingParameters");
  public static final String ERROR_FACILITY_NOT_FOUND = join(ERROR_PREFIX, FACILITY, NOT_FOUND);
  public static final String ERROR_FACILITY_DOES_NOT_SUPPORT_PROGRAM = join(ERROR_PREFIX,
      FACILITY, "doesNotSupportProgram");
  public static final String ERROR_PROGRAM_NOT_FOUND = join(ERROR_PREFIX, PROGRAM, NOT_FOUND);
  public static final String ERROR_PROCESSING_PERIOD_NOT_FOUND = join(ERROR_PREFIX,
      PROCESSING_PERIOD, NOT_FOUND);
  public static final String ERROR_ORDERABLE_NOT_FOUND = join(ERROR_PREFIX, ORDERABLE, NOT_FOUND);
  public static final String ERROR_ID_MISMATCH = join(ERROR_PREFIX, ID, MISMATCH);
  private static final String ERROR_INVALID_FORMAT = join(ERROR_PREFIX, INVALID, FORMAT);
  public static final String ERROR_INVALID_FORMAT_DATE = join(ERROR_INVALID_FORMAT, DATE);
  public static final String ERROR_INVALID_FORMAT_UUID = join(ERROR_INVALID_FORMAT, UUID);
  public static final String ERROR_INVALID_FORMAT_BOOLEAN = join(ERROR_INVALID_FORMAT, BOOLEAN);
  public static final String ERROR_INVALID_SEARCH_PARAMS = join(ERROR_PREFIX, INVALID,
      "searchParams");
  public static final String ERROR_INVALID_PARAMETER_BOTTOM_UP_QUANTIFICATION_STATUS =
      join(ERROR_PREFIX, INVALID, PARAMETER, BOTTOM_UP_QUANTIFICATION_STATUS);
  public static final String ERROR_MUST_BE_DRAFT_OR_REJECTED_TO_BE_SUBMITTED =
          join(ERROR_PREFIX, SUBMIT, "mustBeDraftOrRejectedToBeSubmitted");
  public static final String ERROR_MUST_BE_SUBMITTED_OR_REJECTED_TO_BE_AUTHORIZED =
          join(ERROR_PREFIX,
      AUTHORIZE, "mustBeSubmittedOrRejectedToBeAuthorized");
  public static final String ERROR_MUST_BE_AUTHORIZED_OR_IN_APPROVAL_TO_BE_APPROVED =
          join(ERROR_PREFIX,
      APPROVE, "mustBeAuthorizedOrInApprovalToBeApproved");
  public static final String ERROR_MUST_BE_AUTHORIZED_OR_IN_APPROVAL_TO_BE_REJECTED =
          join(ERROR_PREFIX,
      REJECT, "mustBeAuthorizedOrInApprovalToBeRejected");
  public static final String ERROR_LINE_ITEM_FIELD_REQUIRED = join(ERROR_PREFIX, LINE_ITEM, FIELD,
      REQUIRED);
  public static final String ERROR_LINE_ITEM_FIELD_MUST_BE_NON_NEGATIVE = join(ERROR_PREFIX,
      LINE_ITEM, FIELD, "mustBeNonNegative");
  public static final String ERROR_LINE_ITEM_REMARK_REQUIRED = join(ERROR_PREFIX, LINE_ITEM, REMARK,
      REQUIRED);
  public static final String ERROR_USER_HOME_FACILITY_AND_BUQ_FACILITY_MISMATCH =
      join(ERROR_PREFIX, USER, "home", FACILITY, AND, BOTTOM_UP_QUANTIFICATION, FACILITY,
          MISMATCH);
  public static final String ERROR_PRODUCT_GROUP_NOT_FOUND = join(ERROR_PREFIX, PRODUCT_GROUP,
      NOT_FOUND);
  public static final String ERROR_PRODUCT_GROUP_ID_MISMATCH = join(ERROR_PREFIX, PRODUCT_GROUP,
      ID, MISMATCH);
  public static final String ERROR_JAVERS_EXISTING_ENTRY =
      join(ERROR_PREFIX, JAVERS, "entryAlreadyExists");

  public static final String ERROR_PERIOD_FACILITY_PAIR_UNIQUE =
      join(ERROR_PREFIX, PERIOD_FACILITY_UNIQUE);

  public static final String ERROR_NO_FOLLOWING_PERMISSION = join(ERROR_PREFIX, AUTHENTICATION,
      "noFollowingPermission");

  public static final String ERROR_NO_SINGLE_FOLLOWING_PERMISSION = join(ERROR_PREFIX,
      AUTHENTICATION, "noSingleFollowingPermission");

  public static final String ERROR_PERMISSION_CHECK_FAILED = join(ERROR_PREFIX, AUTHENTICATION,
      "failed");

  private MessageKeys() {
    throw new UnsupportedOperationException();
  }

  private static String join(String... params) {
    return String.join(DELIMITER, Arrays.asList(params));
  }
}
