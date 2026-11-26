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

import java.math.BigDecimal;
import java.util.UUID;
import org.junit.Test;
import org.openlmis.buq.builder.PopulationSegmentDataBuilder;
import org.openlmis.buq.domain.forecasting.PopulationSegment;
import org.openlmis.buq.repository.BaseCrudRepositoryIntegrationTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.repository.CrudRepository;

public class PopulationSegmentRepositoryIntegrationTest extends
    BaseCrudRepositoryIntegrationTest<PopulationSegment> {

  @Autowired
  private PopulationSegmentRepository populationSegmentRepository;

  @Override
  public CrudRepository<PopulationSegment, UUID> getRepository() {
    return populationSegmentRepository;
  }

  @Override
  public PopulationSegment generateInstance() {
    return new PopulationSegmentDataBuilder().buildAsNew();
  }

  @Test(expected = DataIntegrityViolationException.class)
  public void shouldNotAllowDuplicateCode() {
    PopulationSegment segment1 = generateInstance();
    PopulationSegment segment2 = new PopulationSegmentDataBuilder()
        .withCode(segment1.getCode())
        .buildAsNew();

    populationSegmentRepository.saveAndFlush(segment1);
    populationSegmentRepository.saveAndFlush(segment2);
  }

  @Test(expected = DataIntegrityViolationException.class)
  public void shouldNotAllowPercentageAbove100() {
    populationSegmentRepository.saveAndFlush(new PopulationSegmentDataBuilder()
        .withPopulationPercentage(new BigDecimal("100.01"))
        .buildAsNew());
  }

  @Test(expected = DataIntegrityViolationException.class)
  public void shouldNotAllowNegativePercentage() {
    populationSegmentRepository.saveAndFlush(new PopulationSegmentDataBuilder()
        .withPopulationPercentage(new BigDecimal("-0.01"))
        .buildAsNew());
  }

}
