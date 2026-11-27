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
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openlmis.buq.builder.ForecastingSettingDataBuilder;
import org.openlmis.buq.builder.OrderableDtoDataBuilder;
import org.openlmis.buq.builder.PopulationSegmentDataBuilder;
import org.openlmis.buq.domain.forecasting.ForecastingSetting;
import org.openlmis.buq.domain.forecasting.PopulationSegment;
import org.openlmis.buq.dto.forecasting.ForecastingSettingDto;
import org.openlmis.buq.dto.referencedata.BasicOrderableDto;
import org.openlmis.buq.exception.NotFoundException;
import org.openlmis.buq.exception.ValidationMessageException;
import org.openlmis.buq.i18n.MessageKeys;
import org.openlmis.buq.repository.forecasting.ForecastingSettingRepository;
import org.openlmis.buq.repository.forecasting.PopulationSegmentRepository;
import org.openlmis.buq.service.RequestParameters;
import org.openlmis.buq.service.referencedata.OrderableReferenceDataService;

@SuppressWarnings("PMD.TooManyMethods")
@RunWith(MockitoJUnitRunner.class)
public class ForecastingSettingServiceTest {

  private static final String BCG = "bcg";

  @Mock
  private ForecastingSettingRepository forecastingSettingRepository;

  @Mock
  private PopulationSegmentRepository populationSegmentRepository;

  @Mock
  private OrderableReferenceDataService orderableReferenceDataService;

  @InjectMocks
  private ForecastingSettingService forecastingSettingService;

  private final UUID programId = UUID.randomUUID();
  private final PopulationSegment segment = new PopulationSegmentDataBuilder().build();
  private final BasicOrderableDto orderable = new OrderableDtoDataBuilder()
      .withCommodityType(BCG)
      .buildAsDto();

  private ForecastingSettingDto dto;

  @Before
  public void setUp() {
    dto = ForecastingSettingDto.newInstance(new ForecastingSettingDataBuilder()
        .withProgramId(programId)
        .withOrderableId(orderable.getId())
        .withPopulationSegment(segment)
        .build());
  }

  @Test
  public void shouldCreateSettingWithoutIdForProductWithFreeCommodityType() {
    givenSegmentExists();
    givenOrderableExists(orderable);
    givenSettingsInProgram();
    givenSaveReturnsArgument();

    ForecastingSetting created = forecastingSettingService.create(dto);

    assertThat(created.getId()).isNull();
    assertThat(created.getOrderableId()).isEqualTo(orderable.getId());
    assertThat(created.getPopulationSegment()).isSameAs(segment);
    verify(orderableReferenceDataService, never()).findAll(any(RequestParameters.class));
  }

  @Test
  public void shouldRefuseOrderableWithoutCommodityType() {
    BasicOrderableDto tradeItemOnly = new OrderableDtoDataBuilder()
        .withId(orderable.getId())
        .buildAsDto();
    givenSegmentExists();
    givenOrderableExists(tradeItemOnly);

    assertThatThrownBy(() -> forecastingSettingService.create(dto))
        .isInstanceOf(ValidationMessageException.class)
        .hasMessageContaining(MessageKeys.ERROR_FORECASTING_SETTING_COMMODITY_TYPE_MISSING);
    verify(forecastingSettingRepository, never()).saveAndFlush(any(ForecastingSetting.class));
  }

  @Test
  public void shouldRefuseSecondProductOfSameCommodityTypeInProgram() {
    BasicOrderableDto sibling = new OrderableDtoDataBuilder().withCommodityType(BCG).buildAsDto();
    givenSegmentExists();
    givenOrderableExists(orderable);
    givenSettingsInProgram(settingFor(sibling));
    givenOrderablesFound(sibling);

    assertThatThrownBy(() -> forecastingSettingService.create(dto))
        .isInstanceOf(ValidationMessageException.class)
        .hasMessageContaining(MessageKeys.ERROR_FORECASTING_SETTING_COMMODITY_TYPE_TAKEN);
    verify(forecastingSettingRepository, never()).saveAndFlush(any(ForecastingSetting.class));
  }

  @Test
  public void shouldAllowProductsOfDifferentCommodityTypesInProgram() {
    BasicOrderableDto measles = new OrderableDtoDataBuilder()
        .withCommodityType("measles")
        .buildAsDto();
    givenSegmentExists();
    givenOrderableExists(orderable);
    givenSettingsInProgram(settingFor(measles));
    givenOrderablesFound(measles);
    givenSaveReturnsArgument();

    forecastingSettingService.create(dto);

    verify(forecastingSettingRepository).saveAndFlush(any(ForecastingSetting.class));
  }

