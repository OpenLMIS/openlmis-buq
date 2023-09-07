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

import static org.openlmis.buq.i18n.MessageKeys.ERROR_BOTTOM_UP_QUANTIFICATION_NOT_FOUND;
import static org.openlmis.buq.i18n.MessageKeys.ERROR_FACILITY_NOT_FOUND;
import static org.openlmis.buq.i18n.MessageKeys.ERROR_ID_MISMATCH;
import static org.openlmis.buq.i18n.MessageKeys.ERROR_ORDERABLE_NOT_FOUND;
import static org.openlmis.buq.i18n.MessageKeys.ERROR_PROCESSING_PERIOD_NOT_FOUND;
import static org.openlmis.buq.i18n.MessageKeys.ERROR_PROGRAM_NOT_FOUND;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.openlmis.buq.domain.Remark;
import org.openlmis.buq.domain.buq.BottomUpQuantification;
import org.openlmis.buq.domain.buq.BottomUpQuantificationFundingDetails;
import org.openlmis.buq.domain.buq.BottomUpQuantificationLineItem;
import org.openlmis.buq.domain.buq.BottomUpQuantificationStatus;
import org.openlmis.buq.domain.buq.BottomUpQuantificationStatusChange;
import org.openlmis.buq.dto.buq.BottomUpQuantificationDto;
import org.openlmis.buq.dto.csv.BottomUpQuantificationLineItemCsv;
import org.openlmis.buq.dto.referencedata.BasicOrderableDto;
import org.openlmis.buq.dto.referencedata.FacilityDto;
import org.openlmis.buq.dto.referencedata.ProcessingPeriodDto;
import org.openlmis.buq.dto.referencedata.ProgramDto;
import org.openlmis.buq.dto.requisition.RequisitionLineItemDataProjection;
import org.openlmis.buq.exception.BindingResultException;
import org.openlmis.buq.exception.ContentNotFoundMessageException;
import org.openlmis.buq.exception.ValidationMessageException;
import org.openlmis.buq.i18n.MessageKeys;
import org.openlmis.buq.repository.buq.BottomUpQuantificationRepository;
import org.openlmis.buq.service.CsvService;
import org.openlmis.buq.service.referencedata.FacilityReferenceDataService;
import org.openlmis.buq.service.referencedata.OrderableReferenceDataService;
import org.openlmis.buq.service.referencedata.PeriodReferenceDataService;
import org.openlmis.buq.service.referencedata.ProgramReferenceDataService;
import org.openlmis.buq.service.remark.RemarkService;
import org.openlmis.buq.util.AuthenticationHelper;
import org.openlmis.buq.util.FacilitySupportsProgramHelper;
import org.openlmis.buq.util.Message;
import org.openlmis.buq.validate.BottomUpQuantificationValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

@SuppressWarnings("PMD.TooManyMethods")
@Service
public class BottomUpQuantificationService {

  @Autowired
  private AuthenticationHelper authenticationHelper;

  @Autowired
  private FacilityReferenceDataService facilityReferenceDataService;

  @Autowired
  private FacilitySupportsProgramHelper facilitySupportsProgramHelper;

  @Autowired
  private ProgramReferenceDataService programReferenceDataService;

  @Autowired
  private PeriodReferenceDataService periodReferenceDataService;

  @Autowired
  private OrderableReferenceDataService orderableReferenceDataService;

  @Autowired
  private CsvService csvService;

  @Autowired
  private BottomUpQuantificationRepository bottomUpQuantificationRepository;

  @Autowired
  private BottomUpQuantificationDtoBuilder bottomUpQuantificationDtoBuilder;

  @Autowired
  private BottomUpQuantificationValidator validator;

  @Autowired
  private RemarkService remarkService;

  private static final String MESSAGE_SEPARATOR = ":";

  private static final String PARAMETER_SEPARATOR = ",";

