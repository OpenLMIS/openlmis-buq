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
import java.util.UUID;
import org.openlmis.buq.domain.buq.BottomUpQuantification;
import org.openlmis.buq.domain.buq.BottomUpQuantificationStatus;
import org.openlmis.buq.domain.buq.BottomUpQuantificationStatusChange;

public class BottomUpQuantificationStatusChangeDataBuilder {

  private UUID id = UUID.randomUUID();
  private BottomUpQuantification bottomUpQuantification =
      new BottomUpQuantificationDataBuilder().build();
  private UUID authorId = UUID.randomUUID();
  private ZonedDateTime occurredDate = ZonedDateTime.now();
  private BottomUpQuantificationStatus status = bottomUpQuantification.getStatus();

  /**
   * Creates new instance of {@link BottomUpQuantificationStatusChange}.
   */
  public BottomUpQuantificationStatusChange build() {
    BottomUpQuantificationStatusChange statusChange = buildAsNew();
    statusChange.setId(id);

    return statusChange;
  }

  /**
   * Creates new instance of {@link BottomUpQuantificationStatusChange} dedicated to
   * repository usage.
   */
  public BottomUpQuantificationStatusChange buildAsNew() {
    BottomUpQuantificationStatusChange statusChange = new BottomUpQuantificationStatusChange(
        bottomUpQuantification, authorId, occurredDate, status
    );

    return statusChange;
  }

  public BottomUpQuantificationStatusChangeDataBuilder withId(UUID id) {
    this.id = id;
    return this;
  }

  public BottomUpQuantificationStatusChangeDataBuilder withBottomUpQuantification(
      BottomUpQuantification buq) {
    this.bottomUpQuantification = buq;
    return this;
  }

  public BottomUpQuantificationStatusChangeDataBuilder withAuthorId(UUID authorId) {
    this.authorId = authorId;
    return this;
  }

  public BottomUpQuantificationStatusChangeDataBuilder withOccurredDate(
      ZonedDateTime occurredDate) {
    this.occurredDate = occurredDate;
    return this;
  }

  public BottomUpQuantificationStatusChangeDataBuilder withStatus(
      BottomUpQuantificationStatus newStatus) {
    this.status = newStatus;
    return this;
  }

}
