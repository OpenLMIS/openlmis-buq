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
import org.openlmis.buq.domain.forecasting.ForecastingSetting;
import org.openlmis.buq.domain.forecasting.PopulationSegment;

public class ForecastingSettingDataBuilder {

  private UUID id = UUID.randomUUID();
  private UUID programId = UUID.randomUUID();
  private UUID orderableId = UUID.randomUUID();
  private PopulationSegment populationSegment = new PopulationSegmentDataBuilder().buildAsNew();
  private Integer dosesPerTarget = 1;
  private BigDecimal coveragePercentage = new BigDecimal("95.00");
  private BigDecimal wastagePercentage = new BigDecimal("5.00");
  private BigDecimal bufferMonths = BigDecimal.ONE;

  public ForecastingSettingDataBuilder withProgramId(UUID programId) {
    this.programId = programId;
    return this;
  }

  public ForecastingSettingDataBuilder withOrderableId(UUID orderableId) {
    this.orderableId = orderableId;
    return this;
  }

  public ForecastingSettingDataBuilder withPopulationSegment(PopulationSegment segment) {
    this.populationSegment = segment;
    return this;
  }

  public ForecastingSettingDataBuilder withDosesPerTarget(Integer dosesPerTarget) {
    this.dosesPerTarget = dosesPerTarget;
    return this;
  }

  public ForecastingSettingDataBuilder withCoveragePercentage(BigDecimal coveragePercentage) {
    this.coveragePercentage = coveragePercentage;
    return this;
  }

  public ForecastingSettingDataBuilder withWastagePercentage(BigDecimal wastagePercentage) {
    this.wastagePercentage = wastagePercentage;
    return this;
  }

  public ForecastingSettingDataBuilder withBufferMonths(BigDecimal bufferMonths) {
    this.bufferMonths = bufferMonths;
    return this;
  }

  /**
   * Builds new instance of ForecastingSetting (with id field).
   */
  public ForecastingSetting build() {
    ForecastingSetting setting = buildAsNew();
    setting.setId(id);

    return setting;
  }

  /**
   * Builds new instance of ForecastingSetting as a new object (without id field).
   */
  public ForecastingSetting buildAsNew() {
    ForecastingSetting setting = new ForecastingSetting();
    setting.setProgramId(programId);
    setting.setOrderableId(orderableId);
    setting.setPopulationSegment(populationSegment);
    setting.setDosesPerTarget(dosesPerTarget);
    setting.setCoveragePercentage(coveragePercentage);
    setting.setWastagePercentage(wastagePercentage);
    setting.setBufferMonths(bufferMonths);

    return setting;
  }

}
