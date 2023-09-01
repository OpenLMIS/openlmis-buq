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

package org.openlmis.buq.validate;

import static org.mockito.Mockito.when;

import java.util.UUID;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openlmis.buq.builder.BottomUpQuantificationDataBuilder;
import org.openlmis.buq.domain.buq.BottomUpQuantification;
import org.openlmis.buq.domain.buq.BottomUpQuantificationStatus;
import org.openlmis.buq.dto.buq.BottomUpQuantificationDto;
import org.openlmis.buq.exception.ValidationMessageException;
import org.openlmis.buq.service.buq.BottomUpQuantificationService;

@RunWith(MockitoJUnitRunner.class)
public class BottomUpQuantificationValidatorTest {

  @InjectMocks
  private BottomUpQuantificationValidator validator;

  @Mock
  private BottomUpQuantificationService quantificationService;

  @Test(expected = ValidationMessageException.class)
  public void shouldThrowExceptionIfStatusIsNotDraftForSubmission() {
    UUID targetId = UUID.randomUUID();
    BottomUpQuantification target = new BottomUpQuantificationDataBuilder()
        .withStatus(BottomUpQuantificationStatus.APPROVED_BY_DP)
        .build();

    when(quantificationService.findBottomUpQuantification(targetId))
        .thenReturn(target);

    validator.validateCanBeSubmitted(BottomUpQuantificationDto.newInstance(target), targetId);
  }

  @Test(expected = ValidationMessageException.class)
  public void shouldThrowExceptionIfStatusIsNotSubmittedForAuthorization() {
    UUID targetId = UUID.randomUUID();
    BottomUpQuantification target = new BottomUpQuantificationDataBuilder()
        .withStatus(BottomUpQuantificationStatus.DRAFT)
        .build();

    when(quantificationService.findBottomUpQuantification(targetId))
        .thenReturn(target);

    validator.validateCanBeAuthorized(BottomUpQuantificationDto.newInstance(target), targetId);
  }

}