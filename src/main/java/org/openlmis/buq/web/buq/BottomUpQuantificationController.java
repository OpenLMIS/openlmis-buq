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

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.openlmis.buq.ApproveFacilityForecastingStats;
import org.openlmis.buq.domain.buq.BottomUpQuantification;
import org.openlmis.buq.domain.buq.Rejection;
import org.openlmis.buq.dto.buq.BottomUpQuantificationDto;
import org.openlmis.buq.dto.buq.RejectionDto;
import org.openlmis.buq.exception.NotFoundException;
import org.openlmis.buq.i18n.MessageKeys;
import org.openlmis.buq.repository.buq.BottomUpQuantificationRepository;
import org.openlmis.buq.repository.buq.BottomUpQuantificationSearchParams;
import org.openlmis.buq.service.buq.BottomUpQuantificationDtoBuilder;
import org.openlmis.buq.service.buq.BottomUpQuantificationService;
import org.openlmis.buq.service.buq.RejectionService;
import org.openlmis.buq.util.Pagination;
import org.openlmis.buq.web.BaseController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.MultiValueMap;
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
  public static final String TEXT_CSV_MEDIA_TYPE = "text/csv";
  public static final String BUQ_FORM_CSV_FILENAME = "buq_quantification_preparation_report";

  @Autowired
  private BottomUpQuantificationRepository bottomUpQuantificationRepository;

  @Autowired
  private BottomUpQuantificationService bottomUpQuantificationService;

  @Autowired
  private BottomUpQuantificationDtoBuilder bottomUpQuantificationDtoBuilder;

  @Autowired
  private RejectionService rejectionService;

  /**
   * Retrieves all BottomUpQuantifications that match the parameters passed.
   *
   * @param queryParams {@link BottomUpQuantificationSearchParams} request parameters.
   * @param pageable object used to encapsulate the pagination related values: page, size and sort.
   * @return List of wanted BottomUpQuantifications matching query parameters.
   */
  @GetMapping
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public Page<BottomUpQuantificationDto> getAllBottomUpQuantifications(
      @RequestParam(required = false) MultiValueMap<String, String> queryParams,
      Pageable pageable) {
    BottomUpQuantificationSearchParams params =
        new QueryBottomUpQuantificationSearchParams(queryParams);

    Page<BottomUpQuantification> page = bottomUpQuantificationRepository.search(params, pageable);
    List<BottomUpQuantificationDto> content = page
        .getContent()
        .stream()
        .map(bottomUpQuantificationDtoBuilder::buildDto)
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

    return bottomUpQuantificationDtoBuilder.buildDto(buq);
  }

  /**
   * Deletes the specified bottom-up quantification.
   */
  @DeleteMapping(value = "/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteBottomUpQuantification(@PathVariable("id") UUID id) {
    BottomUpQuantification buq = bottomUpQuantificationRepository.findById(id).orElseThrow(
        () -> new NotFoundException(MessageKeys.ERROR_BOTTOM_UP_QUANTIFICATION_NOT_FOUND));

    bottomUpQuantificationService.delete(buq);
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

    return bottomUpQuantificationDtoBuilder.buildDto(bottomUpQuantification);
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

    return bottomUpQuantificationDtoBuilder.buildDto(updatedBottomUpQuantification);
  }

  /**
   * Allows downloading csv file.
   *
   * @return bytes containing bottom-up quantification data in csv format.
   * @throws IOException I/O exception
   */
  @GetMapping("/{id}/download")
  @ResponseStatus(HttpStatus.OK)
  public ResponseEntity<byte[]> download(@PathVariable("id") UUID bottomUpQuantificationId)
      throws IOException {
    BottomUpQuantification buq = bottomUpQuantificationRepository
        .findById(bottomUpQuantificationId).orElseThrow(
          () -> new NotFoundException(MessageKeys.ERROR_BOTTOM_UP_QUANTIFICATION_NOT_FOUND));

    return ResponseEntity.ok()
        .contentType(MediaType.valueOf(TEXT_CSV_MEDIA_TYPE))
        .header(HttpHeaders.CONTENT_DISPOSITION,
            "attachment;filename=" + BUQ_FORM_CSV_FILENAME + ".csv")
        .body(bottomUpQuantificationService.getPreparationFormData(buq));
  }

  /**
   * Authorize given bottom-up quantification.
   *
   * @param bottomUpQuantificationId UUID of BottomUpQuantification to authorize.
   * @return authorized BottomUpQuantification.
   */
  @PostMapping("/{id}/authorize")
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public BottomUpQuantificationDto authorize(@PathVariable("id") UUID bottomUpQuantificationId,
      @RequestBody BottomUpQuantificationDto bottomUpQuantificationDto) {
    if (!bottomUpQuantificationRepository.existsById(bottomUpQuantificationId)) {
      throw new NotFoundException(MessageKeys.ERROR_BOTTOM_UP_QUANTIFICATION_NOT_FOUND);
    }

    BottomUpQuantification updatedBottomUpQuantification = bottomUpQuantificationService
        .authorize(bottomUpQuantificationDto, bottomUpQuantificationId);

    return bottomUpQuantificationDtoBuilder.buildDto(updatedBottomUpQuantification);
  }

  /**
   * Approve given bottom-up quantification.
   *
   * @param bottomUpQuantificationId UUID of BottomUpQuantification to approve.
   * @return approved BottomUpQuantification.
   */
  @PostMapping("/{id}/approve")
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public BottomUpQuantificationDto approve(@PathVariable("id") UUID bottomUpQuantificationId,
      @RequestBody BottomUpQuantificationDto bottomUpQuantificationDto) {
    if (!bottomUpQuantificationRepository.existsById(bottomUpQuantificationId)) {
      throw new NotFoundException(MessageKeys.ERROR_BOTTOM_UP_QUANTIFICATION_NOT_FOUND);
    }

    BottomUpQuantification updatedBottomUpQuantification = bottomUpQuantificationService
        .approve(bottomUpQuantificationDto, bottomUpQuantificationId);

    return bottomUpQuantificationDtoBuilder.buildDto(updatedBottomUpQuantification);
  }

  /**
   * Retrieves statistics related to the approval of facility forecasting for a specific program.
   * The statistics include total facilities number, the number and percentage of bottom-up
   * quantifications submitted.
   *
   * @param programId The UUID of the program for which to retrieve approval statistics.
   * @return An instance of the {@link ApproveFacilityForecastingStats} class containing the
   *         approval statistics for the specified program.
   */
  @GetMapping(value = "/approveFacilityForecastingStats")
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public ApproveFacilityForecastingStats getApproveFacilityForecastingStats(
      @RequestParam(value = "programId") UUID programId) {
    return bottomUpQuantificationService.getApproveFacilityForecastingStats(programId);
  }

  /**
   * Get bottom-up quantifications to approve for right supervisor.
   *
   * @param pageable object used to encapsulate the pagination related values: page, size and sort.
   * @return List of bottom-up quantifications to approve.
   */
  @GetMapping(value = "/forApproval")
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public Page<BottomUpQuantificationDto> getForApproval(Pageable pageable,
      @RequestParam(value = "programId") UUID programId) {
    Page<BottomUpQuantification> bottomUpQuantificationsForApproval =
        bottomUpQuantificationService.getBottomUpQuantificationsForApproval(programId, pageable);

    List<BottomUpQuantificationDto> content = bottomUpQuantificationsForApproval
        .getContent()
        .stream()
        .map(bottomUpQuantificationDtoBuilder::buildDto)
        .collect(Collectors.toList());

    return Pagination.getPage(content, pageable,
        bottomUpQuantificationsForApproval.getTotalElements());
  }

  /**
   * Rejects given bottom-up quantification.
   *
   * @param bottomUpQuantificationId UUID of BottomUpQuantification to authorize.
   * @param rejectionDto DTO of Rejection
   * @return rejected BottomUpQuantification.
   */
  @PostMapping("/{id}/reject")
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public BottomUpQuantificationDto reject(@PathVariable("id") UUID bottomUpQuantificationId,
      @RequestBody RejectionDto rejectionDto) {
    BottomUpQuantification updatedBottomUpQuantification = bottomUpQuantificationService
            .reject(bottomUpQuantificationId, rejectionDto);
    return bottomUpQuantificationDtoBuilder.buildDto(updatedBottomUpQuantification);
  }

  /**
   * Returns the latest rejection.
   *
   * @param bottomUpQuantificationId UUID of BottomUpQuantification to authorize.
   * @return rejectionDto
   */
  @GetMapping("/{id}/mostRecentRejection")
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public RejectionDto getMostRecentRejection(@PathVariable("id") UUID bottomUpQuantificationId) {
    Rejection latestRejection = rejectionService.getLatestRejection(bottomUpQuantificationId);
    return RejectionDto.newInstance(latestRejection);
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

  /**
   * Endpoint to submit a bottomUpQuantification.
   *
   * @param id UUID of bottom-up quantification which we want to update.
   * @param bottomUpQuantificationDto A bottom-up quantification DTO bound to the request body.
   * @return updated bottom-up quantification dto.
   */
  @PostMapping("/{id}/submit")
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public BottomUpQuantificationDto submitBottomUpQuantification(
          @PathVariable("id") UUID id,
          @RequestBody BottomUpQuantificationDto bottomUpQuantificationDto) {
    return bottomUpQuantificationService
            .submitBottomUpQuantification(bottomUpQuantificationDto, id);
  }

}
