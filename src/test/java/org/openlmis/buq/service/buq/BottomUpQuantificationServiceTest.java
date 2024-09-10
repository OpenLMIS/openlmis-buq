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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.openlmis.buq.service.buq.BottomUpQuantificationService.APPROVE_BUQ_RIGHT_NAME;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.openlmis.buq.ApproveFacilityForecastingStats;
import org.openlmis.buq.builder.BottomUpQuantificationDataBuilder;
import org.openlmis.buq.builder.BottomUpQuantificationLineItemDataBuilder;
import org.openlmis.buq.builder.FacilityDtoDataBuilder;
import org.openlmis.buq.builder.ProcessingPeriodDtoDataBuilder;
import org.openlmis.buq.builder.ProgramDtoDataBuilder;
import org.openlmis.buq.builder.UserDtoDataBuilder;
import org.openlmis.buq.domain.BaseEntity;
import org.openlmis.buq.domain.buq.BottomUpQuantification;
import org.openlmis.buq.domain.buq.BottomUpQuantificationLineItem;
import org.openlmis.buq.domain.buq.BottomUpQuantificationStatus;
import org.openlmis.buq.domain.buq.BottomUpQuantificationStatusChange;
import org.openlmis.buq.domain.buq.Rejection;
import org.openlmis.buq.dto.buq.BottomUpQuantificationDto;
import org.openlmis.buq.dto.buq.BottomUpQuantificationLineItemDto;
import org.openlmis.buq.dto.buq.RejectionDto;
import org.openlmis.buq.dto.csv.BottomUpQuantificationLineItemCsv;
import org.openlmis.buq.dto.referencedata.BasicOrderableDto;
import org.openlmis.buq.dto.referencedata.FacilityDto;
import org.openlmis.buq.dto.referencedata.ProcessingPeriodDto;
import org.openlmis.buq.dto.referencedata.ProgramDto;
import org.openlmis.buq.dto.referencedata.SupervisoryNodeDto;
import org.openlmis.buq.dto.referencedata.SupportedProgramDto;
import org.openlmis.buq.dto.referencedata.UserDto;
import org.openlmis.buq.dto.requisition.RequisitionLineItemDataProjection;
import org.openlmis.buq.exception.ContentNotFoundMessageException;
import org.openlmis.buq.exception.NotFoundException;
import org.openlmis.buq.exception.ValidationMessageException;
import org.openlmis.buq.repository.buq.BottomUpQuantificationLineItemRepository;
import org.openlmis.buq.repository.buq.BottomUpQuantificationRepository;
import org.openlmis.buq.repository.buq.BottomUpQuantificationStatusChangeRepository;
import org.openlmis.buq.service.CsvService;
import org.openlmis.buq.service.RequestParameters;
import org.openlmis.buq.service.referencedata.FacilityReferenceDataService;
import org.openlmis.buq.service.referencedata.OrderableReferenceDataService;
import org.openlmis.buq.service.referencedata.PeriodReferenceDataService;
import org.openlmis.buq.service.referencedata.ProgramReferenceDataService;
import org.openlmis.buq.service.referencedata.SupervisoryNodeReferenceDataService;
import org.openlmis.buq.service.referencedata.SupplyLineReferenceDataService;
import org.openlmis.buq.service.referencedata.UserReferenceDataService;
import org.openlmis.buq.service.remark.RemarkService;
import org.openlmis.buq.util.AuthenticationHelper;
import org.openlmis.buq.util.FacilitySupportsProgramHelper;
import org.openlmis.buq.validate.BottomUpQuantificationValidator;
import org.springframework.validation.Errors;

@SuppressWarnings("PMD.TooManyMethods")
@RunWith(MockitoJUnitRunner.class)
public class BottomUpQuantificationServiceTest {

  @InjectMocks
  private BottomUpQuantificationService bottomUpQuantificationService;

  @Mock
  private AuthenticationHelper authenticationHelper;

  @Mock
  private FacilityReferenceDataService facilityReferenceDataService;

