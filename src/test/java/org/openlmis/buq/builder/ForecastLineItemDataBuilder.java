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

package org.openlmis.buq.builder;

import java.math.BigDecimal;
import java.util.UUID;
import org.openlmis.buq.domain.forecasting.ForecastLineItem;
import org.openlmis.buq.domain.forecasting.ForecastingSetting;
import org.openlmis.buq.domain.forecasting.PopulationSegment;

public class ForecastLineItemDataBuilder {

  private UUID id = UUID.randomUUID();
  private UUID orderableId = UUID.randomUUID();
  private boolean supply = false;
  private PopulationSegment populationSegment = new PopulationSegmentDataBuilder().buildAsNew();
  private Integer dosesPerTarget = 2;
  private BigDecimal coveragePercentage = new BigDecimal("95.00");
  private BigDecimal wastagePercentage = new BigDecimal("5.00");
  private BigDecimal bufferMonths = new BigDecimal("0.10");
  private Integer targetPopulation = 76584;
  private Integer annualDemand = 152785;
  private Integer bufferStock = 1273;
  private Integer forecastedAmount = 154058;

  /**
   * Turns the built item into a supply row with an amount of 1000.
   */
  public ForecastLineItemDataBuilder asSupply() {
    this.supply = true;
    this.forecastedAmount = 1000;
    return this;
  }

  public ForecastLineItemDataBuilder withOrderableId(UUID orderableId) {
    this.orderableId = orderableId;
    return this;
  }

  public ForecastLineItemDataBuilder withPopulationSegment(PopulationSegment segment) {
    this.populationSegment = segment;
    return this;
  }

  public ForecastLineItemDataBuilder withTargetPopulation(Integer targetPopulation) {
    this.targetPopulation = targetPopulation;
    return this;
  }

  public ForecastLineItemDataBuilder withForecastedAmount(Integer forecastedAmount) {
    this.forecastedAmount = forecastedAmount;
    return this;
  }

  /**
   * Builds new instance of ForecastLineItem (with id field).
   */
  public ForecastLineItem build() {
    ForecastLineItem lineItem = buildAsNew();
    lineItem.setId(id);

    return lineItem;
  }

  /**
   * Builds new instance of ForecastLineItem as a new object (without id field).
   */
  public ForecastLineItem buildAsNew() {
    if (supply) {
      return ForecastLineItem.supply(orderableId, forecastedAmount);
    }

    ForecastingSetting setting = new ForecastingSettingDataBuilder()
        .withOrderableId(orderableId)
        .withPopulationSegment(populationSegment)
        .withDosesPerTarget(dosesPerTarget)
        .withCoveragePercentage(coveragePercentage)
        .withWastagePercentage(wastagePercentage)
        .withBufferMonths(bufferMonths)
        .buildAsNew();

    return ForecastLineItem.vaccine(setting, targetPopulation, annualDemand, bufferStock,
        forecastedAmount);
  }

}
