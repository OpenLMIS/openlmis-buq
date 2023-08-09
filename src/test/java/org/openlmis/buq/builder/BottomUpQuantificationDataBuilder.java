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

package org.openlmis.buq.builder;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;
import org.assertj.core.util.Lists;
import org.openlmis.buq.domain.buq.BottomUpQuantification;
import org.openlmis.buq.domain.buq.BottomUpQuantificationLineItem;
import org.openlmis.buq.domain.buq.BottomUpQuantificationStatus;
import org.openlmis.buq.domain.buq.BottomUpQuantificationStatusChange;

@SuppressWarnings("PMD.TooManyMethods")
public class BottomUpQuantificationDataBuilder {

  private UUID id = UUID.randomUUID();
  private ZonedDateTime createdDate = ZonedDateTime.now();
  private ZonedDateTime modifiedDate = ZonedDateTime.now();
  private UUID facilityId = UUID.randomUUID();
  private UUID programId = UUID.randomUUID();
  private UUID processingPeriodId = UUID.randomUUID();
  private Integer targetYear = 2019;
  private BottomUpQuantificationStatus buqStatus =
      BottomUpQuantificationStatus.DRAFT;
  private List<BottomUpQuantificationLineItem> buqLineItems =
      Lists.newArrayList();
  private List<BottomUpQuantificationStatusChange> statusChanges =
      Lists.newArrayList();

  /**
   * Creates new instance of {@link BottomUpQuantification}.
   */
  public BottomUpQuantification build() {
    BottomUpQuantification buq = buildAsNew();
    buq.setId(id);
    buqLineItems.forEach(item -> item.setBottomUpQuantification(buq));

    return buq;
  }

  /**
   * Creates new instance of {@link BottomUpQuantification} dedicated to repository usage.
   */
  public BottomUpQuantification buildAsNew() {
    BottomUpQuantification buq = new BottomUpQuantification(
        facilityId, programId, processingPeriodId, targetYear,
        buqStatus, buqLineItems, statusChanges
    );
    buq.setCreatedDate(createdDate);
    buq.setModifiedDate(modifiedDate);

    return buq;
  }

  public BottomUpQuantificationDataBuilder addLineItem(BottomUpQuantificationLineItem item) {
    buqLineItems.add(item);
    return this;
  }

  public BottomUpQuantificationDataBuilder withId(UUID id) {
    this.id = id;
    return this;
  }

  public BottomUpQuantificationDataBuilder withFacilityId(UUID facilityId) {
    this.facilityId = facilityId;
    return this;
  }

  public BottomUpQuantificationDataBuilder withProgramId(UUID programId) {
    this.programId = programId;
    return this;
  }

  public BottomUpQuantificationDataBuilder withProcessingPeriodId(UUID processingPeriodId) {
    this.processingPeriodId = processingPeriodId;
    return this;
  }

  public BottomUpQuantificationDataBuilder withTargetYear(Integer targetYear) {
    this.targetYear = targetYear;
    return this;
  }

  public BottomUpQuantificationDataBuilder withStatus(BottomUpQuantificationStatus status) {
    this.buqStatus = status;
    return this;
  }

  public BottomUpQuantificationDataBuilder withLineItems(
      List<BottomUpQuantificationLineItem> lineItems) {
    this.buqLineItems = lineItems;
    return this;
  }

  public BottomUpQuantificationDataBuilder withCreatedDate(ZonedDateTime createdDate) {
    this.createdDate = createdDate;
    return this;
  }

}
