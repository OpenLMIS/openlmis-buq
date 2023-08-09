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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.openlmis.buq.builder.FacilityDtoDataBuilder;
import org.openlmis.buq.builder.ProcessingPeriodDtoDataBuilder;
import org.openlmis.buq.builder.ProgramDtoDataBuilder;
import org.openlmis.buq.domain.buq.BottomUpQuantification;
import org.openlmis.buq.domain.buq.BottomUpQuantificationStatus;
import org.openlmis.buq.dto.buq.BottomUpQuantificationDto;
import org.openlmis.buq.dto.referencedata.ApprovedProductDto;
import org.openlmis.buq.dto.referencedata.BasicOrderableDto;
import org.openlmis.buq.dto.referencedata.FacilityDto;
import org.openlmis.buq.dto.referencedata.ProcessingPeriodDto;
import org.openlmis.buq.dto.referencedata.ProgramDto;
import org.openlmis.buq.dto.referencedata.SupportedProgramDto;
import org.openlmis.buq.dto.referencedata.UserDto;
import org.openlmis.buq.exception.ValidationMessageException;
import org.openlmis.buq.repository.buq.BottomUpQuantificationRepository;
import org.openlmis.buq.service.referencedata.ApprovedProductReferenceDataService;
import org.openlmis.buq.service.referencedata.FacilityReferenceDataService;
import org.openlmis.buq.service.referencedata.PeriodReferenceDataService;
import org.openlmis.buq.service.referencedata.ProgramReferenceDataService;
import org.openlmis.buq.util.AuthenticationHelper;
import org.openlmis.buq.util.FacilitySupportsProgramHelper;

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
  private ApprovedProductReferenceDataService approvedProductReferenceDataService;

  @Mock
  private BottomUpQuantificationRepository bottomUpQuantificationRepository;

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

    ApprovedProductDto approvedProductDto = new ApprovedProductDto();
    approvedProductDto.setOrderable(orderableDto);
    final List<ApprovedProductDto> approvedProductDtos = Collections
        .singletonList(approvedProductDto);

    UserDto userDto = new UserDto();
    userDto.setId(UUID.randomUUID());

    when(facilityReferenceDataService.findOne(facilityId)).thenReturn(facilityDto);
    when(programReferenceDataService.findOne(programId)).thenReturn(programDto);
    when(periodReferenceDataService.findOne(processingPeriodId)).thenReturn(processingPeriodDto);
    when(approvedProductReferenceDataService.getApprovedProducts(any(), any()))
        .thenReturn(approvedProductDtos);
    when(authenticationHelper.getCurrentUser()).thenReturn(userDto);
    doNothing().when(facilitySupportsProgramHelper).checkIfFacilitySupportsProgram(facilityDto,
        programId);
    when(bottomUpQuantificationRepository.save(any())).thenReturn(new BottomUpQuantification());

    BottomUpQuantification result = bottomUpQuantificationService.prepare(facilityId, programId,
        processingPeriodId);

    assertNotNull(result);
    assertEquals(facilityDto.getId(), result.getFacilityId());
    assertEquals(programDto.getId(), result.getProgramId());
    assertEquals(processingPeriodDto.getId(), result.getProcessingPeriodId());
    assertEquals(targetYear, result.getTargetYear());
    assertEquals(BottomUpQuantificationStatus.DRAFT, result.getStatus());
    assertNotNull(result.getBottomUpQuantificationLineItems());
    assertEquals(approvedProductDtos.size(), result.getBottomUpQuantificationLineItems().size());

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

    BottomUpQuantification bottomUpQuantification = new BottomUpQuantification();
    bottomUpQuantification.setBottomUpQuantificationLineItems(Collections.emptyList());
    when(bottomUpQuantificationRepository.findById(bottomUpQuantificationId))
        .thenReturn(Optional.of(bottomUpQuantification));

    when(bottomUpQuantificationRepository.save(any())).thenReturn(bottomUpQuantification);

    BottomUpQuantification result = bottomUpQuantificationService.save(bottomUpQuantificationDto,
        bottomUpQuantificationId);

    assertNotNull(result);
    assertEquals(bottomUpQuantification, result);
  }

  @Test(expected = ValidationMessageException.class)
  public void shouldNotSaveBottomUpQuantificationWithInvalidId() {
    UUID bottomUpQuantificationId = UUID.randomUUID();
    BottomUpQuantificationDto bottomUpQuantificationDto = new BottomUpQuantificationDto();
    bottomUpQuantificationDto.setId(UUID.randomUUID());

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

}