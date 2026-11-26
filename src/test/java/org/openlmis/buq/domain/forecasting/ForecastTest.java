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

import java.util.UUID;
import org.junit.Test;
import org.openlmis.buq.builder.ForecastDataBuilder;
import org.openlmis.buq.builder.ForecastLineItemDataBuilder;

public class ForecastTest {

  @Test
  public void shouldStartInDraft() {
    Forecast forecast = new Forecast(UUID.randomUUID(), UUID.randomUUID(), 2027, 1000);

    assertThat(forecast.getStatus()).isEqualTo(ForecastStatus.DRAFT);
    assertThat(forecast.getLineItems()).isEmpty();
    assertThat(forecast.getStatusChanges()).isEmpty();
  }

  @Test
  public void shouldSetBackReferenceWhenAddingLineItem() {
    Forecast forecast = new ForecastDataBuilder().buildAsNew();
    ForecastLineItem lineItem = new ForecastLineItemDataBuilder().buildAsNew();

    forecast.addLineItem(lineItem);

    assertThat(forecast.getLineItems()).containsExactly(lineItem);
    assertThat(lineItem.getForecast()).isSameAs(forecast);
  }

  @Test
  public void shouldRecordCurrentStatusInStatusChange() {
    Forecast forecast = new ForecastDataBuilder()
        .withStatus(ForecastStatus.SUBMITTED)
        .buildAsNew();
    UUID authorId = UUID.randomUUID();

    ForecastStatusChange statusChange = forecast.addStatusChange(authorId);

    assertThat(statusChange.getStatus()).isEqualTo(ForecastStatus.SUBMITTED);
    assertThat(statusChange.getAuthorId()).isEqualTo(authorId);
    assertThat(statusChange.getForecast()).isSameAs(forecast);
    assertThat(forecast.getStatusChanges()).containsExactly(statusChange);
  }

  @Test
  public void shouldNotRecurseInHashCodeAndToString() {
    Forecast forecast = new ForecastDataBuilder()
        .withLineItem(new ForecastLineItemDataBuilder().build())
        .build();
    forecast.addStatusChange(UUID.randomUUID());

    assertThat(forecast.hashCode()).isEqualTo(forecast.hashCode());
    assertThat(forecast.toString()).contains(forecast.getId().toString());
  }

}
