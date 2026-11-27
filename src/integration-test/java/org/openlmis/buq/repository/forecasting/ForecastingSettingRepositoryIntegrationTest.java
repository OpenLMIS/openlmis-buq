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

import java.math.BigDecimal;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import org.openlmis.buq.builder.ForecastingSettingDataBuilder;
import org.openlmis.buq.builder.PopulationSegmentDataBuilder;
import org.openlmis.buq.domain.forecasting.ForecastingSetting;
import org.openlmis.buq.domain.forecasting.PopulationSegment;
import org.openlmis.buq.repository.BaseCrudRepositoryIntegrationTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;

@SuppressWarnings("PMD.TooManyMethods")
public class ForecastingSettingRepositoryIntegrationTest extends
    BaseCrudRepositoryIntegrationTest<ForecastingSetting> {

  private static final Pageable PAGEABLE = PageRequest.of(0, 10);

  @Autowired
  private ForecastingSettingRepository forecastingSettingRepository;

  @Autowired
  private PopulationSegmentRepository populationSegmentRepository;

  private PopulationSegment segment;

  @Override
  public CrudRepository<ForecastingSetting, UUID> getRepository() {
    return forecastingSettingRepository;
  }

  @Override
  public ForecastingSetting generateInstance() {
    return new ForecastingSettingDataBuilder()
        .withPopulationSegment(segment)
        .buildAsNew();
  }

  @Before
  public void setUp() {
    segment = populationSegmentRepository.saveAndFlush(
        new PopulationSegmentDataBuilder().buildAsNew());
  }

  @Test(expected = DataIntegrityViolationException.class)
  public void shouldNotAllowDuplicateProgramAndOrderable() {
    ForecastingSetting setting1 = generateInstance();
    ForecastingSetting setting2 = new ForecastingSettingDataBuilder()
        .withPopulationSegment(segment)
        .withProgramId(setting1.getProgramId())
        .withOrderableId(setting1.getOrderableId())
        .buildAsNew();

    forecastingSettingRepository.saveAndFlush(setting1);
    forecastingSettingRepository.saveAndFlush(setting2);
  }

  @Test(expected = DataIntegrityViolationException.class)
  public void shouldNotAllowZeroDosesPerTarget() {
    forecastingSettingRepository.saveAndFlush(new ForecastingSettingDataBuilder()
        .withPopulationSegment(segment)
        .withDosesPerTarget(0)
        .buildAsNew());
  }

  @Test(expected = DataIntegrityViolationException.class)
  public void shouldNotAllowWastageOf100() {
    forecastingSettingRepository.saveAndFlush(new ForecastingSettingDataBuilder()
        .withPopulationSegment(segment)
        .withWastagePercentage(new BigDecimal("100.00"))
        .buildAsNew());
  }

  @Test
  public void shouldAllowWastageOf99_99() {
    ForecastingSetting saved = forecastingSettingRepository.saveAndFlush(
        new ForecastingSettingDataBuilder()
            .withPopulationSegment(segment)
            .withWastagePercentage(new BigDecimal("99.99"))
            .buildAsNew());

    assertThat(forecastingSettingRepository.existsById(saved.getId())).isTrue();
  }

  @Test(expected = DataIntegrityViolationException.class)
  public void shouldNotAllowCoverageAbove100() {
    forecastingSettingRepository.saveAndFlush(new ForecastingSettingDataBuilder()
        .withPopulationSegment(segment)
        .withCoveragePercentage(new BigDecimal("100.01"))
        .buildAsNew());
  }

  @Test(expected = DataIntegrityViolationException.class)
  public void shouldNotAllowNegativeBufferMonths() {
    forecastingSettingRepository.saveAndFlush(new ForecastingSettingDataBuilder()
        .withPopulationSegment(segment)
        .withBufferMonths(new BigDecimal("-0.01"))
        .buildAsNew());
  }

  @Test(expected = DataIntegrityViolationException.class)
  public void shouldNotAllowBufferMonthsAbove12() {
    forecastingSettingRepository.saveAndFlush(new ForecastingSettingDataBuilder()
        .withPopulationSegment(segment)
        .withBufferMonths(new BigDecimal("12.01"))
        .buildAsNew());
  }

  @Test
  public void shouldAllowBufferMonthsOf0_1() {
    ForecastingSetting saved = forecastingSettingRepository.saveAndFlush(
        new ForecastingSettingDataBuilder()
            .withPopulationSegment(segment)
            .withBufferMonths(new BigDecimal("0.10"))
            .buildAsNew());

    assertThat(forecastingSettingRepository.findById(saved.getId()).get().getBufferMonths())
        .isEqualByComparingTo("0.10");
  }

  @Test(expected = DataIntegrityViolationException.class)
  public void shouldNotDeleteSegmentUsedBySetting() {
    forecastingSettingRepository.saveAndFlush(generateInstance());

    populationSegmentRepository.delete(segment);
    populationSegmentRepository.flush();
  }

  @Test
  public void shouldReportSegmentUsedBySetting() {
    forecastingSettingRepository.saveAndFlush(generateInstance());

    assertThat(forecastingSettingRepository.existsByPopulationSegmentId(segment.getId()))
        .isTrue();
  }

  @Test
  public void shouldReportSegmentWithoutSettingsAsUnused() {
    PopulationSegment unused = populationSegmentRepository.saveAndFlush(
        new PopulationSegmentDataBuilder().buildAsNew());
    forecastingSettingRepository.saveAndFlush(generateInstance());

    assertThat(forecastingSettingRepository.existsByPopulationSegmentId(unused.getId()))
        .isFalse();
  }

  @Test
  public void shouldFindSettingsOfProgram() {
    ForecastingSetting match = forecastingSettingRepository.save(generateInstance());
    forecastingSettingRepository.save(generateInstance());

    assertThat(forecastingSettingRepository.findByProgramId(match.getProgramId()))
        .containsExactly(match);
  }

  @Test
  public void shouldSearchByProgram() {
    UUID programId = UUID.randomUUID();
    ForecastingSetting first = forecastingSettingRepository.save(settingInProgram(programId));
    ForecastingSetting second = forecastingSettingRepository.save(settingInProgram(programId));
    forecastingSettingRepository.save(generateInstance());

    Page<ForecastingSetting> page = forecastingSettingRepository.search(programId, null,
        PAGEABLE);

    assertThat(page.getContent()).containsExactlyInAnyOrder(first, second);
  }

  @Test
  public void shouldSearchByOrderable() {
    ForecastingSetting match = forecastingSettingRepository.save(generateInstance());
    forecastingSettingRepository.save(generateInstance());

    Page<ForecastingSetting> page = forecastingSettingRepository.search(null,
        match.getOrderableId(), PAGEABLE);

    assertThat(page.getContent()).containsExactly(match);
  }

  @Test
  public void shouldSearchByProgramAndOrderable() {
    UUID programId = UUID.randomUUID();
    ForecastingSetting match = forecastingSettingRepository.save(settingInProgram(programId));
    forecastingSettingRepository.save(settingInProgram(programId));

    Page<ForecastingSetting> page = forecastingSettingRepository.search(programId,
        match.getOrderableId(), PAGEABLE);

    assertThat(page.getContent()).containsExactly(match);
    assertThat(page.getTotalElements()).isEqualTo(1);
  }

  private ForecastingSetting settingInProgram(UUID programId) {
    return new ForecastingSettingDataBuilder()
        .withPopulationSegment(segment)
        .withProgramId(programId)
        .buildAsNew();
  }

}
