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

import java.time.ZonedDateTime;
import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.PrePersist;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@MappedSuperclass
public abstract class BaseTimestampedEntity extends BaseEntity {

  @Column(columnDefinition = "timestamp with time zone")
  @Getter
  @Setter
  private ZonedDateTime createdDate;

  @Column(columnDefinition = "timestamp with time zone")
  @Getter
  @Setter
  private ZonedDateTime modifiedDate;

  @PrePersist
  private void prePersist() {
    this.createdDate = ZonedDateTime.now();
  }

  public interface BaseTimestampedExporter extends BaseExporter {

    void setCreatedDate(ZonedDateTime createdDate);

    void setModifiedDate(ZonedDateTime modifiedDate);

  }

  public interface BaseTimestampedImporter extends BaseImporter {

    ZonedDateTime getCreatedDate();

    ZonedDateTime getModifiedDate();

  }

}
