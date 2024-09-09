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

import static java.util.stream.Collectors.toSet;
import static org.openlmis.buq.CurrencyConfig.currencyCode;
import static org.openlmis.buq.i18n.MessageKeys.ERROR_BOTTOM_UP_QUANTIFICATION_NOT_FOUND;
import static org.openlmis.buq.i18n.MessageKeys.ERROR_FACILITY_NOT_FOUND;
import static org.openlmis.buq.i18n.MessageKeys.ERROR_ID_MISMATCH;
import static org.openlmis.buq.i18n.MessageKeys.ERROR_ORDERABLE_NOT_FOUND;
import static org.openlmis.buq.i18n.MessageKeys.ERROR_PROCESSING_PERIOD_NOT_FOUND;
import static org.openlmis.buq.i18n.MessageKeys.ERROR_PROGRAM_NOT_FOUND;
import static org.openlmis.buq.i18n.MessageKeys.ERROR_SOURCE_OF_FUND_NOT_FOUND;
import static org.openlmis.buq.i18n.MessageKeys.ERROR_SUPERVISORY_NODE_CANNOT_BE_NULL_TO_BE_AUTHORIZED;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.joda.money.CurrencyUnit;
import org.joda.money.Money;
import org.openlmis.buq.ApproveFacilityForecastingStats;
import org.openlmis.buq.domain.BaseEntity;
import org.openlmis.buq.domain.Remark;
import org.openlmis.buq.domain.buq.BottomUpQuantification;
import org.openlmis.buq.domain.buq.BottomUpQuantificationFundingDetails;
import org.openlmis.buq.domain.buq.BottomUpQuantificationLineItem;
import org.openlmis.buq.domain.buq.BottomUpQuantificationSourceOfFund;
import org.openlmis.buq.domain.buq.BottomUpQuantificationStatus;
import org.openlmis.buq.domain.buq.BottomUpQuantificationStatusChange;
import org.openlmis.buq.domain.buq.Rejection;
import org.openlmis.buq.domain.productgroup.ProductGroup;
import org.openlmis.buq.domain.sourceoffund.SourceOfFund;
import org.openlmis.buq.dto.BottomUpQuantificationGroupCostsData;
import org.openlmis.buq.dto.ResultDto;
import org.openlmis.buq.dto.buq.BottomUpQuantificationDto;
import org.openlmis.buq.dto.buq.BottomUpQuantificationLineItemDto;
import org.openlmis.buq.dto.buq.RejectionDto;
import org.openlmis.buq.dto.csv.BottomUpQuantificationLineItemCsv;
import org.openlmis.buq.dto.productgroup.ProductGroupsCostData;
import org.openlmis.buq.dto.referencedata.BasicOrderableDto;
import org.openlmis.buq.dto.referencedata.DetailedRoleAssignmentDto;
import org.openlmis.buq.dto.referencedata.FacilityDto;
import org.openlmis.buq.dto.referencedata.GeographicZoneDto;
import org.openlmis.buq.dto.referencedata.MinimalFacilityDto;
import org.openlmis.buq.dto.referencedata.ObjectReferenceDto;
import org.openlmis.buq.dto.referencedata.ProcessingPeriodDto;
import org.openlmis.buq.dto.referencedata.ProgramDto;
import org.openlmis.buq.dto.referencedata.RightDto;
import org.openlmis.buq.dto.referencedata.SupervisoryNodeDto;
import org.openlmis.buq.dto.referencedata.SupplyLineDto;
import org.openlmis.buq.dto.referencedata.UserDto;
import org.openlmis.buq.dto.requisition.RequisitionLineItemDataProjection;
import org.openlmis.buq.exception.BindingResultException;
import org.openlmis.buq.exception.ContentNotFoundMessageException;
import org.openlmis.buq.exception.ValidationMessageException;
import org.openlmis.buq.i18n.MessageKeys;
import org.openlmis.buq.repository.buq.BottomUpQuantificationFundingDetailsRepository;
import org.openlmis.buq.repository.buq.BottomUpQuantificationLineItemRepository;
import org.openlmis.buq.repository.buq.BottomUpQuantificationRepository;
import org.openlmis.buq.repository.buq.BottomUpQuantificationSourceOfFundRepository;
import org.openlmis.buq.repository.buq.BottomUpQuantificationStatusChangeRepository;
import org.openlmis.buq.repository.productgroup.ProductGroupRepository;
import org.openlmis.buq.repository.sourceoffund.SourceOfFundRepository;
import org.openlmis.buq.service.CsvService;
import org.openlmis.buq.service.RequestParameters;
import org.openlmis.buq.service.referencedata.FacilityReferenceDataService;
import org.openlmis.buq.service.referencedata.OrderableReferenceDataService;
import org.openlmis.buq.service.referencedata.PeriodReferenceDataService;
import org.openlmis.buq.service.referencedata.ProgramReferenceDataService;
import org.openlmis.buq.service.referencedata.RightReferenceDataService;
import org.openlmis.buq.service.referencedata.SupervisoryNodeReferenceDataService;
import org.openlmis.buq.service.referencedata.SupplyLineReferenceDataService;
import org.openlmis.buq.service.referencedata.UserReferenceDataService;
import org.openlmis.buq.service.referencedata.UserRoleAssignmentsReferenceDataService;
import org.openlmis.buq.service.remark.RemarkService;
import org.openlmis.buq.util.AuthenticationHelper;
import org.openlmis.buq.util.FacilitySupportsProgramHelper;
import org.openlmis.buq.util.Message;
import org.openlmis.buq.util.Pagination;
import org.openlmis.buq.validate.BottomUpQuantificationValidator;
import org.openlmis.buq.web.buq.ApproveParams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