  /**
   * Prepares given bottom-up quantification if possible.
   *
   * @param facilityId         Facility ID.
   * @param programId          Program ID.
   * @param processingPeriodId Processing Period ID.
   * @return Prepared bottom-up quantification.
   */
  public BottomUpQuantification prepare(UUID facilityId, UUID programId,
      UUID processingPeriodId) {
    validatePreparationParams(facilityId, programId, processingPeriodId);

    FacilityDto facility = findFacility(facilityId);
    ProgramDto program = findProgram(programId);
    facilitySupportsProgramHelper.checkIfFacilitySupportsProgram(facility, program.getId());
    ProcessingPeriodDto period = findPeriod(processingPeriodId);

    List<RequisitionLineItemDataProjection> requisitionLineItemsData =
        bottomUpQuantificationRepository.getRequisitionLineItemsData(facility.getId(),
            period.getId());

    BottomUpQuantification newBottomUpQuantification = prepareBottomUpQuantification(facility,
        program, period, requisitionLineItemsData);

    BindingResult errors = new BeanPropertyBindingResult(newBottomUpQuantification,
        "bottomUpQuantification");
    validator.validate(newBottomUpQuantification, errors);
    if (errors.hasErrors()) {
      throw new BindingResultException(getErrors(errors));
    }

    bottomUpQuantificationRepository.save(newBottomUpQuantification);

    return newBottomUpQuantification;
  }

  /**
   * Saves new data for bottom-up quantification.
   *
   * @param bottomUpQuantificationImporter DTO containing new data.
   * @param bottomUpQuantificationId ID of the bottom-up quantification to be saved.
   * @return Bottom-up quantification with new data.
   */
  public BottomUpQuantification save(BottomUpQuantificationDto bottomUpQuantificationImporter,
      UUID bottomUpQuantificationId) {
    BottomUpQuantification updatedBottomUpQuantification =
        updateBottomUpQuantification(bottomUpQuantificationImporter, bottomUpQuantificationId);
    bottomUpQuantificationRepository.save(updatedBottomUpQuantification);

    return updatedBottomUpQuantification;
  }

  /**
   * Prepares data for downloading.
   *
   * @param bottomUpQuantification BottomUpQuantification containing data to be downloaded.
   * @return byte array containing the data to be downloaded.
   * @throws IOException I/O exception
   */
  public byte[] getPreparationFormData(BottomUpQuantification bottomUpQuantification)
      throws IOException {
    List<BottomUpQuantificationLineItemCsv> csvLineItems = bottomUpQuantification
        .getBottomUpQuantificationLineItems()
        .stream()
        .map(lineItem -> {
          BasicOrderableDto dto = findOrderable(lineItem.getOrderableId());
          return new BottomUpQuantificationLineItemCsv(
              dto.getProductCode(),
              dto.getFullProductName(),
              dto.getNetContent(),
              lineItem.getAnnualAdjustedConsumption()
          );
        })
        .collect(Collectors.toList());

    return csvService.generateCsv(csvLineItems, BottomUpQuantificationLineItemCsv.class);
  }

  /**
   * Changes BUQ status to authorized and updates it with the given data.
   *
   * @param bottomUpQuantificationImporter DTO containing new data.
   * @param bottomUpQuantificationId ID of the bottom-up quantification to be authorized.
   * @return Authorized Bottom-up quantification.
   */
  public BottomUpQuantification authorize(BottomUpQuantificationDto bottomUpQuantificationImporter,
      UUID bottomUpQuantificationId) {
    validator.validateCanBeAuthorized(bottomUpQuantificationImporter, bottomUpQuantificationId);

    BottomUpQuantification updatedBottomUpQuantification =
        updateBottomUpQuantification(bottomUpQuantificationImporter, bottomUpQuantificationId);
    updatedBottomUpQuantification.setStatus(BottomUpQuantificationStatus.AUTHORIZED);
    addNewStatusChange(updatedBottomUpQuantification);
    bottomUpQuantificationRepository.save(updatedBottomUpQuantification);

    return updatedBottomUpQuantification;
  }

  /**
   * Submits a bottomUpQuantification.
   *
   * @param bottomUpQuantificationDto DTO of bottomUpQuantification
   * @param id id of bottomUpQuantification
   * @return Bottom-up quantification dto with new data.
   */
  public BottomUpQuantificationDto submitBottomUpQuantification(
      BottomUpQuantificationDto bottomUpQuantificationDto, UUID id) {
    validator.validateCanBeSubmitted(bottomUpQuantificationDto, id);

    BottomUpQuantification bottomUpQuantification = save(bottomUpQuantificationDto, id);
    changeStatus(bottomUpQuantification, BottomUpQuantificationStatus.SUBMITTED);
    return bottomUpQuantificationDtoBuilder
        .buildDto(bottomUpQuantification);
  }

