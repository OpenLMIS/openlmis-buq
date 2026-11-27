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

package org.openlmis.buq.web.forecasting;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.validation.Valid;
import org.openlmis.buq.domain.forecasting.ForecastingSetting;
import org.openlmis.buq.dto.forecasting.ForecastingSettingDto;
import org.openlmis.buq.exception.NotFoundException;
import org.openlmis.buq.exception.ValidationMessageException;
import org.openlmis.buq.i18n.MessageKeys;
import org.openlmis.buq.repository.forecasting.ForecastingSettingRepository;
import org.openlmis.buq.service.forecasting.ForecastingSettingService;
import org.openlmis.buq.service.role.ForecastingRights;
import org.openlmis.buq.service.role.PermissionService;
import org.openlmis.buq.util.Pagination;
import org.openlmis.buq.web.BaseController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Controller used to expose forecasting settings via HTTP.
 */
@Controller
@RequestMapping(ForecastingSettingController.RESOURCE_PATH)
@Transactional
public class ForecastingSettingController extends BaseController {

  public static final String RESOURCE_PATH = API_PATH + "/forecastingSettings";

  @Autowired
  private PermissionService permissionService;

  @Autowired
  private ForecastingSettingRepository forecastingSettingRepository;

  @Autowired
  private ForecastingSettingService forecastingSettingService;

  /**
   * Creates a new forecasting setting. An id in the body is ignored.
   */
  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  @ResponseBody
  public ForecastingSettingDto createForecastingSetting(
      @RequestBody @Valid ForecastingSettingDto settingDto) {
    permissionService.hasPermission(ForecastingRights.MANAGE);

    return ForecastingSettingDto.newInstance(forecastingSettingService.create(settingDto));
  }

  /**
   * Updates the specified forecasting setting.
   */
  @PutMapping("/{id}")
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public ForecastingSettingDto updateForecastingSetting(@PathVariable("id") UUID id,
      @RequestBody @Valid ForecastingSettingDto settingDto) {
    permissionService.hasPermission(ForecastingRights.MANAGE);

    if (null != settingDto.getId() && !Objects.equals(settingDto.getId(), id)) {
      throw new ValidationMessageException(MessageKeys.ERROR_FORECASTING_SETTING_ID_MISMATCH);
    }

    return ForecastingSettingDto.newInstance(forecastingSettingService.update(id, settingDto));
  }

  /**
   * Deletes the specified forecasting setting. Forecasts keep their own copy of the values they
   * were computed from.
   */
  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteForecastingSetting(@PathVariable("id") UUID id) {
    permissionService.hasPermission(ForecastingRights.MANAGE);

    if (!forecastingSettingRepository.existsById(id)) {
      throw new NotFoundException(MessageKeys.ERROR_FORECASTING_SETTING_NOT_FOUND);
    }

    forecastingSettingRepository.deleteById(id);
  }

  /**
   * Retrieves a page of forecasting settings, optionally limited to a program and a product.
   */
  @GetMapping
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public Page<ForecastingSettingDto> searchForecastingSettings(
      @RequestParam(name = "programId", required = false) UUID programId,
      @RequestParam(name = "orderableId", required = false) UUID orderableId,
      Pageable pageable) {
    permissionService.hasPermission(ForecastingRights.MANAGE);

    Page<ForecastingSetting> page = forecastingSettingRepository.search(programId, orderableId,
        pageable);
    List<ForecastingSettingDto> content = page.getContent()
        .stream()
        .map(ForecastingSettingDto::newInstance)
        .collect(Collectors.toList());

    return Pagination.getPage(content, pageable, page.getTotalElements());
  }

  /**
   * Retrieves the specified forecasting setting.
   */
  @GetMapping("/{id}")
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public ForecastingSettingDto getForecastingSetting(@PathVariable("id") UUID id) {
    permissionService.hasPermission(ForecastingRights.MANAGE);

    ForecastingSetting setting = forecastingSettingRepository.findById(id)
        .orElseThrow(() -> new NotFoundException(MessageKeys.ERROR_FORECASTING_SETTING_NOT_FOUND));

    return ForecastingSettingDto.newInstance(setting);
  }

}
