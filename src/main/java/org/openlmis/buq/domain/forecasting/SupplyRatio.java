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
import java.util.UUID;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.openlmis.buq.domain.BaseTimestampedEntity;

@Entity
@Table(name = "supply_ratios")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class SupplyRatio extends BaseTimestampedEntity {

  @NotNull
  private UUID vaccineOrderableId;

  @NotNull
  private UUID supplyOrderableId;

  @NotNull
  @Enumerated(EnumType.STRING)
  private SupplyRatioType type;

  @NotNull
  private BigDecimal ratio;

  /**
   * Creates new instance based on data from the importer.
   */
  public static SupplyRatio newInstance(Importer importer) {
    SupplyRatio supplyRatio = new SupplyRatio();
    supplyRatio.setId(importer.getId());
    supplyRatio.updateFrom(importer);

    return supplyRatio;
  }

  /**
   * Copies every field except the id from the importer.
   */
  public void updateFrom(Importer importer) {
    vaccineOrderableId = importer.getVaccineOrderableId();
    supplyOrderableId = importer.getSupplyOrderableId();
    type = importer.getType();
    ratio = importer.getRatio();
  }

  /**
   * Exports data to the exporter.
   */
  public void export(Exporter exporter) {
    exporter.setId(getId());
    exporter.setVaccineOrderableId(vaccineOrderableId);
    exporter.setSupplyOrderableId(supplyOrderableId);
    exporter.setType(type);
    exporter.setRatio(ratio);
  }

  public interface Exporter extends BaseExporter {

    void setVaccineOrderableId(UUID vaccineOrderableId);

    void setSupplyOrderableId(UUID supplyOrderableId);

    void setType(SupplyRatioType type);

    void setRatio(BigDecimal ratio);

  }

  public interface Importer extends BaseImporter {

    UUID getVaccineOrderableId();

    UUID getSupplyOrderableId();

    SupplyRatioType getType();

    BigDecimal getRatio();

  }

}
