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

package org.openlmis.buq.domain.forecasting;

import java.math.BigDecimal;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.openlmis.buq.domain.BaseEntity;

@Entity
@Table(name = "population_segments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class PopulationSegment extends BaseEntity {

  @NotBlank
  private String code;

  @NotBlank
  private String name;

  private String description;

  @NotNull
  private BigDecimal populationPercentage;

  /**
   * Creates new instance based on data from the importer.
   */
  public static PopulationSegment newInstance(Importer importer) {
    PopulationSegment segment = new PopulationSegment();
    segment.setId(importer.getId());
    segment.updateFrom(importer);

    return segment;
  }

  /**
   * Copies every field except the id from the importer.
   */
  public void updateFrom(Importer importer) {
    code = importer.getCode();
    name = importer.getName();
    description = importer.getDescription();
    populationPercentage = importer.getPopulationPercentage();
  }

  /**
   * Exports data to the exporter.
   */
  public void export(Exporter exporter) {
    exporter.setId(getId());
    exporter.setCode(code);
    exporter.setName(name);
    exporter.setDescription(description);
    exporter.setPopulationPercentage(populationPercentage);
  }

  public interface Exporter extends BaseExporter {

    void setCode(String code);

    void setName(String name);

    void setDescription(String description);

    void setPopulationPercentage(BigDecimal populationPercentage);

  }

  public interface Importer extends BaseImporter {

    String getCode();

    String getName();

    String getDescription();

    BigDecimal getPopulationPercentage();

  }

}
