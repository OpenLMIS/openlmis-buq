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
import org.openlmis.buq.domain.forecasting.SupplyRatio;
import org.openlmis.buq.dto.forecasting.SupplyRatioDto;
import org.openlmis.buq.exception.NotFoundException;
import org.openlmis.buq.exception.ValidationMessageException;
import org.openlmis.buq.i18n.MessageKeys;
import org.openlmis.buq.repository.forecasting.SupplyRatioRepository;
import org.openlmis.buq.service.forecasting.SupplyRatioService;
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
 * Controller used to expose supply ratios via HTTP.
 */
@Controller
@RequestMapping(SupplyRatioController.RESOURCE_PATH)
@Transactional
public class SupplyRatioController extends BaseController {

  public static final String RESOURCE_PATH = API_PATH + "/supplyRatios";

  @Autowired
  private PermissionService permissionService;

  @Autowired
  private SupplyRatioRepository supplyRatioRepository;

  @Autowired
  private SupplyRatioService supplyRatioService;

  /**
   * Creates a new supply ratio. An id in the body is ignored.
   */
  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  @ResponseBody
  public SupplyRatioDto createSupplyRatio(@RequestBody @Valid SupplyRatioDto supplyRatioDto) {
    permissionService.hasPermission(ForecastingRights.MANAGE);

    return SupplyRatioDto.newInstance(supplyRatioService.create(supplyRatioDto));
  }

  /**
   * Updates the specified supply ratio.
   */
  @PutMapping("/{id}")
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public SupplyRatioDto updateSupplyRatio(@PathVariable("id") UUID id,
      @RequestBody @Valid SupplyRatioDto supplyRatioDto) {
    permissionService.hasPermission(ForecastingRights.MANAGE);

    if (null != supplyRatioDto.getId() && !Objects.equals(supplyRatioDto.getId(), id)) {
      throw new ValidationMessageException(MessageKeys.ERROR_SUPPLY_RATIO_ID_MISMATCH);
    }

    return SupplyRatioDto.newInstance(supplyRatioService.update(id, supplyRatioDto));
  }

  /**
   * Deletes the specified supply ratio. Nothing refers to a supply ratio: a forecast keeps the
   * amounts computed from it.
   */
  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteSupplyRatio(@PathVariable("id") UUID id) {
    permissionService.hasPermission(ForecastingRights.MANAGE);

    if (!supplyRatioRepository.existsById(id)) {
      throw new NotFoundException(MessageKeys.ERROR_SUPPLY_RATIO_NOT_FOUND);
    }

    supplyRatioRepository.deleteById(id);
  }

  /**
   * Retrieves a page of supply ratios, optionally limited to a vaccine and a supply product.
   */
  @GetMapping
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public Page<SupplyRatioDto> searchSupplyRatios(
      @RequestParam(name = "vaccineOrderableId", required = false) UUID vaccineOrderableId,
      @RequestParam(name = "supplyOrderableId", required = false) UUID supplyOrderableId,
      Pageable pageable) {
    permissionService.hasPermission(ForecastingRights.MANAGE);

    Page<SupplyRatio> page = supplyRatioRepository.search(vaccineOrderableId, supplyOrderableId,
        pageable);
    List<SupplyRatioDto> content = page.getContent()
        .stream()
        .map(SupplyRatioDto::newInstance)
        .collect(Collectors.toList());

    return Pagination.getPage(content, pageable, page.getTotalElements());
  }

  /**
   * Retrieves the specified supply ratio.
   */
  @GetMapping("/{id}")
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public SupplyRatioDto getSupplyRatio(@PathVariable("id") UUID id) {
    permissionService.hasPermission(ForecastingRights.MANAGE);

    SupplyRatio supplyRatio = supplyRatioRepository.findById(id)
        .orElseThrow(() -> new NotFoundException(MessageKeys.ERROR_SUPPLY_RATIO_NOT_FOUND));

    return SupplyRatioDto.newInstance(supplyRatio);
  }

}
