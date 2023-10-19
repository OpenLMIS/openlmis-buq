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

package org.openlmis.buq.validate;

import static org.openlmis.buq.i18n.MessageKeys.ERROR_LINE_ITEM_FIELD_MUST_BE_NON_NEGATIVE;
import static org.openlmis.buq.i18n.MessageKeys.ERROR_LINE_ITEM_FIELD_REQUIRED;
import static org.openlmis.buq.i18n.MessageKeys.ERROR_LINE_ITEM_REMARK_REQUIRED;
import static org.openlmis.buq.i18n.MessageKeys.ERROR_MUST_BE_AUTHORIZED_IN_APPROVAL_OR_APPROVED_TO_BE_REJECTED;
import static org.openlmis.buq.i18n.MessageKeys.ERROR_MUST_BE_AUTHORIZED_OR_IN_APPROVAL_TO_BE_APPROVED;
import static org.openlmis.buq.i18n.MessageKeys.ERROR_MUST_BE_DRAFT_OR_REJECTED_TO_BE_SUBMITTED;
import static org.openlmis.buq.i18n.MessageKeys.ERROR_MUST_BE_SUBMITTED_OR_REJECTED_TO_BE_AUTHORIZED;
import static org.openlmis.buq.i18n.MessageKeys.ERROR_PERIOD_FACILITY_PAIR_UNIQUE;

import java.util.Objects;
import java.util.UUID;
import org.openlmis.buq.domain.buq.BottomUpQuantification;
import org.openlmis.buq.domain.buq.BottomUpQuantificationStatus;
import org.openlmis.buq.dto.buq.BottomUpQuantificationDto;
import org.openlmis.buq.dto.buq.BottomUpQuantificationLineItemDto;
import org.openlmis.buq.exception.ValidationMessageException;
import org.openlmis.buq.service.buq.BottomUpQuantificationService;
import org.openlmis.buq.util.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;

@SuppressWarnings("PMD.TooManyMethods")
@Component
public class BottomUpQuantificationValidator extends BaseValidator {

  public static final String ANNUAL_ADJUSTED_CONSUMPTION_FIELD = "annualAdjustedConsumption";
  public static final String VERIFIED_ANNUAL_ADJUSTED_CONSUMPTION_FIELD =
      "verifiedAnnualAdjustedConsumption";
  public static final String FORECASTED_DEMAND_FIELD = "forecastedDemand";
  private static final String PROCESSING_PERIOD_ID_FIELD = "processingPeriodId";
  public static final String REMARK_FIELD = "remark";
  public static final String TOTAL_COST_FIELD = "totalCost";

  private final BottomUpQuantificationService bottomUpQuantificationService;

  @Autowired
  public BottomUpQuantificationValidator(
      @Lazy BottomUpQuantificationService bottomUpQuantificationService) {
    this.bottomUpQuantificationService = bottomUpQuantificationService;
  }

  @Override
  public boolean supports(Class<?> clazz) {
    return BottomUpQuantification.class.equals(clazz);
  }

  @Override
  public void validate(Object target, Errors errors) {
    validatePeriod(target, errors);
  }

  /**
   * Validates whether a BottomUpQuantificationDto can be submitted.
   *
   * @param target   The BottomUpQuantificationDto to be validated.
   * @param targetId The ID of the BottomUpQuantification to be validated.
   * @throws ValidationMessageException If the target cannot be submitted.
   */
  public void validateCanBeSubmitted(BottomUpQuantificationDto target, UUID targetId) {
    BottomUpQuantificationStatus buqStatus =
            bottomUpQuantificationService
                    .findBottomUpQuantification(targetId)
                    .getStatus();
    if (!buqStatus.equals(BottomUpQuantificationStatus.DRAFT)
        && !buqStatus.equals(BottomUpQuantificationStatus.REJECTED)) {
      throw new ValidationMessageException(
              new Message(ERROR_MUST_BE_DRAFT_OR_REJECTED_TO_BE_SUBMITTED));
    } else {
      validateCanChangeStatus(target);
    }
  }

  /**
   * Validates whether a BottomUpQuantificationDto can be authorized.
   *
   * @param target The BottomUpQuantificationDto to be validated.
   * @param targetId The ID of the BottomUpQuantification to be validated.
   * @throws ValidationMessageException If the target cannot be authorized.
   */
  public void validateCanBeAuthorized(BottomUpQuantificationDto target, UUID targetId) {
    BottomUpQuantification bottomUpQuantification = bottomUpQuantificationService
        .findBottomUpQuantification(targetId);
    if (!bottomUpQuantification.getStatus().equals(BottomUpQuantificationStatus.SUBMITTED)
        && !bottomUpQuantification.getStatus().equals(BottomUpQuantificationStatus.DRAFT)
        && !bottomUpQuantification.getStatus().equals(BottomUpQuantificationStatus.REJECTED)
        && bottomUpQuantificationService.canSkipAuthorization(bottomUpQuantification)
    ) {
      throw new ValidationMessageException(
              new Message(ERROR_MUST_BE_SUBMITTED_OR_REJECTED_TO_BE_AUTHORIZED));
    } else {
      validateCanChangeStatus(target);
    }
  }