@SuppressWarnings("PMD.TooManyMethods")
@Service
public class BottomUpQuantificationService {

  public static final String APPROVE_BUQ_RIGHT_NAME = "APPROVE_BUQ";
  public static final String MOH_APPROVAL_RIGHT_NAME = "MOH_APPROVAL";
  public static final String PORALG_APPROVAL_RIGHT_NAME = "PORALG_APPROVAL";

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

  @Autowired
  private SourceOfFundRepository sourceOfFundRepository;

  @Autowired
  private UserReferenceDataService userReferenceDataService;

  @Autowired
  private RejectionService rejectionService;

  @Autowired
  private BottomUpQuantificationStatusChangeRepository bottomUpQuantificationStatusChangeRepository;

  @Autowired
  private RightReferenceDataService rightReferenceDataService;

  @Autowired
  private UserRoleAssignmentsReferenceDataService userRoleAssignmentsReferenceDataService;

  @Autowired
  private SupervisoryNodeReferenceDataService supervisoryNodeReferenceDataService;

  @Autowired
  private SupplyLineReferenceDataService supplyLineReferenceDataService;

  @Autowired
  private ProductGroupRepository productGroupRepository;

  @Autowired
  private BottomUpQuantificationLineItemRepository bottomUpQuantificationLineItemRepository;

  @Autowired
  private BottomUpQuantificationSourceOfFundRepository bottomUpQuantificationSourceOfFundRepository;

  @Autowired
  private BottomUpQuantificationFundingDetailsRepository
            bottomUpQuantificationFundingDetailsRepository;

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
    checkFacilityPermission(bottomUpQuantificationImporter.getFacilityId());
    BottomUpQuantification updatedBottomUpQuantification =
        updateBottomUpQuantification(bottomUpQuantificationImporter, bottomUpQuantificationId);

    assignInitialSupervisoryNode(updatedBottomUpQuantification);

