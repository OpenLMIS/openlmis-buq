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
import javax.persistence.OneToOne;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.openlmis.buq.domain.BaseEntity;

@Getter
@Entity
@Table(name = "bottom_up_quantification_funding_details", schema = "buq")
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class BottomUpQuantificationFundingDetails extends BaseEntity {

  @OneToOne(cascade = {CascadeType.REFRESH})
  @JoinColumn(name = "bottomUpQuantificationId")
  @Setter
  private BottomUpQuantification bottomUpQuantification;

  @Setter
  private Long totalProjectedFund;

  @Setter
  private Long totalForecastedCost;

  @Setter
  private Long gap;

  public BottomUpQuantificationFundingDetails(BottomUpQuantification bottomUpQuantification) {
    this.bottomUpQuantification = bottomUpQuantification;
  }

  /**
   * Copy values of attributes into new or updated BottomUpQuantificationFundingDetails.
   *
   * @param importer Importer object with new values.
   */
  public void updateFrom(Importer importer) {
    this.totalProjectedFund = importer.getTotalProjectedFund();
    this.totalForecastedCost = importer.getTotalForecastedCost();
    this.gap = importer.getGap();
  }

  /**
   * Export this object to the specified exporter (DTO).
   *
   * @param exporter exporter to export to
   */
  public void export(Exporter exporter) {
    exporter.setId(getId());
    exporter.setBottomUpQuantificationId(bottomUpQuantification.getId());
    exporter.setTotalProjectedFund(totalProjectedFund);
    exporter.setTotalForecastedCost(totalForecastedCost);
    exporter.setGap(gap);
  }

  public interface Exporter extends BaseExporter {

    void setId(UUID id);

    void setBottomUpQuantificationId(UUID bottomUpQuantificationId);

    void setTotalProjectedFund(Long totalProjectedFund);

    void setTotalForecastedCost(Long totalForecastedCost);

    void setGap(Long gap);

  }

  public interface Importer extends BaseImporter {

    UUID getId();

    UUID getBottomUpQuantificationId();

    Long getTotalProjectedFund();

    Long getTotalForecastedCost();

    Long getGap();

  }

}

