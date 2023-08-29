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

import static org.openlmis.buq.i18n.MessageKeys.ERROR_PERIOD_FACILITY_PAIR_UNIQUE;

import org.openlmis.buq.domain.buq.BottomUpQuantification;
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

  private void validatePeriod(Object target, Errors errors) {
    BottomUpQuantification bottomUpQuantification = (BottomUpQuantification) target;
    if (bottomUpQuantificationService
            .existsByPeriodAndFacility(bottomUpQuantification.getFacilityId(),
                    bottomUpQuantification.getProcessingPeriodId())) {
      rejectValue(errors, PROCESSING_PERIOD_ID, new Message(ERROR_PERIOD_FACILITY_PAIR_UNIQUE));
    }
  }
}