  @Mock
  private FacilitySupportsProgramHelper facilitySupportsProgramHelper;

  @Mock
  private ProgramReferenceDataService programReferenceDataService;

  @Mock
  private PeriodReferenceDataService periodReferenceDataService;

  @Mock
  private BottomUpQuantificationRepository bottomUpQuantificationRepository;

  @Mock
  private OrderableReferenceDataService orderableReferenceDataService;

  @Mock
  private BottomUpQuantificationStatusChangeRepository bottomUpQuantificationStatusChangeRepository;

  @Mock
  private RejectionService rejectionService;

  @Mock
  private CsvService csvService;

  @Mock
  private BottomUpQuantificationValidator validator;

  @Mock
  private RemarkService remarkService;

  @Mock
  private UserReferenceDataService userReferenceDataService;

  @Mock
  private SupervisoryNodeReferenceDataService supervisoryNodeReferenceDataService;

  @Mock
  private SupplyLineReferenceDataService supplyLineReferenceDataService;

  @Mock
  private BottomUpQuantificationLineItemRepository bottomUpQuantificationLineItemRepository;

  public UUID facilityId = UUID.randomUUID();
  public UUID programId = UUID.randomUUID();
  public UUID processingPeriodId = UUID.randomUUID();

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void shouldPrepareBottomUpQuantificationWithValidData() {
    final FacilityDto facilityDto = new FacilityDtoDataBuilder().withId(facilityId).buildAsDto();
    final ProgramDto programDto = new ProgramDtoDataBuilder().withId(programId).buildAsDto();
    final ProcessingPeriodDto processingPeriodDto = new ProcessingPeriodDtoDataBuilder()
        .withId(processingPeriodId).buildAsDto();
    final Integer targetYear = processingPeriodDto.getEndDate().getYear();

    BasicOrderableDto orderableDto = new BasicOrderableDto();
    orderableDto.setId(UUID.randomUUID());

    RequisitionLineItemDataProjection reqItemData = createRequisitionLineItem(
        orderableDto.getId().toString(),
        100,
        10,
        2,
        false
    );
    final Integer requisitionAdjustedConsumptionInPacks = 10;
    final List<RequisitionLineItemDataProjection> reqLineItemsData = Collections
        .singletonList(reqItemData);

    UserDto userDto = new UserDto();
    userDto.setId(UUID.randomUUID());

    when(facilityReferenceDataService.findOne(facilityId)).thenReturn(facilityDto);
    when(programReferenceDataService.findOne(programId)).thenReturn(programDto);
    when(periodReferenceDataService.findOne(processingPeriodId)).thenReturn(processingPeriodDto);
    when(bottomUpQuantificationRepository.getRequisitionLineItemsData(
        any(UUID.class), any(UUID.class))).thenReturn(reqLineItemsData);
    when(authenticationHelper.getCurrentUser()).thenReturn(userDto);
    doNothing().when(facilitySupportsProgramHelper).checkIfFacilitySupportsProgram(facilityDto,
        programId);
    doNothing().when(validator).validate(any(), any(Errors.class));
    when(bottomUpQuantificationRepository.save(any())).thenReturn(new BottomUpQuantification());

    BottomUpQuantification result = bottomUpQuantificationService.prepare(facilityId, programId,
        processingPeriodId);

    assertNotNull(result);
    assertEquals(facilityDto.getId(), result.getFacilityId());
    assertEquals(programDto.getId(), result.getProgramId());
    assertEquals(processingPeriodDto.getId(), result.getProcessingPeriodId());
    assertEquals(targetYear, result.getTargetYear());
    assertEquals(BottomUpQuantificationStatus.DRAFT, result.getStatus());
    assertNotNull(result.getFundingDetails());

    List<BottomUpQuantificationLineItem> resultLineItems = result
        .getBottomUpQuantificationLineItems();

    assertNotNull(result.getBottomUpQuantificationLineItems());
    assertEquals(reqLineItemsData.size(), resultLineItems.size());

    BottomUpQuantificationLineItem resultLineItem = resultLineItems.get(0);

    assertEquals(resultLineItem.getOrderableId().toString(), reqItemData.getOrderableId());
    assertEquals(resultLineItem.getAnnualAdjustedConsumption(),
        requisitionAdjustedConsumptionInPacks);
  }

