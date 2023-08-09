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

package org.openlmis.buq.util;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Collections;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openlmis.buq.builder.FacilityDtoDataBuilder;
import org.openlmis.buq.builder.SupportedProgramDtoDataBuilder;
import org.openlmis.buq.dto.referencedata.FacilityDto;
import org.openlmis.buq.dto.referencedata.SupportedProgramDto;
import org.openlmis.buq.exception.ValidationMessageException;

@RunWith(MockitoJUnitRunner.class)
public class FacilitySupportsProgramHelperTest {

  private static final LocalDate SUPPORT_START_DATE = LocalDate.of(2011, 12, 12);

  @Mock
  DateHelper dateHelper;

  @InjectMocks
  FacilitySupportsProgramHelper facilitySupportsProgramHelper;

  private FacilityDto facilityDto;
  private UUID programId = UUID.randomUUID();

  @Before
  public void setUp() throws Exception {
    facilityDto = new FacilityDtoDataBuilder().buildAsDto();

    when(dateHelper.isDateBeforeNow(nullable(LocalDate.class))).thenReturn(true);
  }

  @Test(expected = ValidationMessageException.class)
  public void shouldThrowExceptionWhenNoSupportedPrograms() {
    facilityDto.setSupportedPrograms(Collections.emptyList());

    facilitySupportsProgramHelper.checkIfFacilitySupportsProgram(facilityDto, programId);
  }

  @Test(expected = ValidationMessageException.class)
  public void shouldThrowExceptionWhenProgramIsNotSupported() {
    SupportedProgramDto supportedProgramDto = new SupportedProgramDtoDataBuilder()
        .withId(UUID.randomUUID())
        .withSupportActive(true)
        .withProgramActive(true)
        .withSupportStartDate(SUPPORT_START_DATE)
        .buildAsDto();

    facilityDto.setSupportedPrograms(Collections.singletonList(supportedProgramDto));

    facilitySupportsProgramHelper.checkIfFacilitySupportsProgram(facilityDto, programId);
  }

  @Test
  public void shouldPassWhenProgramIsSupported() {
    SupportedProgramDto supportedProgramDto = new SupportedProgramDtoDataBuilder()
        .withId(programId)
        .withSupportActive(true)
        .withProgramActive(true)
        .withSupportStartDate(SUPPORT_START_DATE)
        .buildAsDto();

    facilityDto.setSupportedPrograms(Collections.singletonList(supportedProgramDto));

    facilitySupportsProgramHelper.checkIfFacilitySupportsProgram(facilityDto, programId);
  }

  @Test(expected = ValidationMessageException.class)
  public void shouldThrowExceptionWhenSupportIsNotActive() {
    SupportedProgramDto supportedProgramDto = new SupportedProgramDtoDataBuilder()
        .withId(programId)
        .withSupportActive(false)
        .withProgramActive(true)
        .withSupportStartDate(SUPPORT_START_DATE)
        .buildAsDto();

    facilityDto.setSupportedPrograms(Collections.singletonList(supportedProgramDto));

    facilitySupportsProgramHelper.checkIfFacilitySupportsProgram(facilityDto, programId);
  }

  @Test(expected = ValidationMessageException.class)
  public void shouldThrowExceptionWhenProgramIsNotActive() {
    SupportedProgramDto supportedProgramDto = new SupportedProgramDtoDataBuilder()
        .withId(programId)
        .withSupportActive(true)
        .withProgramActive(false)
        .withSupportStartDate(SUPPORT_START_DATE)
        .buildAsDto();

    facilityDto.setSupportedPrograms(Collections.singletonList(supportedProgramDto));

    facilitySupportsProgramHelper.checkIfFacilitySupportsProgram(facilityDto, programId);
  }

  @Test(expected = ValidationMessageException.class)
  public void shouldThrowExceptionWhenStartDateIsAfterCurrentDate() {
    SupportedProgramDto supportedProgramDto = new SupportedProgramDtoDataBuilder()
        .withId(programId)
        .withSupportActive(true)
        .withProgramActive(true)
        .buildAsDto();
    when(dateHelper.isDateBeforeNow(any(LocalDate.class))).thenReturn(false);

    facilityDto.setSupportedPrograms(Collections.singletonList(supportedProgramDto));

    facilitySupportsProgramHelper.checkIfFacilitySupportsProgram(facilityDto, programId);
  }

  @Test
  public void shouldPassWhenProgramStartDateIsNull() {
    SupportedProgramDto supportedProgramDto = new SupportedProgramDtoDataBuilder()
        .withId(programId)
        .withSupportActive(true)
        .withProgramActive(true)
        .withSupportStartDate(null)
        .buildAsDto();

    facilityDto.setSupportedPrograms(Collections.singletonList(supportedProgramDto));

    facilitySupportsProgramHelper.checkIfFacilitySupportsProgram(facilityDto, programId);
  }


}
