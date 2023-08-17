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

package org.openlmis.buq.service.buq;

import java.util.List;
import java.util.Optional;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.openlmis.buq.dto.referencedata.ProcessingPeriodDto;
import org.openlmis.buq.dto.requisition.RequisitionLineItemDataProjection;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class AnnualAdjustedConsumptionCalculator {

  /**
   * Calculates annual adjusted consumption for the product.
   *
   * @param requisitionLineItems items containing data on the adjusted consumption of the product.
   * @param processingPeriod the period from which we consider data for calculations.
   * @return calculated annual adjusted consumption.
   */
  public static Integer calculate(List<RequisitionLineItemDataProjection> requisitionLineItems,
      ProcessingPeriodDto processingPeriod) {
    return requisitionLineItems.stream()
        .filter(lineItem -> !lineItem.getStartDate().isBefore(processingPeriod.getStartDate())
            && !lineItem.getEndDate().isAfter(processingPeriod.getEndDate()))
        .map(lineItem -> Optional
            .ofNullable(lineItem.getAdjustedConsumption())
            .orElse(0))
        .mapToInt(Integer::intValue)
        .sum();
  }

}