  /**
   * Checks if a bottomUpQuantification of given period and facility exists.
   *
   * @param periodId UUID of period
   * @param facilityId UUID of facility
   * @return returns true if a buq of given period and facility exists. False otherwise.
   */
  public boolean existsByPeriodAndFacility(UUID facilityId, UUID periodId) {
    return bottomUpQuantificationRepository
        .existsByFacilityIdAndProcessingPeriodId(facilityId, periodId);
  }

  /**
   * Finds and retrieves a BottomUpQuantification entity by its unique identifier.
   *
   * @param bottomUpQuantificationId The UUID identifier of the BottomUpQuantification to find.
   * @return The found BottomUpQuantification entity.
   * @throws ContentNotFoundMessageException If the BottomUpQuantification with the given ID is
   *     not found.
   */
  public BottomUpQuantification findBottomUpQuantification(UUID bottomUpQuantificationId) {
    return bottomUpQuantificationRepository
        .findById(bottomUpQuantificationId)
        .orElseThrow(() -> new ContentNotFoundMessageException(
            ERROR_BOTTOM_UP_QUANTIFICATION_NOT_FOUND, bottomUpQuantificationId)
        );
  }

  Map<String, Message> getErrors(BindingResult bindingResult) {
    Map<String, Message> errors = new HashMap<>();

    for (FieldError error : bindingResult.getFieldErrors()) {
      String[] parts = error.getCode().split(MESSAGE_SEPARATOR);
      String messageKey = parts[0];
      String[] parameters = parts[1].split(PARAMETER_SEPARATOR);
      errors.put(error.getField(), new Message(messageKey.trim(),
          Arrays.stream(parameters).map(String::trim).toArray()));
    }

    return errors;
  }

  private BottomUpQuantification prepareBottomUpQuantification(FacilityDto facility,
      ProgramDto program, ProcessingPeriodDto processingPeriod,
      List<RequisitionLineItemDataProjection> requisitionLineItemsData) {
    int targetYear = processingPeriod.getEndDate().getYear();
    BottomUpQuantification bottomUpQuantification = new BottomUpQuantification(facility.getId(),
        program.getId(), processingPeriod.getId(), targetYear);
    BottomUpQuantificationFundingDetails fundingDetails =
        new BottomUpQuantificationFundingDetails(bottomUpQuantification);
    bottomUpQuantification.setFundingDetails(fundingDetails);

    prepareLineItems(bottomUpQuantification, requisitionLineItemsData);
    bottomUpQuantification.setStatus(BottomUpQuantificationStatus.DRAFT);
    addNewStatusChange(bottomUpQuantification);

    return bottomUpQuantification;
  }

  private void prepareLineItems(
      BottomUpQuantification bottomUpQuantification,
      List<RequisitionLineItemDataProjection> requisitionLineItemsData) {
    List<BottomUpQuantificationLineItem> bottomUpQuantificationLineItems = new ArrayList<>();
    for (RequisitionLineItemDataProjection itemData : requisitionLineItemsData) {
      BottomUpQuantificationLineItem lineItem = new BottomUpQuantificationLineItem();
      lineItem.setBottomUpQuantification(bottomUpQuantification);
      lineItem.setOrderableId(UUID.fromString(itemData.getOrderableId()));
      Integer annualAdjustedConsumption = Math.toIntExact(OrderableReferenceDataService
          .calculatePacks(itemData.getAnnualAdjustedConsumption(), itemData.getNetContent(),
              itemData.getPackRoundingThreshold(), itemData.getRoundToZero()
          ));
      lineItem.setAnnualAdjustedConsumption(annualAdjustedConsumption);

      bottomUpQuantificationLineItems.add(lineItem);
    }

    bottomUpQuantification.setBottomUpQuantificationLineItems(bottomUpQuantificationLineItems);
  }

  private void addNewStatusChange(BottomUpQuantification bottomUpQuantification) {
    bottomUpQuantification.getStatusChanges().add(
        BottomUpQuantificationStatusChange.newInstance(
            bottomUpQuantification,
            authenticationHelper.getCurrentUser().getId(),
            bottomUpQuantification.getStatus())
    );
    bottomUpQuantification.setModifiedDate(ZonedDateTime.now());
  }

