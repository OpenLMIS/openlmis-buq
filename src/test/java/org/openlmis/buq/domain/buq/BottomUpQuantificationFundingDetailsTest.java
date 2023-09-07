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
import org.openlmis.buq.builder.BottomUpQuantificationFundingDetailsDataBuilder;
import org.openlmis.buq.dto.buq.BottomUpQuantificationFundingDetailsDto;


public class BottomUpQuantificationFundingDetailsTest {

  @Test
  public void equalsContract() {
    BottomUpQuantification buq1 = new BottomUpQuantificationDataBuilder().build();
    BottomUpQuantification buq2 = new BottomUpQuantification();

    EqualsVerifier
        .forClass(BottomUpQuantificationFundingDetails.class)
        .withRedefinedSuperclass()
        .withPrefabValues(BottomUpQuantification.class, buq1, buq2)
        .suppress(Warning.NONFINAL_FIELDS)
        .verify();
  }

  @Test
  public void shouldImplementToString() {
    BottomUpQuantificationFundingDetails fundingDetails =
        new BottomUpQuantificationFundingDetailsDataBuilder().build();
    ToStringTestUtils.verify(BottomUpQuantificationFundingDetails.class, fundingDetails, "TEXT");
  }

  @Test
  public void shouldUpdateFrom() {
    BottomUpQuantificationFundingDetailsDto importer =
        new BottomUpQuantificationFundingDetailsDto();
    importer.setTotalProjectedFund(100L);
    importer.setTotalForecastedCost(150L);
    importer.setGap(50L);
    BottomUpQuantificationFundingDetails fundingDetails =
        new BottomUpQuantificationFundingDetailsDataBuilder().build();

    fundingDetails.updateFrom(importer);

    assertThat(fundingDetails.getTotalProjectedFund()).isEqualTo(100L);
    assertThat(fundingDetails.getTotalForecastedCost()).isEqualTo(150L);
    assertThat(fundingDetails.getGap()).isEqualTo(50L);
  }

  @Test
  public void shouldExportData() {
    BottomUpQuantificationFundingDetails fundingDetails =
        new BottomUpQuantificationFundingDetailsDataBuilder().build();
    BottomUpQuantificationFundingDetailsDto dto = new BottomUpQuantificationFundingDetailsDto();

    fundingDetails.export(dto);

    assertThat(dto.getBottomUpQuantificationId())
        .isEqualTo(fundingDetails.getBottomUpQuantification().getId());
    assertThat(dto.getTotalProjectedFund()).isEqualTo(fundingDetails.getTotalProjectedFund());
    assertThat(dto.getTotalForecastedCost()).isEqualTo(fundingDetails.getTotalForecastedCost());
    assertThat(dto.getGap()).isEqualTo(fundingDetails.getGap());
  }

}
