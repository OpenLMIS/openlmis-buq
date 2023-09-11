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

package org.openlmis.buq.builder;

import static org.openlmis.buq.CurrencyConfig.currencyCode;

import java.util.UUID;
import org.joda.money.CurrencyUnit;
import org.joda.money.Money;
import org.openlmis.buq.domain.buq.BottomUpQuantificationFundingDetails;
import org.openlmis.buq.domain.buq.BottomUpQuantificationSourceOfFund;
import org.openlmis.buq.domain.sourceoffund.SourceOfFund;

public class BottomUpQuantificationSourceOfFundDataBuilder {

  private UUID id = UUID.randomUUID();
  private BottomUpQuantificationFundingDetails fundingDetails =
      new BottomUpQuantificationFundingDetailsDataBuilder().build();
  private Money amountUsedInLastFinancialYear = asMoney(20);
  private Money projectedFund = asMoney(30);
  private SourceOfFund sourceOfFunds = new SourceOfFundDataBuilder().buildAsNew();

  /**
   * Creates new instance of {@link BottomUpQuantificationSourceOfFund}.
   */
  public BottomUpQuantificationSourceOfFund build() {
    BottomUpQuantificationSourceOfFund bottomUpQuantificationSourceOfFund = buildAsNew();
    bottomUpQuantificationSourceOfFund.setId(id);
    bottomUpQuantificationSourceOfFund.setFundingDetails(fundingDetails);

    return bottomUpQuantificationSourceOfFund;
  }

  /**
   * Creates new instance of {@link BottomUpQuantificationSourceOfFund} dedicated to repository
   * usage.
   */
  public BottomUpQuantificationSourceOfFund buildAsNew() {
    BottomUpQuantificationSourceOfFund bottomUpQuantificationSourceOfFund =
        new BottomUpQuantificationSourceOfFund(
            fundingDetails, amountUsedInLastFinancialYear, projectedFund, sourceOfFunds
        );

    return bottomUpQuantificationSourceOfFund;
  }

  public BottomUpQuantificationSourceOfFundDataBuilder withId(UUID id) {
    this.id = id;
    return this;
  }

  public BottomUpQuantificationSourceOfFundDataBuilder withFundingDetails(
      BottomUpQuantificationFundingDetails fundingDetails) {
    this.fundingDetails = fundingDetails;
    return this;
  }

  public BottomUpQuantificationSourceOfFundDataBuilder withAmountUsedInLastFinancialYear(
      Money amountUsedInLastFinancialYear) {
    this.amountUsedInLastFinancialYear = amountUsedInLastFinancialYear;
    return this;
  }

  public BottomUpQuantificationSourceOfFundDataBuilder withProjectedFund(Money projectedFund) {
    this.projectedFund = projectedFund;
    return this;
  }

  public BottomUpQuantificationSourceOfFundDataBuilder withSourceOfFund(
      SourceOfFund sourceOfFunds) {
    this.sourceOfFunds = sourceOfFunds;
    return this;
  }

  private Money asMoney(Number value) {
    return Money.of(CurrencyUnit.of(currencyCode), value.doubleValue());
  }

}
