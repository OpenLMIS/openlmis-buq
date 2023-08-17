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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import org.openlmis.buq.builder.ProcessingPeriodDtoDataBuilder;
import org.openlmis.buq.dto.referencedata.ProcessingPeriodDto;
import org.openlmis.buq.dto.requisition.RequisitionLineItemDataProjection;

public class AnnualAdjustedConsumptionCalculatorTest {

  @Test
  public void shouldCalculateWithNoLineItems() {
    List<RequisitionLineItemDataProjection> lineItems = new ArrayList<>();
    ProcessingPeriodDto processingPeriod = createProcessingPeriod(
        LocalDate.of(2023, 1, 1),
        LocalDate.of(2023, 12, 31)
    );
    int result = AnnualAdjustedConsumptionCalculator.calculate(lineItems, processingPeriod);
    assertEquals(0, result);
  }

  @Test
  public void shouldNotIncludeAdjustedConsumptionForCalculationsIfNoOverlappingDays() {
    List<RequisitionLineItemDataProjection> lineItems = new ArrayList<>();
    lineItems.add(createRequisitionLineItem(10,
        LocalDate.of(2022, 1, 1),
        LocalDate.of(2022, 12, 31)));
    ProcessingPeriodDto processingPeriod = createProcessingPeriod(
        LocalDate.of(2023, 1, 1),
        LocalDate.of(2023, 12, 31)
    );
    int result = AnnualAdjustedConsumptionCalculator.calculate(lineItems, processingPeriod);
    assertEquals(0, result);
  }

  @Test
  public void shouldCalculateWithFullOverlap() {
    List<RequisitionLineItemDataProjection> lineItems = new ArrayList<>();
    lineItems.add(createRequisitionLineItem(10,
        LocalDate.of(2023, 1, 1),
        LocalDate.of(2023, 12, 31)));
    ProcessingPeriodDto processingPeriod = createProcessingPeriod(
        LocalDate.of(2023, 1, 1),
        LocalDate.of(2023, 12, 31)
    );
    int result = AnnualAdjustedConsumptionCalculator.calculate(lineItems, processingPeriod);
    assertEquals(10, result);
  }

  @Test
  public void shouldNotCalculateWithPartialOverlapAfterStartDate() {
    List<RequisitionLineItemDataProjection> lineItems = new ArrayList<>();
    lineItems.add(createRequisitionLineItem(10,
        LocalDate.of(2022, 1, 1),
        LocalDate.of(2023, 6, 30)));
    ProcessingPeriodDto processingPeriod = createProcessingPeriod(
        LocalDate.of(2023, 1, 1),
        LocalDate.of(2023, 12, 31)
    );
    int result = AnnualAdjustedConsumptionCalculator.calculate(lineItems, processingPeriod);
    assertEquals(0, result);
  }

  @Test
  public void shouldNotCalculateWithPartialOverlapBeforeEndDate() {
    List<RequisitionLineItemDataProjection> lineItems = new ArrayList<>();
    lineItems.add(createRequisitionLineItem(10,
        LocalDate.of(2023, 6, 1),
        LocalDate.of(2024, 12, 31)));
    ProcessingPeriodDto processingPeriod = createProcessingPeriod(
        LocalDate.of(2023, 1, 1),
        LocalDate.of(2023, 12, 31)
    );
    int result = AnnualAdjustedConsumptionCalculator.calculate(lineItems, processingPeriod);
    assertEquals(0, result);
  }

  @Test
  public void shouldCalculateWithMultipleLineItems() {
    List<RequisitionLineItemDataProjection> lineItems = new ArrayList<>();
    lineItems.add(createRequisitionLineItem(5,
        LocalDate.of(2023, 1, 1),
        LocalDate.of(2023, 6, 30)));
    lineItems.add(createRequisitionLineItem(10,
        LocalDate.of(2023, 7, 1),
        LocalDate.of(2023, 12, 31)));
    ProcessingPeriodDto processingPeriod = createProcessingPeriod(
        LocalDate.of(2023, 1, 1),
        LocalDate.of(2023, 12, 31)
    );
    int result = AnnualAdjustedConsumptionCalculator.calculate(lineItems, processingPeriod);
    assertEquals(15, result);
  }

  @Test
  public void shouldCalculateWithNullAdjustedConsumption() {
    List<RequisitionLineItemDataProjection> lineItems = new ArrayList<>();
    lineItems.add(createRequisitionLineItem(null,
        LocalDate.of(2023, 1, 1),
        LocalDate.of(2023, 12, 31)));
    ProcessingPeriodDto processingPeriod = createProcessingPeriod(
        LocalDate.of(2023, 1, 1),
        LocalDate.of(2023, 12, 31)
    );
    int result = AnnualAdjustedConsumptionCalculator.calculate(lineItems, processingPeriod);
    assertEquals(0, result);
  }

  private RequisitionLineItemDataProjection createRequisitionLineItem(
      Integer adjustedConsumption,
      LocalDate startDate,
      LocalDate endDate) {
    RequisitionLineItemDataProjection lineItem = mock(RequisitionLineItemDataProjection.class);
    when(lineItem.getAdjustedConsumption()).thenReturn(adjustedConsumption);
    when(lineItem.getStartDate()).thenReturn(startDate);
    when(lineItem.getEndDate()).thenReturn(endDate);
    return lineItem;
  }

  private ProcessingPeriodDto createProcessingPeriod(LocalDate startDate, LocalDate endDate) {
    return new ProcessingPeriodDtoDataBuilder()
        .withStartDate(startDate)
        .withEndDate(endDate)
        .buildAsDto();
  }
}
