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

package org.openlmis.buq.service.remark;

import java.util.List;
import java.util.UUID;
import org.openlmis.buq.domain.Remark;
import org.openlmis.buq.exception.NotFoundException;
import org.openlmis.buq.i18n.MessageKeys;
import org.openlmis.buq.repository.RemarkRepository;
import org.openlmis.buq.util.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Transactional
@Service
public class RemarkServiceImpl implements RemarkService {

  private final RemarkRepository remarkRepository;

  @Autowired
  public RemarkServiceImpl(RemarkRepository remarkRepository) {
    this.remarkRepository = remarkRepository;
  }

  @Override
  public List<Remark> findAll() {
    return remarkRepository.findAll();
  }

  @Override
  public Remark findOne(UUID id) {
    Message errorMessage = new Message(MessageKeys.ERROR_REMARK_NOT_FOUND);
    return remarkRepository.findById(id)
            .orElseThrow(() -> new NotFoundException(errorMessage));
  }

  @Override
  public Remark save(Remark remark) {
    return remarkRepository.save(remark);
  }

  @Override
  public void deleteById(UUID id) {
    remarkRepository.deleteById(id);
  }

  @Override
  public Remark update(UUID id, Remark.Importer updated) {
    Remark remark = findOne(id);
    remark.updateFrom(updated);
    return remark;
  }

  @Override
  public boolean existsById(UUID id) {
    return remarkRepository.existsById(id);
  }
}
