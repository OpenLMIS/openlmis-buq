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

import java.util.UUID;
import javax.persistence.EntityManager;
import org.junit.Before;
import org.junit.Test;
import org.openlmis.buq.builder.ForecastDataBuilder;
import org.openlmis.buq.builder.ForecastRejectionDataBuilder;
import org.openlmis.buq.domain.forecasting.Forecast;
import org.openlmis.buq.domain.forecasting.ForecastRejection;
import org.openlmis.buq.domain.forecasting.ForecastStatus;
import org.openlmis.buq.domain.forecasting.ForecastStatusChange;
import org.openlmis.buq.repository.BaseCrudRepositoryIntegrationTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.repository.CrudRepository;

public class ForecastRejectionRepositoryIntegrationTest extends
    BaseCrudRepositoryIntegrationTest<ForecastRejection> {

  @Autowired
  private ForecastRejectionRepository forecastRejectionRepository;

  @Autowired
  private ForecastRepository forecastRepository;

  @Autowired
  private EntityManager entityManager;

  private ForecastStatusChange statusChange;

  @Override
  public CrudRepository<ForecastRejection, UUID> getRepository() {
    return forecastRejectionRepository;
  }

  @Override
  public ForecastRejection generateInstance() {
    return new ForecastRejectionDataBuilder()
        .withStatusChange(statusChange)
        .buildAsNew();
  }

  @Before
  public void setUp() {
    Forecast forecast = new ForecastDataBuilder()
        .withStatus(ForecastStatus.REJECTED)
        .buildAsNew();
    statusChange = forecast.addStatusChange(UUID.randomUUID());
    forecastRepository.saveAndFlush(forecast);
  }

  @Test
  public void shouldKeepReasonsAndComments() {
    ForecastRejection rejection = generateInstance();
    UUID id = forecastRejectionRepository.saveAndFlush(rejection).getId();
    entityManager.clear();

    ForecastRejection found = forecastRejectionRepository.findById(id).get();

    assertThat(found.getRejectionReasonIds())
        .containsExactlyInAnyOrderElementsOf(rejection.getRejectionReasonIds());
    assertThat(found.getGeneralComments()).isEqualTo(rejection.getGeneralComments());
    assertThat(found.getStatusChange().getId()).isEqualTo(statusChange.getId());
  }

  @Test(expected = DataIntegrityViolationException.class)
  public void shouldNotAllowTwoRejectionsForOneStatusChange() {
    forecastRejectionRepository.saveAndFlush(generateInstance());
    forecastRejectionRepository.saveAndFlush(generateInstance());
  }

}
