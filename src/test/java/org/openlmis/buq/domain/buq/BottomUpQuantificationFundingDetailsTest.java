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
import static org.openlmis.buq.CurrencyConfig.currencyCode;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.joda.money.CurrencyUnit;
import org.joda.money.Money;
import org.junit.Test;
import org.openlmis.buq.ToStringTestUtils;
import org.openlmis.buq.builder.BottomUpQuantificationDataBuilder;
import org.openlmis.buq.builder.BottomUpQuantificationFundingDetailsDataBuilder;
import org.openlmis.buq.builder.BottomUpQuantificationSourceOfFundDataBuilder;
import org.openlmis.buq.dto.buq.BottomUpQuantificationFundingDetailsDto;

public class BottomUpQuantificationFundingDetailsTest {

  @Test
  public void equalsContract() {
    BottomUpQuantification buq1 = new BottomUpQuantificationDataBuilder().build();
    BottomUpQuantification buq2 = new BottomUpQuantification();

    BottomUpQuantificationSourceOfFund bs1 =
        new BottomUpQuantificationSourceOfFundDataBuilder().build();
    BottomUpQuantificationSourceOfFund bs2 = new BottomUpQuantificationSourceOfFund();

    EqualsVerifier
        .forClass(BottomUpQuantificationFundingDetails.class)
        .withRedefinedSuperclass()
        .withPrefabValues(BottomUpQuantification.class, buq1, buq2)
        .withPrefabValues(BottomUpQuantificationSourceOfFund.class, bs1, bs2)
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
    CurrencyUnit currency = CurrencyUnit.of(currencyCode);

    BottomUpQuantificationFundingDetailsDto importer =
        new BottomUpQuantificationFundingDetailsDto();
    importer.setTotalProjectedFund(Money.of(currency, 100));
    importer.setTotalForecastedCost(Money.of(currency, 150));
    importer.setGap(Money.of(currency, 50));
    BottomUpQuantificationFundingDetails fundingDetails =
        new BottomUpQuantificationFundingDetailsDataBuilder().build();

    fundingDetails.updateFrom(importer);

    assertThat(fundingDetails.getTotalProjectedFund()).isEqualTo(Money.of(currency, 100));
    assertThat(fundingDetails.getTotalForecastedCost()).isEqualTo(Money.of(currency, 150));
    assertThat(fundingDetails.getGap()).isEqualTo(Money.of(currency, 50));
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
