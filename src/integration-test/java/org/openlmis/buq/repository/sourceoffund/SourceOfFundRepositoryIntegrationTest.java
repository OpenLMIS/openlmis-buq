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

package org.openlmis.buq.repository.sourceoffund;

import java.util.UUID;
import org.junit.Test;
import org.openlmis.buq.builder.SourceOfFundDataBuilder;
import org.openlmis.buq.domain.sourceoffund.SourceOfFund;
import org.openlmis.buq.repository.BaseCrudRepositoryIntegrationTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.repository.CrudRepository;

public class SourceOfFundRepositoryIntegrationTest extends
        BaseCrudRepositoryIntegrationTest<SourceOfFund> {

  @Autowired
  private SourceOfFundRepository sourceOfFundRepository;

  @Override
  public CrudRepository<SourceOfFund, UUID> getRepository() {
    return sourceOfFundRepository;
  }

  @Override
  public SourceOfFund generateInstance() {
    return new SourceOfFundDataBuilder()
            .withName("name" + getNextInstanceNumber())
            .withDescription("description" + getNextInstanceNumber())
            .buildAsNew();
  }

  @Test(expected = DataIntegrityViolationException.class)
  public void shouldNotAllowForSeveralSourcesOfFundsWithSameName() {
    SourceOfFund sourceOfFund1 = generateInstance();
    SourceOfFund sourceOfFund2 = generateInstance();
    sourceOfFund2.setName(sourceOfFund1.getName());

    sourceOfFundRepository.saveAndFlush(sourceOfFund1);
    sourceOfFundRepository.saveAndFlush(sourceOfFund2);
  }

}
