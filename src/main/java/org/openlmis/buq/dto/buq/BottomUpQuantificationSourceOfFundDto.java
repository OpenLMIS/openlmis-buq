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

package org.openlmis.buq.dto.buq;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.joda.money.Money;
import org.openlmis.buq.domain.buq.BottomUpQuantificationSourceOfFund;
import org.openlmis.buq.domain.sourceoffund.SourceOfFund;
import org.openlmis.buq.dto.BaseDto;
import org.openlmis.buq.dto.sourceoffund.SourceOfFundDto;
import org.openlmis.buq.util.MoneyDeserializer;
import org.openlmis.buq.util.MoneySerializer;

@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public final class BottomUpQuantificationSourceOfFundDto extends BaseDto
    implements BottomUpQuantificationSourceOfFund.Importer,
    BottomUpQuantificationSourceOfFund.Exporter {

  @JsonSerialize(using = MoneySerializer.class)
  @JsonDeserialize(using = MoneyDeserializer.class)
  private Money amountUsedInLastFinancialYear;
  @JsonSerialize(using = MoneySerializer.class)
  @JsonDeserialize(using = MoneyDeserializer.class)
  private Money projectedFund;
  private SourceOfFundDto sourceOfFund;

  /**
   * Creates new instance based on domain object.
   */
  public static BottomUpQuantificationSourceOfFundDto newInstance(
      BottomUpQuantificationSourceOfFund sourceOfFunds) {
    BottomUpQuantificationSourceOfFundDto dto = new BottomUpQuantificationSourceOfFundDto();
    sourceOfFunds.export(dto);

    return dto;
  }

  @JsonSetter("sourceOfFund")
  public void setSourceOfFunds(SourceOfFundDto sourceOfFunds) {
    this.sourceOfFund = sourceOfFunds;
  }

  /**
   * Sets the source of funds for this BottomUpQuantificationSourceOfFundDto.
   *
   * @param sourceOfFunds The SourceOfFund object.
   */
  @JsonIgnore
  public void setSourceOfFund(SourceOfFund sourceOfFunds) {
    if (sourceOfFunds != null) {
      this.sourceOfFund = SourceOfFundDto.newInstance(sourceOfFunds);
    }
  }

}
