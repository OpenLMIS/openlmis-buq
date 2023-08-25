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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import org.openlmis.buq.domain.buq.BottomUpQuantification;
import org.openlmis.buq.repository.BaseCustomRepository;
import org.openlmis.buq.repository.buq.BottomUpQuantificationSearchParams;
import org.openlmis.buq.util.Pagination;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public class BottomUpQuantificationRepositoryImpl
    extends BaseCustomRepository<BottomUpQuantification>
    implements BottomUpQuantificationRepositoryCustom {

  private static final String CREATED_DATE = "createdDate";
  private static final String STATUS = "status";

  /**
   * This method is supposed to retrieve all bottom-up quantifications with matched parameters.
   * Method is ignoring case for bottom-up quantification status.
   *
   * @param searchParams Params to search bottom-up quantifications by.
   * @param pageable object used to encapsulate the pagination related values: page, size and sort.
   * @return Page of BottomUpQuantifications matching the parameters.
   */
  public Page<BottomUpQuantification> search(
      BottomUpQuantificationSearchParams searchParams,
      Pageable pageable) {
    CriteriaBuilder builder = getCriteriaBuilder();

    CriteriaQuery<Long> countQuery = builder.createQuery(Long.class);
    countQuery = prepareQuery(builder, countQuery, searchParams, true, pageable);

    Long count = countEntities(countQuery);

    if (isZeroEntities(count)) {
      return Pagination.getPage(Collections.emptyList(), pageable, count);
    }

    CriteriaQuery<BottomUpQuantification> bottomUpQuantificationCriteriaQuery = builder
        .createQuery(BottomUpQuantification.class);
    bottomUpQuantificationCriteriaQuery = prepareQuery(builder,
        bottomUpQuantificationCriteriaQuery, searchParams, false, pageable);

    List<BottomUpQuantification> bottomUpQuantifications =
        getEntities(bottomUpQuantificationCriteriaQuery, pageable);
    return Pagination.getPage(bottomUpQuantifications, pageable, count);
  }

  private <T> CriteriaQuery<T> prepareQuery(CriteriaBuilder builder,
      CriteriaQuery<T> query, BottomUpQuantificationSearchParams params, boolean count,
      Pageable pageable) {
    Root<BottomUpQuantification> root = query.from(BottomUpQuantification.class);

    if (count) {
      CriteriaQuery<Long> countQuery = (CriteriaQuery<Long>) query;
      query = (CriteriaQuery<T>) countQuery.select(builder.count(root));
    } else {
      query.orderBy(builder.asc(root.get(CREATED_DATE)));
    }

    Predicate predicate = builder.conjunction();
    predicate = addInFilter(predicate, builder, root, STATUS, params.getStatuses());

    query.where(predicate);

    query.distinct(true);

    if (!count && pageable != null && !pageable.getSort().isEmpty()) {
      List<Order> orders = new ArrayList<>();
      Iterator<Sort.Order> iterator = pageable.getSort().iterator();
      Sort.Order order;
      while (iterator.hasNext()) {
        order = iterator.next();
        if (order.isAscending()) {
          orders.add(builder.asc(root.get(order.getProperty())));
        } else {
          orders.add(builder.desc(root.get(order.getProperty())));
        }
      }
      query.orderBy(orders);
    }

    return query;
  }

}
