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


package org.openlmis.buq.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.openlmis.buq.dto.remark.RemarkDto;


public class RemarkTest {
  @Test
  public void shouldCreateNewInstance() {
    Remark remark = new Remark("name", "description");
    RemarkDto dto = RemarkDto.newInstance(remark);

    Remark newRemark = Remark.newInstance(dto);

    assertThat(newRemark).isEqualTo(remark);
  }

  @Test
  public void shouldUpdateFrom() {
    Remark remark = new Remark("name2", "description2");
    RemarkDto dto = RemarkDto.newInstance(remark);
    dto.setName("ala");

    remark.updateFrom(dto);

    assertThat(remark.getName()).isEqualTo("ala");
  }

  @Test
  public void shouldExportData() {
    Remark remark = new Remark("name3", "description3");
    RemarkDto dto = new RemarkDto();

    remark.export(dto);

    assertThat(dto.getId()).isEqualTo(remark.getId());
    assertThat(dto.getName()).isEqualTo(remark.getName());
  }
}