  @Test(expected = ValidationMessageException.class)
  public void shouldNotPrepareBottomUpQuantificationWithMissingFacilityId() {
    bottomUpQuantificationService.prepare(null, programId, processingPeriodId);
  }

  @Test(expected = ValidationMessageException.class)
  public void shouldNotPrepareBottomUpQuantificationWithMissingProgramId() {
    bottomUpQuantificationService.prepare(facilityId, null, processingPeriodId);
  }

  @Test(expected = ValidationMessageException.class)
  public void shouldNotPrepareBottomUpQuantificationWithMissingProcessingPeriodId() {
    bottomUpQuantificationService.prepare(facilityId, programId, null);
  }

  @Test
  public void shouldSaveBottomUpQuantificationWithValidData() {
    UUID bottomUpQuantificationId = UUID.randomUUID();
    BottomUpQuantificationDto bottomUpQuantificationDto = new BottomUpQuantificationDto();
    bottomUpQuantificationDto.setId(bottomUpQuantificationId);
    bottomUpQuantificationDto.setFacilityId(UUID.randomUUID());
    bottomUpQuantificationDto.setStatus(BottomUpQuantificationStatus.DRAFT);
    mockUserHomeFacilityPermission(bottomUpQuantificationDto);
    List<BasicOrderableDto> orderables = new ArrayList<>();
    orderables.add(new BasicOrderableDto());
    orderables.add(new BasicOrderableDto());
    when(orderableReferenceDataService
        .findAll(any(RequestParameters.class)))
        .thenReturn(orderables);
    BottomUpQuantificationLineItem lineItem1 =
            new BottomUpQuantificationLineItemDataBuilder().build();
    BottomUpQuantificationLineItem lineItem2 =
            new BottomUpQuantificationLineItemDataBuilder().withRemark(null).build();
    BottomUpQuantificationLineItemDto lineItemDto1 = BottomUpQuantificationLineItemDto
            .newInstance(lineItem1);
    BottomUpQuantificationLineItemDto lineItemDto2 = BottomUpQuantificationLineItemDto
            .newInstance(lineItem2);
    bottomUpQuantificationDto.setBottomUpQuantificationLineItems(
        Arrays.asList(lineItemDto1, lineItemDto2));
    BottomUpQuantification bottomUpQuantification = new BottomUpQuantification();
    bottomUpQuantification.setStatus(BottomUpQuantificationStatus.DRAFT);
    bottomUpQuantification.setBottomUpQuantificationLineItems(new ArrayList<>());
    when(bottomUpQuantificationLineItemRepository.saveAll(any()))
            .thenReturn(new ArrayList<>());
    mockUpdateBottomUpQuantification(bottomUpQuantificationId, bottomUpQuantification);
    BottomUpQuantification result = bottomUpQuantificationService.save(bottomUpQuantificationDto,
        bottomUpQuantificationId);

    assertNotNull(result);
    assertEquals(bottomUpQuantification, result);
  }

  @Test(expected = ValidationMessageException.class)
  public void shouldNotSaveBottomUpQuantificationWithInvalidId() {
    BottomUpQuantificationDto bottomUpQuantificationDto = new BottomUpQuantificationDto();
    bottomUpQuantificationDto.setFacilityId(UUID.randomUUID());
    bottomUpQuantificationDto.setId(UUID.randomUUID());
    mockUserHomeFacilityPermission(bottomUpQuantificationDto);
    UUID bottomUpQuantificationId = UUID.randomUUID();

    bottomUpQuantificationService.save(bottomUpQuantificationDto, bottomUpQuantificationId);
  }

