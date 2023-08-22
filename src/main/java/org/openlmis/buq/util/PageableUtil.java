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

package org.openlmis.buq.util;

import com.google.common.base.Joiner;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public final class PageableUtil {

  private PageableUtil() {
    throw new UnsupportedOperationException();
  }

  private static final String ASC = "ASC";
  private static final String DESC = "DESC";

  /**
   * get {@link javax.persistence.Query}'s {@link javax.persistence.Query#setMaxResults(int)} and
   * {@link javax.persistence.Query#setFirstResult(int)} from a {@link Pageable}.
   * @param pageable the pageable that has {@link Pageable#getPageNumber()} and
   * {@link Pageable#getPageSize()}.  If the pageable is null, then sensible defaults are returned.
   * @return a pair where {@link Pair#getLeft()} is for
   *   {@link javax.persistence.Query#setMaxResults(int)} and {@link Pair#getRight()} is for
   *   {@link javax.persistence.Query#setFirstResult(int)}. 0,0 if Pageable is null.
   */
  public static Pair<Integer, Integer> querysMaxAndFirstResult(Pageable pageable) {
    int pageSize = null != pageable ? pageable.getPageSize() : 0;
    int firstResult = null != pageable ? pageable.getPageNumber() * pageSize : 0;
    return new ImmutablePair<>(pageSize, firstResult);
  }

  /**
   * Constructs an SQL order predicate string based on the provided {@link Pageable} object's
   * sorting information, an alias, and a default sorting string.
   *
   * @param pageable The {@link Pageable} object containing sorting information.
   * @param alias The alias to prepend to the property names in the order predicate.
   * @param defaultSort The default sorting string to use if sorting is not defined or the
   *                    Pageable is null.
   * @return A formatted SQL order predicate string.
   */
  public static String getOrderPredicate(Pageable pageable, String alias, String defaultSort) {
    if (pageable.getSort().isSorted()) {
      List<String> orderPredicate = new ArrayList<>();
      List<String> sql = new ArrayList<>();
      Iterator<Sort.Order> iterator = pageable.getSort().iterator();
      Sort.Order order;
      Sort.Direction sortDirection = Sort.Direction.ASC;

      while (iterator.hasNext()) {
        order = iterator.next();
        orderPredicate.add(alias.concat(order.getProperty()));
        sortDirection = order.getDirection();
      }

      sql.add(Joiner.on(",").join(orderPredicate));
      sql.add(sortDirection.isAscending() ? ASC : DESC);

      return Joiner.on(' ').join(sql);
    }

    return defaultSort;
  }

}
