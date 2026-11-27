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

import java.util.UUID;
import org.openlmis.buq.domain.forecasting.Forecast;
import org.openlmis.buq.domain.forecasting.ForecastStatus;
import org.openlmis.buq.domain.forecasting.ForecastStatusChange;

public class ForecastStatusChangeDataBuilder {

  private UUID id = UUID.randomUUID();
  private Forecast forecast = new ForecastDataBuilder().buildAsNew();
  private UUID authorId = UUID.randomUUID();
  private ForecastStatus status = ForecastStatus.DRAFT;

  public ForecastStatusChangeDataBuilder withForecast(Forecast forecast) {
    this.forecast = forecast;
    return this;
  }

  public ForecastStatusChangeDataBuilder withStatus(ForecastStatus status) {
    this.status = status;
    return this;
  }

  /**
   * Builds new instance of ForecastStatusChange (with id field).
   */
  public ForecastStatusChange build() {
    ForecastStatusChange statusChange = buildAsNew();
    statusChange.setId(id);

    return statusChange;
  }

  /**
   * Builds new instance of ForecastStatusChange as a new object (without id field).
   */
  public ForecastStatusChange buildAsNew() {
    return ForecastStatusChange.newInstance(forecast, authorId, status);
  }

}
