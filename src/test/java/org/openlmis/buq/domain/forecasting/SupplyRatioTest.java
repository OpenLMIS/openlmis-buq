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
import org.openlmis.buq.builder.SupplyRatioDataBuilder;
import org.openlmis.buq.dto.forecasting.SupplyRatioDto;

public class SupplyRatioTest {

  @Test
  public void shouldRoundTripAllTypesByName() {
    assertThat(SupplyRatioType.values())
        .containsExactly(SupplyRatioType.DOSE, SupplyRatioType.VIAL, SupplyRatioType.BOX);
    for (SupplyRatioType type : SupplyRatioType.values()) {
      assertThat(SupplyRatioType.valueOf(type.name())).isEqualTo(type);
    }
  }

  @Test
  public void shouldCreateNewInstance() {
    SupplyRatio ratio = new SupplyRatioDataBuilder().build();
    SupplyRatioDto dto = SupplyRatioDto.newInstance(ratio);

    SupplyRatio newRatio = SupplyRatio.newInstance(dto);

    assertThat(newRatio).isEqualTo(ratio);
  }

  @Test
  public void shouldUpdateFromKeepingId() {
    SupplyRatio ratio = new SupplyRatioDataBuilder().build();
    final UUID id = ratio.getId();
    SupplyRatioDto dto = SupplyRatioDto.newInstance(ratio);
    dto.setId(UUID.randomUUID());
    dto.setType(SupplyRatioType.BOX);
    dto.setRatio(new BigDecimal("0.0100"));

    ratio.updateFrom(dto);

    assertThat(ratio.getId()).isEqualTo(id);
    assertThat(ratio.getType()).isEqualTo(SupplyRatioType.BOX);
    assertThat(ratio.getRatio()).isEqualByComparingTo("0.01");
  }

  @Test
  public void shouldExportData() {
    SupplyRatio ratio = new SupplyRatioDataBuilder().build();
    SupplyRatioDto dto = new SupplyRatioDto();

    ratio.export(dto);

    assertThat(dto.getId()).isEqualTo(ratio.getId());
    assertThat(dto.getVaccineOrderableId()).isEqualTo(ratio.getVaccineOrderableId());
    assertThat(dto.getSupplyOrderableId()).isEqualTo(ratio.getSupplyOrderableId());
    assertThat(dto.getType()).isEqualTo(ratio.getType());
    assertThat(dto.getRatio()).isEqualTo(ratio.getRatio());
  }

}
