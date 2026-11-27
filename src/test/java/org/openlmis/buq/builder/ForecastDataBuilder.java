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

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.openlmis.buq.domain.forecasting.Forecast;
import org.openlmis.buq.domain.forecasting.ForecastLineItem;
import org.openlmis.buq.domain.forecasting.ForecastStatus;

public class ForecastDataBuilder {

  private UUID id = UUID.randomUUID();
  private UUID facilityId = UUID.randomUUID();
  private UUID programId = UUID.randomUUID();
  private Integer targetYear = 2027;
  private Integer catchmentPopulation = 2252483;
  private ForecastStatus status = ForecastStatus.DRAFT;
  private UUID supervisoryNodeId = UUID.randomUUID();
  private final List<ForecastLineItem> lineItems = new ArrayList<>();

  public ForecastDataBuilder withFacilityId(UUID facilityId) {
    this.facilityId = facilityId;
    return this;
  }

  public ForecastDataBuilder withProgramId(UUID programId) {
    this.programId = programId;
    return this;
  }

  public ForecastDataBuilder withTargetYear(Integer targetYear) {
    this.targetYear = targetYear;
    return this;
  }

  public ForecastDataBuilder withCatchmentPopulation(Integer catchmentPopulation) {
    this.catchmentPopulation = catchmentPopulation;
    return this;
  }

  public ForecastDataBuilder withStatus(ForecastStatus status) {
    this.status = status;
    return this;
  }

  public ForecastDataBuilder withLineItem(ForecastLineItem lineItem) {
    this.lineItems.add(lineItem);
    return this;
  }

  /**
   * Builds new instance of Forecast (with id field).
   */
  public Forecast build() {
    Forecast forecast = buildAsNew();
    forecast.setId(id);

    return forecast;
  }

  /**
   * Builds new instance of Forecast as a new object (without id field).
   */
  public Forecast buildAsNew() {
    Forecast forecast = new Forecast(facilityId, programId, targetYear, catchmentPopulation);
    forecast.setStatus(status);
    forecast.setSupervisoryNodeId(supervisoryNodeId);
    lineItems.forEach(forecast::addLineItem);

    return forecast;
  }

}
