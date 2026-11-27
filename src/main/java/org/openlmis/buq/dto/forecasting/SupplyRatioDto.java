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

package org.openlmis.buq.dto.forecasting;

import java.math.BigDecimal;
import java.util.UUID;
import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Digits;
import javax.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.openlmis.buq.domain.forecasting.SupplyRatio;
import org.openlmis.buq.domain.forecasting.SupplyRatioType;
import org.openlmis.buq.dto.BaseDto;

@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public final class SupplyRatioDto extends BaseDto implements SupplyRatio.Importer,
    SupplyRatio.Exporter {

  @NotNull
  private UUID vaccineOrderableId;

  @NotNull
  private UUID supplyOrderableId;

  @NotNull
  private SupplyRatioType type;

  @NotNull
  @DecimalMin(value = "0", inclusive = false)
  @DecimalMax("100")
  @Digits(integer = 5, fraction = 4)
  private BigDecimal ratio;

  /**
   * Creates new instance based on domain object.
   */
  public static SupplyRatioDto newInstance(SupplyRatio supplyRatio) {
    SupplyRatioDto dto = new SupplyRatioDto();
    supplyRatio.export(dto);

    return dto;
  }

}
