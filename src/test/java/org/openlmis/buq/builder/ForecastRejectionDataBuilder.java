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
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import org.openlmis.buq.domain.forecasting.ForecastRejection;
import org.openlmis.buq.domain.forecasting.ForecastStatusChange;

public class ForecastRejectionDataBuilder {

  private UUID id = UUID.randomUUID();
  private ForecastStatusChange statusChange = new ForecastStatusChangeDataBuilder().buildAsNew();
  private List<UUID> rejectionReasonIds = new ArrayList<>(
      Arrays.asList(UUID.randomUUID(), UUID.randomUUID()));
  private String generalComments = "rejection-comment";

  public ForecastRejectionDataBuilder withStatusChange(ForecastStatusChange statusChange) {
    this.statusChange = statusChange;
    return this;
  }

  /**
   * Builds new instance of ForecastRejection (with id field).
   */
  public ForecastRejection build() {
    ForecastRejection rejection = buildAsNew();
    rejection.setId(id);

    return rejection;
  }

  /**
   * Builds new instance of ForecastRejection as a new object (without id field).
   */
  public ForecastRejection buildAsNew() {
    ForecastRejection rejection = new ForecastRejection();
    rejection.setStatusChange(statusChange);
    rejection.setRejectionReasonIds(new ArrayList<>(rejectionReasonIds));
    rejection.setGeneralComments(generalComments);

    return rejection;
  }

}
