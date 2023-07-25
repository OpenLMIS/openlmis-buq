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

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import javax.validation.ConstraintViolationException;
import org.junit.Test;
import org.openlmis.buq.domain.Remark;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;


public class RemarkRepositoryIntegrationTest extends BaseCrudRepositoryIntegrationTest<Remark> {

  @Autowired
  private RemarkRepository remarkRepository;


  @Override
  public CrudRepository<Remark, UUID> getRepository() {
    return remarkRepository;
  }

  @Override
  public Remark generateInstance() {
    return new Remark("name" + getNextInstanceNumber(), "description" + getNextInstanceNumber());
  }

  @Test
  public void shouldAllowRemarkWithTheSameDescription() {
    long count = remarkRepository.count();
    Remark remark1 = new Remark("name1", "descriptionTheSame");
    Remark remark2 = new Remark("name2", "descriptionTheSame");
    remarkRepository.saveAndFlush(remark1);
    remarkRepository.saveAndFlush(remark2);

    assertThat(remarkRepository.count()).isEqualTo(count + 2);
  }

  @Test(expected = ConstraintViolationException.class)
  public void shouldNotAllowRemarkWithBlankName() {
    Remark remark = new Remark("", "descriptionTest");
    remarkRepository.saveAndFlush(remark);
  }

}
