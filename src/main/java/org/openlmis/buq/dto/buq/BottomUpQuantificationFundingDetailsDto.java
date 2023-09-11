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

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.collect.Lists;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.joda.money.Money;
import org.openlmis.buq.domain.buq.BottomUpQuantificationFundingDetails;
import org.openlmis.buq.dto.BaseDto;
import org.openlmis.buq.util.MoneyDeserializer;
import org.openlmis.buq.util.MoneySerializer;

@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public final class BottomUpQuantificationFundingDetailsDto extends BaseDto
    implements BottomUpQuantificationFundingDetails.Importer,
    BottomUpQuantificationFundingDetails.Exporter {

  @Getter
  @Setter
  private UUID bottomUpQuantificationId;

  @Getter
  @Setter
  @JsonSerialize(using = MoneySerializer.class)
  @JsonDeserialize(using = MoneyDeserializer.class)
  private Money totalProjectedFund;

  @Getter
  @Setter
  @JsonSerialize(using = MoneySerializer.class)
  @JsonDeserialize(using = MoneyDeserializer.class)
  private Money totalForecastedCost;

  @Getter
  @Setter
  @JsonSerialize(using = MoneySerializer.class)
  @JsonDeserialize(using = MoneyDeserializer.class)
  private Money gap;

  @Setter
  private List<BottomUpQuantificationSourceOfFundDto> sourcesOfFunds;

  /**
   * Creates new instance based on domain object.
   */
  public static BottomUpQuantificationFundingDetailsDto newInstance(
      BottomUpQuantificationFundingDetails fundingDetails) {
    BottomUpQuantificationFundingDetailsDto dto = new BottomUpQuantificationFundingDetailsDto();
    fundingDetails.export(dto);

    return dto;
  }

  public List<BottomUpQuantificationSourceOfFundDto> getSourcesOfFunds() {
    return Lists.newArrayList(Optional.ofNullable(sourcesOfFunds)
        .orElse(Collections.emptyList()));
  }

}
