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
import org.openlmis.buq.builder.PopulationSegmentDataBuilder;
import org.openlmis.buq.dto.forecasting.ForecastingSettingDto;

public class ForecastingSettingTest {

  @Test
  public void shouldDefaultBufferMonthsToOne() {
    assertThat(new ForecastingSetting().getBufferMonths()).isEqualByComparingTo("1");
  }

  @Test
  public void shouldDefaultBufferMonthsAtTheColumnScale() {
    assertThat(new ForecastingSetting().getBufferMonths()).isEqualTo(new BigDecimal("1.00"));
  }

  @Test
  public void shouldBuildAsNewWithoutIdAndWithSegment() {
    ForecastingSetting setting = new ForecastingSettingDataBuilder().buildAsNew();

    assertThat(setting.getId()).isNull();
    assertThat(setting.getPopulationSegment()).isNotNull();
  }

  @Test
  public void shouldCreateNewInstanceWithGivenSegment() {
    ForecastingSetting setting = new ForecastingSettingDataBuilder()
        .withPopulationSegment(new PopulationSegmentDataBuilder().build())
        .build();
    ForecastingSettingDto dto = ForecastingSettingDto.newInstance(setting);

    ForecastingSetting newSetting = ForecastingSetting.newInstance(dto,
        setting.getPopulationSegment());

    assertThat(newSetting).isEqualTo(setting);
  }

  @Test
  public void shouldUpdateFromKeepingId() {
    ForecastingSetting setting = new ForecastingSettingDataBuilder().build();
    final UUID id = setting.getId();
    final PopulationSegment otherSegment = new PopulationSegmentDataBuilder().build();
    ForecastingSettingDto dto = ForecastingSettingDto.newInstance(setting);
    dto.setId(UUID.randomUUID());
    dto.setDosesPerTarget(3);
    dto.setWastagePercentage(new BigDecimal("10.00"));

    setting.updateFrom(dto, otherSegment);

    assertThat(setting.getId()).isEqualTo(id);
    assertThat(setting.getDosesPerTarget()).isEqualTo(3);
    assertThat(setting.getWastagePercentage()).isEqualByComparingTo("10");
    assertThat(setting.getPopulationSegment()).isSameAs(otherSegment);
  }

  @Test
  public void shouldDefaultBufferMonthsToOneWhenImportedAsNull() {
    ForecastingSetting setting = new ForecastingSettingDataBuilder()
        .withBufferMonths(new BigDecimal("3.00"))
        .build();
    ForecastingSettingDto dto = ForecastingSettingDto.newInstance(setting);
    dto.setBufferMonths(null);

    setting.updateFrom(dto, setting.getPopulationSegment());

    assertThat(setting.getBufferMonths()).isEqualTo(new BigDecimal("1.00"));
  }

  @Test
  public void shouldExportSegmentAsId() {
    ForecastingSetting setting = new ForecastingSettingDataBuilder()
        .withPopulationSegment(new PopulationSegmentDataBuilder().build())
        .build();
    ForecastingSettingDto dto = new ForecastingSettingDto();

    setting.export(dto);

    assertThat(dto.getId()).isEqualTo(setting.getId());
    assertThat(dto.getProgramId()).isEqualTo(setting.getProgramId());
    assertThat(dto.getOrderableId()).isEqualTo(setting.getOrderableId());
    assertThat(dto.getPopulationSegmentId()).isEqualTo(setting.getPopulationSegment().getId());
    assertThat(dto.getDosesPerTarget()).isEqualTo(setting.getDosesPerTarget());
    assertThat(dto.getCoveragePercentage()).isEqualTo(setting.getCoveragePercentage());
    assertThat(dto.getWastagePercentage()).isEqualTo(setting.getWastagePercentage());
    assertThat(dto.getBufferMonths()).isEqualTo(setting.getBufferMonths());
  }

}
