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

import java.util.Collections;
import java.util.List;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.Test;
import org.openlmis.buq.ToStringTestUtils;
import org.openlmis.buq.builder.BottomUpQuantificationDataBuilder;
import org.openlmis.buq.builder.BottomUpQuantificationFundingDetailsDataBuilder;
import org.openlmis.buq.builder.BottomUpQuantificationLineItemDataBuilder;
import org.openlmis.buq.builder.BottomUpQuantificationStatusChangeDataBuilder;
import org.openlmis.buq.dto.buq.BottomUpQuantificationDto;

public class BottomUpQuantificationTest {

  @Test
  public void equalsContract() {
    BottomUpQuantificationStatus buqStatus1 = BottomUpQuantificationStatus.DRAFT;
    BottomUpQuantificationStatus buqStatus2 = BottomUpQuantificationStatus.AUTHORIZED;

    BottomUpQuantificationLineItem buqItem1 =
        new BottomUpQuantificationLineItemDataBuilder().build();
    BottomUpQuantificationLineItem buqItem2 = new BottomUpQuantificationLineItem();

    BottomUpQuantificationStatusChange sc1 =
        new BottomUpQuantificationStatusChangeDataBuilder().build();
    BottomUpQuantificationStatusChange sc2 = new BottomUpQuantificationStatusChange();

    BottomUpQuantificationFundingDetails fd1 =
        new BottomUpQuantificationFundingDetailsDataBuilder().build();
    BottomUpQuantificationFundingDetails fd2 = new BottomUpQuantificationFundingDetails();

    EqualsVerifier
        .forClass(BottomUpQuantification.class)
        .withRedefinedSuperclass()
        .withPrefabValues(BottomUpQuantificationStatus.class, buqStatus1, buqStatus2)
        .withPrefabValues(BottomUpQuantificationLineItem.class, buqItem1, buqItem2)
        .withPrefabValues(BottomUpQuantificationStatusChange.class, sc1, sc2)
        .withPrefabValues(BottomUpQuantificationFundingDetails.class, fd1, fd2)
        .suppress(Warning.NONFINAL_FIELDS)
        .verify();
  }

  @Test
  public void shouldImplementToString() {
    BottomUpQuantification buq = new BottomUpQuantificationDataBuilder().build();
    ToStringTestUtils.verify(BottomUpQuantification.class, buq, "TEXT");
  }

  @Test
  public void shouldUpdateFrom() {
    BottomUpQuantification buq = new BottomUpQuantificationDataBuilder().build();
    BottomUpQuantificationLineItem buqLineItem =
        new BottomUpQuantificationLineItemDataBuilder().build();
    List<BottomUpQuantificationLineItem> updatedLineItems = Collections.singletonList(buqLineItem);

    buq.updateFrom(updatedLineItems);

    assertThat(buq.getBottomUpQuantificationLineItems())
        .isEqualTo(updatedLineItems);
  }

  @Test
  public void shouldExportData() {
    BottomUpQuantification buq = new BottomUpQuantificationDataBuilder().build();
    BottomUpQuantificationDto dto = new BottomUpQuantificationDto();

    buq.export(dto);

    assertThat(dto.getId()).isEqualTo(buq.getId());
    assertThat(dto.getFacilityId()).isEqualTo(buq.getFacilityId());
    assertThat(dto.getProgramId()).isEqualTo(buq.getProgramId());
    assertThat(dto.getProcessingPeriodId()).isEqualTo(buq.getProcessingPeriodId());
  }

}