  @Test(expected = NotFoundException.class)
  public void shouldNotSaveBottomUpQuantificationWithInvalidRemarkId() {
    UUID bottomUpQuantificationId = UUID.randomUUID();
    BottomUpQuantificationDto bottomUpQuantificationDto = new BottomUpQuantificationDto();
    bottomUpQuantificationDto.setId(bottomUpQuantificationId);
    bottomUpQuantificationDto.setFacilityId(UUID.randomUUID());
    mockUserHomeFacilityPermission(bottomUpQuantificationDto);
    BottomUpQuantificationLineItem lineItem =
        new BottomUpQuantificationLineItemDataBuilder().build();
    when(remarkService.findOne(lineItem.getRemark().getId()))
        .thenThrow(NotFoundException.class);
    BottomUpQuantificationLineItemDto lineItemDto = BottomUpQuantificationLineItemDto
        .newInstance(lineItem);
    List<BasicOrderableDto> orderables = new ArrayList<>();
    orderables.add(new BasicOrderableDto());
    when(orderableReferenceDataService
            .findAll(any(RequestParameters.class)))
            .thenReturn(orderables);
    bottomUpQuantificationDto.setBottomUpQuantificationLineItems(
        Collections.singletonList(lineItemDto)
    );

    BottomUpQuantification bottomUpQuantification = new BottomUpQuantification();
    bottomUpQuantification.setBottomUpQuantificationLineItems(new ArrayList<>());
    when(bottomUpQuantificationRepository.findById(bottomUpQuantificationId))
        .thenReturn(Optional.of(bottomUpQuantification));

    bottomUpQuantificationService.save(bottomUpQuantificationDto, bottomUpQuantificationId);
  }

  @Test(expected = ContentNotFoundMessageException.class)
  public void shouldNotSaveBottomUpQuantificationWithInvalidOrderableId() {
    UUID bottomUpQuantificationId = UUID.randomUUID();
    BottomUpQuantificationDto bottomUpQuantificationDto = new BottomUpQuantificationDto();
    bottomUpQuantificationDto.setId(bottomUpQuantificationId);
    bottomUpQuantificationDto.setFacilityId(UUID.randomUUID());
    mockUserHomeFacilityPermission(bottomUpQuantificationDto);
    BottomUpQuantificationLineItem lineItem =
        new BottomUpQuantificationLineItemDataBuilder().build();
    final BottomUpQuantificationLineItemDto lineItemDto = BottomUpQuantificationLineItemDto
        .newInstance(lineItem);
    when(orderableReferenceDataService
            .findAll(any(RequestParameters.class)))
            .thenThrow(ContentNotFoundMessageException.class);
    bottomUpQuantificationDto.setBottomUpQuantificationLineItems(
        Collections.singletonList(lineItemDto)
    );

    BottomUpQuantification bottomUpQuantification = new BottomUpQuantification();
    bottomUpQuantification.setBottomUpQuantificationLineItems(new ArrayList<>());
    when(bottomUpQuantificationRepository.findById(bottomUpQuantificationId))
        .thenReturn(Optional.of(bottomUpQuantification));

    bottomUpQuantificationService.save(bottomUpQuantificationDto, bottomUpQuantificationId);
  }

  @Test(expected = ValidationMessageException.class)
  public void shouldNotPrepareBottomUpQuantificationIfFacilityDoesNotSupportProgram() {
    FacilityDto facilityDto = new FacilityDto();
    facilityDto.setId(facilityId);

    List<SupportedProgramDto> supportedPrograms = new ArrayList<>();
    facilityDto.setSupportedPrograms(supportedPrograms);

    ProgramDto programDto = new ProgramDto();
    programDto.setId(programId);

    doThrow(ValidationMessageException.class).when(facilitySupportsProgramHelper)
        .checkIfFacilitySupportsProgram(facilityDto, programId);
    when(facilityReferenceDataService.findOne(facilityId)).thenReturn(facilityDto);
    when(programReferenceDataService.findOne(programId)).thenReturn(programDto);

    bottomUpQuantificationService.prepare(facilityId, programId, processingPeriodId);

    verify(facilitySupportsProgramHelper).checkIfFacilitySupportsProgram(facilityDto, programId);
  }

