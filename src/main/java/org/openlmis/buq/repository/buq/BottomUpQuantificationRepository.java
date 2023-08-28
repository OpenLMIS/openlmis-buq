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

import java.util.List;
import java.util.UUID;
import org.javers.spring.annotation.JaversSpringDataAuditable;
import org.openlmis.buq.domain.buq.BottomUpQuantification;
import org.openlmis.buq.dto.requisition.RequisitionLineItemDataProjection;
import org.openlmis.buq.repository.BaseAuditableRepository;
import org.openlmis.buq.repository.buq.custom.BottomUpQuantificationRepositoryCustom;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

@JaversSpringDataAuditable
public interface BottomUpQuantificationRepository extends
    PagingAndSortingRepository<BottomUpQuantification, UUID>,
    BottomUpQuantificationRepositoryCustom,
    BaseAuditableRepository<BottomUpQuantification, UUID> {

  @Query(
      value = "SELECT\n"
          + "  CAST(rli.orderableid AS VARCHAR) AS orderableId,\n"
          + "  SUM(COALESCE(rli.adjustedconsumption, 0)) AS annualAdjustedConsumption,\n"
          + "  o.netcontent AS netContent,\n"
          + "  o.packroundingthreshold AS packRoundingThreshold,\n"
          + "  o.roundtozero AS roundToZero\n"
          + "FROM\n"
          + "requisition.requisition_line_items rli\n"
          + "  JOIN requisition.requisitions r ON rli.requisitionid = r.id\n"
          + "  JOIN referencedata.processing_periods pp_considered ON pp_considered.id "
          + "= :processingPeriodId\n"
          + "  JOIN referencedata.processing_periods pp_requisition ON r.processingperiodid "
          + "= pp_requisition.id\n"
          + "  JOIN referencedata.orderables o ON rli.orderableid = o.id\n"
          + "WHERE\n"
          + "  r.facilityid = :facilityId\n"
          + "  AND r.status IN ('APPROVED', 'RELEASED', 'RELEASED_WITHOUT_ORDER')\n"
          + "  AND NOT r.emergency\n"
          + "  AND (\n"
          + "    pp_requisition.startdate >= pp_considered.startdate\n"
          + "    AND pp_requisition.enddate <= pp_considered.enddate\n"
          + "  )\n"
          + "GROUP BY\n"
          + "  rli.orderableid, o.netcontent, o.packroundingthreshold, o.roundtozero;\n",
      nativeQuery = true
  )
  List<RequisitionLineItemDataProjection> getRequisitionLineItemsData(
      @Param("facilityId") UUID facilityId, @Param("processingPeriodId") UUID processingPeriodId);

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
