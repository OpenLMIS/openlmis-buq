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
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import org.apache.commons.lang3.tuple.Pair;
import org.openlmis.buq.domain.BaseEntity;
import org.openlmis.buq.domain.buq.BottomUpQuantification;
import org.openlmis.buq.domain.buq.BottomUpQuantificationStatus;
import org.openlmis.buq.domain.buq.BottomUpQuantificationStatusChange;
import org.openlmis.buq.repository.BaseCustomRepository;
import org.openlmis.buq.repository.buq.BottomUpQuantificationSearchParams;
import org.openlmis.buq.repository.buq.BottomUpQuantificationStatusChangeRepository;
import org.openlmis.buq.util.PageableUtil;
import org.openlmis.buq.util.Pagination;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public class BottomUpQuantificationRepositoryImpl
    extends BaseCustomRepository<BottomUpQuantification>
    implements BottomUpQuantificationRepositoryCustom {

  private static final String CREATED_DATE = "createdDate";
  private static final String STATUS = "status";
  private static final String FACILITY_ID = "facilityId";
  private static final String PROGRAM_ID = "programId";
  private static final String SUPERVISORY_NODE_ID = "supervisoryNodeId";

  @PersistenceContext
  private EntityManager entityManager;

  @Autowired
  private BottomUpQuantificationStatusChangeRepository
      bottomUpQuantificationStatusChangeRepository;

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
    CriteriaBuilder builder = getCriteriaBuilder();
    CriteriaQuery<Long> countQuery = builder.createQuery(Long.class);
    countQuery = prepareApprovableQuery(builder, countQuery, programNodePairs, true, pageable);

    Long count = countEntities(countQuery);
    if (isZeroEntities(count)) {
      return Pagination.getPage(Collections.emptyList(), pageable, count);
    }

    final Pair<Integer, Integer> maxAndFirst = PageableUtil.querysMaxAndFirstResult(pageable);
    CriteriaQuery<BottomUpQuantification> query =
        builder.createQuery(BottomUpQuantification.class);
    query = prepareApprovableQuery(builder, query, programNodePairs, false, pageable);

    List<BottomUpQuantification> bottomUpQuantifications = entityManager.createQuery(query)
        .setMaxResults(maxAndFirst.getLeft())
        .setFirstResult(maxAndFirst.getRight())
        .getResultList();

    Set<UUID> bottomUpQuantificationIds = bottomUpQuantifications
        .stream()
        .map(BaseEntity::getId)
        .collect(Collectors.toSet());

    Map<UUID, List<BottomUpQuantificationStatusChange>> allStatusChanges =
        bottomUpQuantificationStatusChangeRepository
        .findByBottomUpQuantificationIdIn(bottomUpQuantificationIds)
        .stream()
        .collect(Collectors.groupingBy(status -> status.getBottomUpQuantification().getId()));

    bottomUpQuantifications
        .forEach(buq -> {
          List<BottomUpQuantificationStatusChange> statusChanges =
              allStatusChanges.get(buq.getId());
          buq.setStatusChanges(statusChanges);
        });

    return Pagination.getPage(bottomUpQuantifications, pageable, count);
  }

  private <T> CriteriaQuery<T> prepareApprovableQuery(CriteriaBuilder builder,
      CriteriaQuery<T> query, Set<Pair<UUID, UUID>> programNodePairs,
      boolean isCountQuery, Pageable pageable) {
    Root<BottomUpQuantification> root = query.from(BottomUpQuantification.class);

    if (isCountQuery) {
      CriteriaQuery<Long> countQuery = (CriteriaQuery<Long>) query;
      query = (CriteriaQuery<T>) countQuery.select(builder.count(root));
    }

    Predicate pairPredicate = createProgramNodePairPredicate(builder, root, programNodePairs);
    Predicate statusPredicate = root
        .get(STATUS)
        .in(BottomUpQuantificationStatus.AUTHORIZED, BottomUpQuantificationStatus.IN_APPROVAL);

    Predicate predicate = builder.and(pairPredicate, statusPredicate);

    // adapt the rest of the method like requisition
    // https://github.com/OpenLMIS/openlmis-requisition/blob/master/src/main/
    // java/org/openlmis/requisition/repository/custom/impl/RequisitionRepositoryImpl.java#L437)

    return null;
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

}
