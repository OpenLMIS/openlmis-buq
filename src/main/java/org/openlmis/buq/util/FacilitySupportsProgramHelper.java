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

import static org.openlmis.buq.i18n.MessageKeys.ERROR_FACILITY_DOES_NOT_SUPPORT_PROGRAM;

import java.util.List;
import java.util.UUID;
import org.openlmis.buq.dto.referencedata.FacilityDto;
import org.openlmis.buq.dto.referencedata.SupportedProgramDto;
import org.openlmis.buq.exception.ValidationMessageException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class FacilitySupportsProgramHelper {

  @Autowired
  DateHelper dateHelper;

  /**
   * Method check if facility supports program.
   *
   * @param facility Facility.
   * @param programId programId UUID of the Program.
   */
  public void checkIfFacilitySupportsProgram(FacilityDto facility, UUID programId) {
    List<SupportedProgramDto> supportedPrograms = facility.getSupportedPrograms();

    if (!isProgramSupported(supportedPrograms, programId)) {
      throw new ValidationMessageException(
          new Message(ERROR_FACILITY_DOES_NOT_SUPPORT_PROGRAM, facility.getId(), programId));
    }
  }

  private boolean isProgramSupported(List<SupportedProgramDto> supportedPrograms, UUID programId) {
    return supportedPrograms
        .stream()
        .anyMatch(supportedProgram -> find(supportedProgram, programId));
  }

  private boolean find(SupportedProgramDto supportedProgram, UUID programId) {
    return supportedProgram.getId().equals(programId)
        && supportedProgram.isSupportActive() && supportedProgram.isProgramActive()
        && dateHelper.isDateBeforeNow(supportedProgram.getSupportStartDate());
  }

}
