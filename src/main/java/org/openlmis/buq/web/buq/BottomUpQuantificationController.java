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

package org.openlmis.buq.web.buq;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.openlmis.buq.domain.buq.BottomUpQuantification;
import org.openlmis.buq.domain.buq.BottomUpQuantificationLineItem;
import org.openlmis.buq.domain.buq.BottomUpQuantificationStatusChange;
import org.openlmis.buq.dto.buq.BottomUpQuantificationDto;
import org.openlmis.buq.dto.buq.BottomUpQuantificationLineItemDto;
import org.openlmis.buq.dto.buq.BottomUpQuantificationStatusChangeDto;
import org.openlmis.buq.exception.NotFoundException;
import org.openlmis.buq.i18n.MessageKeys;
import org.openlmis.buq.repository.buq.BottomUpQuantificationRepository;
import org.openlmis.buq.service.buq.BottomUpQuantificationService;
import org.openlmis.buq.util.Pagination;
import org.openlmis.buq.web.BaseController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
 * Controller used to expose Bottom-Up Quantifications via HTTP.
 */
@Controller
@RequestMapping(BottomUpQuantificationController.RESOURCE_PATH)
@Transactional
public class BottomUpQuantificationController extends BaseController {

  public static final String RESOURCE_PATH = API_PATH + "/bottomUpQuantifications";

  @Autowired
  private BottomUpQuantificationRepository bottomUpQuantificationRepository;

  @Autowired
  private BottomUpQuantificationService bottomUpQuantificationService;

  /**
   * Retrieves all bottom-up quantifications. Note that an empty collection rather than a 404
   * should be returned if no bottom-up quantifications exist.
   */
  @GetMapping
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public Page<BottomUpQuantificationDto> getAllBottomUpQuantifications(Pageable pageable) {
    Page<BottomUpQuantification> page = bottomUpQuantificationRepository.findAll(pageable);
    List<BottomUpQuantificationDto> content = page
        .getContent()
        .stream()
        .map(this::buildDto)
        .collect(Collectors.toList());
    return Pagination.getPage(content, pageable, page.getTotalElements());
  }

  /**
   * Retrieves the specified bottom-up quantification.
   */
  @GetMapping(value = "/{id}")
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public BottomUpQuantificationDto getSpecifiedBottomUpQuantification(
      @PathVariable("id") UUID id) {
    BottomUpQuantification buq = bottomUpQuantificationRepository.findById(id).orElseThrow(
        () -> new NotFoundException(MessageKeys.ERROR_BOTTOM_UP_QUANTIFICATION_NOT_FOUND));

    return buildDto(buq);
  }

  /**
   * Deletes the specified bottom-up quantification.
   */
  @DeleteMapping(value = "/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteBottomUpQuantification(@PathVariable("id") UUID id) {
    if (!bottomUpQuantificationRepository.existsById(id)) {
      throw new NotFoundException(MessageKeys.ERROR_BOTTOM_UP_QUANTIFICATION_NOT_FOUND);
    }

    bottomUpQuantificationRepository.deleteById(id);
  }

  /**
   * Allows creating new bottom-up quantification.
   *
   * @param programId UUID of Program.
   * @param facilityId UUID of Facility.
   * @param processingPeriodId Period for requisition.
   * @return created requisition.
   */
  @PostMapping("/prepare")
  @ResponseStatus(HttpStatus.CREATED)
  @ResponseBody
  public BottomUpQuantificationDto prepare(
      @RequestParam(value = "facilityId") UUID facilityId,
      @RequestParam(value = "programId") UUID programId,
      @RequestParam(value = "processingPeriodId") UUID processingPeriodId) {
    BottomUpQuantification bottomUpQuantification = bottomUpQuantificationService
        .prepare(facilityId, programId, processingPeriodId);

    return buildDto(bottomUpQuantification);
  }

  /**
   * Allows updating bottom-up quantification.
   *
   * @param bottomUpQuantificationId UUID of bottom-up quantification which we want to update.
   * @param bottomUpQuantificationDto A bottom-up quantification DTO bound to the request body.
   * @return updated bottom-up quantification.
   */
  @PutMapping("/{id}")
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public BottomUpQuantificationDto save(@PathVariable("id") UUID bottomUpQuantificationId,
      @RequestBody BottomUpQuantificationDto bottomUpQuantificationDto) {
    if (!bottomUpQuantificationRepository.existsById(bottomUpQuantificationId)) {
      throw new NotFoundException(MessageKeys.ERROR_BOTTOM_UP_QUANTIFICATION_NOT_FOUND);
    }

    BottomUpQuantification updatedBottomUpQuantification = bottomUpQuantificationService
        .save(bottomUpQuantificationDto, bottomUpQuantificationId);

    return buildDto(updatedBottomUpQuantification);
  }


  /**
   * Retrieves audit information related to the specified bottom-up quantification.
   *
   * @param author The author of the changes which should be returned.
   *               If null or empty, changes are returned regardless of author.
   * @param changedPropertyName The name of the property about which changes should be returned.
   *               If null or empty, changes associated with any and all properties are returned.
   * @param page A Pageable object that allows client to optionally add "page" (page number)
   *             and "size" (page size) query parameters to the request.
   */
  @GetMapping(value = "/{id}/auditLog")
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public ResponseEntity<String> getBottomUpQuantificationAuditLog(@PathVariable("id") UUID id,
      @RequestParam(name = "author", required = false, defaultValue = "") String author,
      @RequestParam(name = "changedPropertyName", required = false, defaultValue = "")
      String changedPropertyName, Pageable page) {

    // Return a 404 if the specified instance can't be found
    if (!bottomUpQuantificationRepository.existsById(id)) {
      throw new NotFoundException(MessageKeys.ERROR_BOTTOM_UP_QUANTIFICATION_NOT_FOUND);
    }

    return getAuditLogResponse(BottomUpQuantification.class, id, author, changedPropertyName,
        page);
  }

  private BottomUpQuantificationDto buildDto(BottomUpQuantification bottomUpQuantification) {
    BottomUpQuantificationDto dto = new BottomUpQuantificationDto();
    bottomUpQuantification.export(dto);

    List<BottomUpQuantificationLineItem> bottomUpQuantificationLineItems =
        bottomUpQuantification.getBottomUpQuantificationLineItems();
    List<BottomUpQuantificationLineItemDto> lineItemDtoList = bottomUpQuantificationLineItems
        .stream()
        .map(lineItem -> {
          BottomUpQuantificationLineItemDto lineItemDto = new BottomUpQuantificationLineItemDto();
          lineItem.export(lineItemDto);

          return lineItemDto;
        })
        .collect(Collectors.toList());
    dto.setBottomUpQuantificationLineItems(lineItemDtoList);

    List<BottomUpQuantificationStatusChange> statusChanges =
        bottomUpQuantification.getStatusChanges();
    List<BottomUpQuantificationStatusChangeDto> statusChangeDtos = statusChanges
        .stream()
        .map(statusChange -> {
          BottomUpQuantificationStatusChangeDto statusChangeDto =
              new BottomUpQuantificationStatusChangeDto();
          statusChange.export(statusChangeDto);

          return statusChangeDto;
        })
        .collect(Collectors.toList());
    dto.setStatusChanges(statusChangeDtos);

    return dto;
  }

}
