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
import org.openlmis.buq.domain.forecasting.SupplyRatio;
import org.openlmis.buq.domain.forecasting.SupplyRatioType;

public class SupplyRatioDataBuilder {

  private UUID id = UUID.randomUUID();
  private UUID vaccineOrderableId = UUID.randomUUID();
  private UUID supplyOrderableId = UUID.randomUUID();
  private SupplyRatioType type = SupplyRatioType.DOSE;
  private BigDecimal ratio = new BigDecimal("1.1100");

  public SupplyRatioDataBuilder withVaccineOrderableId(UUID vaccineOrderableId) {
    this.vaccineOrderableId = vaccineOrderableId;
    return this;
  }

  public SupplyRatioDataBuilder withSupplyOrderableId(UUID supplyOrderableId) {
    this.supplyOrderableId = supplyOrderableId;
    return this;
  }

  public SupplyRatioDataBuilder withType(SupplyRatioType type) {
    this.type = type;
    return this;
  }

  public SupplyRatioDataBuilder withRatio(BigDecimal ratio) {
    this.ratio = ratio;
    return this;
  }

  /**
   * Builds new instance of SupplyRatio (with id field).
   */
  public SupplyRatio build() {
    SupplyRatio supplyRatio = buildAsNew();
    supplyRatio.setId(id);

    return supplyRatio;
  }

  /**
   * Builds new instance of SupplyRatio as a new object (without id field).
   */
  public SupplyRatio buildAsNew() {
    SupplyRatio supplyRatio = new SupplyRatio();
    supplyRatio.setVaccineOrderableId(vaccineOrderableId);
    supplyRatio.setSupplyOrderableId(supplyOrderableId);
    supplyRatio.setType(type);
    supplyRatio.setRatio(ratio);

    return supplyRatio;
  }

}
