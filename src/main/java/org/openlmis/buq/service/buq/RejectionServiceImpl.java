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
import org.openlmis.buq.domain.buq.BottomUpQuantificationStatusChange;
import org.openlmis.buq.domain.buq.Rejection;
import org.openlmis.buq.exception.NotFoundException;
import org.openlmis.buq.i18n.MessageKeys;
import org.openlmis.buq.repository.buq.RejectionRepository;
import org.openlmis.buq.util.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@Transactional
public class RejectionServiceImpl implements RejectionService {

  private final RejectionRepository rejectionRepository;

  @Autowired
  RejectionServiceImpl(RejectionRepository rejectionRepository) {
    this.rejectionRepository = rejectionRepository;
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
}