  @Test
  public void shouldReturnArrayOfBytes() throws IOException {
    BottomUpQuantificationLineItem lineItem = new BottomUpQuantificationLineItemDataBuilder()
        .build();
    BottomUpQuantification bottomUpQuantification = new BottomUpQuantificationDataBuilder()
        .withLineItems(Collections.singletonList(lineItem)).build();
    when(orderableReferenceDataService.findOne(any(UUID.class))).thenReturn(
        new BasicOrderableDto(lineItem.getOrderableId(), "test-code", "test-full-name",
            0, 0, false, null));
    final String resultBytes = "result-bytes";
    when(csvService.generateCsv(any(), eq(BottomUpQuantificationLineItemCsv.class)))
        .thenReturn(resultBytes.getBytes());

    byte[] result = bottomUpQuantificationService.getPreparationFormData(bottomUpQuantification);

    assertArrayEquals(resultBytes.getBytes(), result);
  }

  @Test
  public void shouldAuthorizeBottomUpQuantification() {
    UUID bottomUpQuantificationId = UUID.randomUUID();
    BottomUpQuantification bottomUpQuantification = new BottomUpQuantificationDataBuilder()
        .withId(bottomUpQuantificationId).build();
    BottomUpQuantificationDto bottomUpQuantificationDto = BottomUpQuantificationDto
        .newInstance(bottomUpQuantification);
    mockUserHomeFacilityPermission(bottomUpQuantificationDto);
    doNothing().when(validator).validateCanBeAuthorized(bottomUpQuantificationDto,
        bottomUpQuantificationId);
    mockUpdateBottomUpQuantification(bottomUpQuantificationId, bottomUpQuantification);
    when(bottomUpQuantificationLineItemRepository
            .saveAll(bottomUpQuantification.getBottomUpQuantificationLineItems()))
            .thenReturn(new ArrayList<>());
    BottomUpQuantificationStatusChange statusChange =
            new BottomUpQuantificationStatusChange();
    statusChange.setStatus(BottomUpQuantificationStatus.AUTHORIZED);
    when(bottomUpQuantificationStatusChangeRepository
            .save(any(BottomUpQuantificationStatusChange.class)))
            .thenReturn(statusChange);

    BottomUpQuantification result = bottomUpQuantificationService
        .authorize(bottomUpQuantificationDto, bottomUpQuantificationId);
    List<BottomUpQuantificationStatusChange> resultStatusChanges = result.getStatusChanges();

    assertNotNull(result);
    assertEquals(bottomUpQuantification, result);
    assertEquals(BottomUpQuantificationStatus.AUTHORIZED,
        resultStatusChanges.get(resultStatusChanges.size() - 1).getStatus());
    assertEquals(BottomUpQuantificationStatus.AUTHORIZED, result.getStatus());
  }

  @Test
  public void shouldRejectBottomUpQuantification() {
    UUID bottomUpQuantificationId = UUID.randomUUID();
    BottomUpQuantification bottomUpQuantification = new BottomUpQuantificationDataBuilder()
            .withId(bottomUpQuantificationId).build();
    BottomUpQuantificationDto bottomUpQuantificationDto = BottomUpQuantificationDto
            .newInstance(bottomUpQuantification);
    mockUserHomeFacilityPermission(bottomUpQuantificationDto);
    BottomUpQuantificationStatusChange statusChange = new BottomUpQuantificationStatusChange();
    statusChange.setStatus(BottomUpQuantificationStatus.REJECTED);
    doNothing().when(validator).validateCanBeRejected(bottomUpQuantification);
    when(bottomUpQuantificationStatusChangeRepository.save(any()))
            .thenReturn(statusChange);
    when(rejectionService.save(any())).thenReturn(new Rejection());

    mockUpdateBottomUpQuantification(bottomUpQuantificationId, bottomUpQuantification);

    Rejection rejection = new Rejection();
    RejectionDto rejectionDto = RejectionDto.newInstance(rejection);
    BottomUpQuantification result = bottomUpQuantificationService
            .reject(bottomUpQuantificationId, rejectionDto);
    List<BottomUpQuantificationStatusChange> resultStatusChanges = result.getStatusChanges();

    assertNotNull(result);
    assertEquals(bottomUpQuantification, result);
    assertEquals(BottomUpQuantificationStatus.REJECTED,
            resultStatusChanges.get(resultStatusChanges.size() - 1).getStatus());
    assertEquals(BottomUpQuantificationStatus.REJECTED, result.getStatus());
  }

