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
import org.openlmis.buq.domain.forecasting.PopulationSegment;
import org.openlmis.buq.dto.forecasting.PopulationSegmentDto;
import org.openlmis.buq.exception.NotFoundException;
import org.openlmis.buq.exception.ValidationMessageException;
import org.openlmis.buq.i18n.MessageKeys;
import org.openlmis.buq.repository.forecasting.ForecastingSettingRepository;
import org.openlmis.buq.repository.forecasting.PopulationSegmentRepository;
import org.openlmis.buq.service.role.ForecastingRights;
import org.openlmis.buq.service.role.PermissionService;
import org.openlmis.buq.util.Pagination;
import org.openlmis.buq.web.BaseController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.SortDefault;
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
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Controller used to expose population segments via HTTP.
 */
@Controller
@RequestMapping(PopulationSegmentController.RESOURCE_PATH)
@Transactional
public class PopulationSegmentController extends BaseController {

  public static final String RESOURCE_PATH = API_PATH + "/populationSegments";

  @Autowired
  private PermissionService permissionService;

  @Autowired
  private PopulationSegmentRepository populationSegmentRepository;

  @Autowired
  private ForecastingSettingRepository forecastingSettingRepository;

  /**
   * Creates a new population segment. An id in the body is ignored.
   */
  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  @ResponseBody
  public PopulationSegmentDto createPopulationSegment(
      @RequestBody @Valid PopulationSegmentDto segmentDto) {
    permissionService.hasPermission(ForecastingRights.MANAGE);

    PopulationSegment segment = PopulationSegment.newInstance(segmentDto);
    segment.setId(null);

    return PopulationSegmentDto.newInstance(populationSegmentRepository.saveAndFlush(segment));
  }

  /**
   * Updates the specified population segment.
   */
  @PutMapping("/{id}")
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public PopulationSegmentDto updatePopulationSegment(@PathVariable("id") UUID id,
      @RequestBody @Valid PopulationSegmentDto segmentDto) {
    permissionService.hasPermission(ForecastingRights.MANAGE);

    if (null != segmentDto.getId() && !Objects.equals(segmentDto.getId(), id)) {
      throw new ValidationMessageException(MessageKeys.ERROR_POPULATION_SEGMENT_ID_MISMATCH);
    }

    PopulationSegment segment = findPopulationSegment(id);
    segment.updateFrom(segmentDto);

    return PopulationSegmentDto.newInstance(populationSegmentRepository.saveAndFlush(segment));
  }

  /**
   * Deletes the specified population segment, unless a forecasting setting or a forecast line
   * item refers to it.
   */
  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deletePopulationSegment(@PathVariable("id") UUID id) {
    permissionService.hasPermission(ForecastingRights.MANAGE);

    if (!populationSegmentRepository.existsById(id)) {
      throw new NotFoundException(MessageKeys.ERROR_POPULATION_SEGMENT_NOT_FOUND);
    }

    if (forecastingSettingRepository.existsByPopulationSegmentId(id)
        || populationSegmentRepository.isReferencedByForecastLineItem(id)) {
      throw new ValidationMessageException(MessageKeys.ERROR_POPULATION_SEGMENT_IN_USE);
    }

    populationSegmentRepository.deleteById(id);
    populationSegmentRepository.flush();
  }

  /**
   * Retrieves a page of population segments, sorted by code unless the request sorts otherwise.
   */
  @GetMapping
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public Page<PopulationSegmentDto> getPopulationSegments(
      @SortDefault(sort = "code") Pageable pageable) {
    permissionService.hasPermission(ForecastingRights.MANAGE);

    Page<PopulationSegment> page = populationSegmentRepository.findAll(pageable);
    List<PopulationSegmentDto> content = page.getContent()
        .stream()
        .map(PopulationSegmentDto::newInstance)
        .collect(Collectors.toList());

    return Pagination.getPage(content, pageable, page.getTotalElements());
  }

  /**
   * Retrieves the specified population segment.
   */
  @GetMapping("/{id}")
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public PopulationSegmentDto getPopulationSegment(@PathVariable("id") UUID id) {
    permissionService.hasPermission(ForecastingRights.MANAGE);

    return PopulationSegmentDto.newInstance(findPopulationSegment(id));
  }

  private PopulationSegment findPopulationSegment(UUID id) {
    return populationSegmentRepository.findById(id)
        .orElseThrow(() -> new NotFoundException(MessageKeys.ERROR_POPULATION_SEGMENT_NOT_FOUND));
  }

}
