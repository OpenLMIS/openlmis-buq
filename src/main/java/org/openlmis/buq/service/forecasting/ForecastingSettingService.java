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

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.openlmis.buq.domain.forecasting.ForecastingSetting;
import org.openlmis.buq.domain.forecasting.PopulationSegment;
import org.openlmis.buq.dto.forecasting.ForecastingSettingDto;
import org.openlmis.buq.dto.referencedata.BasicOrderableDto;
import org.openlmis.buq.exception.NotFoundException;
import org.openlmis.buq.exception.ValidationMessageException;
import org.openlmis.buq.i18n.MessageKeys;
import org.openlmis.buq.repository.forecasting.ForecastingSettingRepository;
import org.openlmis.buq.repository.forecasting.PopulationSegmentRepository;
import org.openlmis.buq.service.RequestParameters;
import org.openlmis.buq.service.referencedata.OrderableReferenceDataService;
import org.openlmis.buq.util.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Creates and updates forecasting settings. A program can forecast one product per commodity
 * type: requisitions approve the ideal stock amount of a commodity type for every product that
 * has it, so two forecasted products of one type would each be ordered at the full amount.
 */
@Service
public class ForecastingSettingService {

  @Autowired
  private ForecastingSettingRepository forecastingSettingRepository;

  @Autowired
  private PopulationSegmentRepository populationSegmentRepository;

  @Autowired
  private OrderableReferenceDataService orderableReferenceDataService;

  /**
   * Creates a forecasting setting. An id in the dto is ignored.
   */
  public ForecastingSetting create(ForecastingSettingDto dto) {
    PopulationSegment segment = validate(dto, null);

    ForecastingSetting setting = ForecastingSetting.newInstance(dto, segment);
    setting.setId(null);

    return forecastingSettingRepository.saveAndFlush(setting);
  }

  /**
   * Updates the specified forecasting setting.
   */
  public ForecastingSetting update(UUID id, ForecastingSettingDto dto) {
    ForecastingSetting setting = forecastingSettingRepository.findById(id)
        .orElseThrow(() -> new NotFoundException(MessageKeys.ERROR_FORECASTING_SETTING_NOT_FOUND));
    PopulationSegment segment = validate(dto, id);

    setting.updateFrom(dto, segment);

    return forecastingSettingRepository.saveAndFlush(setting);
  }

  private PopulationSegment validate(ForecastingSettingDto dto, UUID settingId) {
    PopulationSegment segment = populationSegmentRepository.findById(dto.getPopulationSegmentId())
        .orElseThrow(() -> new ValidationMessageException(new Message(
            MessageKeys.ERROR_FORECASTING_SETTING_SEGMENT_NOT_FOUND,
            dto.getPopulationSegmentId())));

    BasicOrderableDto orderable = Optional
        .ofNullable(orderableReferenceDataService.findOne(dto.getOrderableId()))
        .orElseThrow(() -> new ValidationMessageException(new Message(
            MessageKeys.ERROR_FORECASTING_SETTING_ORDERABLE_NOT_FOUND, dto.getOrderableId())));

    String commodityType = orderable.getCommodityType();
    if (StringUtils.isBlank(commodityType)) {
      throw new ValidationMessageException(new Message(
          MessageKeys.ERROR_FORECASTING_SETTING_COMMODITY_TYPE_MISSING,
          orderable.getProductCode()));
    }

    findForecastedOrderableWithCommodityType(dto, settingId, commodityType)
        .ifPresent(other -> {
          throw new ValidationMessageException(new Message(
              MessageKeys.ERROR_FORECASTING_SETTING_COMMODITY_TYPE_TAKEN,
              orderable.getProductCode(), commodityType, other.getProductCode()));
        });

    return segment;
  }

  private Optional<BasicOrderableDto> findForecastedOrderableWithCommodityType(
      ForecastingSettingDto dto, UUID settingId, String commodityType) {
    List<UUID> otherOrderableIds = forecastingSettingRepository
        .findByProgramId(dto.getProgramId())
        .stream()
        .filter(setting -> !Objects.equals(setting.getId(), settingId))
        .map(ForecastingSetting::getOrderableId)
        .filter(orderableId -> !Objects.equals(orderableId, dto.getOrderableId()))
        .distinct()
        .collect(Collectors.toList());

    if (otherOrderableIds.isEmpty()) {
      return Optional.empty();
    }

    RequestParameters parameters = RequestParameters.init()
        .set("id", otherOrderableIds)
        .set("size", otherOrderableIds.size());

    return orderableReferenceDataService.findAll(parameters)
        .stream()
        .filter(other -> commodityType.equals(other.getCommodityType()))
        .findFirst();
  }

}
