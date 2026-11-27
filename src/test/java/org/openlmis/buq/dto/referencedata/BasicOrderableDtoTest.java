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

package org.openlmis.buq.dto.referencedata;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.openlmis.buq.builder.OrderableDtoDataBuilder;
import org.openlmis.buq.dto.EqualsContractTest;

public class BasicOrderableDtoTest extends EqualsContractTest<BasicOrderableDto> {

  @Override
  protected Class<BasicOrderableDto> getTestClass() {
    return BasicOrderableDto.class;
  }

  @Test
  public void shouldReadCommodityTypeFromIdentifiers() {
    BasicOrderableDto orderable = new OrderableDtoDataBuilder()
        .withCommodityType("bcg")
        .buildAsDto();

    assertThat(orderable.getCommodityType()).isEqualTo("bcg");
  }

  @Test
  public void shouldHaveNoCommodityTypeForTradeItemOnlyOrderable() {
    BasicOrderableDto orderable = new OrderableDtoDataBuilder().buildAsDto();
    orderable.getIdentifiers().put("tradeItem", "trade-item-id");

    assertThat(orderable.getCommodityType()).isNull();
  }

  @Test
  public void shouldHaveNoCommodityTypeWhenIdentifiersAreNull() {
    BasicOrderableDto orderable = new OrderableDtoDataBuilder().buildAsDto();
    orderable.setIdentifiers(null);

    assertThat(orderable.getCommodityType()).isNull();
  }

}
