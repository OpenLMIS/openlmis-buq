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

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.UUID;
import org.junit.Test;
import org.openlmis.buq.builder.ForecastingSettingDataBuilder;

public class ForecastLineItemTest {

  @Test
  public void shouldBuildSupplyWithoutVaccineFields() {
    UUID orderableId = UUID.randomUUID();

    ForecastLineItem lineItem = ForecastLineItem.supply(orderableId, 1000);

    assertThat(lineItem.getType()).isEqualTo(ForecastLineItemType.SUPPLY);
    assertThat(lineItem.getOrderableId()).isEqualTo(orderableId);
    assertThat(lineItem.getForecastedAmount()).isEqualTo(1000);
    assertThat(lineItem.getPopulationSegment()).isNull();
    assertThat(lineItem.getDosesPerTarget()).isNull();
    assertThat(lineItem.getCoveragePercentage()).isNull();
    assertThat(lineItem.getWastagePercentage()).isNull();
    assertThat(lineItem.getBufferMonths()).isNull();
    assertThat(lineItem.getTargetPopulation()).isNull();
    assertThat(lineItem.getAnnualDemand()).isNull();
    assertThat(lineItem.getBufferStock()).isNull();
  }

  @Test
  public void shouldBuildVaccineWithAllFieldsFromSetting() {
    ForecastingSetting setting = new ForecastingSettingDataBuilder()
        .withDosesPerTarget(2)
        .withCoveragePercentage(new BigDecimal("95.00"))
        .withWastagePercentage(new BigDecimal("5.00"))
        .withBufferMonths(new BigDecimal("0.10"))
        .buildAsNew();

    ForecastLineItem lineItem = ForecastLineItem.vaccine(setting, 76584, 152785, 1273, 154058);

    assertThat(lineItem.getType()).isEqualTo(ForecastLineItemType.VACCINE);
    assertThat(lineItem.getOrderableId()).isEqualTo(setting.getOrderableId());
    assertThat(lineItem.getPopulationSegment()).isSameAs(setting.getPopulationSegment());
    assertThat(lineItem.getDosesPerTarget()).isEqualTo(2);
    assertThat(lineItem.getCoveragePercentage()).isEqualByComparingTo("95.00");
    assertThat(lineItem.getWastagePercentage()).isEqualByComparingTo("5.00");
    assertThat(lineItem.getBufferMonths()).isEqualByComparingTo("0.10");
    assertThat(lineItem.getTargetPopulation()).isEqualTo(76584);
    assertThat(lineItem.getAnnualDemand()).isEqualTo(152785);
    assertThat(lineItem.getBufferStock()).isEqualTo(1273);
    assertThat(lineItem.getForecastedAmount()).isEqualTo(154058);
  }

}
