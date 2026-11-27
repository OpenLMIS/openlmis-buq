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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.openlmis.buq.util.PageableUtil;
import org.openlmis.buq.util.Pagination;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.query.QueryUtils;

public abstract class BaseCustomRepository<T> {

  private static final String ID = "id";

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

  /**
   * Retrieve a page of entities whose fields equal the given values, ordered by the pageable's
   * sort and then by id. A filter with a null value matches any value.
   *
   * @param entityClass   The entity type to query.
   * @param filters       Field names mapped to the values they must equal.
   * @param pageable      The Pageable object specifying pagination and sort.
   * @return              A page of matching entities.
   */
  public Page<T> searchByEqualFilters(Class<T> entityClass, Map<String, Object> filters,
      Pageable pageable) {
    CriteriaBuilder builder = getCriteriaBuilder();

    CriteriaQuery<Long> countQuery = builder.createQuery(Long.class);
    Root<T> countRoot = countQuery.from(entityClass);
    countQuery.select(builder.count(countRoot))
        .where(equalFilters(builder, countRoot, filters));
    Long count = countEntities(countQuery);

    if (isZeroEntities(count)) {
      return Pagination.getPage(Collections.emptyList(), pageable, count);
    }

    CriteriaQuery<T> query = builder.createQuery(entityClass);
    Root<T> root = query.from(entityClass);
    // The id ends every ordering, so that LIMIT/OFFSET pages neither repeat nor skip rows.
    List<Order> orders = new ArrayList<>(QueryUtils.toOrders(pageable.getSort(), root, builder));
    if (null == pageable.getSort().getOrderFor(ID)) {
      orders.add(builder.asc(root.get(ID)));
    }
    query.select(root)
        .where(equalFilters(builder, root, filters))
        .orderBy(orders);

    return Pagination.getPage(getEntities(query, pageable), pageable, count);
  }

  private Predicate equalFilters(CriteriaBuilder builder, Root<T> root,
      Map<String, Object> filters) {
    Predicate predicate = builder.conjunction();
    for (Map.Entry<String, Object> filter : filters.entrySet()) {
      predicate = addEqualFilter(predicate, builder, root, filter.getKey(), filter.getValue());
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
