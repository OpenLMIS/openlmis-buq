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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "remarks")
@NoArgsConstructor
@AllArgsConstructor
@Data
@EqualsAndHashCode(callSuper = true)
public class Remark extends BaseEntity {

  @NotBlank
  @Column(unique = true)
  private String name;

  private String description;

  private boolean editable = true;

  public Remark(String name, String description) {
    this.name = name;
    this.description = description;
  }

  /**
   * Creates new instance based on data from the importer.
   */
  public static Remark newInstance(Remark.Importer importer) {
    Remark remark = new Remark();
    remark.setId(importer.getId());
    remark.updateFrom(importer);

    return remark;
  }

  public void updateFrom(Remark.Importer importer) {
    name = importer.getName();
    description = importer.getDescription();
  }

  /**
   * Exports data to the exporter.
   */
  public void export(Remark.Exporter exporter) {
    exporter.setId(getId());
    exporter.setName(name);
    exporter.setDescription(description);
    exporter.setEditable(editable);
  }


  public interface Exporter extends BaseExporter {

    void setName(String name);

    void setDescription(String description);

    void setEditable(boolean editable);

  }

  public interface Importer extends BaseImporter {

    String getName();

    String getDescription();

    boolean isEditable();
  }

}
