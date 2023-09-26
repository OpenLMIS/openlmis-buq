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
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.openlmis.buq.domain.BaseTimestampedEntity;


@Entity
@Getter
@Setter
@Table(name = "rejections")
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class Rejection extends BaseTimestampedEntity {

  @ElementCollection
  @CollectionTable(name = "rejection_rejection_reasons",
          joinColumns = @JoinColumn(name = "rejectionId"))
  private List<UUID> rejectionReasons;

  private String generalComments;

  @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
  BottomUpQuantificationStatusChange statusChange;


  /**
   * Creates new instance based on data from the importer.
   */
  public static Rejection newInstance(Importer importer) {
    Rejection rejection = new Rejection();
    rejection.setRejectionReasons(importer.getRejectionReasons());
    rejection.setGeneralComments(importer.getGeneralComments());
    rejection.setStatusChange(importer.getStatusChange());

    return rejection;
  }

  /**
   * Copy values of attributes into new or updated Rejection.
   *
   * @param importer Importer to update values from
   */
  public void updateFrom(Importer importer) {
    rejectionReasons = importer.getRejectionReasons();
    generalComments = importer.getGeneralComments();
    statusChange = importer.getStatusChange();
  }

  /**
   * Exports data to the exporter.
   */
  public void export(Exporter exporter) {
    exporter.setId(getId());
    exporter.setRejectionReasons(rejectionReasons);
    exporter.setGeneralComments(generalComments);
    exporter.setStatusChange(statusChange);
    exporter.setCreatedDate(getCreatedDate());
    exporter.setModifiedDate(getModifiedDate());
  }

  public interface Exporter extends BaseTimestampedExporter {
    void setRejectionReasons(List<UUID> rejectionsReasons);

    void setGeneralComments(String generalComments);

    void setStatusChange(BottomUpQuantificationStatusChange statusChange);

    void setModifiedDate(ZonedDateTime modifiedDate);

    void setCreatedDate(ZonedDateTime createdDate);
  }


  public interface Importer extends BaseTimestampedImporter {
    List<UUID> getRejectionReasons();

    String getGeneralComments();

    BottomUpQuantificationStatusChange getStatusChange();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    if (!super.equals(o)) {
      return false;
    }
    Rejection rejection = (Rejection) o;
    return Objects.equals(rejectionReasons, rejection.rejectionReasons)
            && Objects.equals(generalComments, rejection.generalComments)
            && Objects.equals(statusChange, rejection.statusChange);
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), rejectionReasons, generalComments, statusChange);
  }
}