  /**
   * Validates whether a BottomUpQuantificationDto can be approved.
   *
   * @param target   The BottomUpQuantificationDto to be validated.
   * @param targetId The ID of the BottomUpQuantification to be validated.
   * @throws ValidationMessageException If the target cannot be approved.
   */
  public void validateCanBeApproved(BottomUpQuantificationDto target, UUID targetId) {
    BottomUpQuantificationStatus status =
            bottomUpQuantificationService.findBottomUpQuantification(targetId).getStatus();
    if (!status.equals(BottomUpQuantificationStatus.AUTHORIZED)
        && !status.equals(BottomUpQuantificationStatus.IN_APPROVAL)) {
      throw new ValidationMessageException(
              new Message(ERROR_MUST_BE_AUTHORIZED_OR_IN_APPROVAL_TO_BE_APPROVED));
    } else {
      validateCanChangeStatus(target);
    }
  }

  /**
   * Validates whether a BottomUpQuantificationDto can be rejected.
   *
   * @param bottomUpQuantification The BottomUpQuantification to be validated.
   * @throws ValidationMessageException If the target cannot be rejected.
   */
  public void validateCanBeRejected(BottomUpQuantification bottomUpQuantification) {
    BottomUpQuantificationStatus status =
            bottomUpQuantification.getStatus();
    if (!status.equals(BottomUpQuantificationStatus.AUTHORIZED)
        && !status.equals(BottomUpQuantificationStatus.IN_APPROVAL)
        && !status.equals(BottomUpQuantificationStatus.APPROVED)) {
      throw new ValidationMessageException(
              new Message(ERROR_MUST_BE_AUTHORIZED_IN_APPROVAL_OR_APPROVED_TO_BE_REJECTED));
    }
  }

  private void validatePeriod(Object target, Errors errors) {
    BottomUpQuantification bottomUpQuantification = (BottomUpQuantification) target;
    if (bottomUpQuantificationService
        .existsByPeriodAndFacility(bottomUpQuantification.getFacilityId(),
            bottomUpQuantification.getProcessingPeriodId())) {
      rejectValue(errors, PROCESSING_PERIOD_ID_FIELD,
          new Message(ERROR_PERIOD_FACILITY_PAIR_UNIQUE));
    }
  }

  private void validateCanChangeStatus(BottomUpQuantificationDto target) {
    if (target.getBottomUpQuantificationLineItems().isEmpty()) {
      return;
    }
    for (BottomUpQuantificationLineItemDto lineItem :
        target.getBottomUpQuantificationLineItems()) {
      validateBottomUpQuantificationLineItemCanChangeStatus(lineItem);
    }
  }

  private void validateBottomUpQuantificationLineItemCanChangeStatus(
      BottomUpQuantificationLineItemDto target) {
    rejectIfNullOrNegative(target.getAnnualAdjustedConsumption(),
        ANNUAL_ADJUSTED_CONSUMPTION_FIELD);
    rejectIfNullOrNegative(target.getVerifiedAnnualAdjustedConsumption(),
        VERIFIED_ANNUAL_ADJUSTED_CONSUMPTION_FIELD);
    rejectIfNullOrNegative(target.getForecastedDemand(), FORECASTED_DEMAND_FIELD);

    rejectIfNull(target.getTotalCost(), ERROR_LINE_ITEM_FIELD_REQUIRED, TOTAL_COST_FIELD);
    rejectIfLessThanZero(target.getTotalCost().getAmountMajorInt(),
        ERROR_LINE_ITEM_FIELD_REQUIRED, TOTAL_COST_FIELD);

    if (!Objects.equals(target.getVerifiedAnnualAdjustedConsumption(),
        target.getForecastedDemand())) {
      rejectIfNull(target.getRemark(), ERROR_LINE_ITEM_REMARK_REQUIRED, REMARK_FIELD);
    }

  }

  private void rejectIfNullOrNegative(Integer value, String fieldName) {
    rejectIfNull(value, ERROR_LINE_ITEM_FIELD_REQUIRED, fieldName);
    rejectIfLessThanZero(value, ERROR_LINE_ITEM_FIELD_MUST_BE_NON_NEGATIVE, fieldName);
  }

  private void rejectIfLessThanZero(Integer value, String errorMessage, String fieldName) {
    if (value != null && value < 0) {
      throw new ValidationMessageException(errorMessage, fieldName);
    }
  }

  private void rejectIfNull(Object value, String errorMessage, String fieldName) {
    if (value == null) {
      throw new ValidationMessageException(errorMessage, fieldName);
    }
  }

}