  @Test(expected = ValidationMessageException.class)
  public void shouldThrowValidationMessageExceptionIfBuqIsInvalidForAuthorize() {
    UUID invalidBottomUpQuantificationId = UUID.randomUUID();
    BottomUpQuantification bottomUpQuantification = new BottomUpQuantificationDataBuilder()
        .withId(invalidBottomUpQuantificationId).build();
    BottomUpQuantificationDto invalidBottomUpQuantificationDto = BottomUpQuantificationDto
        .newInstance(bottomUpQuantification);
    mockUserHomeFacilityPermission(invalidBottomUpQuantificationDto);

    doThrow(ValidationMessageException.class).when(validator)
        .validateCanBeAuthorized(invalidBottomUpQuantificationDto,
            invalidBottomUpQuantificationId);

    bottomUpQuantificationService.authorize(invalidBottomUpQuantificationDto,
        invalidBottomUpQuantificationId);
  }

  @Test(expected = ValidationMessageException.class)
  public void shouldNotAuthorizeBottomUpQuantificationIfSupervisoryNodeNotAssigned() {
    UUID bottomUpQuantificationId = UUID.randomUUID();
    BottomUpQuantification bottomUpQuantification = new BottomUpQuantificationDataBuilder()
        .withId(bottomUpQuantificationId)
        .withSupervisoryNodeId(null)
        .build();
    BottomUpQuantificationDto bottomUpQuantificationDto = BottomUpQuantificationDto
        .newInstance(bottomUpQuantification);
    mockUserHomeFacilityPermission(bottomUpQuantificationDto);
    doNothing().when(validator).validateCanBeAuthorized(bottomUpQuantificationDto,
        bottomUpQuantificationId);
    mockUpdateBottomUpQuantification(bottomUpQuantificationId, bottomUpQuantification);
    when(bottomUpQuantificationLineItemRepository
        .saveAll(bottomUpQuantification.getBottomUpQuantificationLineItems()))
        .thenReturn(new ArrayList<>());
    BottomUpQuantificationStatusChange statusChange =
        new BottomUpQuantificationStatusChange();
    statusChange.setStatus(BottomUpQuantificationStatus.AUTHORIZED);
    when(bottomUpQuantificationStatusChangeRepository
        .save(any(BottomUpQuantificationStatusChange.class)))
        .thenReturn(statusChange);

    bottomUpQuantificationService
        .authorize(bottomUpQuantificationDto, bottomUpQuantificationId);
  }

  @Test
  public void shouldCallDelete() {
    BottomUpQuantification bottomUpQuantification = new BottomUpQuantificationDataBuilder()
        .build();
    mockUserHomeFacilityPermission(BottomUpQuantificationDto.newInstance(bottomUpQuantification));

    List<BottomUpQuantificationStatusChange> statusChanges =
            bottomUpQuantification.getStatusChanges();

    List<UUID> statusChangeIds = statusChanges.stream()
            .filter(status -> status.getStatus().equals(BottomUpQuantificationStatus.REJECTED))
            .map(BaseEntity::getId)
            .collect(Collectors.toList());

    bottomUpQuantificationService.delete(bottomUpQuantification);

    verify(rejectionService).deleteByStatusChangeIdIn(statusChangeIds);
    verify(bottomUpQuantificationRepository).deleteById(bottomUpQuantification.getId());
  }

