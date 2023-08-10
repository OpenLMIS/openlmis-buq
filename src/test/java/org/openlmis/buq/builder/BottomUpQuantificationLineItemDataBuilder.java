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
import org.openlmis.buq.domain.buq.BottomUpQuantification;
import org.openlmis.buq.domain.buq.BottomUpQuantificationLineItem;

@SuppressWarnings("PMD.TooManyMethods")
public class BottomUpQuantificationLineItemDataBuilder {

  private UUID id = UUID.randomUUID();
  private BottomUpQuantification bottomUpQuantification =
      new BottomUpQuantificationDataBuilder().build();
  private UUID orderableId = UUID.randomUUID();
  private Integer annualAdjustedConsumption = 100;
  private Integer verifiedAnnualAdjustedConsumption = 110;
  private Integer forecastedDemand = 111;

  /**
   * Creates new instance of {@link BottomUpQuantificationLineItem}.
   */
  public BottomUpQuantificationLineItem build() {
    BottomUpQuantificationLineItem buq = buildAsNew();
    buq.setId(id);
    buq.setBottomUpQuantification(bottomUpQuantification);

    return buq;
  }

  /**
   * Creates new instance of {@link BottomUpQuantificationLineItem} dedicated to repository usage.
   */
  public BottomUpQuantificationLineItem buildAsNew() {
    BottomUpQuantificationLineItem buq = new BottomUpQuantificationLineItem(
        bottomUpQuantification, orderableId, annualAdjustedConsumption,
        verifiedAnnualAdjustedConsumption, forecastedDemand
    );

    return buq;
  }

  public BottomUpQuantificationLineItemDataBuilder withBottomUpQuantification(
      BottomUpQuantification buq) {
    this.bottomUpQuantification = buq;
    return this;
  }

  public BottomUpQuantificationLineItemDataBuilder withId(UUID id) {
    this.id = id;
    return this;
  }

  public BottomUpQuantificationLineItemDataBuilder withOrderableId(UUID orderableId) {
    this.orderableId = orderableId;
    return this;
  }

  public BottomUpQuantificationLineItemDataBuilder withAnnualAdjustedConsumption(
      Integer annualAdjustedConsumption) {
    this.annualAdjustedConsumption = annualAdjustedConsumption;
    return this;
  }

  public BottomUpQuantificationLineItemDataBuilder withVerifiedAnnualAdjustedConsumption(
      Integer verifiedAnnualAdjustedConsumption) {
    this.verifiedAnnualAdjustedConsumption = verifiedAnnualAdjustedConsumption;
    return this;
  }

  public BottomUpQuantificationLineItemDataBuilder withForecastedDemand(
      Integer forecastedDemand) {
    this.forecastedDemand = forecastedDemand;
    return this;
  }

}
