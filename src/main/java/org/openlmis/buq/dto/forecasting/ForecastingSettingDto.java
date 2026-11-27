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
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.openlmis.buq.domain.forecasting.ForecastingSetting;
import org.openlmis.buq.dto.BaseDto;

@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public final class ForecastingSettingDto extends BaseDto implements ForecastingSetting.Importer,
    ForecastingSetting.Exporter {

  @NotNull
  private UUID programId;

  @NotNull
  private UUID orderableId;

  @NotNull
  private UUID populationSegmentId;

  @NotNull
  @Min(1)
  private Integer dosesPerTarget;

  @NotNull
  @DecimalMin("0")
  @DecimalMax("100")
  @Digits(integer = 3, fraction = 2)
  private BigDecimal coveragePercentage;

  // The calculation divides by (100 - wastage), so 100 itself is refused.
  @NotNull
  @DecimalMin("0")
  @DecimalMax(value = "100", inclusive = false)
  @Digits(integer = 3, fraction = 2)
  private BigDecimal wastagePercentage;

  @DecimalMin("0")
  @DecimalMax("12")
  @Digits(integer = 2, fraction = 2)
  private BigDecimal bufferMonths;

  /**
   * Creates new instance based on domain object.
   */
  public static ForecastingSettingDto newInstance(ForecastingSetting setting) {
    ForecastingSettingDto dto = new ForecastingSettingDto();
    setting.export(dto);

    return dto;
  }

}
