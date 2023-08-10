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

package org.openlmis.buq.domain.buq;

import static org.assertj.core.api.Assertions.assertThat;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.Test;
import org.openlmis.buq.ToStringTestUtils;
import org.openlmis.buq.builder.BottomUpQuantificationDataBuilder;
import org.openlmis.buq.builder.BottomUpQuantificationLineItemDataBuilder;
import org.openlmis.buq.dto.buq.BottomUpQuantificationLineItemDto;

public class BottomUpQuantificationLineItemTest {

  @Test
  public void equalsContract() {
    BottomUpQuantification buq1 = new BottomUpQuantificationDataBuilder().build();
    BottomUpQuantification buq2 = new BottomUpQuantification();

    EqualsVerifier
        .forClass(BottomUpQuantificationLineItem.class)
        .withRedefinedSuperclass()
        .withPrefabValues(BottomUpQuantification.class, buq1, buq2)
        .suppress(Warning.NONFINAL_FIELDS)
        .verify();
  }

  @Test
  public void shouldImplementToString() {
    BottomUpQuantificationLineItem buqLineItem =
        new BottomUpQuantificationLineItemDataBuilder().build();
    ToStringTestUtils.verify(BottomUpQuantificationLineItem.class, buqLineItem, "TEXT");
  }

  @Test
  public void shouldExportData() {
    BottomUpQuantificationLineItem buqLineItem = new BottomUpQuantificationLineItemDataBuilder()
        .build();
    BottomUpQuantificationLineItemDto dto = new BottomUpQuantificationLineItemDto();

    buqLineItem.export(dto);

    assertThat(dto.getOrderableId()).isEqualTo(buqLineItem.getOrderableId());
    assertThat(dto.getAnnualAdjustedConsumption())
        .isEqualTo(buqLineItem.getAnnualAdjustedConsumption());
    assertThat(dto.getVerifiedAnnualAdjustedConsumption())
        .isEqualTo(buqLineItem.getVerifiedAnnualAdjustedConsumption());
    assertThat(dto.getForecastedDemand()).isEqualTo(buqLineItem.getForecastedDemand());
  }

}
