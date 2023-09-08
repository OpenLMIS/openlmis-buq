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

package org.openlmis.buq.service.buq;

import java.util.List;
import java.util.stream.Collectors;
import org.openlmis.buq.domain.buq.BottomUpQuantification;
import org.openlmis.buq.domain.buq.BottomUpQuantificationLineItem;
import org.openlmis.buq.domain.buq.BottomUpQuantificationSourceOfFund;
import org.openlmis.buq.domain.buq.BottomUpQuantificationStatusChange;
import org.openlmis.buq.dto.buq.BottomUpQuantificationDto;
import org.openlmis.buq.dto.buq.BottomUpQuantificationLineItemDto;
import org.openlmis.buq.dto.buq.BottomUpQuantificationSourceOfFundDto;
import org.openlmis.buq.dto.buq.BottomUpQuantificationStatusChangeDto;
import org.springframework.stereotype.Component;

@Component
public class BottomUpQuantificationDtoBuilder {

  /**
   * Creates a DTO based on the bottom-up quantification passed.
   *
   * @param bottomUpQuantification {@link BottomUpQuantification} object to be converted to a DTO.
   * @return {@link BottomUpQuantificationDto}
   */
  public BottomUpQuantificationDto buildDto(BottomUpQuantification bottomUpQuantification) {
    BottomUpQuantificationDto dto = new BottomUpQuantificationDto();
    bottomUpQuantification.export(dto);

    List<BottomUpQuantificationLineItem> bottomUpQuantificationLineItems =
        bottomUpQuantification.getBottomUpQuantificationLineItems();
    List<BottomUpQuantificationLineItemDto> lineItemDtoList = bottomUpQuantificationLineItems
        .stream()
        .map(BottomUpQuantificationLineItemDto::newInstance)
        .collect(Collectors.toList());
    dto.setBottomUpQuantificationLineItems(lineItemDtoList);

    List<BottomUpQuantificationStatusChange> statusChanges =
        bottomUpQuantification.getStatusChanges();
    List<BottomUpQuantificationStatusChangeDto> statusChangeDtos = statusChanges
        .stream()
        .map(BottomUpQuantificationStatusChangeDto::newInstance)
        .collect(Collectors.toList());
    dto.setStatusChanges(statusChangeDtos);

    if (bottomUpQuantification.getFundingDetails().getSourcesOfFunds() != null) {
      List<BottomUpQuantificationSourceOfFund> sourceOfFunds =
          bottomUpQuantification.getFundingDetails().getSourcesOfFunds();
      List<BottomUpQuantificationSourceOfFundDto> sourceOfFundsDtos = sourceOfFunds
          .stream()
          .map(BottomUpQuantificationSourceOfFundDto::newInstance)
          .collect(Collectors.toList());
      dto.getFundingDetails().setSourcesOfFunds(sourceOfFundsDtos);
    }

    return dto;
  }

}
