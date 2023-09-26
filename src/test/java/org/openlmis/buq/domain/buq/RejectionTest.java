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

package org.openlmis.buq.domain.buq;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import org.junit.Test;
import org.openlmis.buq.builder.BottomUpQuantificationStatusChangeDataBuilder;
import org.openlmis.buq.dto.buq.RejectionDto;


public class RejectionTest {
  @Test
  public void shouldCreateNewInstance() {
    BottomUpQuantificationStatusChange sc =
            new BottomUpQuantificationStatusChangeDataBuilder().build();
    Rejection rejection = new Rejection(new ArrayList<>(), "comments", sc);
    RejectionDto dto = RejectionDto.newInstance(rejection);

    Rejection newRejection = Rejection.newInstance(dto);

    assertThat(newRejection).isEqualTo(rejection);
  }

  @Test
  public void shouldUpdateFrom() {
    BottomUpQuantificationStatusChange sc =
            new BottomUpQuantificationStatusChangeDataBuilder().build();
    Rejection rejection = new Rejection(new ArrayList<>(), "comments", sc);
    RejectionDto dto = RejectionDto.newInstance(rejection);
    dto.setGeneralComments("comment updated");

    rejection.updateFrom(dto);

    assertThat(rejection.getGeneralComments()).isEqualTo("comment updated");
  }

  @Test
  public void shouldExportData() {
    BottomUpQuantificationStatusChange sc =
            new BottomUpQuantificationStatusChangeDataBuilder().build();
    Rejection rejection = new Rejection(new ArrayList<>(), "comments", sc);
    RejectionDto dto = RejectionDto.newInstance(rejection);

    rejection.export(dto);

    assertThat(dto.getId()).isEqualTo(rejection.getId());
    assertThat(dto.getGeneralComments()).isEqualTo(rejection.getGeneralComments());
  }
}