  @Test(expected = ValidationMessageException.class)
  public void shouldThrowValidationMessageExceptionIfUserHomeFacilityIsNotEqualsToBuqFacility() {
    BottomUpQuantification bottomUpQuantification = new BottomUpQuantificationDataBuilder()
        .build();
    UserDto userDto = new UserDtoDataBuilder().withHomeFacilityId(UUID.randomUUID()).buildAsDto();
    when(authenticationHelper.getCurrentUser()).thenReturn(userDto);

    bottomUpQuantificationService.delete(bottomUpQuantification);
  }

  @Test
  public void shouldApproveBottomUpQuantification() {
    UUID bottomUpQuantificationId = UUID.randomUUID();
    BottomUpQuantification bottomUpQuantification = new BottomUpQuantificationDataBuilder()
        .withId(bottomUpQuantificationId).build();
    BottomUpQuantificationDto bottomUpQuantificationDto = BottomUpQuantificationDto
        .newInstance(bottomUpQuantification);
    bottomUpQuantification.setStatus(BottomUpQuantificationStatus.DRAFT);
    bottomUpQuantificationDto.setStatus(BottomUpQuantificationStatus.DRAFT);
    mockUserHomeFacilityPermission(bottomUpQuantificationDto);
    doNothing().when(validator).validateCanBeApproved(bottomUpQuantificationDto,
        bottomUpQuantificationId);
    BottomUpQuantification buq = new BottomUpQuantification();
    buq.setStatus(BottomUpQuantificationStatus.DRAFT);
    when(bottomUpQuantificationLineItemRepository.saveAll(any()))
            .thenReturn(new ArrayList<>());
    Mockito.lenient().when(supervisoryNodeReferenceDataService
            .findSupervisoryNode(buq.getProgramId(), buq.getFacilityId()))
            .thenReturn(new SupervisoryNodeDto());
    ProcessingPeriodDto processingPeriodDto = new ProcessingPeriodDto();
    processingPeriodDto.setId(any(UUID.class));
    when(periodReferenceDataService.findOne(buq.getProcessingPeriodId()))
            .thenReturn(processingPeriodDto);
    when(supplyLineReferenceDataService.search(any(UUID.class), any(UUID.class)))
            .thenReturn(Collections.emptyList());

    mockUpdateBottomUpQuantification(bottomUpQuantificationId, bottomUpQuantification);

    BottomUpQuantification result = bottomUpQuantificationService
        .approve(bottomUpQuantificationDto, bottomUpQuantificationId);
    List<BottomUpQuantificationStatusChange> resultStatusChanges = result.getStatusChanges();

    assertNotNull(result);
    assertEquals(bottomUpQuantification, result);
    assertEquals(BottomUpQuantificationStatus.APPROVED,
        resultStatusChanges.get(resultStatusChanges.size() - 1).getStatus());
    assertEquals(BottomUpQuantificationStatus.APPROVED, result.getStatus());
  }

  @Test(expected = ValidationMessageException.class)
  public void shouldThrowValidationMessageExceptionIfBuqIsInvalidForApprove() {
    UUID invalidBottomUpQuantificationId = UUID.randomUUID();
    BottomUpQuantification bottomUpQuantification = new BottomUpQuantificationDataBuilder()
        .withId(invalidBottomUpQuantificationId).build();
    BottomUpQuantificationDto invalidBottomUpQuantificationDto = BottomUpQuantificationDto
        .newInstance(bottomUpQuantification);
    mockUserHomeFacilityPermission(invalidBottomUpQuantificationDto);

    doThrow(ValidationMessageException.class).when(validator)
        .validateCanBeApproved(invalidBottomUpQuantificationDto,
            invalidBottomUpQuantificationId);

    bottomUpQuantificationService.approve(invalidBottomUpQuantificationDto,
        invalidBottomUpQuantificationId);
  }

