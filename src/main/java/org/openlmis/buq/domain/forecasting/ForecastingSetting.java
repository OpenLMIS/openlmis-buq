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

package org.openlmis.buq.domain.forecasting;

import java.math.BigDecimal;
import java.util.UUID;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.openlmis.buq.domain.BaseTimestampedEntity;

@Entity
@Table(name = "forecasting_settings")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ForecastingSetting extends BaseTimestampedEntity {

  @NotNull
  private UUID programId;

  @NotNull
  private UUID orderableId;

  @NotNull
  @ManyToOne(optional = false)
  @JoinColumn(name = "populationSegmentId")
  private PopulationSegment populationSegment;

  @NotNull
  private Integer dosesPerTarget;

  @NotNull
  private BigDecimal coveragePercentage;

  @NotNull
  private BigDecimal wastagePercentage;

  @NotNull
  private BigDecimal bufferMonths = new BigDecimal("1.00");

}
