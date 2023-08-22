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

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import org.openlmis.buq.domain.buq.BottomUpQuantification;
import org.openlmis.buq.repository.buq.BottomUpQuantificationSearchParams;
import org.openlmis.buq.util.PageableUtil;
import org.openlmis.buq.util.Pagination;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public class BottomUpQuantificationRepositoryCustomImpl
    implements BottomUpQuantificationRepositoryCustom {

  private static final String HQL_COUNT = "SELECT DISTINCT COUNT(*)"
      + " FROM BottomUpQuantification AS b";
  private static final String HQL_SELECT = "SELECT DISTINCT b"
      + " FROM BottomUpQuantification AS b";

  private static final String WHERE = "WHERE";
  private static final String AND = " AND ";
  private static final String DEFAULT_SORT = "b.createdDate ASC";
  private static final String ORDER_BY = "ORDER BY";
  private static final String WITH_STATUS = "b.status IN (:status)";

  @PersistenceContext
  private EntityManager entityManager;

  /**
   * This method is supposed to retrieve all bottom-up quantifications with matched parameters.
   * Method is ignoring case for bottom-up quantification status.
   *
   * @param searchParams Params to search bottom-up quantifications by.
   * @param pageable object used to encapsulate the pagination related values: page, size and sort.
   * @return Page of BottomUpQuantifications matching the parameters.
   */
  @Override
  public Page<BottomUpQuantification> search(BottomUpQuantificationSearchParams searchParams,
      Pageable pageable) {
    Map<String, Object> params = Maps.newHashMap();
    Query countQuery = entityManager.createQuery(
        prepareQuery(HQL_COUNT, searchParams, params), Long.class);
    params.forEach(countQuery::setParameter);
    Long count = (Long) countQuery.getSingleResult();

    if (count < 1) {
      return Pagination.getPage(Collections.emptyList(), pageable, 0);
    }

    params = Maps.newHashMap();
    String hqlWithSort = Joiner.on(' ').join(Lists.newArrayList(
        prepareQuery(HQL_SELECT, searchParams, params),
        ORDER_BY, PageableUtil.getOrderPredicate(pageable, "b.", DEFAULT_SORT)));

    Query searchQuery = entityManager.createQuery(hqlWithSort, BottomUpQuantification.class);
    params.forEach(searchQuery::setParameter);
    List<BottomUpQuantification> bottomUpQuantifications = searchQuery
        .setMaxResults(pageable.getPageSize())
        .setFirstResult(Math.toIntExact(pageable.getOffset()))
        .getResultList();

    return Pagination.getPage(bottomUpQuantifications, pageable, count);
  }

  private String prepareQuery(String baseSql, BottomUpQuantificationSearchParams searchParams,
      Map<String, Object> params) {
    List<String> sql = Lists.newArrayList(baseSql);
    List<String> where = Lists.newArrayList();

    if (!searchParams.getStatuses().isEmpty()) {
      where.add(WITH_STATUS);
      params.put("status", searchParams.getStatuses());
    }

    if (!where.isEmpty()) {
      sql.add(WHERE);
      sql.add(Joiner.on(AND).join(where));
    }

    return Joiner.on(' ').join(sql);
  }

}
