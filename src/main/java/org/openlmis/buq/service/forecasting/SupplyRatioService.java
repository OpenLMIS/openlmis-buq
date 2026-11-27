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

package org.openlmis.buq.service.forecasting;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import org.apache.commons.lang3.StringUtils;
import org.openlmis.buq.domain.forecasting.SupplyRatio;
import org.openlmis.buq.dto.forecasting.SupplyRatioDto;
import org.openlmis.buq.dto.referencedata.BasicOrderableDto;
import org.openlmis.buq.exception.NotFoundException;
import org.openlmis.buq.exception.ValidationMessageException;
import org.openlmis.buq.i18n.MessageKeys;
import org.openlmis.buq.repository.forecasting.SupplyRatioRepository;
import org.openlmis.buq.service.referencedata.OrderableReferenceDataService;
import org.openlmis.buq.util.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Creates and updates supply ratios. The supply product must have a commodity type, since
 * requisitions take the ideal stock amount of a supply through its commodity type, as they do for
 * a vaccine.
 */
@Service
public class SupplyRatioService {

  @Autowired
  private SupplyRatioRepository supplyRatioRepository;

  @Autowired
  private OrderableReferenceDataService orderableReferenceDataService;

  /**
   * Creates a supply ratio. An id in the dto is ignored.
   */
  public SupplyRatio create(SupplyRatioDto dto) {
    validate(dto);

    SupplyRatio supplyRatio = SupplyRatio.newInstance(dto);
    supplyRatio.setId(null);

    return supplyRatioRepository.saveAndFlush(supplyRatio);
  }

  /**
   * Updates the specified supply ratio.
   */
  public SupplyRatio update(UUID id, SupplyRatioDto dto) {
    SupplyRatio supplyRatio = supplyRatioRepository.findById(id)
        .orElseThrow(() -> new NotFoundException(MessageKeys.ERROR_SUPPLY_RATIO_NOT_FOUND));
    validate(dto);

    supplyRatio.updateFrom(dto);

    return supplyRatioRepository.saveAndFlush(supplyRatio);
  }

  private void validate(SupplyRatioDto dto) {
    if (Objects.equals(dto.getVaccineOrderableId(), dto.getSupplyOrderableId())) {
      throw new ValidationMessageException(MessageKeys.ERROR_SUPPLY_RATIO_SAME_ORDERABLE);
    }

    BasicOrderableDto supply = Optional
        .ofNullable(orderableReferenceDataService.findOne(dto.getSupplyOrderableId()))
        .orElseThrow(() -> new ValidationMessageException(new Message(
            MessageKeys.ERROR_SUPPLY_RATIO_ORDERABLE_NOT_FOUND, dto.getSupplyOrderableId())));

    if (StringUtils.isBlank(supply.getCommodityType())) {
      throw new ValidationMessageException(new Message(
          MessageKeys.ERROR_SUPPLY_RATIO_COMMODITY_TYPE_MISSING, supply.getProductCode()));
    }
  }

}
