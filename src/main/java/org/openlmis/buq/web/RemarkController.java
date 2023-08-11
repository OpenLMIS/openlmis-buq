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

package org.openlmis.buq.web;

import static org.openlmis.buq.web.RemarkController.RESOURCE_PATH;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.validation.Valid;
import org.openlmis.buq.domain.Remark;
import org.openlmis.buq.dto.remark.RemarkDto;
import org.openlmis.buq.exception.NotFoundException;
import org.openlmis.buq.i18n.MessageKeys;
import org.openlmis.buq.service.remark.RemarkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping(RESOURCE_PATH)
public class RemarkController extends BaseController {
  public static final String RESOURCE_PATH = API_PATH + "/remark";

  private final RemarkService remarkService;

  @Autowired
  RemarkController(RemarkService remarkService) {
    this.remarkService = remarkService;
  }

  /**
   * Fetch all remarks.
   */
  @GetMapping
  public ResponseEntity<List<RemarkDto>> findAllRemarks() {
    List<RemarkDto> remarkList = remarkService
            .findAll()
            .stream()
            .map(RemarkDto::newInstance)
            .collect(Collectors.toList());
    return ResponseEntity.ok(remarkList);
  }

  /**
   * Fetch one remark.
   */
  @GetMapping("/{id}")
  public ResponseEntity<RemarkDto> findOne(@PathVariable UUID id) {
    Remark remark = remarkService.findOne(id);
    return ResponseEntity.ok(RemarkDto.newInstance(remark));
  }

  /**
   * Create a new remark.
   */
  @PostMapping
  public ResponseEntity<RemarkDto> createRemark(@RequestBody @Valid RemarkDto remark) {
    Remark newRemark = Remark.newInstance(remark);
    Remark persistedRemark = remarkService.save(newRemark);
    return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(RemarkDto.newInstance(persistedRemark));
  }

  /**
   * Delete a remark of given id.
   */
  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteRemark(@PathVariable UUID id) {
    remarkService.deleteById(id);
  }

  /**
   * Edit a remark of given id.
   */
  @PutMapping("/{id}")
  public ResponseEntity<RemarkDto> updateRemark(
          @PathVariable UUID id,
          @RequestBody @Valid RemarkDto remarkDto) {
    Remark persistedRemark = remarkService.update(id, remarkDto);
    return ResponseEntity.ok(RemarkDto.newInstance(persistedRemark));
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
  public ResponseEntity<String> getRemarkAuditLog(@PathVariable("id") UUID id,
      @RequestParam(name = "author", required = false, defaultValue = "") String author,
      @RequestParam(name = "changedPropertyName", required = false, defaultValue = "")
      String changedPropertyName, Pageable page) {

    // Return a 404 if the specified instance can't be found
    if (!remarkService.existsById(id)) {
      throw new NotFoundException(MessageKeys.ERROR_REMARK_NOT_FOUND);
    }

    return getAuditLogResponse(Remark.class, id, author, changedPropertyName, page);
  }
}
