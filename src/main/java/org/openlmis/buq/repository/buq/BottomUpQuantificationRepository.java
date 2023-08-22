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
import org.javers.spring.annotation.JaversSpringDataAuditable;
import org.openlmis.buq.domain.buq.BottomUpQuantification;
import org.openlmis.buq.repository.BaseAuditableRepository;
import org.openlmis.buq.repository.buq.custom.BottomUpQuantificationRepositoryCustom;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

@JaversSpringDataAuditable
public interface BottomUpQuantificationRepository extends
    PagingAndSortingRepository<BottomUpQuantification, UUID>,
    BottomUpQuantificationRepositoryCustom,
    BaseAuditableRepository<BottomUpQuantification, UUID> {

  @Query(value = "SELECT\n"
      + "    bs.*\n"
      + "FROM\n"
      + "    buq.bottom_up_quantifications bs\n"
      + "WHERE\n"
      + "    id NOT IN (\n"
      + "        SELECT\n"
      + "            id\n"
      + "        FROM\n"
      + "            buq.bottom_up_quantifications bs\n"
      + "            INNER JOIN buq.jv_global_id g "
      + "ON CAST(bs.id AS varchar) = SUBSTRING(g.local_id, 2, 36)\n"
      + "            INNER JOIN buq.jv_snapshot s  ON g.global_id_pk = s.global_id_fk\n"
      + "    )\n",
      nativeQuery = true)
  Page<BottomUpQuantification> findAllWithoutSnapshots(Pageable pageable);

}
