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
import org.junit.Test;
import org.openlmis.buq.builder.SupplyRatioDataBuilder;
import org.openlmis.buq.domain.forecasting.SupplyRatio;
import org.openlmis.buq.domain.forecasting.SupplyRatioType;
import org.openlmis.buq.repository.BaseCrudRepositoryIntegrationTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.repository.CrudRepository;

public class SupplyRatioRepositoryIntegrationTest extends
    BaseCrudRepositoryIntegrationTest<SupplyRatio> {

  @Autowired
  private SupplyRatioRepository supplyRatioRepository;

  @Override
  public CrudRepository<SupplyRatio, UUID> getRepository() {
    return supplyRatioRepository;
  }

  @Override
  public SupplyRatio generateInstance() {
    return new SupplyRatioDataBuilder().buildAsNew();
  }

  @Test(expected = DataIntegrityViolationException.class)
  public void shouldNotAllowZeroRatio() {
    supplyRatioRepository.saveAndFlush(new SupplyRatioDataBuilder()
        .withRatio(BigDecimal.ZERO)
        .buildAsNew());
  }

  @Test(expected = DataIntegrityViolationException.class)
  public void shouldNotAllowSameVaccineAndSupply() {
    UUID orderableId = UUID.randomUUID();

    supplyRatioRepository.saveAndFlush(new SupplyRatioDataBuilder()
        .withVaccineOrderableId(orderableId)
        .withSupplyOrderableId(orderableId)
        .buildAsNew());
  }

  @Test(expected = DataIntegrityViolationException.class)
  public void shouldNotAllowDuplicatePair() {
    SupplyRatio ratio1 = generateInstance();
    SupplyRatio ratio2 = new SupplyRatioDataBuilder()
        .withVaccineOrderableId(ratio1.getVaccineOrderableId())
        .withSupplyOrderableId(ratio1.getSupplyOrderableId())
        .withType(SupplyRatioType.VIAL)
        .buildAsNew();

    supplyRatioRepository.saveAndFlush(ratio1);
    supplyRatioRepository.saveAndFlush(ratio2);
  }

  @Test
  public void shouldAllowBoxRatioOf0_01() {
    SupplyRatio saved = supplyRatioRepository.saveAndFlush(new SupplyRatioDataBuilder()
        .withType(SupplyRatioType.BOX)
        .withRatio(new BigDecimal("0.01"))
        .buildAsNew());

    SupplyRatio found = supplyRatioRepository.findById(saved.getId()).get();
    assertThat(found.getRatio()).isEqualByComparingTo("0.0100");
    assertThat(found.getType()).isEqualTo(SupplyRatioType.BOX);
  }

}
