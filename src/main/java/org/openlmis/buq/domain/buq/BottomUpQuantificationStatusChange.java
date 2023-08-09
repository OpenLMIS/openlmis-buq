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

import java.time.ZonedDateTime;
import java.util.UUID;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.PrePersist;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.openlmis.buq.domain.BaseEntity;

@Entity
@Table(name = "bottom_up_quantification_status_changes")
@NoArgsConstructor
@AllArgsConstructor
public class BottomUpQuantificationStatusChange extends BaseEntity {

  @ManyToOne(cascade = {CascadeType.REFRESH})
  @JoinColumn(name = "bottomupquantificationId", nullable = false)
  @Getter
  @Setter
  private BottomUpQuantification bottomUpQuantification;

  @NotNull
  @Getter
  @Setter
  private UUID authorId;

  @Column(columnDefinition = "timestamp with time zone")
  @Getter
  @Setter
  private ZonedDateTime occurredDate;

  @NotNull
  @Enumerated(EnumType.STRING)
  @Getter
  @Setter
  private BottomUpQuantificationStatus status;

  /**
   * Creates new instance based on provided data.
   */
  public static BottomUpQuantificationStatusChange newInstance(BottomUpQuantification buq,
      UUID authorId, BottomUpQuantificationStatus newStatus) {
    BottomUpQuantificationStatusChange statusChange = new BottomUpQuantificationStatusChange();
    statusChange.setStatus(newStatus);
    statusChange.setBottomUpQuantification(buq);
    statusChange.setAuthorId(authorId);

    return statusChange;
  }

  /**
   * Export this object to the specified exporter (DTO).
   *
   * @param exporter exporter to export to
   */
  public void export(BottomUpQuantificationStatusChange.Exporter exporter) {
    exporter.setOccurredDate(occurredDate);
    exporter.setStatus(status);
    exporter.setAuthorId(authorId);
  }

  @PrePersist
  private void prePersist() {
    this.occurredDate = ZonedDateTime.now();
  }

  public interface Exporter {

    void setStatus(BottomUpQuantificationStatus status);

    void setAuthorId(UUID authorId);

    void setOccurredDate(ZonedDateTime occurredDate);

  }

}
