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

import static com.fasterxml.jackson.annotation.JsonFormat.Shape.STRING;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.google.common.collect.Lists;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.openlmis.buq.domain.buq.BottomUpQuantification;
import org.openlmis.buq.domain.buq.BottomUpQuantificationFundingDetails;
import org.openlmis.buq.domain.buq.BottomUpQuantificationStatus;
import org.openlmis.buq.dto.BaseDto;

@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public final class BottomUpQuantificationDto extends BaseDto
    implements BottomUpQuantification.Importer, BottomUpQuantification.Exporter {

  @Getter
  @Setter
  private UUID facilityId;

  @Getter
  @Setter
  private UUID programId;

  @Getter
  @Setter
  private UUID processingPeriodId;

  @Getter
  @Setter
  @JsonFormat(shape = STRING)
  private ZonedDateTime createdDate;

  @Getter
  @Setter
  @JsonFormat(shape = STRING)
  private ZonedDateTime modifiedDate;

  @Getter
  @Setter
  private Integer targetYear;

  @Getter
  @Setter
  private BottomUpQuantificationStatus status;

  @Setter
  private List<BottomUpQuantificationLineItemDto> bottomUpQuantificationLineItems;

  @Setter
  private List<BottomUpQuantificationStatusChangeDto> statusChanges;

  @Getter
  @Setter
  private BottomUpQuantificationFundingDetailsDto fundingDetails;

  /**
   * Creates new instance based on domain object.
   */
  public static BottomUpQuantificationDto newInstance(BottomUpQuantification buq) {
    BottomUpQuantificationDto dto = new BottomUpQuantificationDto();
    buq.export(dto);

    return dto;
  }

  public List<BottomUpQuantificationLineItemDto> getBottomUpQuantificationLineItems() {
    return Lists.newArrayList(Optional.ofNullable(bottomUpQuantificationLineItems)
        .orElse(Collections.emptyList()));
  }

  public List<BottomUpQuantificationStatusChangeDto> getStatusChanges() {
    return Lists.newArrayList(Optional.ofNullable(statusChanges)
        .orElse(Collections.emptyList()));
  }

  @Override
  @JsonIgnore
  public void setFundingDetails(BottomUpQuantificationFundingDetails fundingDetails) {
    if (fundingDetails != null) {
      this.fundingDetails = BottomUpQuantificationFundingDetailsDto.newInstance(fundingDetails);
    }
  }

  @JsonSetter("fundingDetails")
  public void setFundingDetails(BottomUpQuantificationFundingDetailsDto fundingDetails) {
    this.fundingDetails = fundingDetails;
  }

}
