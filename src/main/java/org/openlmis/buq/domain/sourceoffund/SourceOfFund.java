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

package org.openlmis.buq.domain.sourceoffund;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.javers.core.metamodel.annotation.TypeName;
import org.openlmis.buq.domain.BaseEntity;

@Entity
@TypeName("SourceOfFund")
@Table(name = "sources_of_funds", schema = "buq")
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class SourceOfFund extends BaseEntity {

  @NotBlank
  @Column(unique = true)
  @Getter
  @Setter
  private String name;

  @NotBlank
  @Getter
  @Setter
  private String description;

  /**
   * Creates new instance based on data from the importer.
   */
  public static SourceOfFund newInstance(Importer importer) {
    SourceOfFund sourceOfFund = new SourceOfFund();
    sourceOfFund.setId(importer.getId());
    sourceOfFund.updateFrom(importer);

    return sourceOfFund;
  }

  public void updateFrom(Importer importer) {
    name = importer.getName();
    description = importer.getDescription();
  }

  /**
   * Exports data to the exporter.
   */
  public void export(Exporter exporter) {
    exporter.setId(getId());
    exporter.setName(name);
    exporter.setDescription(description);
  }


  public interface Exporter extends BaseExporter {

    void setName(String name);

    void setDescription(String description);

  }

  public interface Importer extends BaseImporter {

    String getName();

    String getDescription();

  }

}
