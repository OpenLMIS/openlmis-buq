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

package org.openlmis.buq.repository.buq.custom;

import java.util.Set;
import java.util.UUID;
import org.apache.commons.lang3.tuple.Pair;
import org.openlmis.buq.domain.buq.BottomUpQuantification;
import org.openlmis.buq.repository.buq.BottomUpQuantificationSearchParams;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BottomUpQuantificationRepositoryCustom {

  Page<BottomUpQuantification> search(BottomUpQuantificationSearchParams searchParams,
      Pageable pageable);

  Page<BottomUpQuantification> searchApprovableByProgramSupervisoryNodePairs(
      Set<Pair<UUID, UUID>> programNodePairs, Pageable pageable);

  Page<BottomUpQuantification> searchCostCalculationForProductGroups(
          UUID processingPeriodId,
          Set<Pair<UUID, UUID>> programNodePairs,
          Pageable pageable
  );
}
