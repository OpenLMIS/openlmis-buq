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
import org.openlmis.buq.builder.BottomUpQuantificationFundingDetailsDataBuilder;
import org.openlmis.buq.builder.BottomUpQuantificationSourceOfFundDataBuilder;
import org.openlmis.buq.dto.buq.BottomUpQuantificationSourceOfFundDto;

public class BottomUpQuantificationSourceOfFundTest {

  @Test
  public void equalsContract() {
    BottomUpQuantificationFundingDetails fd1 =
        new BottomUpQuantificationFundingDetailsDataBuilder().build();
    BottomUpQuantificationFundingDetails fd2 = new BottomUpQuantificationFundingDetails();

    EqualsVerifier
        .forClass(BottomUpQuantificationSourceOfFund.class)
        .withRedefinedSuperclass()
        .withPrefabValues(BottomUpQuantificationFundingDetails.class, fd1, fd2)
        .suppress(Warning.NONFINAL_FIELDS)
        .verify();
  }

  @Test
  public void shouldImplementToString() {
    BottomUpQuantificationSourceOfFund sourceOfFunds =
        new BottomUpQuantificationSourceOfFundDataBuilder().build();
    ToStringTestUtils.verify(BottomUpQuantificationSourceOfFund.class, sourceOfFunds, "TEXT");
  }

  @Test
  public void shouldExportData() {
    BottomUpQuantificationSourceOfFund sourceOfFunds =
        new BottomUpQuantificationSourceOfFundDataBuilder().build();
    BottomUpQuantificationSourceOfFundDto dto = new BottomUpQuantificationSourceOfFundDto();

    sourceOfFunds.export(dto);

    assertThat(dto.getAmountUsedInLastFinancialYear())
        .isEqualTo(sourceOfFunds.getAmountUsedInLastFinancialYear());
    assertThat(dto.getProjectedFund()).isEqualTo(sourceOfFunds.getProjectedFund());
  }

}
