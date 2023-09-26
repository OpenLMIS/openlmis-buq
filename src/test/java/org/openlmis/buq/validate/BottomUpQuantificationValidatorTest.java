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

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.UUID;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openlmis.buq.builder.BottomUpQuantificationDataBuilder;
import org.openlmis.buq.builder.BottomUpQuantificationLineItemDataBuilder;
import org.openlmis.buq.domain.buq.BottomUpQuantification;
import org.openlmis.buq.domain.buq.BottomUpQuantificationLineItem;
import org.openlmis.buq.domain.buq.BottomUpQuantificationStatus;
import org.openlmis.buq.dto.buq.BottomUpQuantificationDto;
import org.openlmis.buq.dto.buq.BottomUpQuantificationLineItemDto;
import org.openlmis.buq.exception.ValidationMessageException;
import org.openlmis.buq.service.buq.BottomUpQuantificationService;

@SuppressWarnings("PMD.TooManyMethods")
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
        .withStatus(BottomUpQuantificationStatus.IN_APPROVAL)
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
    when(quantificationService.canSkipAuthorization(target))
        .thenReturn(false);

    validator.validateCanBeAuthorized(BottomUpQuantificationDto.newInstance(target), targetId);

    verify(quantificationService).canSkipAuthorization(target);
  }

  @Test(expected = ValidationMessageException.class)
  public void shouldThrowExceptionIfStatusIsNotAuthorizedForRejection() {
    BottomUpQuantification target = new BottomUpQuantificationDataBuilder()
            .withStatus(BottomUpQuantificationStatus.APPROVED_BY_DP)
            .build();

    validator.validateCanBeRejected(target);
  }

  @Test(expected = ValidationMessageException.class)
  public void shouldThrowExceptionIfAnnualAdjustedConsumptionIsNullForSubmission() {
    UUID targetId = UUID.randomUUID();
    BottomUpQuantification target = new BottomUpQuantificationDataBuilder()
        .withStatus(BottomUpQuantificationStatus.DRAFT)
        .build();
    when(quantificationService.findBottomUpQuantification(targetId))
        .thenReturn(target);
    BottomUpQuantificationDto targetDto = BottomUpQuantificationDto.newInstance(target);
    BottomUpQuantificationLineItem lineItem = new BottomUpQuantificationLineItemDataBuilder()
        .withAnnualAdjustedConsumption(null)
        .build();
    targetDto.setBottomUpQuantificationLineItems(Collections
        .singletonList(BottomUpQuantificationLineItemDto.newInstance(lineItem)));

    validator.validateCanBeSubmitted(targetDto, targetId);
  }

  @Test(expected = ValidationMessageException.class)
  public void shouldThrowExceptionIfVerifiedAnnualAdjustedConsumptionIsNullForSubmission() {
    UUID targetId = UUID.randomUUID();
    BottomUpQuantification target = new BottomUpQuantificationDataBuilder()
        .withStatus(BottomUpQuantificationStatus.DRAFT)
        .build();
    when(quantificationService.findBottomUpQuantification(targetId))
        .thenReturn(target);
    BottomUpQuantificationDto targetDto = BottomUpQuantificationDto.newInstance(target);
    BottomUpQuantificationLineItem lineItem = new BottomUpQuantificationLineItemDataBuilder()
        .withVerifiedAnnualAdjustedConsumption(null)
        .build();
    targetDto.setBottomUpQuantificationLineItems(Collections
        .singletonList(BottomUpQuantificationLineItemDto.newInstance(lineItem)));

    validator.validateCanBeSubmitted(targetDto, targetId);
  }

  @Test(expected = ValidationMessageException.class)
  public void shouldThrowExceptionIfForecastedDemandIsNullForSubmission() {
    UUID targetId = UUID.randomUUID();
    BottomUpQuantification target = new BottomUpQuantificationDataBuilder()
        .withStatus(BottomUpQuantificationStatus.DRAFT)
        .build();
    when(quantificationService.findBottomUpQuantification(targetId))
        .thenReturn(target);
    BottomUpQuantificationDto targetDto = BottomUpQuantificationDto.newInstance(target);
    BottomUpQuantificationLineItem lineItem = new BottomUpQuantificationLineItemDataBuilder()
        .withForecastedDemand(null)
        .build();
    targetDto.setBottomUpQuantificationLineItems(Collections
        .singletonList(BottomUpQuantificationLineItemDto.newInstance(lineItem)));

    validator.validateCanBeSubmitted(targetDto, targetId);
  }

  @Test(expected = ValidationMessageException.class)
  public void shouldThrowExceptionIfTotalCostIsNullForSubmission() {
    UUID targetId = UUID.randomUUID();
    BottomUpQuantification target = new BottomUpQuantificationDataBuilder()
        .withStatus(BottomUpQuantificationStatus.DRAFT)
        .build();
    when(quantificationService.findBottomUpQuantification(targetId))
        .thenReturn(target);
    BottomUpQuantificationDto targetDto = BottomUpQuantificationDto.newInstance(target);
    BottomUpQuantificationLineItem lineItem = new BottomUpQuantificationLineItemDataBuilder()
        .withTotalCost(null)
        .build();
    targetDto.setBottomUpQuantificationLineItems(Collections
        .singletonList(BottomUpQuantificationLineItemDto.newInstance(lineItem)));

    validator.validateCanBeSubmitted(targetDto, targetId);
  }

  @Test(expected = ValidationMessageException.class)
  public void shouldThrowExceptionIfAnnualAdjustedConsumptionValueIsNegativeForSubmission() {
    UUID targetId = UUID.randomUUID();
    BottomUpQuantification target = new BottomUpQuantificationDataBuilder()
        .withStatus(BottomUpQuantificationStatus.DRAFT)
        .build();
    when(quantificationService.findBottomUpQuantification(targetId))
        .thenReturn(target);
    BottomUpQuantificationDto targetDto = BottomUpQuantificationDto.newInstance(target);
    BottomUpQuantificationLineItem lineItem = new BottomUpQuantificationLineItemDataBuilder()
        .withAnnualAdjustedConsumption(-1)
        .build();
    targetDto.setBottomUpQuantificationLineItems(Collections
        .singletonList(BottomUpQuantificationLineItemDto.newInstance(lineItem)));

    validator.validateCanBeSubmitted(targetDto, targetId);
  }

  @Test(expected = ValidationMessageException.class)
  public void shouldThrowExceptionIfVerifiedAnnualAdjustedConsumptionValueIsNegativeForSubmit() {
    UUID targetId = UUID.randomUUID();
    BottomUpQuantification target = new BottomUpQuantificationDataBuilder()
        .withStatus(BottomUpQuantificationStatus.DRAFT)
        .build();
    when(quantificationService.findBottomUpQuantification(targetId))
        .thenReturn(target);
    BottomUpQuantificationDto targetDto = BottomUpQuantificationDto.newInstance(target);
    BottomUpQuantificationLineItem lineItem = new BottomUpQuantificationLineItemDataBuilder()
        .withVerifiedAnnualAdjustedConsumption(-1)
        .build();
    targetDto.setBottomUpQuantificationLineItems(Collections
        .singletonList(BottomUpQuantificationLineItemDto.newInstance(lineItem)));

    validator.validateCanBeSubmitted(targetDto, targetId);
  }

  @Test(expected = ValidationMessageException.class)
  public void shouldThrowExceptionIfForecastedDemandValueIsNegativeForSubmission() {
    UUID targetId = UUID.randomUUID();
    BottomUpQuantification target = new BottomUpQuantificationDataBuilder()
        .withStatus(BottomUpQuantificationStatus.DRAFT)
        .build();
    when(quantificationService.findBottomUpQuantification(targetId))
        .thenReturn(target);
    BottomUpQuantificationDto targetDto = BottomUpQuantificationDto.newInstance(target);
    BottomUpQuantificationLineItem lineItem = new BottomUpQuantificationLineItemDataBuilder()
        .withForecastedDemand(-1)
        .build();
    targetDto.setBottomUpQuantificationLineItems(Collections
        .singletonList(BottomUpQuantificationLineItemDto.newInstance(lineItem)));

    validator.validateCanBeSubmitted(targetDto, targetId);
  }

  @Test(expected = ValidationMessageException.class)
  public void shouldThrowExceptionIfTotalCostValueIsNegativeForSubmission() {
    UUID targetId = UUID.randomUUID();
    BottomUpQuantification target = new BottomUpQuantificationDataBuilder()
        .withStatus(BottomUpQuantificationStatus.DRAFT)
        .build();
    when(quantificationService.findBottomUpQuantification(targetId))
        .thenReturn(target);
    BottomUpQuantificationDto targetDto = BottomUpQuantificationDto.newInstance(target);
    BottomUpQuantificationLineItem lineItem = new BottomUpQuantificationLineItemDataBuilder()
        .withTotalCost(-10.0d)
        .build();
    targetDto.setBottomUpQuantificationLineItems(Collections
        .singletonList(BottomUpQuantificationLineItemDto.newInstance(lineItem)));

    validator.validateCanBeSubmitted(targetDto, targetId);
  }

  @Test(expected = ValidationMessageException.class)
  public void shouldThrowExceptionWhenNoRemarkProvidedForValuesDifferencesForSubmission() {
    UUID targetId = UUID.randomUUID();
    BottomUpQuantification target = new BottomUpQuantificationDataBuilder()
        .withStatus(BottomUpQuantificationStatus.DRAFT)
        .build();
    when(quantificationService.findBottomUpQuantification(targetId))
        .thenReturn(target);
    BottomUpQuantificationDto targetDto = BottomUpQuantificationDto.newInstance(target);
    BottomUpQuantificationLineItem lineItem = new BottomUpQuantificationLineItemDataBuilder()
        .withVerifiedAnnualAdjustedConsumption(1)
        .withForecastedDemand(2)
        .withRemark(null)
        .build();
    targetDto.setBottomUpQuantificationLineItems(Collections
        .singletonList(BottomUpQuantificationLineItemDto.newInstance(lineItem)));

    validator.validateCanBeSubmitted(targetDto, targetId);
  }

  @Test(expected = ValidationMessageException.class)
  public void shouldThrowExceptionIfAnnualAdjustedConsumptionIsNullForAuthorization() {
    UUID targetId = UUID.randomUUID();
    BottomUpQuantification target = new BottomUpQuantificationDataBuilder()
        .withStatus(BottomUpQuantificationStatus.SUBMITTED)
        .build();
    when(quantificationService.findBottomUpQuantification(targetId))
        .thenReturn(target);
    BottomUpQuantificationDto targetDto = BottomUpQuantificationDto.newInstance(target);
    BottomUpQuantificationLineItem lineItem = new BottomUpQuantificationLineItemDataBuilder()
        .withAnnualAdjustedConsumption(null)
        .build();
    targetDto.setBottomUpQuantificationLineItems(Collections
        .singletonList(BottomUpQuantificationLineItemDto.newInstance(lineItem)));

    validator.validateCanBeAuthorized(targetDto, targetId);
  }

  @Test(expected = ValidationMessageException.class)
  public void shouldThrowExceptionIfVerifiedAnnualAdjustedConsumptionIsNullForAuthorization() {
    UUID targetId = UUID.randomUUID();
    BottomUpQuantification target = new BottomUpQuantificationDataBuilder()
        .withStatus(BottomUpQuantificationStatus.SUBMITTED)
        .build();
    when(quantificationService.findBottomUpQuantification(targetId))
        .thenReturn(target);
    BottomUpQuantificationDto targetDto = BottomUpQuantificationDto.newInstance(target);
    BottomUpQuantificationLineItem lineItem = new BottomUpQuantificationLineItemDataBuilder()
        .withVerifiedAnnualAdjustedConsumption(null)
        .build();
    targetDto.setBottomUpQuantificationLineItems(Collections
        .singletonList(BottomUpQuantificationLineItemDto.newInstance(lineItem)));

    validator.validateCanBeAuthorized(targetDto, targetId);
  }

  @Test(expected = ValidationMessageException.class)
  public void shouldThrowExceptionIfForecastedDemandIsNullForAuthorization() {
    UUID targetId = UUID.randomUUID();
    BottomUpQuantification target = new BottomUpQuantificationDataBuilder()
        .withStatus(BottomUpQuantificationStatus.SUBMITTED)
        .build();
    when(quantificationService.findBottomUpQuantification(targetId))
        .thenReturn(target);
    BottomUpQuantificationDto targetDto = BottomUpQuantificationDto.newInstance(target);
    BottomUpQuantificationLineItem lineItem = new BottomUpQuantificationLineItemDataBuilder()
        .withForecastedDemand(null)
        .build();
    targetDto.setBottomUpQuantificationLineItems(Collections
        .singletonList(BottomUpQuantificationLineItemDto.newInstance(lineItem)));

    validator.validateCanBeAuthorized(targetDto, targetId);
  }

  @Test(expected = ValidationMessageException.class)
  public void shouldThrowExceptionIfTotalCostIsNullForAuthorization() {
    UUID targetId = UUID.randomUUID();
    BottomUpQuantification target = new BottomUpQuantificationDataBuilder()
        .withStatus(BottomUpQuantificationStatus.SUBMITTED)
        .build();
    when(quantificationService.findBottomUpQuantification(targetId))
        .thenReturn(target);
    BottomUpQuantificationDto targetDto = BottomUpQuantificationDto.newInstance(target);
    BottomUpQuantificationLineItem lineItem = new BottomUpQuantificationLineItemDataBuilder()
        .withTotalCost(null)
        .build();
    targetDto.setBottomUpQuantificationLineItems(Collections
        .singletonList(BottomUpQuantificationLineItemDto.newInstance(lineItem)));

    validator.validateCanBeAuthorized(targetDto, targetId);
  }

  @Test(expected = ValidationMessageException.class)
  public void shouldThrowExceptionIfAnnualAdjustedConsumptionValueIsNegativeForAuthorization() {
    UUID targetId = UUID.randomUUID();
    BottomUpQuantification target = new BottomUpQuantificationDataBuilder()
        .withStatus(BottomUpQuantificationStatus.SUBMITTED)
        .build();
    when(quantificationService.findBottomUpQuantification(targetId))
        .thenReturn(target);
    BottomUpQuantificationDto targetDto = BottomUpQuantificationDto.newInstance(target);
    BottomUpQuantificationLineItem lineItem = new BottomUpQuantificationLineItemDataBuilder()
        .withAnnualAdjustedConsumption(-1)
        .build();
    targetDto.setBottomUpQuantificationLineItems(Collections
        .singletonList(BottomUpQuantificationLineItemDto.newInstance(lineItem)));

    validator.validateCanBeAuthorized(targetDto, targetId);
  }

  @Test(expected = ValidationMessageException.class)
  public void shouldThrowExceptionIfVerifiedAnnualAdjustedConsumptionIsNegativeForAuthorization() {
    UUID targetId = UUID.randomUUID();
    BottomUpQuantification target = new BottomUpQuantificationDataBuilder()
        .withStatus(BottomUpQuantificationStatus.SUBMITTED)
        .build();
    when(quantificationService.findBottomUpQuantification(targetId))
        .thenReturn(target);
    BottomUpQuantificationDto targetDto = BottomUpQuantificationDto.newInstance(target);
    BottomUpQuantificationLineItem lineItem = new BottomUpQuantificationLineItemDataBuilder()
        .withVerifiedAnnualAdjustedConsumption(-1)
        .build();
    targetDto.setBottomUpQuantificationLineItems(Collections
        .singletonList(BottomUpQuantificationLineItemDto.newInstance(lineItem)));

    validator.validateCanBeAuthorized(targetDto, targetId);
  }

  @Test(expected = ValidationMessageException.class)
  public void shouldThrowExceptionIfForecastedDemandValueIsNegativeForAuthorization() {
    UUID targetId = UUID.randomUUID();
    BottomUpQuantification target = new BottomUpQuantificationDataBuilder()
        .withStatus(BottomUpQuantificationStatus.SUBMITTED)
        .build();
    when(quantificationService.findBottomUpQuantification(targetId))
        .thenReturn(target);
    BottomUpQuantificationDto targetDto = BottomUpQuantificationDto.newInstance(target);
    BottomUpQuantificationLineItem lineItem = new BottomUpQuantificationLineItemDataBuilder()
        .withForecastedDemand(-1)
        .build();
    targetDto.setBottomUpQuantificationLineItems(Collections
        .singletonList(BottomUpQuantificationLineItemDto.newInstance(lineItem)));

    validator.validateCanBeAuthorized(targetDto, targetId);
  }

  @Test(expected = ValidationMessageException.class)
  public void shouldThrowExceptionIfTotalCostValueIsNegativeForAuthorization() {
    UUID targetId = UUID.randomUUID();
    BottomUpQuantification target = new BottomUpQuantificationDataBuilder()
        .withStatus(BottomUpQuantificationStatus.SUBMITTED)
        .build();
    when(quantificationService.findBottomUpQuantification(targetId))
        .thenReturn(target);
    BottomUpQuantificationDto targetDto = BottomUpQuantificationDto.newInstance(target);
    BottomUpQuantificationLineItem lineItem = new BottomUpQuantificationLineItemDataBuilder()
        .withTotalCost(-10.0d)
        .build();
    targetDto.setBottomUpQuantificationLineItems(Collections
        .singletonList(BottomUpQuantificationLineItemDto.newInstance(lineItem)));

    validator.validateCanBeAuthorized(targetDto, targetId);
  }

  @Test(expected = ValidationMessageException.class)
  public void shouldThrowExceptionWhenNoRemarkProvidedForValuesDifferencesForAuthorization() {
    UUID targetId = UUID.randomUUID();
    BottomUpQuantification target = new BottomUpQuantificationDataBuilder()
        .withStatus(BottomUpQuantificationStatus.SUBMITTED)
        .build();
    when(quantificationService.findBottomUpQuantification(targetId))
        .thenReturn(target);
    BottomUpQuantificationDto targetDto = BottomUpQuantificationDto.newInstance(target);
    BottomUpQuantificationLineItem lineItem = new BottomUpQuantificationLineItemDataBuilder()
        .withVerifiedAnnualAdjustedConsumption(1)
        .withForecastedDemand(2)
        .withRemark(null)
        .build();
    targetDto.setBottomUpQuantificationLineItems(Collections
        .singletonList(BottomUpQuantificationLineItemDto.newInstance(lineItem)));

    validator.validateCanBeAuthorized(targetDto, targetId);
  }

}
