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

import java.util.UUID;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.openlmis.buq.domain.BaseEntity;

@Entity
@Table(name = "bottom_up_quantification_line_items")
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class BottomUpQuantificationLineItem extends BaseEntity {

  @ManyToOne(cascade = CascadeType.REFRESH)
  @JoinColumn(name = "bottomupquantificationId")
  @Getter
  @Setter
  private BottomUpQuantification bottomUpQuantification;

  @NotNull
  @Getter
  @Setter
  private UUID orderableId;

  @Getter
  @Setter
  private Integer annualAdjustedConsumption;

  @Getter
  @Setter
  private Integer verifiedAnnualAdjustedConsumption;

  @Getter
  @Setter
  private Integer forecastedDemand;

  /**
   * Creates new instance based on data from the importer.
   */
  public static BottomUpQuantificationLineItem newInstance(Importer importer) {
    BottomUpQuantificationLineItem buqLineItem = new BottomUpQuantificationLineItem();

    buqLineItem.setOrderableId(importer.getOrderableId());
    buqLineItem.setAnnualAdjustedConsumption(importer.getAnnualAdjustedConsumption());
    buqLineItem.setVerifiedAnnualAdjustedConsumption(importer
        .getVerifiedAnnualAdjustedConsumption());
    buqLineItem.setForecastedDemand(importer.getForecastedDemand());

    return buqLineItem;
  }

  /**
   * Exports data to the exporter.
   */
  public void export(Exporter exporter) {
    exporter.setId(getId());
    exporter.setOrderableId(orderableId);
    exporter.setAnnualAdjustedConsumption(annualAdjustedConsumption);
    exporter.setVerifiedAnnualAdjustedConsumption(verifiedAnnualAdjustedConsumption);
    exporter.setForecastedDemand(forecastedDemand);
  }

  public interface Exporter extends BaseExporter {

    void setOrderableId(UUID orderableId);

    void setAnnualAdjustedConsumption(Integer annualAdjustedConsumption);

    void setVerifiedAnnualAdjustedConsumption(Integer verifiedAnnualAdjustedConsumption);

    void setForecastedDemand(Integer forecastedDemand);

  }

  public interface Importer extends BaseImporter {

    UUID getOrderableId();

    Integer getAnnualAdjustedConsumption();

    Integer getVerifiedAnnualAdjustedConsumption();

    Integer getForecastedDemand();

  }

}
