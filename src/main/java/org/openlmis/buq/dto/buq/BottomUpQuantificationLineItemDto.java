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
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.joda.money.Money;
import org.openlmis.buq.domain.Remark;
import org.openlmis.buq.domain.buq.BottomUpQuantificationLineItem;
import org.openlmis.buq.dto.BaseDto;
import org.openlmis.buq.dto.remark.RemarkDto;
import org.openlmis.buq.util.MoneyDeserializer;
import org.openlmis.buq.util.MoneySerializer;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public final class BottomUpQuantificationLineItemDto extends BaseDto
    implements BottomUpQuantificationLineItem.Importer, BottomUpQuantificationLineItem.Exporter {

  private UUID orderableId;
  private Integer annualAdjustedConsumption;
  private Integer verifiedAnnualAdjustedConsumption;
  private Integer forecastedDemand;
  @JsonSerialize(using = MoneySerializer.class)
  @JsonDeserialize(using = MoneyDeserializer.class)
  private Money totalCost;
  private RemarkDto remark;

  /**
   * Creates new instance based on domain object.
   */
  public static BottomUpQuantificationLineItemDto newInstance(
      BottomUpQuantificationLineItem buqLineItem) {
    BottomUpQuantificationLineItemDto dto = new BottomUpQuantificationLineItemDto();
    buqLineItem.export(dto);

    return dto;
  }

  @JsonSetter("remark")
  public void setRemark(RemarkDto remark) {
    this.remark = remark;
  }

  @Override
  @JsonIgnore
  public void setRemark(Remark remark) {
    if (remark != null) {
      this.remark = RemarkDto.newInstance(remark);
    }
  }

}
