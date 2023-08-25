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

package org.openlmis.buq.repository;

import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.openlmis.buq.util.PageableUtil;
import org.springframework.data.domain.Pageable;

public abstract class BaseCustomRepository<T> {

  @PersistenceContext
  private EntityManager entityManager;

  public CriteriaBuilder getCriteriaBuilder() {
    return entityManager.getCriteriaBuilder();
  }

  public Long countEntities(CriteriaQuery<Long> query) {
    return entityManager.createQuery(query).getSingleResult();
  }

  public boolean isZeroEntities(Long count) {
    return ObjectUtils.compare(count, 0L) == 0;
  }

  /**
   * Retrieve a list of entities based on the given CriteriaQuery and Pageable settings.
   *
   * @param query     The CriteriaQuery for fetching entities.
   * @param pageable  The Pageable object specifying pagination parameters.
   * @return          A list of entities within the specified page.
   */
  public List<T> getEntities(CriteriaQuery<T> query, Pageable pageable) {
    Pair<Integer, Integer> maxAndFirst = PageableUtil.querysMaxAndFirstResult(pageable);
    return entityManager
        .createQuery(query)
        .setMaxResults(maxAndFirst.getLeft())
        .setFirstResult(maxAndFirst.getRight())
        .getResultList();
  }

  /**
   * Add an equal filter condition to a CriteriaBuilder Predicate.
   *
   * @param predicate     The existing Predicate to extend.
   * @param builder       The CriteriaBuilder for creating conditions.
   * @param root          The Root representing the entity being queried.
   * @param field         The field name to apply the equal filter to.
   * @param filterValue   The value to compare for equality.
   * @return              The updated Predicate with the equal filter condition.
   */
  public Predicate addEqualFilter(Predicate predicate, CriteriaBuilder builder, Root<T> root,
      String field, Object filterValue) {
    return null == filterValue
        ? predicate
        : builder.and(predicate, builder.equal(getField(root, field), filterValue));
  }

  /**
   * Add an "in" filter condition to a CriteriaBuilder Predicate.
   *
   * @param predicate     The existing Predicate to extend.
   * @param builder       The CriteriaBuilder for creating conditions.
   * @param root          The Root representing the entity being queried.
   * @param field         The field name to apply the "in" filter to.
   * @param values        A collection of values to check for inclusion.
   * @return              The updated Predicate with the "in" filter condition.
   */
  public Predicate addInFilter(Predicate predicate, CriteriaBuilder builder, Root<T> root,
      String field, Collection values) {
    return null == values || values.isEmpty()
        ? predicate
        : builder.and(predicate, getField(root, field).in(values));
  }

  /**
   * Add a date range filter condition to a CriteriaBuilder Predicate.
   *
   * @param predicate     The existing Predicate to extend.
   * @param builder       The CriteriaBuilder for creating conditions.
   * @param root          The Root representing the entity being queried.
   * @param field         The field name to apply the date range filter to.
   * @param startDate     The start date of the range (inclusive).
   * @param endDate       The end date of the range (inclusive).
   * @return              The updated Predicate with the date range filter condition.
   */
  public Predicate addDateRangeFilter(Predicate predicate, CriteriaBuilder builder,
      Root<T> root, String field, ZonedDateTime startDate, ZonedDateTime endDate) {
    if (null != startDate && null != endDate) {
      return builder.and(predicate, builder.between(getField(root, field), startDate, endDate));
    }

    if (null != startDate) {
      return builder.and(predicate, builder.greaterThanOrEqualTo(getField(root, field), startDate));
    }

    if (null != endDate) {
      return builder.and(predicate, builder.lessThanOrEqualTo(getField(root, field), endDate));
    }

    return predicate;
  }

  private <Y> Expression<Y> getField(Root<T> root, String field) {
    String[] fields = field.split("\\.");

    if (fields.length < 2) {
      return root.get(field);
    }

    Path<Y> path = root.get(fields[0]);
    for (int i = 1, length = fields.length; i < length; ++i) {
      path = path.get(fields[i]);
    }

    return path;
  }

}
