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

import static org.openlmis.buq.domain.BaseEntity.CREATED_DATE;
import static org.openlmis.buq.domain.buq.BottomUpQuantification.FACILITY_ID;
import static org.openlmis.buq.domain.buq.BottomUpQuantification.PROCESSING_PERIOD_ID;
import static org.openlmis.buq.domain.buq.BottomUpQuantification.PROGRAM_ID;
import static org.openlmis.buq.domain.buq.BottomUpQuantification.STATUS;
import static org.openlmis.buq.domain.buq.BottomUpQuantification.STATUS_CHANGES;
import static org.openlmis.buq.domain.buq.BottomUpQuantification.SUPERVISORY_NODE_ID;
import static org.openlmis.buq.domain.buq.BottomUpQuantificationStatusChange.BOTTOM_UP_QUANTIFICATION;
import static org.openlmis.buq.domain.buq.BottomUpQuantificationStatusChange.OCCURED_DATE;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.ListJoin;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;
import org.apache.commons.lang3.tuple.Pair;
import org.openlmis.buq.domain.buq.BottomUpQuantification;
import org.openlmis.buq.domain.buq.BottomUpQuantificationStatus;
import org.openlmis.buq.domain.buq.BottomUpQuantificationStatusChange;
import org.openlmis.buq.repository.BaseCustomRepository;
import org.openlmis.buq.repository.buq.BottomUpQuantificationSearchParams;
import org.openlmis.buq.util.PageableUtil;
import org.openlmis.buq.util.Pagination;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@SuppressWarnings("PMD.TooManyMethods")
public class BottomUpQuantificationRepositoryImpl
    extends BaseCustomRepository<BottomUpQuantification>
    implements BottomUpQuantificationRepositoryCustom {

  private static final String AUTHORIZED_DATE = "authorizedDate";

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

  /**
   * Get all bottom-up quantifications that match any of the program/supervisoryNode pairs, that
   * can be approved (AUTHORIZED, IN_APPROVAL). Pairs must not be null.
   *
   * @param programNodePairs program / supervisoryNode pairs.
   * @return matching bottom-up quantifications.
   */
  public Page<BottomUpQuantification> searchApprovableByProgramSupervisoryNodePairs(
      Set<Pair<UUID, UUID>> programNodePairs, Pageable pageable) {
    CriteriaQuery<Long> countQuery = prepareApprovableCountQuery(programNodePairs);

    Long count = countEntities(countQuery);
    if (isZeroEntities(count)) {
      return Pagination.getPage(Collections.emptyList(), pageable, count);
    }

    final Pair<Integer, Integer> maxAndFirst = PageableUtil.querysMaxAndFirstResult(pageable);
    CriteriaQuery<BottomUpQuantification> query =
        prepareApprovableQuery(programNodePairs, pageable);

    List<BottomUpQuantification> bottomUpQuantifications = entityManager.createQuery(query)
        .setMaxResults(maxAndFirst.getLeft())
        .setFirstResult(maxAndFirst.getRight())
        .getResultList();

    return Pagination.getPage(bottomUpQuantifications, pageable, count);
  }

  private CriteriaQuery<Long> prepareApprovableCountQuery(Set<Pair<UUID, UUID>> programNodePairs) {
    final CriteriaBuilder builder = getCriteriaBuilder();
    final CriteriaQuery<Long> countQuery = builder.createQuery(Long.class);

    final Root<BottomUpQuantification> root = countQuery.from(BottomUpQuantification.class);

    countQuery.select(builder.count(root));

    final List<Predicate> queryPredicates =
        getCommonApprovableQueryPredicates(builder, root, programNodePairs);

    return countQuery.where(queryPredicates.toArray(new Predicate[0]));
  }

  private CriteriaQuery<Long> prepareCostCalculationCountQuery(
      UUID processingPeriodId,
      Set<Pair<UUID, UUID>> programNodePairs) {
    final CriteriaBuilder builder = getCriteriaBuilder();
    final CriteriaQuery<Long> countQuery = builder.createQuery(Long.class);

    final Root<BottomUpQuantification> root = countQuery.from(BottomUpQuantification.class);

    countQuery.select(builder.count(root));

    final List<Predicate> queryPredicates =
        getCommonCostCalculationQueryPredicates(
            builder,
            root,
            processingPeriodId,
            programNodePairs);

    return countQuery.where(queryPredicates.toArray(new Predicate[0]));
  }

  private CriteriaQuery<BottomUpQuantification> prepareApprovableQuery(
      Set<Pair<UUID, UUID>> programNodePairs, Pageable pageable) {
    final CriteriaBuilder builder = getCriteriaBuilder();
    final CriteriaQuery<BottomUpQuantification> query =
        builder.createQuery(BottomUpQuantification.class);

    final Root<BottomUpQuantification> root = query.from(BottomUpQuantification.class);

    final List<Predicate> queryPredicates =
        getCommonApprovableQueryPredicates(builder, root, programNodePairs);
    queryPredicates.add(createStatusChangePredicate(builder, query, root));

    query.orderBy(createSortProperties(builder, root, pageable));

    return query.where(queryPredicates.toArray(new Predicate[0]));
  }

  private CriteriaQuery<BottomUpQuantification> prepareCostCalculationQuery(
      UUID processingPeriodId,
      Set<Pair<UUID, UUID>> programNodePairs,
      Pageable pageable) {
    final CriteriaBuilder builder = getCriteriaBuilder();
    final CriteriaQuery<BottomUpQuantification> query =
        builder.createQuery(BottomUpQuantification.class);

    final Root<BottomUpQuantification> root = query.from(BottomUpQuantification.class);

    final List<Predicate> queryPredicates =
        getCommonCostCalculationQueryPredicates(builder, root, processingPeriodId,
            programNodePairs);

    query.orderBy(createSortProperties(builder, root, pageable));

    return query.where(queryPredicates.toArray(new Predicate[0]));
  }

  private List<Predicate> getCommonApprovableQueryPredicates(
      CriteriaBuilder builder,
      Root<BottomUpQuantification> root,
      Set<Pair<UUID, UUID>> programNodePairs) {
    final List<Predicate> queryPredicates = new ArrayList<>();
    queryPredicates.add(createProgramNodePairPredicate(builder, root, programNodePairs));
    queryPredicates.add(
        root
            .get(STATUS)
            .in(BottomUpQuantificationStatus.AUTHORIZED, BottomUpQuantificationStatus.IN_APPROVAL));
    return queryPredicates;
  }

  private List<Predicate> getCommonCostCalculationQueryPredicates(
      CriteriaBuilder builder,
      Root<BottomUpQuantification> root,
      UUID processingPeriodId,
      Set<Pair<UUID, UUID>> programNodePairs) {
    final List<Predicate> queryPredicates = new ArrayList<>();
    queryPredicates.add(createProgramNodePairPredicate(builder, root, programNodePairs));

    Predicate predicate = builder.conjunction();
    queryPredicates.add(
        addEqualFilter(predicate, builder, root, PROCESSING_PERIOD_ID, processingPeriodId)
    );
    queryPredicates.add(
        addEqualFilter(predicate, builder, root, STATUS, BottomUpQuantificationStatus.APPROVED)
    );

    return queryPredicates;
  }

  private Predicate createProgramNodePairPredicate(CriteriaBuilder builder,
      Root<BottomUpQuantification> root, Set<Pair<UUID, UUID>> programNodePairs) {
    Predicate[] combinedPredicates = new Predicate[programNodePairs.size()];

    int index = 0;
    for (Pair pair : programNodePairs) {
      Predicate predicate = builder.conjunction();
      predicate = addEqualFilter(predicate, builder, root, PROGRAM_ID, pair.getLeft());
      predicate = addEqualFilter(predicate, builder, root, SUPERVISORY_NODE_ID, pair.getRight());

      combinedPredicates[index++] = predicate;
    }

    return builder.or(combinedPredicates);
  }

  private Predicate createStatusChangePredicate(CriteriaBuilder builder, CriteriaQuery<?> query,
                                                Root<BottomUpQuantification> quantificationRoot) {
    final Subquery<ZonedDateTime> subquery = query.subquery(ZonedDateTime.class);
    final Root<BottomUpQuantificationStatusChange> statusChangeSubRoot =
        subquery.from(BottomUpQuantificationStatusChange.class);

    subquery.select(builder.greatest(statusChangeSubRoot.<ZonedDateTime>get(OCCURED_DATE)));
    subquery.where(builder.and(
        builder.equal(statusChangeSubRoot.get(STATUS), BottomUpQuantificationStatus.AUTHORIZED),
        builder.equal(statusChangeSubRoot.get(BOTTOM_UP_QUANTIFICATION), quantificationRoot)));

    ListJoin<Object, Object> statusChanges = quantificationRoot
        .joinList(STATUS_CHANGES, JoinType.LEFT);

    statusChanges
        .on(builder.equal(statusChanges.get(STATUS), BottomUpQuantificationStatus.AUTHORIZED));

    return builder
        .or(statusChanges.isNull(), statusChanges.get(OCCURED_DATE).in(subquery));
  }

  private List<Order> createSortProperties(
      CriteriaBuilder builder,
      Root<BottomUpQuantification> root,
      Pageable pageable) {
    List<Order> orders = new ArrayList<>();
    Iterator<Sort.Order> iterator = pageable.getSort().iterator();
    Sort.Order order;

    while (iterator.hasNext()) {
      order = iterator.next();
      String property = order.getProperty();

      Path<?> path;

      if (AUTHORIZED_DATE.equals(property)) {
        path = root
            .getJoins()
            .stream()
            .filter(item -> STATUS_CHANGES.equals(item.getAttribute().getName()))
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("Can't find statusChanges join"))
            .get(CREATED_DATE);
      } else {
        path = root.get(property);
      }

      if (order.isAscending()) {
        orders.add(builder.asc(path));
      } else {
        orders.add(builder.desc(path));
      }
    }

    return orders;
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
    predicate = addEqualFilter(predicate, builder, root, FACILITY_ID, params.getFacility());

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

  @Override
  public Page<BottomUpQuantification> searchCostCalculationForProductGroups(
      UUID processingPeriodId,
      Set<Pair<UUID, UUID>> programNodePairs,
      Pageable pageable) {
    CriteriaQuery<Long> countQuery = prepareCostCalculationCountQuery(processingPeriodId,
        programNodePairs);

    Long count = countEntities(countQuery);
    if (isZeroEntities(count)) {
      return Pagination.getPage(Collections.emptyList(), pageable, count);
    }

    final Pair<Integer, Integer> maxAndFirst = PageableUtil.querysMaxAndFirstResult(pageable);
    CriteriaQuery<BottomUpQuantification> query =
        prepareCostCalculationQuery(processingPeriodId, programNodePairs, pageable);

    List<BottomUpQuantification> bottomUpQuantifications = entityManager.createQuery(query)
        .setMaxResults(maxAndFirst.getLeft())
        .setFirstResult(maxAndFirst.getRight())
        .getResultList();

    return Pagination.getPage(bottomUpQuantifications, pageable, count);
  }

}
