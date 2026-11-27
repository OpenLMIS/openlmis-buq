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
import org.openlmis.buq.builder.PopulationSegmentDataBuilder;
import org.openlmis.buq.dto.forecasting.PopulationSegmentDto;

public class PopulationSegmentTest {

  private static final String CODE = "CHB1";

  @Test
  public void shouldCreateWithFields() {
    PopulationSegment segment = new PopulationSegment(CODE, "Children below 1 years old",
        "description", new BigDecimal("3.50"));

    assertThat(segment.getCode()).isEqualTo(CODE);
    assertThat(segment.getName()).isEqualTo("Children below 1 years old");
    assertThat(segment.getDescription()).isEqualTo("description");
    assertThat(segment.getPopulationPercentage()).isEqualByComparingTo("3.50");
  }

  @Test
  public void shouldBeEqualForSameIdAndFields() {
    UUID id = UUID.randomUUID();
    PopulationSegment segment1 = new PopulationSegment(CODE, "name", null, BigDecimal.ONE);
    PopulationSegment segment2 = new PopulationSegment(CODE, "name", null, BigDecimal.ONE);
    segment1.setId(id);
    segment2.setId(id);

    assertThat(segment1).isEqualTo(segment2);
    assertThat(segment1.hashCode()).isEqualTo(segment2.hashCode());
  }

  @Test
  public void shouldCreateNewInstance() {
    PopulationSegment segment = new PopulationSegmentDataBuilder().build();
    PopulationSegmentDto dto = PopulationSegmentDto.newInstance(segment);

    PopulationSegment newSegment = PopulationSegment.newInstance(dto);

    assertThat(newSegment).isEqualTo(segment);
  }

  @Test
  public void shouldUpdateFromKeepingId() {
    PopulationSegment segment = new PopulationSegmentDataBuilder().build();
    final UUID id = segment.getId();
    PopulationSegmentDto dto = PopulationSegmentDto.newInstance(segment);
    dto.setId(UUID.randomUUID());
    dto.setName("Pregnant women");
    dto.setPopulationPercentage(new BigDecimal("3.60"));

    segment.updateFrom(dto);

    assertThat(segment.getId()).isEqualTo(id);
    assertThat(segment.getName()).isEqualTo("Pregnant women");
    assertThat(segment.getPopulationPercentage()).isEqualByComparingTo("3.60");
  }

  @Test
  public void shouldExportData() {
    PopulationSegment segment = new PopulationSegmentDataBuilder().build();
    PopulationSegmentDto dto = new PopulationSegmentDto();

    segment.export(dto);

    assertThat(dto.getId()).isEqualTo(segment.getId());
    assertThat(dto.getCode()).isEqualTo(segment.getCode());
    assertThat(dto.getName()).isEqualTo(segment.getName());
    assertThat(dto.getDescription()).isEqualTo(segment.getDescription());
    assertThat(dto.getPopulationPercentage()).isEqualTo(segment.getPopulationPercentage());
  }

}
