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
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.openlmis.buq.domain.BaseTimestampedEntity;

@Entity
@Table(name = "forecasting_settings")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ForecastingSetting extends BaseTimestampedEntity {

  private static final String DEFAULT_BUFFER_MONTHS = "1.00";

  @NotNull
  private UUID programId;

  @NotNull
  private UUID orderableId;

  @NotNull
  @ManyToOne(optional = false)
  @JoinColumn(name = "populationSegmentId")
  private PopulationSegment populationSegment;

  @NotNull
  private Integer dosesPerTarget;

  @NotNull
  private BigDecimal coveragePercentage;

  @NotNull
  private BigDecimal wastagePercentage;

  @NotNull
  private BigDecimal bufferMonths = new BigDecimal(DEFAULT_BUFFER_MONTHS);

  /**
   * Creates new instance based on data from the importer, with the segment the importer's
   * populationSegmentId resolves to.
   */
  public static ForecastingSetting newInstance(Importer importer, PopulationSegment segment) {
    ForecastingSetting setting = new ForecastingSetting();
    setting.setId(importer.getId());
    setting.updateFrom(importer, segment);

    return setting;
  }

  /**
   * Copies every field except the id from the importer. Buffer months default to one when the
   * importer gives none.
   */
  public void updateFrom(Importer importer, PopulationSegment segment) {
    programId = importer.getProgramId();
    orderableId = importer.getOrderableId();
    populationSegment = segment;
    dosesPerTarget = importer.getDosesPerTarget();
    coveragePercentage = importer.getCoveragePercentage();
    wastagePercentage = importer.getWastagePercentage();
    bufferMonths = null == importer.getBufferMonths()
        ? new BigDecimal(DEFAULT_BUFFER_MONTHS)
        : importer.getBufferMonths();
  }

  /**
   * Exports data to the exporter, the segment as its id.
   */
  public void export(Exporter exporter) {
    exporter.setId(getId());
    exporter.setProgramId(programId);
    exporter.setOrderableId(orderableId);
    exporter.setPopulationSegmentId(populationSegment.getId());
    exporter.setDosesPerTarget(dosesPerTarget);
    exporter.setCoveragePercentage(coveragePercentage);
    exporter.setWastagePercentage(wastagePercentage);
    exporter.setBufferMonths(bufferMonths);
  }

  public interface Exporter extends BaseExporter {

    void setProgramId(UUID programId);

    void setOrderableId(UUID orderableId);

    void setPopulationSegmentId(UUID populationSegmentId);

    void setDosesPerTarget(Integer dosesPerTarget);

    void setCoveragePercentage(BigDecimal coveragePercentage);

    void setWastagePercentage(BigDecimal wastagePercentage);

    void setBufferMonths(BigDecimal bufferMonths);

  }

  public interface Importer extends BaseImporter {

    UUID getProgramId();

    UUID getOrderableId();

    UUID getPopulationSegmentId();

    Integer getDosesPerTarget();

    BigDecimal getCoveragePercentage();

    BigDecimal getWastagePercentage();

    BigDecimal getBufferMonths();

  }

}
