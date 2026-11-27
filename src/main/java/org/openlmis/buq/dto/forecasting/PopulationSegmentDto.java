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
import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Digits;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.openlmis.buq.domain.forecasting.PopulationSegment;
import org.openlmis.buq.dto.BaseDto;

@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public final class PopulationSegmentDto extends BaseDto implements PopulationSegment.Importer,
    PopulationSegment.Exporter {

  @NotBlank
  @Size(max = 50)
  private String code;

  @NotBlank
  @Size(max = 255)
  private String name;

  private String description;

  @NotNull
  @DecimalMin("0")
  @DecimalMax("100")
  @Digits(integer = 3, fraction = 2)
  private BigDecimal populationPercentage;

  /**
   * Creates new instance based on domain object.
   */
  public static PopulationSegmentDto newInstance(PopulationSegment segment) {
    PopulationSegmentDto dto = new PopulationSegmentDto();
    segment.export(dto);

    return dto;
  }

}