  private BottomUpQuantification updateBottomUpQuantification(
      BottomUpQuantificationDto bottomUpQuantificationDto, UUID bottomUpQuantificationId) {
    if (null != bottomUpQuantificationDto.getId()
        && !Objects.equals(bottomUpQuantificationDto.getId(), bottomUpQuantificationId)) {
      throw new ValidationMessageException(ERROR_ID_MISMATCH);
    }

    BottomUpQuantification bottomUpQuantificationToUpdate =
        findBottomUpQuantification(bottomUpQuantificationId);

    List<BottomUpQuantificationLineItem> updatedLineItems = bottomUpQuantificationDto
        .getBottomUpQuantificationLineItems()
        .stream()
        .map(lineItemDto -> {
          BottomUpQuantificationLineItem lineItem = BottomUpQuantificationLineItem
              .newInstance(lineItemDto);
          lineItem.setBottomUpQuantification(bottomUpQuantificationToUpdate);
          lineItem.setId(lineItemDto.getId());
          if (lineItemDto.getRemark() != null) {
            Remark remark = remarkService.findOne(lineItemDto.getRemark().getId());
            lineItem.setRemark(remark);
          }

          return lineItem;
        })
        .collect(Collectors.toList());

    if (bottomUpQuantificationDto.getFundingDetails() != null) {
      BottomUpQuantificationFundingDetails fundingDetails = bottomUpQuantificationToUpdate
          .getFundingDetails();
      fundingDetails.updateFrom(bottomUpQuantificationDto.getFundingDetails());
      bottomUpQuantificationToUpdate.setFundingDetails(fundingDetails);
    }

    bottomUpQuantificationToUpdate.updateFrom(updatedLineItems);

    return bottomUpQuantificationToUpdate;
  }

  private FacilityDto findFacility(UUID facilityId) {
    return findResource(facilityId, facilityReferenceDataService::findOne,
        ERROR_FACILITY_NOT_FOUND);
  }

  private ProgramDto findProgram(UUID programId) {
    return findResource(programId, programReferenceDataService::findOne,
        ERROR_PROGRAM_NOT_FOUND);
  }

  private ProcessingPeriodDto findPeriod(UUID programId) {
    return findResource(programId, periodReferenceDataService::findOne,
        ERROR_PROCESSING_PERIOD_NOT_FOUND);
  }

  private BasicOrderableDto findOrderable(UUID orderableId) {
    return findResource(orderableId, orderableReferenceDataService::findOne,
        ERROR_ORDERABLE_NOT_FOUND);
  }

  private <R> R findResource(UUID id, Function<UUID, R> finder, String errorMessage) {
    return Optional
        .ofNullable(finder.apply(id))
        .orElseThrow(() -> new ContentNotFoundMessageException(errorMessage, id)
        );
  }

  private void validatePreparationParams(UUID facilityId, UUID programId,
      UUID processingPeriodId) {
    List<String> missingParamsList = Stream.of(
            new AbstractMap.SimpleEntry<>(facilityId, "facility ID"),
            new AbstractMap.SimpleEntry<>(programId, "program ID"),
            new AbstractMap.SimpleEntry<>(processingPeriodId, "processing period ID"))
        .filter(entry -> entry.getKey() == null)
        .map(Map.Entry::getValue)
        .collect(Collectors.toList());

    if (!missingParamsList.isEmpty()) {
      String missingParams = String.join(", ", missingParamsList);
      throw new ValidationMessageException(
          new Message(MessageKeys.ERROR_PREPARE_MISSING_PARAMETERS, missingParams));
    }
  }

  /**
   * Changes the status of bottomUpQuantification.
   *
   * @param bottomUpQuantification entity of bottomUpQuantification
   * @param status status to be applied to bottomUpQuantification
   */
  private void changeStatus(
      BottomUpQuantification bottomUpQuantification,
      BottomUpQuantificationStatus status) {
    bottomUpQuantification.setStatus(status);
    addNewStatusChange(bottomUpQuantification);
    bottomUpQuantificationRepository.save(bottomUpQuantification);
  }

}
