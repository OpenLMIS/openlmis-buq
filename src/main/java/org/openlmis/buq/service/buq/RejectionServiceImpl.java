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

package org.openlmis.buq.service.buq;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.openlmis.buq.domain.buq.BottomUpQuantification;
import org.openlmis.buq.domain.buq.BottomUpQuantificationStatus;
import org.openlmis.buq.domain.buq.BottomUpQuantificationStatusChange;
import org.openlmis.buq.domain.buq.Rejection;
import org.openlmis.buq.exception.NotFoundException;
import org.openlmis.buq.i18n.MessageKeys;
import org.openlmis.buq.repository.buq.RejectionRepository;
import org.openlmis.buq.util.BottomUpQuantificationStatusChangeComparatorByDate;
import org.openlmis.buq.util.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@Transactional
public class RejectionServiceImpl implements RejectionService {

  private final RejectionRepository rejectionRepository;

  private final BottomUpQuantificationService bottomUpQuantificationService;

  @Autowired
  RejectionServiceImpl(RejectionRepository rejectionRepository,
       @Lazy BottomUpQuantificationService bottomUpQuantificationService) {
    this.rejectionRepository = rejectionRepository;
    this.bottomUpQuantificationService = bottomUpQuantificationService;
  }

  @Override
  public Rejection save(Rejection rejection) {
    return rejectionRepository.save(rejection);
  }

  @Override
  public Rejection findByStatusChange(BottomUpQuantificationStatusChange statusChange) {
    Message errorMessage = new Message(MessageKeys.ERROR_REJECTION_NOT_FOUND);
    return rejectionRepository.findByStatusChange(statusChange)
            .orElseThrow(() -> new NotFoundException(errorMessage));
  }

  @Override
  public List<Rejection> findAll() {
    return rejectionRepository.findAll();
  }

  @Override
  public void deleteByStatusChangeIdIn(List<UUID> statusChangeIds) {
    rejectionRepository.deleteByStatusChangeIdIn(statusChangeIds);
  }

  @Override
  public Rejection getLatestRejection(UUID bottomUpQuantificationId) {
    BottomUpQuantification bottomUpQuantification =
            bottomUpQuantificationService.findBottomUpQuantification(bottomUpQuantificationId);
    List<BottomUpQuantificationStatusChange> rejectedStatuses = bottomUpQuantification
            .getStatusChanges()
            .stream()
            .filter(bottomUpQuantificationStatusChange ->
                    bottomUpQuantificationStatusChange.getStatus()
                            == BottomUpQuantificationStatus.REJECTED)
            .sorted(new BottomUpQuantificationStatusChangeComparatorByDate())
            .collect(Collectors.toList());
    BottomUpQuantificationStatusChange latestRejection = null;
    if (!rejectedStatuses.isEmpty()) {
      latestRejection = rejectedStatuses.get(0);
    }
    return findByStatusChange(latestRejection);
  }
}
