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

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.Type;
import org.joda.money.Money;
import org.openlmis.buq.domain.BaseEntity;
import org.openlmis.buq.domain.sourceoffund.SourceOfFund;

@Entity
@Table(name = "bottom_up_quantification_sources_of_funds")
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class BottomUpQuantificationSourceOfFund extends BaseEntity {

  @ManyToOne(cascade = CascadeType.REFRESH)
  @JoinColumn(name = "bottomUpQuantificationFundingDetailsId")
  @Getter
  @Setter
  private BottomUpQuantificationFundingDetails fundingDetails;

  @Getter
  @Setter
  @Type(type = "org.openlmis.buq.domain.type.CustomSingleColumnMoneyUserType")
  private Money amountUsedInLastFinancialYear;

  @Getter
  @Setter
  @Type(type = "org.openlmis.buq.domain.type.CustomSingleColumnMoneyUserType")
  private Money projectedFund;

  @ManyToOne
  @JoinColumn(name = "sourceOfFundId")
  @Getter
  @Setter
  private SourceOfFund sourceOfFund;

  /**
   * Creates new instance based on data from the importer.
   */
  public static BottomUpQuantificationSourceOfFund newInstance(Importer importer) {
    BottomUpQuantificationSourceOfFund sourceOfFunds = new BottomUpQuantificationSourceOfFund();
    sourceOfFunds.setAmountUsedInLastFinancialYear(importer.getAmountUsedInLastFinancialYear());
    sourceOfFunds.setProjectedFund(importer.getProjectedFund());

    return sourceOfFunds;
  }

  /**
   * Exports data to the exporter.
   */
  public void export(Exporter exporter) {
    exporter.setId(getId());
    exporter.setAmountUsedInLastFinancialYear(amountUsedInLastFinancialYear);
    exporter.setProjectedFund(projectedFund);
    exporter.setSourceOfFund(sourceOfFund);
  }

  public interface Exporter extends BaseExporter {

    void setAmountUsedInLastFinancialYear(Money amountUsedInLastFinancialYear);

    void setProjectedFund(Money projectedFund);

    void setSourceOfFund(SourceOfFund sourceOfFund);

  }

  public interface Importer extends BaseImporter {

    Money getAmountUsedInLastFinancialYear();

    Money getProjectedFund();

    SourceOfFund.Importer getSourceOfFund();

  }

}
