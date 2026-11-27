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

package org.openlmis.buq.repository.forecasting.custom;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.openlmis.buq.domain.forecasting.ForecastingSetting;
import org.openlmis.buq.repository.BaseCustomRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public class ForecastingSettingRepositoryImpl extends BaseCustomRepository<ForecastingSetting>
    implements ForecastingSettingRepositoryCustom {

  private static final String PROGRAM_ID = "programId";
  private static final String ORDERABLE_ID = "orderableId";

  @Override
  public Page<ForecastingSetting> search(UUID programId, UUID orderableId, Pageable pageable) {
    Map<String, Object> filters = new HashMap<>();
    filters.put(PROGRAM_ID, programId);
    filters.put(ORDERABLE_ID, orderableId);

    return searchByEqualFilters(ForecastingSetting.class, filters, pageable);
  }

}