    return updatedBottomUpQuantification;
  }

  private void assignInitialSupervisoryNode(BottomUpQuantification bottomUpQuantification) {
    if (bottomUpQuantification.isApprovable()
            && bottomUpQuantification.getSupervisoryNodeId() == null) {
      SupervisoryNodeDto supervisoryNode = supervisoryNodeReferenceDataService.findSupervisoryNode(
              bottomUpQuantification.getProgramId(),
              bottomUpQuantification.getFacilityId());
      if (supervisoryNode != null) {
        bottomUpQuantification.setSupervisoryNodeId(supervisoryNode.getId());
        return;
      }
      bottomUpQuantification.setSupervisoryNodeId(null);
    }
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
    checkFacilityPermission(bottomUpQuantificationImporter.getFacilityId());
    validator.validateCanBeAuthorized(bottomUpQuantificationImporter, bottomUpQuantificationId);

    BottomUpQuantification updatedBottomUpQuantification =
        updateBottomUpQuantification(bottomUpQuantificationImporter, bottomUpQuantificationId);
    updatedBottomUpQuantification.setStatus(BottomUpQuantificationStatus.AUTHORIZED);
    addNewStatusChange(updatedBottomUpQuantification);
    assignInitialSupervisoryNode(updatedBottomUpQuantification);
    if (Objects.isNull(updatedBottomUpQuantification.getSupervisoryNodeId())) {
      throw new ValidationMessageException(
          ERROR_SUPERVISORY_NODE_CANNOT_BE_NULL_TO_BE_AUTHORIZED);
    }

    return updatedBottomUpQuantification;
  }

  /**
   * Rejects a bottomUpQuantification.
   *
   * @param rejectionDto DTO of rejection
   * @param bottomUpQuantificationId id of bottomUpQuantification
   * @return Bottom-up quantification dto with new data.
   */
  public BottomUpQuantification reject(UUID bottomUpQuantificationId, RejectionDto rejectionDto) {
    BottomUpQuantification bottomUpQuantification =
            findBottomUpQuantification(bottomUpQuantificationId);
    validator.validateCanBeRejected(bottomUpQuantification);
    bottomUpQuantification.setStatus(BottomUpQuantificationStatus.REJECTED);
    BottomUpQuantificationStatusChange statusChange =
            BottomUpQuantificationStatusChange.newInstance(
              bottomUpQuantification,
              authenticationHelper.getCurrentUser().getId(),
              bottomUpQuantification.getStatus());

    BottomUpQuantificationStatusChange persistedStatusChange =
            bottomUpQuantificationStatusChangeRepository.save(statusChange);
    Rejection rejection = Rejection.newInstance(rejectionDto);
    bottomUpQuantification.getStatusChanges().add(persistedStatusChange);
    rejection.setStatusChange(persistedStatusChange);

    bottomUpQuantification.setSupervisoryNodeId(null);
    rejectionService.save(rejection);
    return bottomUpQuantification;

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
    checkFacilityPermission(bottomUpQuantificationDto.getFacilityId());
    validator.validateCanBeSubmitted(bottomUpQuantificationDto, id);

    BottomUpQuantification bottomUpQuantification = save(bottomUpQuantificationDto, id);
    bottomUpQuantification.setStatus(BottomUpQuantificationStatus.SUBMITTED);
    addNewStatusChange(bottomUpQuantification);

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
   * Checks whether the authorization step can be skipped for the given BottomUpQuantification.
   * This method determines if the program associated with the BottomUpQuantification allows
   * skipping the authorization step.
   *
   * @param bottomUpQuantification The BottomUpQuantification for which authorization is checked.
   * @return Returns true if the program associated with the BottomUpQuantification allows skipping
   *         authorization. False otherwise.
   */
  public boolean canSkipAuthorization(BottomUpQuantification bottomUpQuantification) {
    return findProgram(bottomUpQuantification.getProgramId()).getSkipAuthorization();
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

  /**
   * Deletes a Bottom-Up Quantification record.
   *
   * @param bottomUpQuantification The Bottom-Up Quantification entity to be deleted.
   */
  public void delete(BottomUpQuantification bottomUpQuantification) {
    checkFacilityPermission(bottomUpQuantification.getFacilityId());

    List<BottomUpQuantificationStatusChange> statusChanges =
            bottomUpQuantification.getStatusChanges();

    List<UUID> statusChangeIds = statusChanges.stream()
            .filter(status -> status.getStatus().equals(BottomUpQuantificationStatus.REJECTED))
            .map(BaseEntity::getId)
            .collect(Collectors.toList());

    rejectionService.deleteByStatusChangeIdIn(statusChangeIds);
    bottomUpQuantificationRepository.deleteById(bottomUpQuantification.getId());
  }

  /**
   * Changes BUQ status to approved and updates it with the given data.
   *
   * @param bottomUpQuantificationImporter DTO containing new data.
   * @param bottomUpQuantificationId ID of the bottom-up quantification to be approved.
   * @return Approved Bottom-up quantification.
   */
  public BottomUpQuantification approve(BottomUpQuantificationDto bottomUpQuantificationImporter,
                                          UUID bottomUpQuantificationId) {
    validator.validateCanBeApproved(bottomUpQuantificationImporter, bottomUpQuantificationId);

    BottomUpQuantification updatedBottomUpQuantification =
        updateBottomUpQuantification(bottomUpQuantificationImporter, bottomUpQuantificationId);

    UserDto user = authenticationHelper.getCurrentUser();

    SupervisoryNodeDto supervisoryNodeDto = supervisoryNodeReferenceDataService
            .findOne(updatedBottomUpQuantification.getSupervisoryNodeId());
    ProcessingPeriodDto period = periodReferenceDataService
            .findOne(updatedBottomUpQuantification.getProcessingPeriodId());
    List<SupplyLineDto> supplyLines = period.isReportOnly()
            ? Collections.emptyList()
            : supplyLineReferenceDataService
              .search(updatedBottomUpQuantification.getProgramId(),
                      updatedBottomUpQuantification.getSupervisoryNodeId());
    ApproveParams approveParams =
            new ApproveParams(user, supervisoryNodeDto, supplyLines, period);
    doApprove(updatedBottomUpQuantification, approveParams);

    return updatedBottomUpQuantification;
  }

  private void doApprove(BottomUpQuantification bottomUpQuantification,
       ApproveParams approveParams) {
    ObjectReferenceDto parentNode = null;
    UUID parentNodeId = null;
    if (approveParams.getSupervisoryNode() != null) {
      parentNode = approveParams.getSupervisoryNode().getParentNode();
    }

    if (parentNode != null) {
      parentNodeId = parentNode.getId();
    }

    BottomUpQuantificationStatusChange statusChange =
            bottomUpQuantification.approve(parentNodeId,
            approveParams.getSupplyLines(),
            approveParams.getUser().getId());
    bottomUpQuantificationStatusChangeRepository.save(statusChange);
  }

  /**
   * Calculates and retrieves statistics related to the approval of facility forecasting for a
   * given program.
   *
   * @param programId The UUID of the program for which statistics are calculated.
   * @return {@link ApproveFacilityForecastingStats} containing the calculated data.
   */
  public ApproveFacilityForecastingStats getApproveFacilityForecastingStats(UUID programId) {
    List<UUID> userSupervisedFacilities = getUserSupervisedFacilities(
        programId, APPROVE_BUQ_RIGHT_NAME);
    int totalFacilities = userSupervisedFacilities.size();

    List<BottomUpQuantification> bottomUpQuantifications =
        bottomUpQuantificationRepository.findByFacilityIdIn(userSupervisedFacilities);
    int totalBottomUpQuantifications = bottomUpQuantifications.size();

    if (totalBottomUpQuantifications == 0) {
      return new ApproveFacilityForecastingStats(totalFacilities, 0, 0);
    }

    int submittedBottomUpQuantifications = (int) bottomUpQuantifications.stream()
        .filter(buq -> buq.getStatus().isPostSubmitted())
        .count();

    int percentageOfSubmittedBottomUpQuantifications = Math
        .round((float) (submittedBottomUpQuantifications * 100) / totalBottomUpQuantifications);

    return new ApproveFacilityForecastingStats(totalFacilities, submittedBottomUpQuantifications,
        percentageOfSubmittedBottomUpQuantifications);
  }

  /**
   * Get bottom-up quantifications to approve for the specified user.
   */
  public Page<BottomUpQuantification> getBottomUpQuantificationsForApproval(UUID programId,
      Pageable pageable) {
    UserDto user = authenticationHelper.getCurrentUser();

    RightDto right = rightReferenceDataService.findRight(APPROVE_BUQ_RIGHT_NAME);
    List<DetailedRoleAssignmentDto> roleAssignments = userRoleAssignmentsReferenceDataService
        .hasRight(user, right);

    if (CollectionUtils.isEmpty(roleAssignments)) {
      return Pagination.getPage(Collections.emptyList(), pageable);
    }

    Set<Pair<UUID, UUID>> programNodePairs = roleAssignments
        .stream()
        .filter(item -> Objects.nonNull(item.getRole().getId()))
        .filter(item -> Objects.nonNull(item.getSupervisoryNodeId()))
        .filter(item -> Objects.nonNull(item.getProgramId()))
        .filter(item -> null == programId || programId.equals(item.getProgramId()))
        .map(item -> new ImmutablePair<>(item.getProgramId(), item.getSupervisoryNodeId()))
        .collect(toSet());

    return bottomUpQuantificationRepository
        .searchApprovableByProgramSupervisoryNodePairs(programNodePairs, pageable);
  }

  /**
   * Retrieves a supervised geographic zones for a given program.
   * This method fetches the geographic zones supervised by the current user for a specific
   * program. It organizes these zones into a hierarchical map, grouped by their level in the
   * geographic zone hierarchy.
   *
   * @param programId The UUID of the program.
   * @return A hierarchical map of supervised geographic zones, organized by level.
   */
  public Map<UUID, Map<UUID, Map<UUID, Set<UUID>>>> getSupervisedGeographicZones(UUID programId) {
    List<UUID> userSupervisedFacilities = Stream
        .concat(getUserSupervisedFacilities(programId, MOH_APPROVAL_RIGHT_NAME).stream(),
            getUserSupervisedFacilities(programId, PORALG_APPROVAL_RIGHT_NAME).stream())
        .collect(Collectors.toList());
    Map<UUID, Map<UUID, Map<UUID, Set<UUID>>>> zones = new HashMap<>();

    for (UUID facilityId : userSupervisedFacilities) {
      FacilityDto facilityDto = facilityReferenceDataService.findOne(facilityId);
      GeographicZoneDto geographicZone = facilityDto.getGeographicZone();
      int levelNumber = geographicZone.getLevel().getLevelNumber();

      if (levelNumber == 1) {
        UUID countryId = geographicZone.getId();
        zones.computeIfAbsent(countryId, k -> new HashMap<>());
      } else if (levelNumber == 2) {
        UUID countryId = geographicZone.getParent().getId();
        UUID zoneId = geographicZone.getId();
        zones.computeIfAbsent(countryId, k -> new HashMap<>())
            .computeIfAbsent(zoneId, k -> new HashMap<>());
      } else if (levelNumber == 3) {
        UUID countryId = geographicZone.getParent().getParent().getId();
        UUID zoneId = geographicZone.getParent().getId();
        UUID regionId = geographicZone.getId();
        zones.computeIfAbsent(countryId, k -> new HashMap<>())
            .computeIfAbsent(zoneId, k -> new HashMap<>())
            .computeIfAbsent(regionId, k -> new HashSet<>());
      } else if (levelNumber == 4) {
        UUID countryId = geographicZone.getParent().getParent().getParent().getId();
        UUID zoneId = geographicZone.getParent().getParent().getId();
        UUID regionId = geographicZone.getParent().getId();
        UUID districtId = geographicZone.getId();
        zones.computeIfAbsent(countryId, k -> new HashMap<>())
            .computeIfAbsent(zoneId, k -> new HashMap<>())
            .computeIfAbsent(regionId, k -> new HashSet<>())
            .add(districtId);
      }
    }

    return zones;
  }

  private List<UUID> getUserSupervisedFacilities(UUID programId, String rightName) {
    UserDto currentUser = authenticationHelper.getCurrentUser();
    List<String> userPermissionStrings = userReferenceDataService
        .getPermissionStrings(currentUser.getId());

    return userPermissionStrings.stream()
        .filter(p -> p.length() - p.replace("|", "").length() == 2
            && rightName.equals(p.substring(0, p.indexOf('|'))))
        .map(p -> p.split("\\|"))
        .filter(p -> programId.equals(UUID.fromString(p[2])))
        .map(p -> UUID.fromString(p[1]))
        .collect(Collectors.toList());
  }

  /**
   * Final approve a bottomUpQuantification.
   */
  public List<BottomUpQuantification> finalApproveBottomUpQuantification(List<UUID> ids) {
    List<BottomUpQuantification> updatedBottomUpQuantifications = new ArrayList<>();
    ids.forEach(id -> {
      BottomUpQuantification bottomUpQuantification =
              findBottomUpQuantification(id);
      updatedBottomUpQuantifications
          .add(changeStatus(bottomUpQuantification, BottomUpQuantificationStatus.APPROVED_BY_NQT));
    });
    return updatedBottomUpQuantifications;
  }

  /**
   * Retrieves cost data for product groups within a specified geographic zone and program,
   * based on the Bottom-Up Quantification records.
   *
   * @param processingPeriodId The UUID of the processing period.
   * @param programId The UUID of the program for which cost data is retrieved.
   * @param geographicZoneId The UUID of the geographic zone for which cost data is calculated.
   * @param geographicZones The Map of geographic zones for which cost data is calculated.
   * @param pageable object used to encapsulate the pagination related values: page, size and sort.
   * @return A list of {@link ProductGroupsCostData} containing cost data for product groups.
   */
  public List<ProductGroupsCostData> getProductsCostData(UUID processingPeriodId, UUID programId,
      UUID geographicZoneId, Map<UUID, Map<UUID, Map<UUID, Set<UUID>>>> geographicZones,
      Pageable pageable) {
    List<BottomUpQuantification> bottomUpQuantificationList =
        getBottomUpQuantificationsForFinalApproval(programId, processingPeriodId,
            pageable).getContent();

    Set<UUID> subZones = new HashSet<>();
    boolean isDistrictLevel = false;
    Set<UUID> countries = geographicZones.keySet();
    if (countries.contains(geographicZoneId)) {
      subZones = geographicZones.get(geographicZoneId).keySet();
    } else {
      for (Map.Entry<UUID, Map<UUID, Map<UUID, Set<UUID>>>> country : geographicZones.entrySet()) {
        Set<UUID> zones = country.getValue().keySet();
        if (zones.contains(geographicZoneId)) {
          subZones = country.getValue().get(geographicZoneId).keySet();
          break;
        } else {
          for (Map.Entry<UUID, Map<UUID, Set<UUID>>> zone : country.getValue().entrySet()) {
            Set<UUID> regions = zone.getValue().keySet();
            if (regions.contains(geographicZoneId)) {
              subZones = zone.getValue().get(geographicZoneId);
              break;
            } else {
              for (Map.Entry<UUID, Set<UUID>> region : zone.getValue().entrySet()) {
                Set<UUID> districts = region.getValue();
                if (districts.contains(geographicZoneId)) {
                  isDistrictLevel = true;
                  break;
                }
              }
            }
          }
        }
      }
    }

    return createProductsCostData(isDistrictLevel, geographicZoneId, subZones,
        bottomUpQuantificationList);
  }


  /**
   * Retrieves bottom-up quantifications that are ready for final approval based on the specified
   * processing period, program, and user permissions.
   *
   * @param programId          The UUID of the program for which quantifications are retrieved.
   * @param processingPeriodId The UUID of the processing period.
   * @param geographicZoneId   The UUID of the geographic zone.
   * @param pageable           object used to encapsulate the pagination related values: page,
   *     size and sort.
   * @return A page of {@link BottomUpQuantification} objects representing quantifications ready
   *     for final approval.
   */
  public Page<BottomUpQuantification> getBottomUpQuantificationsForFinalApproval(
      UUID programId,
      UUID processingPeriodId,
      UUID geographicZoneId,
      Pageable pageable) {
    List<String> allowedRightNames = new ArrayList<>();
    allowedRightNames.add(MOH_APPROVAL_RIGHT_NAME);
    allowedRightNames.add(PORALG_APPROVAL_RIGHT_NAME);
    List<RightDto> rights = new ArrayList<>();
    allowedRightNames.forEach(right -> {
      RightDto rightDto = rightReferenceDataService.findRight(right);
      if (rightDto != null) {
        rights.add(rightDto);
      }
    });
    UserDto user = authenticationHelper.getCurrentUser();
    List<List<DetailedRoleAssignmentDto>> roleAssignments = new ArrayList<>();
    rights.forEach(right -> {
      List<DetailedRoleAssignmentDto> roleAssignment =
          userRoleAssignmentsReferenceDataService
              .hasRight(user, right);
      if (!roleAssignment.isEmpty()) {
        roleAssignments.add(roleAssignment);
      }
    });

    if (CollectionUtils.isEmpty(roleAssignments)) {
      return Pagination.getPage(Collections.emptyList(), pageable);
    }

    Set<Pair<UUID, UUID>> programNodePairs = roleAssignments
        .stream()
        .map(role ->
            role
                .stream()
                .filter(item -> Objects.nonNull(item.getRole().getId()))
                .filter(item -> Objects.nonNull(item.getSupervisoryNodeId()))
                .filter(item -> Objects.nonNull(item.getProgramId()))
                .filter(item -> null == programId || programId.equals(item.getProgramId()))
                .map(item -> new ImmutablePair<>(item.getProgramId(), item.getSupervisoryNodeId()))
                .collect(toSet())
        )
        .flatMap(Set::stream)
        .collect(toSet());

    Page<BottomUpQuantification> bottomUpQuantifications = bottomUpQuantificationRepository
        .searchForFinalApproval(processingPeriodId, programNodePairs, pageable);

    if (geographicZoneId != null) {
      List<BottomUpQuantification> bottomUpQuantificationsFilteredByZone =
          bottomUpQuantifications.getContent()
              .stream()
              .filter(buq -> {
                FacilityDto facility = findFacility(buq.getFacilityId());
                GeographicZoneDto geographicZoneDto = facility.getGeographicZone();

                return isZoneInHierarchy(geographicZoneId, geographicZoneDto);
              })
              .collect(Collectors.toList());

      bottomUpQuantifications = Pagination.getPage(bottomUpQuantificationsFilteredByZone,
          bottomUpQuantifications.getPageable(), bottomUpQuantifications.getTotalElements());
    }

    return bottomUpQuantifications;
  }

  public Page<BottomUpQuantification> getBottomUpQuantificationsForFinalApproval(
      UUID programId,
      UUID processingPeriodId,
      Pageable pageable) {
    return getBottomUpQuantificationsForFinalApproval(programId, processingPeriodId, null,
        pageable);
  }

  /**
   * Retrieves bottom-up quantifications that are ready for final approval
   * based on the specified processing period, program, geographic zone,
   * and user permissions, along with group costs data.
   *
   * @param programId          The UUID of the program for which quantifications are retrieved.
   * @param processingPeriodId The UUID of the processing period.
   * @param geographicZoneId   The UUID of the geographic zone.
   * @param pageable           object used to encapsulate the pagination related values: page,
   *     size and sort.
   * @return A page of {@link BottomUpQuantification} objects representing quantifications ready
   *     for final approval.
   */
  public List<BottomUpQuantificationGroupCostsData>
      getBottomUpQuantificationsForFinalApprovalWithGroupCosts(
      UUID programId,
      UUID processingPeriodId,
      UUID geographicZoneId,
      Pageable pageable) {
    Page<BottomUpQuantification> bottomUpQuantifications =
        getBottomUpQuantificationsForFinalApproval(programId, processingPeriodId,
            geographicZoneId, pageable);
    List<ProductGroup> productGroups = productGroupRepository.findAll();

    return bottomUpQuantifications.stream()
        .filter(buq -> {
          FacilityDto facility = facilityReferenceDataService.findOne(buq.getFacilityId());
          return checkFacilityTypeAndPermission(facility);
        })
        .map(buq -> buildBottomUpQuantificationGroupCostsData(buq, productGroups))
        .collect(Collectors.toList());
  }

  private BottomUpQuantificationGroupCostsData buildBottomUpQuantificationGroupCostsData(
      BottomUpQuantification bottomUpQuantification, List<ProductGroup> productGroups) {
    BottomUpQuantificationGroupCostsData bottomUpQuantificationGroupCostsData =
        new BottomUpQuantificationGroupCostsData();
    bottomUpQuantificationGroupCostsData.setBottomUpQuantification(
        bottomUpQuantificationDtoBuilder.buildDto(bottomUpQuantification));
    bottomUpQuantificationGroupCostsData.setCalculatedGroupsCosts(
        calculateProductGroupsCost(Collections.singletonList(bottomUpQuantification),
            productGroups)
    );
    return bottomUpQuantificationGroupCostsData;
  }

  private boolean isZoneInHierarchy(UUID targetZoneId, GeographicZoneDto zone) {
    if (zone == null) {
      return false;
    }
    if (zone.getId().equals(targetZoneId)) {
      return true;
    }
    return isZoneInHierarchy(targetZoneId, zone.getParent());
  }

  private List<ProductGroupsCostData> createProductsCostData(boolean isDistrictLevel,
      UUID geographicZoneId, Set<UUID> subZones,
      List<BottomUpQuantification> bottomUpQuantificationList) {
    List<ProductGroupsCostData> productsCostsList = new ArrayList<>();
    List<ProductGroup> productGroups = productGroupRepository.findAll();

    if (isDistrictLevel) {
      List<BottomUpQuantification> bottomUpQuantificationsForCalculations =
          bottomUpQuantificationList
          .stream()
          .filter(buq -> {
            FacilityDto facility = facilityReferenceDataService.findOne(buq.getFacilityId());
            return checkFacilityTypeAndPermission(facility)
                && isGeographicZoneInHierarchy(facility.getGeographicZone(), geographicZoneId);
          })
          .collect(Collectors.toList());

      for (BottomUpQuantification buq : bottomUpQuantificationsForCalculations) {
        ProductGroupsCostData productsCosts = new ProductGroupsCostData();
        productsCosts.setDataSourceId(buq.getFacilityId());

        Map<String, String> calculatedGroups =
            calculateProductGroupsCost(Collections.singletonList(buq), productGroups);
        productsCosts.setCalculatedGroupsCosts(calculatedGroups);
        productsCosts.setBottomUpQuantificationIds(Collections.singletonList(buq.getId()));
        productsCosts.setDataSourceFacility(true);
        productsCostsList.add(productsCosts);
      }
    } else {
      for (UUID locationId : subZones) {
        Set<String> facilityTypes = new HashSet<>();

        List<BottomUpQuantification> bottomUpQuantificationForZone = bottomUpQuantificationList
            .stream()
            .filter(buq -> {
              FacilityDto facility = facilityReferenceDataService.findOne(buq.getFacilityId());
              if (checkFacilityTypeAndPermission(facility)) {
                boolean isInZone =
                    isGeographicZoneInHierarchy(facility.getGeographicZone(), locationId);
                if (isInZone) {
                  facilityTypes.add(facility.getType().getName());
                }
                return isInZone;
              }
              return false;
            })
            .collect(Collectors.toList());

        for (String facilityType : facilityTypes) {
          List<BottomUpQuantification> bottomUpQuantificationsForCalculations =
              bottomUpQuantificationForZone.stream()
                  .filter(buq -> {
                    FacilityDto facility =
                        facilityReferenceDataService.findOne(buq.getFacilityId());
                    return facility.getType().getName().equals(facilityType);
                  })
                  .collect(Collectors.toList());

          ProductGroupsCostData productsCosts = new ProductGroupsCostData();
          productsCosts.setFacilityType(facilityType);
          productsCosts.setDataSourceId(locationId);
          Map<String, String> calculatedGroups =
              calculateProductGroupsCost(bottomUpQuantificationsForCalculations, productGroups);
          productsCosts.setCalculatedGroupsCosts(calculatedGroups);

          List<UUID> bottomUpQuantificationsForCostCalculationIds =
              bottomUpQuantificationsForCalculations.stream()
                  .map(BottomUpQuantification::getId)
                  .collect(Collectors.toList());
          productsCosts.setBottomUpQuantificationIds(bottomUpQuantificationsForCostCalculationIds);
          productsCostsList.add(productsCosts);
        }
      }
    }

    return productsCostsList;
  }

  private boolean isGeographicZoneInHierarchy(GeographicZoneDto geographicZone,
      UUID geographicZoneId) {
    if (geographicZone == null) {
      return false;
    }

    if (geographicZone.getId().equals(geographicZoneId)) {
      return true;
    }

    if (geographicZone.getParent() != null) {
      return isGeographicZoneInHierarchy(geographicZone.getParent(), geographicZoneId);
    }

    return false;
  }

  private boolean checkFacilityTypeAndPermission(FacilityDto facility) {
    UserDto user = authenticationHelper.getCurrentUser();
    RightDto mohRight = rightReferenceDataService.findRight(MOH_APPROVAL_RIGHT_NAME);
    ResultDto<Boolean> hasMohRight = new ResultDto<>();
    if (mohRight != null) {
      hasMohRight = userReferenceDataService
          .hasRight(user.getId(), mohRight.getId(), null, null, null);
    } else {
      hasMohRight.setResult(false);
    }

    RightDto poralgRight = rightReferenceDataService.findRight(PORALG_APPROVAL_RIGHT_NAME);
    ResultDto<Boolean> hasPoralgRight = new ResultDto<>();
    if (poralgRight != null) {
      hasPoralgRight = userReferenceDataService
          .hasRight(user.getId(), poralgRight.getId(), null, null, null);
    } else {
      hasPoralgRight.setResult(false);
    }

    if (hasMohRight.getResult() && hasPoralgRight.getResult()) {
      return true;
    } else if (Boolean.TRUE.equals(hasPoralgRight.getResult())) {
      return facility.getType().isPrimaryHealthCare();
    } else if (Boolean.TRUE.equals(hasMohRight.getResult())) {
      return !facility.getType().isPrimaryHealthCare();
    }

    return false;
  }

  private Map<String, String> calculateProductGroupsCost(
      List<BottomUpQuantification> bottomUpQuantifications,
      List<ProductGroup> productGroups) {
    Map<String, Money> groupsCalculations = new HashMap<>();
    List<String> productGroupCodes = new ArrayList<>();
    Map<String, String> productGroupsCodeNameMap = new HashMap<>();

    for (ProductGroup group : productGroups) {
      productGroupsCodeNameMap.put(group.getCode(), group.getName());
      groupsCalculations.put(group.getName(), Money.of(CurrencyUnit.of(currencyCode), 0.00));
      productGroupCodes.add(group.getCode());
    }

    for (BottomUpQuantification buq : bottomUpQuantifications) {
      List<BottomUpQuantificationLineItem> lineItems = buq.getBottomUpQuantificationLineItems();

      List<UUID> orderableIds = lineItems.stream()
          .map(BottomUpQuantificationLineItem::getOrderableId)
          .collect(Collectors.toList());
      List<BasicOrderableDto> orderableDtos = new ArrayList<>();
      if (!orderableIds.isEmpty()) {
        orderableDtos = findOrderables(orderableIds);
      }
      Map<UUID, BasicOrderableDto> orderablesMap = new HashMap<>();
      orderableDtos.forEach(orderable ->
          orderablesMap.put(orderable.getId(), orderable));

      for (BottomUpQuantificationLineItem lineItem : lineItems) {
        BasicOrderableDto orderable = orderablesMap.get(lineItem.getOrderableId());
        String orderableCodeSuffix = orderable.getProductCode().substring(0, 2);
        if (productGroupCodes.contains(orderableCodeSuffix)) {
          Money currentValue = groupsCalculations.get(
              productGroupsCodeNameMap.get(orderableCodeSuffix));
          groupsCalculations.put(productGroupsCodeNameMap.get(orderableCodeSuffix),
              currentValue.plus(lineItem.getTotalCost()));
        } else {
          Money currentValue = groupsCalculations.get(
              productGroupsCodeNameMap.get(null));
          groupsCalculations.put(productGroupsCodeNameMap.get(null),
              currentValue.plus(lineItem.getTotalCost()));
        }
      }
    }

    return groupsCalculations.entrySet().stream()
        .collect(Collectors.toMap(Map.Entry::getKey, entry -> {
          return entry.getValue().getAmount() + " " + entry.getValue().getCurrencyUnit().getCode();
        }));
  }

  private Map<String, Message> getErrors(BindingResult bindingResult) {
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
    bottomUpQuantification.getStatusChanges().add(
            BottomUpQuantificationStatusChange.newInstance(
                    bottomUpQuantification,
                    authenticationHelper.getCurrentUser().getId(),
                    bottomUpQuantification.getStatus())
    );
    bottomUpQuantification.setModifiedDate(ZonedDateTime.now());

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
    BottomUpQuantificationStatusChange statusChange =
            BottomUpQuantificationStatusChange.newInstance(
                    bottomUpQuantification,
                    authenticationHelper.getCurrentUser().getId(),
                    bottomUpQuantification.getStatus());
    BottomUpQuantificationStatusChange persistedStatusChange =
            bottomUpQuantificationStatusChangeRepository.save(statusChange);

    bottomUpQuantification.getStatusChanges().add(persistedStatusChange);
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
    List<BottomUpQuantificationLineItemDto> buqDtoLineItems =
            bottomUpQuantificationDto.getBottomUpQuantificationLineItems();
    if (!buqDtoLineItems.isEmpty()) {
      List<UUID> orderableIds = new ArrayList<>();
      bottomUpQuantificationDto
              .getBottomUpQuantificationLineItems()
              .forEach(lineItemDto ->
                      orderableIds.add(lineItemDto.getOrderableId()));
      List<BasicOrderableDto> orderableDtos = findOrderables(orderableIds);
      if (orderableDtos.size() != orderableIds.size()) {
        throw new ContentNotFoundMessageException(ERROR_ORDERABLE_NOT_FOUND);
      }
    }
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

      List<BottomUpQuantificationSourceOfFund> updatedSourcesOfFunds = bottomUpQuantificationDto
          .getFundingDetails().getSourcesOfFunds()
          .stream()
          .map(sourceOfFundsDto -> {
            BottomUpQuantificationSourceOfFund sourceOfFunds = BottomUpQuantificationSourceOfFund
                    .newInstance(sourceOfFundsDto);
            sourceOfFunds.setFundingDetails(fundingDetails);
            sourceOfFunds.setId(sourceOfFundsDto.getId());
            if (sourceOfFundsDto.getSourceOfFund() != null) {
              SourceOfFund source = findSourceOfFunds(sourceOfFundsDto.getSourceOfFund().getId());
              sourceOfFunds.setSourceOfFund(source);
            }

            return sourceOfFunds;
          })
          .collect(Collectors.toList());

      fundingDetails.getSourcesOfFunds().clear();
      List<BottomUpQuantificationSourceOfFund> persistedSourcesOfFunds =
              bottomUpQuantificationSourceOfFundRepository.saveAll(updatedSourcesOfFunds);
      fundingDetails.getSourcesOfFunds().addAll(persistedSourcesOfFunds);
      BottomUpQuantificationFundingDetails persistedFundingDetails =
              bottomUpQuantificationFundingDetailsRepository.save(fundingDetails);
      bottomUpQuantificationToUpdate.setFundingDetails(persistedFundingDetails);
    }

    bottomUpQuantificationToUpdate.updateFrom(updatedLineItems);
    bottomUpQuantificationLineItemRepository
            .saveAll(bottomUpQuantificationToUpdate.getBottomUpQuantificationLineItems());
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

  private List<BasicOrderableDto> findOrderables(List<UUID> orderableIds) {
    RequestParameters requestParameters = RequestParameters
            .init()
            .set("id", orderableIds);
    return orderableReferenceDataService.findAll(requestParameters);
  }

  private <R> R findResource(UUID id, Function<UUID, R> finder, String errorMessage) {
    return Optional
        .ofNullable(finder.apply(id))
        .orElseThrow(() -> new ContentNotFoundMessageException(errorMessage, id)
        );
  }

  private SourceOfFund findSourceOfFunds(UUID sourceOfFundsId) {
    return sourceOfFundRepository
        .findById(sourceOfFundsId)
        .orElseThrow(() -> new ContentNotFoundMessageException(
            ERROR_SOURCE_OF_FUND_NOT_FOUND, sourceOfFundsId)
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

  private void checkFacilityPermission(UUID bottomUpQuantificationFacilityId) {
    if (!authenticationHelper.getCurrentUser().getHomeFacilityId()
        .equals(bottomUpQuantificationFacilityId)) {
      throw new ValidationMessageException(new Message(
          MessageKeys.ERROR_USER_HOME_FACILITY_AND_BUQ_FACILITY_MISMATCH));
    }
  }

  /**
   * Changes the status of bottomUpQuantification.
   *
   * @param bottomUpQuantification entity of bottomUpQuantification
   * @param status status to be applied to bottomUpQuantification
   */
  private BottomUpQuantification changeStatus(
      BottomUpQuantification bottomUpQuantification,
      BottomUpQuantificationStatus status) {
    bottomUpQuantification.setStatus(status);
    BottomUpQuantificationStatusChange statusChange =
            BottomUpQuantificationStatusChange.newInstance(
                    bottomUpQuantification,
                    authenticationHelper.getCurrentUser().getId(),
                    bottomUpQuantification.getStatus());
    BottomUpQuantificationStatusChange persistedStatusChange =
            bottomUpQuantificationStatusChangeRepository.save(statusChange);

    bottomUpQuantification.getStatusChanges().add(persistedStatusChange);
    bottomUpQuantification.setModifiedDate(ZonedDateTime.now());
    return bottomUpQuantification;
  }

  private MinimalFacilityDto getFacility(List<MinimalFacilityDto> facilities, UUID id) {
    return facilities.stream()
        .filter(f -> f.getId().equals(id))
        .findFirst()
        .orElseThrow(() -> new ContentNotFoundMessageException(
            ERROR_FACILITY_NOT_FOUND, id)
        );
  }

}
