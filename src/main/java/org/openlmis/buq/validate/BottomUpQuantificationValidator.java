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
import static org.openlmis.buq.i18n.MessageKeys.ERROR_MUST_BE_DRAFT_TO_BE_SUBMITTED;
import static org.openlmis.buq.i18n.MessageKeys.ERROR_MUST_BE_SUBMITTED_TO_BE_AUTHORIZED;
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

@Component
public class BottomUpQuantificationValidator extends BaseValidator {

  private final BottomUpQuantificationService bottomUpQuantificationService;

  private static final String PROCESSING_PERIOD_ID = "processingPeriodId";

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
    if (!bottomUpQuantificationService.findBottomUpQuantification(targetId).getStatus()
        .equals(BottomUpQuantificationStatus.DRAFT)) {
      throw new ValidationMessageException(new Message(ERROR_MUST_BE_DRAFT_TO_BE_SUBMITTED));
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
    if (!bottomUpQuantificationService.findBottomUpQuantification(targetId).getStatus()
        .equals(BottomUpQuantificationStatus.SUBMITTED)) {
      throw new ValidationMessageException(new Message(ERROR_MUST_BE_SUBMITTED_TO_BE_AUTHORIZED));
    } else {
      validateCanChangeStatus(target);
    }
  }

  private void validatePeriod(Object target, Errors errors) {
    BottomUpQuantification bottomUpQuantification = (BottomUpQuantification) target;
    if (bottomUpQuantificationService
        .existsByPeriodAndFacility(bottomUpQuantification.getFacilityId(),
            bottomUpQuantification.getProcessingPeriodId())) {
      rejectValue(errors, PROCESSING_PERIOD_ID, new Message(ERROR_PERIOD_FACILITY_PAIR_UNIQUE));
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
    rejectIfNullOrNegative(target.getAnnualAdjustedConsumption(), "annualAdjustedConsumption");
    rejectIfNullOrNegative(target.getVerifiedAnnualAdjustedConsumption(),
        "verifiedAnnualAdjustedConsumption");
    rejectIfNullOrNegative(target.getForecastedDemand(), "forecastedDemand");

    if (!Objects.equals(target.getAnnualAdjustedConsumption(), target.getForecastedDemand())) {
      rejectIfNull(target.getRemark(), ERROR_LINE_ITEM_REMARK_REQUIRED, "remark");
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
