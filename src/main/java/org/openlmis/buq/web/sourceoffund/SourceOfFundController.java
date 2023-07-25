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

package org.openlmis.buq.web.sourceoffund;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import org.openlmis.buq.domain.sourceoffund.SourceOfFund;
import org.openlmis.buq.dto.sourceoffund.SourceOfFundDto;
import org.openlmis.buq.exception.NotFoundException;
import org.openlmis.buq.exception.ValidationMessageException;
import org.openlmis.buq.i18n.MessageKeys;
import org.openlmis.buq.repository.sourceoffund.SourceOfFundRepository;
import org.openlmis.buq.util.Pagination;
import org.openlmis.buq.web.BaseController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
 * Controller used to expose Sources of funds via HTTP.
 */
@Controller
@RequestMapping(SourceOfFundController.RESOURCE_PATH)
@Transactional
public class SourceOfFundController extends BaseController {

  private static final Logger LOGGER = LoggerFactory.getLogger(SourceOfFundController.class);

  public static final String RESOURCE_PATH = API_PATH + "/sourcesOfFunds";

  @Autowired
  private SourceOfFundRepository sourceOfFundRepository;

  /**
   * Allows the creation of a new source of fund. If the id is specified, it will be ignored.
   */
  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  @ResponseBody
  public SourceOfFundDto createSourceOfFund(@RequestBody SourceOfFundDto sourceOfFund) {
    LOGGER.debug("Creating new source of fund");
    SourceOfFund newSourceOfFund = SourceOfFund.newInstance(sourceOfFund);
    newSourceOfFund.setId(null);
    newSourceOfFund = sourceOfFundRepository.saveAndFlush(newSourceOfFund);

    return SourceOfFundDto.newInstance(newSourceOfFund);
  }

  /**
   * Updates the specified source of fund.
   */
  @PutMapping(value = "/{id}")
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public SourceOfFundDto saveSourceOfFund(@PathVariable("id") UUID id,
      @RequestBody SourceOfFundDto sourceOfFund) {
    if (null != sourceOfFund.getId() && !Objects.equals(sourceOfFund.getId(), id)) {
      throw new ValidationMessageException(MessageKeys.ERROR_SOURCE_OF_FUND_ID_MISMATCH);
    }

    LOGGER.debug("Updating source of fund");
    SourceOfFund db;
    Optional<SourceOfFund> sourceOfFundOptional = sourceOfFundRepository.findById(id);
    if (sourceOfFundOptional.isPresent()) {
      db = sourceOfFundOptional.get();
      db.updateFrom(sourceOfFund);
    } else {
      db = SourceOfFund.newInstance(sourceOfFund);
      db.setId(id);
    }

    sourceOfFundRepository.saveAndFlush(db);

    return SourceOfFundDto.newInstance(db);
  }

  /**
   * Deletes the specified source of fund.
   */
  @DeleteMapping(value = "/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteSourceOfFund(@PathVariable("id") UUID id) {
    if (!sourceOfFundRepository.existsById(id)) {
      throw new NotFoundException(MessageKeys.ERROR_SOURCE_OF_FUND_NOT_FOUND);
    }

    sourceOfFundRepository.deleteById(id);
  }

  /**
   * Retrieves all sources of funds. Note that an empty collection rather than a 404 should be
   * returned if no sources of funds exist.
   */
  @GetMapping
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public Page<SourceOfFundDto> getAllSourcesOfFunds(Pageable pageable) {
    Page<SourceOfFund> page = sourceOfFundRepository.findAll(pageable);
    List<SourceOfFundDto> content = page
        .getContent()
        .stream()
        .map(SourceOfFundDto::newInstance)
        .collect(Collectors.toList());
    return Pagination.getPage(content, pageable, page.getTotalElements());
  }

  /**
   * Retrieves the specified source of fund.
   */
  @GetMapping(value = "/{id}")
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public SourceOfFundDto getSpecifiedSourceOfFund(@PathVariable("id") UUID id) {
    SourceOfFund sourceOfFund = sourceOfFundRepository.findById(id)
        .orElseThrow(() -> new NotFoundException(MessageKeys.ERROR_SOURCE_OF_FUND_NOT_FOUND));

    return SourceOfFundDto.newInstance(sourceOfFund);
  }

  /**
   * Retrieves audit information related to the specified source of fund.
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
  public ResponseEntity<String> getSourceOfFundAuditLog(@PathVariable("id") UUID id,
      @RequestParam(name = "author", required = false, defaultValue = "") String author,
      @RequestParam(name = "changedPropertyName", required = false, defaultValue = "")
          String changedPropertyName, Pageable page) {

    // Return a 404 if the specified instance can't be found
    if (!sourceOfFundRepository.existsById(id)) {
      throw new NotFoundException(MessageKeys.ERROR_SOURCE_OF_FUND_NOT_FOUND);
    }

    return getAuditLogResponse(SourceOfFund.class, id, author, changedPropertyName, page);
  }

}