  @Test
  public void shouldCheckOnlySettingsOfTheSettingsProgram() {
    givenSegmentExists();
    givenOrderableExists(orderable);
    givenSettingsInProgram();
    givenSaveReturnsArgument();

    forecastingSettingService.create(dto);

    verify(forecastingSettingRepository).findByProgramId(programId);
  }

  @Test
  public void shouldAllowUpdateOfSettingThatHoldsTheCommodityTypeItself() {
    BasicOrderableDto previousProduct = new OrderableDtoDataBuilder()
        .withCommodityType(BCG)
        .buildAsDto();
    ForecastingSetting existing = settingFor(previousProduct);
    dto.setId(existing.getId());
    when(forecastingSettingRepository.findById(existing.getId()))
        .thenReturn(Optional.of(existing));
    givenSegmentExists();
    givenOrderableExists(orderable);
    givenSettingsInProgram(existing);
    givenSaveReturnsArgument();

    ForecastingSetting updated = forecastingSettingService.update(existing.getId(), dto);

    assertThat(updated.getId()).isEqualTo(existing.getId());
    assertThat(updated.getOrderableId()).isEqualTo(orderable.getId());
    verify(orderableReferenceDataService, never()).findAll(any(RequestParameters.class));
  }

  @Test
  public void shouldReturnNotFoundWhenUpdatingMissingSetting() {
    UUID id = UUID.randomUUID();
    when(forecastingSettingRepository.findById(id)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> forecastingSettingService.update(id, dto))
        .isInstanceOf(NotFoundException.class)
        .hasMessageContaining(MessageKeys.ERROR_FORECASTING_SETTING_NOT_FOUND);
  }

  @Test
  public void shouldRefuseUnknownSegment() {
    when(populationSegmentRepository.findById(segment.getId())).thenReturn(Optional.empty());

    assertThatThrownBy(() -> forecastingSettingService.create(dto))
        .isInstanceOf(ValidationMessageException.class)
        .hasMessageContaining(MessageKeys.ERROR_FORECASTING_SETTING_SEGMENT_NOT_FOUND);
  }

  @Test
  public void shouldRefuseUnknownOrderable() {
    givenSegmentExists();
    when(orderableReferenceDataService.findOne(orderable.getId())).thenReturn(null);

    assertThatThrownBy(() -> forecastingSettingService.create(dto))
        .isInstanceOf(ValidationMessageException.class)
        .hasMessageContaining(MessageKeys.ERROR_FORECASTING_SETTING_ORDERABLE_NOT_FOUND);
  }

  @Test
  public void shouldRequestAllOtherOrderablesInOnePage() {
    BasicOrderableDto first = new OrderableDtoDataBuilder().withCommodityType("a").buildAsDto();
    BasicOrderableDto second = new OrderableDtoDataBuilder().withCommodityType("b").buildAsDto();
    givenSegmentExists();
    givenOrderableExists(orderable);
    givenSettingsInProgram(settingFor(first), settingFor(second));
    givenOrderablesFound(first, second);
    givenSaveReturnsArgument();

    forecastingSettingService.create(dto);

    ArgumentCaptor<RequestParameters> captor = ArgumentCaptor.forClass(RequestParameters.class);
    verify(orderableReferenceDataService).findAll(captor.capture());
    Map<String, List<String>> parameters = new HashMap<>();
    captor.getValue().forEach(entry -> parameters.put(entry.getKey(), entry.getValue()));
    assertThat(parameters.get("id"))
        .containsExactlyInAnyOrder(first.getId().toString(), second.getId().toString());
    assertThat(parameters.get("size")).containsExactly("2");
  }

  private ForecastingSetting settingFor(BasicOrderableDto product) {
    return new ForecastingSettingDataBuilder()
        .withProgramId(programId)
        .withOrderableId(product.getId())
        .withPopulationSegment(segment)
        .build();
  }

  private void givenSegmentExists() {
    when(populationSegmentRepository.findById(segment.getId())).thenReturn(Optional.of(segment));
  }

  private void givenOrderableExists(BasicOrderableDto product) {
    when(orderableReferenceDataService.findOne(orderable.getId())).thenReturn(product);
  }

  private void givenSettingsInProgram(ForecastingSetting... settings) {
    when(forecastingSettingRepository.findByProgramId(programId))
        .thenReturn(Arrays.asList(settings));
  }

  private void givenOrderablesFound(BasicOrderableDto... products) {
    when(orderableReferenceDataService.findAll(any(RequestParameters.class)))
        .thenReturn(Arrays.asList(products));
  }

  private void givenSaveReturnsArgument() {
    when(forecastingSettingRepository.saveAndFlush(any(ForecastingSetting.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));
  }

}
