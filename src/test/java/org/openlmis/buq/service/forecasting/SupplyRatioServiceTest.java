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

package org.openlmis.buq.service.forecasting;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openlmis.buq.builder.OrderableDtoDataBuilder;
import org.openlmis.buq.builder.SupplyRatioDataBuilder;
import org.openlmis.buq.domain.forecasting.SupplyRatio;
import org.openlmis.buq.dto.forecasting.SupplyRatioDto;
import org.openlmis.buq.dto.referencedata.BasicOrderableDto;
import org.openlmis.buq.exception.NotFoundException;
import org.openlmis.buq.exception.ValidationMessageException;
import org.openlmis.buq.i18n.MessageKeys;
import org.openlmis.buq.repository.forecasting.SupplyRatioRepository;
import org.openlmis.buq.service.referencedata.OrderableReferenceDataService;

@RunWith(MockitoJUnitRunner.class)
public class SupplyRatioServiceTest {

  @Mock
  private SupplyRatioRepository supplyRatioRepository;

  @Mock
  private OrderableReferenceDataService orderableReferenceDataService;

  @InjectMocks
  private SupplyRatioService supplyRatioService;

  private final BasicOrderableDto syringe = new OrderableDtoDataBuilder()
      .withCommodityType("ad-syringe")
      .buildAsDto();

  private SupplyRatioDto dto;

  @Before
  public void setUp() {
    dto = SupplyRatioDto.newInstance(new SupplyRatioDataBuilder()
        .withSupplyOrderableId(syringe.getId())
        .build());
  }

  @Test
  public void shouldCreateRatioWithoutIdForSupplyWithCommodityType() {
    givenSupplyOrderable(syringe);
    givenSaveReturnsArgument();

    SupplyRatio created = supplyRatioService.create(dto);

    assertThat(created.getId()).isNull();
    assertThat(created.getSupplyOrderableId()).isEqualTo(syringe.getId());
  }

  @Test
  public void shouldRefuseSameVaccineAndSupplyWithoutLookingUpTheOrderable() {
    dto.setVaccineOrderableId(dto.getSupplyOrderableId());

    assertThatThrownBy(() -> supplyRatioService.create(dto))
        .isInstanceOf(ValidationMessageException.class)
        .hasMessageContaining(MessageKeys.ERROR_SUPPLY_RATIO_SAME_ORDERABLE);
    verifyZeroInteractions(orderableReferenceDataService);
  }

  @Test
  public void shouldRefuseUnknownSupplyOrderable() {
    when(orderableReferenceDataService.findOne(syringe.getId())).thenReturn(null);

    assertThatThrownBy(() -> supplyRatioService.create(dto))
        .isInstanceOf(ValidationMessageException.class)
        .hasMessageContaining(MessageKeys.ERROR_SUPPLY_RATIO_ORDERABLE_NOT_FOUND);
    verify(supplyRatioRepository, never()).saveAndFlush(any(SupplyRatio.class));
  }

  @Test
  public void shouldRefuseSupplyWithoutCommodityType() {
    givenSupplyOrderable(new OrderableDtoDataBuilder().withId(syringe.getId()).buildAsDto());

    assertThatThrownBy(() -> supplyRatioService.create(dto))
        .isInstanceOf(ValidationMessageException.class)
        .hasMessageContaining(MessageKeys.ERROR_SUPPLY_RATIO_COMMODITY_TYPE_MISSING);
    verify(supplyRatioRepository, never()).saveAndFlush(any(SupplyRatio.class));
  }

  @Test
  public void shouldUpdateRatioKeepingId() {
    SupplyRatio existing = new SupplyRatioDataBuilder().build();
    when(supplyRatioRepository.findById(existing.getId())).thenReturn(Optional.of(existing));
    givenSupplyOrderable(syringe);
    givenSaveReturnsArgument();
    dto.setId(existing.getId());
    dto.setRatio(new BigDecimal("2.5000"));

    SupplyRatio updated = supplyRatioService.update(existing.getId(), dto);

    assertThat(updated.getId()).isEqualTo(existing.getId());
    assertThat(updated.getSupplyOrderableId()).isEqualTo(syringe.getId());
    assertThat(updated.getRatio()).isEqualByComparingTo("2.5");
  }

  @Test
  public void shouldRefuseUpdateToSupplyWithoutCommodityType() {
    SupplyRatio existing = new SupplyRatioDataBuilder().build();
    when(supplyRatioRepository.findById(existing.getId())).thenReturn(Optional.of(existing));
    givenSupplyOrderable(new OrderableDtoDataBuilder().withId(syringe.getId()).buildAsDto());

    assertThatThrownBy(() -> supplyRatioService.update(existing.getId(), dto))
        .isInstanceOf(ValidationMessageException.class)
        .hasMessageContaining(MessageKeys.ERROR_SUPPLY_RATIO_COMMODITY_TYPE_MISSING);
    verify(supplyRatioRepository, never()).saveAndFlush(any(SupplyRatio.class));
  }

  @Test
  public void shouldReturnNotFoundWhenUpdatingMissingRatio() {
    UUID id = UUID.randomUUID();
    when(supplyRatioRepository.findById(id)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> supplyRatioService.update(id, dto))
        .isInstanceOf(NotFoundException.class)
        .hasMessageContaining(MessageKeys.ERROR_SUPPLY_RATIO_NOT_FOUND);
  }

  private void givenSupplyOrderable(BasicOrderableDto orderable) {
    when(orderableReferenceDataService.findOne(syringe.getId())).thenReturn(orderable);
  }

  private void givenSaveReturnsArgument() {
    when(supplyRatioRepository.saveAndFlush(any(SupplyRatio.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));
  }

}
