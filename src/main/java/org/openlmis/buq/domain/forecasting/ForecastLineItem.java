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
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.openlmis.buq.domain.BaseEntity;

@Entity
@Table(name = "forecast_line_items")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ForecastLineItem extends BaseEntity {

  @ManyToOne(optional = false)
  @JoinColumn(name = "forecastId")
  @EqualsAndHashCode.Exclude
  @ToString.Exclude
  private Forecast forecast;

  @NotNull
  private UUID orderableId;

  @NotNull
  @Enumerated(EnumType.STRING)
  private ForecastLineItemType type;

  @ManyToOne
  @JoinColumn(name = "populationSegmentId")
  private PopulationSegment populationSegment;

  private Integer dosesPerTarget;

  private BigDecimal coveragePercentage;

  private BigDecimal wastagePercentage;

  private BigDecimal bufferMonths;

  private Integer targetPopulation;

  private Integer annualDemand;

  private Integer bufferStock;

  @NotNull
  private Integer forecastedAmount;

  /**
   * Creates a vaccine line item, keeping the setting values it was calculated from.
   */
  public static ForecastLineItem vaccine(ForecastingSetting setting, Integer targetPopulation,
      Integer annualDemand, Integer bufferStock, Integer forecastedAmount) {
    ForecastLineItem lineItem = new ForecastLineItem();
    lineItem.setType(ForecastLineItemType.VACCINE);
    lineItem.setOrderableId(setting.getOrderableId());
    lineItem.setPopulationSegment(setting.getPopulationSegment());
    lineItem.setDosesPerTarget(setting.getDosesPerTarget());
    lineItem.setCoveragePercentage(setting.getCoveragePercentage());
    lineItem.setWastagePercentage(setting.getWastagePercentage());
    lineItem.setBufferMonths(setting.getBufferMonths());
    lineItem.setTargetPopulation(targetPopulation);
    lineItem.setAnnualDemand(annualDemand);
    lineItem.setBufferStock(bufferStock);
    lineItem.setForecastedAmount(forecastedAmount);
    return lineItem;
  }

  /**
   * Creates a supply line item, which carries only the forecasted amount.
   */
  public static ForecastLineItem supply(UUID orderableId, Integer forecastedAmount) {
    ForecastLineItem lineItem = new ForecastLineItem();
    lineItem.setType(ForecastLineItemType.SUPPLY);
    lineItem.setOrderableId(orderableId);
    lineItem.setForecastedAmount(forecastedAmount);
    return lineItem;
  }

}
