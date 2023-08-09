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

package org.openlmis.buq.repository.buq;

import java.util.UUID;
import org.openlmis.buq.builder.BottomUpQuantificationDataBuilder;
import org.openlmis.buq.domain.buq.BottomUpQuantification;
import org.openlmis.buq.domain.buq.BottomUpQuantificationStatus;
import org.openlmis.buq.repository.BaseCrudRepositoryIntegrationTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;

public class BottomUpQuantificationRepositoryIntegrationTest extends
    BaseCrudRepositoryIntegrationTest<BottomUpQuantification> {

  @Autowired
  private BottomUpQuantificationRepository bottomUpQuantificationRepository;

  @Override
  public CrudRepository<BottomUpQuantification, UUID> getRepository() {
    return bottomUpQuantificationRepository;
  }

  @Override
  public BottomUpQuantification generateInstance() {
    return new BottomUpQuantificationDataBuilder()
        .withFacilityId(UUID.randomUUID())
        .withProgramId(UUID.randomUUID())
        .withProcessingPeriodId(UUID.randomUUID())
        .withStatus(BottomUpQuantificationStatus.DRAFT)
        .buildAsNew();
  }

}