  @Test
  public void shouldReturnApproveFacilityForecastingStatsWithCalculatedValues() {
    UserDto user = new UserDtoDataBuilder().buildAsDto();
    when(authenticationHelper.getCurrentUser()).thenReturn(user);
    List<String> permissionStrings = Arrays.asList(
        APPROVE_BUQ_RIGHT_NAME + "|" + UUID.randomUUID() + "|" + programId,
        APPROVE_BUQ_RIGHT_NAME + "|" + user.getHomeFacilityId() + "|" + programId,
        APPROVE_BUQ_RIGHT_NAME + "|" + user.getHomeFacilityId() + "|" + UUID.randomUUID(),
        APPROVE_BUQ_RIGHT_NAME + "|" + UUID.randomUUID() + "|" + programId,
        "SOME_RIGHT|" + user.getHomeFacilityId() + "|" + programId,
        "SOME_RIGHT|" + user.getHomeFacilityId(),
        APPROVE_BUQ_RIGHT_NAME + "|" + UUID.randomUUID(),
        APPROVE_BUQ_RIGHT_NAME,
        "SOME_RIGHT"
    );
    when(userReferenceDataService.getPermissionStrings(user.getId()))
        .thenReturn(permissionStrings);
    List<BottomUpQuantification> bottomUpQuantifications = Arrays.asList(
        new BottomUpQuantificationDataBuilder()
            .withStatus(BottomUpQuantificationStatus.DRAFT).buildAsNew(),
        new BottomUpQuantificationDataBuilder()
            .withStatus(BottomUpQuantificationStatus.SUBMITTED).buildAsNew()
    );
    when(bottomUpQuantificationRepository.findByFacilityIdIn(any()))
        .thenReturn(bottomUpQuantifications);

    ApproveFacilityForecastingStats result =
        bottomUpQuantificationService.getApproveFacilityForecastingStats(programId);

    assertEquals(3, result.getTotalFacilities());
    assertEquals(1, result.getTotalSubmitted());
    assertEquals(50, result.getPercentageSubmitted());
  }

  @Test
  public void shouldReturnApproveFacilityForecastingStatsWithZeroValues() {
    UserDto user = new UserDtoDataBuilder().buildAsDto();
    when(authenticationHelper.getCurrentUser()).thenReturn(user);
    when(userReferenceDataService.getPermissionStrings(user.getId()))
        .thenReturn(Collections.emptyList());
    when(bottomUpQuantificationRepository.findByFacilityIdIn(any()))
        .thenReturn(Collections.emptyList());

    ApproveFacilityForecastingStats result =
        bottomUpQuantificationService.getApproveFacilityForecastingStats(programId);

    assertEquals(0, result.getTotalFacilities());
    assertEquals(0, result.getTotalSubmitted());
    assertEquals(0, result.getPercentageSubmitted());
  }

  private void mockUpdateBottomUpQuantification(UUID bottomUpQuantificationId,
      BottomUpQuantification bottomUpQuantification) {
    when(bottomUpQuantificationRepository.findById(bottomUpQuantificationId))
        .thenReturn(Optional.of(bottomUpQuantification));
  }

  private RequisitionLineItemDataProjection createRequisitionLineItem(
      String orderableId,
      Integer adjustedConsumption,
      Integer netContent,
      Integer packRoundingThreshold,
      boolean roundToZero) {
    RequisitionLineItemDataProjection lineItem = mock(RequisitionLineItemDataProjection.class);
    when(lineItem.getOrderableId()).thenReturn(orderableId);
    when(lineItem.getAnnualAdjustedConsumption()).thenReturn(adjustedConsumption);
    when(lineItem.getNetContent()).thenReturn(netContent);
    when(lineItem.getPackRoundingThreshold()).thenReturn(packRoundingThreshold);
    when(lineItem.getRoundToZero()).thenReturn(roundToZero);
    return lineItem;
  }

  private void mockUserHomeFacilityPermission(
      BottomUpQuantificationDto bottomUpQuantificationDto) {
    UserDto user = new UserDtoDataBuilder().withHomeFacilityId(
        bottomUpQuantificationDto.getFacilityId()).buildAsDto();
    when(authenticationHelper.getCurrentUser()).thenReturn(user);
  }

}
