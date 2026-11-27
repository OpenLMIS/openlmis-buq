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

package org.openlmis.buq.repository.forecasting;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.persistence.EntityManager;
import org.junit.Before;
import org.junit.Test;
import org.openlmis.buq.builder.ForecastDataBuilder;
import org.openlmis.buq.builder.ForecastLineItemDataBuilder;
import org.openlmis.buq.builder.PopulationSegmentDataBuilder;
import org.openlmis.buq.domain.forecasting.Forecast;
import org.openlmis.buq.domain.forecasting.ForecastLineItem;
import org.openlmis.buq.domain.forecasting.ForecastLineItemType;
import org.openlmis.buq.domain.forecasting.PopulationSegment;
import org.openlmis.buq.repository.BaseCrudRepositoryIntegrationTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.repository.CrudRepository;
import org.springframework.jdbc.core.JdbcTemplate;

@SuppressWarnings("PMD.TooManyMethods")
public class ForecastRepositoryIntegrationTest extends
    BaseCrudRepositoryIntegrationTest<Forecast> {

  @Autowired
  private ForecastRepository forecastRepository;

  @Autowired
  private PopulationSegmentRepository populationSegmentRepository;

  @Autowired
  private EntityManager entityManager;

  @Autowired
  private JdbcTemplate jdbcTemplate;

  private PopulationSegment segment;

  @Override
  public CrudRepository<Forecast, UUID> getRepository() {
    return forecastRepository;
  }

  @Override
  public Forecast generateInstance() {
    return new ForecastDataBuilder().buildAsNew();
  }

  @Before
  public void setUp() {
    segment = populationSegmentRepository.saveAndFlush(
        new PopulationSegmentDataBuilder().buildAsNew());
  }

  @Test
  public void shouldSaveForecastWithLineItemsAndStatusChangeInOneSave() {
    Forecast forecast = new ForecastDataBuilder()
        .withLineItem(vaccineLineItem())
        .withLineItem(new ForecastLineItemDataBuilder().asSupply().buildAsNew())
        .buildAsNew();
    forecast.addStatusChange(UUID.randomUUID());

    UUID id = forecastRepository.save(forecast).getId();
    forecastRepository.flush();
    entityManager.clear();

    Forecast found = forecastRepository.findById(id).get();
    assertThat(forecastRepository.findAllWithoutSnapshots(PageRequest.of(0, Integer.MAX_VALUE))
        .map(Forecast::getId)
        .getContent()).doesNotContain(id);
    List<ForecastLineItemType> types = found.getLineItems().stream()
        .map(ForecastLineItem::getType)
        .collect(Collectors.toList());
    assertThat(types).containsExactlyInAnyOrder(
        ForecastLineItemType.VACCINE, ForecastLineItemType.SUPPLY);
    assertThat(found.getLineItems()).allMatch(item -> found.equals(item.getForecast()));
    assertThat(found.getStatusChanges()).hasSize(1);
    assertThat(found.getStatusChanges().get(0).getForecast()).isEqualTo(found);
    assertThat(found.getStatusChanges().get(0).getOccurredDate()).isNotNull();
  }

  @Test(expected = DataIntegrityViolationException.class)
  public void shouldNotAllowDuplicateFacilityProgramAndYear() {
    Forecast forecast1 = generateInstance();
    Forecast forecast2 = new ForecastDataBuilder()
        .withFacilityId(forecast1.getFacilityId())
        .withProgramId(forecast1.getProgramId())
        .withTargetYear(forecast1.getTargetYear())
        .buildAsNew();

    forecastRepository.saveAndFlush(forecast1);
    forecastRepository.saveAndFlush(forecast2);
  }

  @Test
  public void shouldAllowSameFacilityAndProgramForAnotherYear() {
    Forecast forecast1 = forecastRepository.saveAndFlush(generateInstance());
    Forecast forecast2 = forecastRepository.saveAndFlush(new ForecastDataBuilder()
        .withFacilityId(forecast1.getFacilityId())
        .withProgramId(forecast1.getProgramId())
        .withTargetYear(forecast1.getTargetYear() + 1)
        .buildAsNew());

    assertThat(forecastRepository.existsById(forecast2.getId())).isTrue();
  }

  @Test(expected = DataIntegrityViolationException.class)
  public void shouldNotAllowNegativeCatchmentPopulation() {
    forecastRepository.saveAndFlush(new ForecastDataBuilder()
        .withCatchmentPopulation(-1)
        .buildAsNew());
  }

  @Test(expected = DataIntegrityViolationException.class)
  public void shouldNotAllowSupplyRowWithSegment() {
    ForecastLineItem supply = new ForecastLineItemDataBuilder().asSupply().buildAsNew();
    supply.setPopulationSegment(segment);

    forecastRepository.saveAndFlush(new ForecastDataBuilder()
        .withLineItem(supply)
        .buildAsNew());
  }

  @Test(expected = DataIntegrityViolationException.class)
  public void shouldNotAllowVaccineRowWithoutTargetPopulation() {
    forecastRepository.saveAndFlush(new ForecastDataBuilder()
        .withLineItem(new ForecastLineItemDataBuilder()
            .withPopulationSegment(segment)
            .withTargetPopulation(null)
            .buildAsNew())
        .buildAsNew());
  }

  @Test(expected = DataIntegrityViolationException.class)
  public void shouldNotAllowNegativeForecastedAmount() {
    forecastRepository.saveAndFlush(new ForecastDataBuilder()
        .withLineItem(new ForecastLineItemDataBuilder()
            .asSupply()
            .withForecastedAmount(-1)
            .buildAsNew())
        .buildAsNew());
  }

  @Test(expected = DataIntegrityViolationException.class)
  public void shouldNotAllowDuplicateOrderableInForecast() {
    UUID orderableId = UUID.randomUUID();

    forecastRepository.saveAndFlush(new ForecastDataBuilder()
        .withLineItem(new ForecastLineItemDataBuilder()
            .asSupply().withOrderableId(orderableId).buildAsNew())
        .withLineItem(new ForecastLineItemDataBuilder()
            .asSupply().withOrderableId(orderableId).buildAsNew())
        .buildAsNew());
  }

  @Test(expected = DataIntegrityViolationException.class)
  public void shouldNotDeleteSegmentUsedOnlyByLineItem() {
    forecastRepository.saveAndFlush(new ForecastDataBuilder()
        .withLineItem(vaccineLineItem())
        .buildAsNew());
    entityManager.clear();

    populationSegmentRepository.deleteById(segment.getId());
    populationSegmentRepository.flush();
  }

  @Test
  public void shouldDeleteLineItemsAndStatusChangesWithForecast() {
    Forecast forecast = new ForecastDataBuilder()
        .withLineItem(vaccineLineItem())
        .withLineItem(new ForecastLineItemDataBuilder().asSupply().buildAsNew())
        .buildAsNew();
    forecast.addStatusChange(UUID.randomUUID());
    UUID id = forecastRepository.saveAndFlush(forecast).getId();

    forecastRepository.deleteById(id);
    forecastRepository.flush();

    assertThat(countRows("buq.forecast_line_items", id)).isZero();
    assertThat(countRows("buq.forecast_status_changes", id)).isZero();
  }

  @Test
  public void shouldFindOnlyForecastsWithoutSnapshots() {
    Forecast audited = forecastRepository.save(generateInstance());
    forecastRepository.flush();
    UUID unaudited = UUID.randomUUID();
    jdbcTemplate.update("INSERT INTO buq.forecasts (id, facilityId, programId, targetYear, "
            + "status, catchmentPopulation) VALUES (?, ?, ?, 2027, 'DRAFT', 1000)",
        unaudited, UUID.randomUUID(), UUID.randomUUID());

    List<UUID> ids = forecastRepository
        .findAllWithoutSnapshots(PageRequest.of(0, Integer.MAX_VALUE))
        .map(Forecast::getId)
        .getContent();

    assertThat(ids).contains(unaudited).doesNotContain(audited.getId());
  }

  @Test
  public void shouldPrintAndHashDetachedForecast() {
    Forecast forecast = new ForecastDataBuilder()
        .withLineItem(vaccineLineItem())
        .buildAsNew();
    forecast.addStatusChange(UUID.randomUUID());
    UUID id = forecastRepository.saveAndFlush(forecast).getId();
    entityManager.clear();
    Forecast found = forecastRepository.findById(id).get();
    entityManager.detach(found);

    assertThat(found.toString()).contains(id.toString());
    assertThat(found.hashCode()).isEqualTo(found.hashCode());
  }

  @Test
  public void shouldIndexForeignKeyColumns() {
    List<String> indexes = jdbcTemplate.queryForList(
        "SELECT tablename || '(' || substring(indexdef from '\\((.*)\\)') || ')' "
            + "FROM pg_indexes WHERE schemaname = 'buq'", String.class);

    assertThat(indexes).contains(
        "forecast_status_changes(forecastid)",
        "forecast_rejection_reasons(rejectionid)");
  }

  private ForecastLineItem vaccineLineItem() {
    return new ForecastLineItemDataBuilder()
        .withPopulationSegment(segment)
        .buildAsNew();
  }

  private Integer countRows(String table, UUID forecastId) {
    return jdbcTemplate.queryForObject(
        "SELECT count(*) FROM " + table + " WHERE forecastId = ?", Integer.class, forecastId);
  }

}
