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

package org.openlmis.buq.domain.sourceoffund;

import static org.assertj.core.api.Assertions.assertThat;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.Test;
import org.openlmis.buq.ToStringTestUtils;
import org.openlmis.buq.builder.SourceOfFundDataBuilder;
import org.openlmis.buq.dto.sourceoffund.SourceOfFundDto;

public class SourceOfFundTest {

  @Test
  public void equalsContract() {
    EqualsVerifier
        .forClass(SourceOfFund.class)
        .withRedefinedSuperclass()
        .suppress(Warning.NONFINAL_FIELDS)
        .verify();
  }

  @Test
  public void shouldImplementToString() {
    SourceOfFund sourceOfFund = new SourceOfFundDataBuilder().build();
    ToStringTestUtils.verify(SourceOfFund.class, sourceOfFund, "TEXT");
  }

  @Test
  public void shouldCreateNewInstance() {
    SourceOfFund sourceOfFund = new SourceOfFundDataBuilder().build();
    SourceOfFundDto dto = SourceOfFundDto.newInstance(sourceOfFund);

    SourceOfFund newSourceOfFund = SourceOfFund.newInstance(dto);

    assertThat(newSourceOfFund).isEqualTo(sourceOfFund);
  }

  @Test
  public void shouldUpdateFrom() {
    SourceOfFund sourceOfFund = new SourceOfFundDataBuilder().build();
    SourceOfFundDto dto = SourceOfFundDto.newInstance(sourceOfFund);
    dto.setName("ala");

    sourceOfFund.updateFrom(dto);

    assertThat(sourceOfFund.getName()).isEqualTo("ala");
  }

  @Test
  public void shouldExportData() {
    SourceOfFund sourceOfFund = new SourceOfFundDataBuilder().build();
    SourceOfFundDto dto = new SourceOfFundDto();

    sourceOfFund.export(dto);

    assertThat(dto.getId()).isEqualTo(sourceOfFund.getId());
    assertThat(dto.getName()).isEqualTo(sourceOfFund.getName());
  }

}
