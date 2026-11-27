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

package org.openlmis.buq.builder;

import java.math.BigDecimal;
import java.util.UUID;
import org.openlmis.buq.domain.forecasting.PopulationSegment;

public class PopulationSegmentDataBuilder {

  private static int instanceNumber = 0;

  private UUID id = UUID.randomUUID();
  private String code;
  private String name;
  private String description = "segment-description";
  private BigDecimal populationPercentage = new BigDecimal("3.50");

  /**
   * Creates builder with a unique code and name.
   */
  public PopulationSegmentDataBuilder() {
    instanceNumber++;
    code = "SEG" + instanceNumber;
    name = "Segment " + instanceNumber;
  }

  public PopulationSegmentDataBuilder withCode(String code) {
    this.code = code;
    return this;
  }

  public PopulationSegmentDataBuilder withPopulationPercentage(BigDecimal populationPercentage) {
    this.populationPercentage = populationPercentage;
    return this;
  }

  /**
   * Builds new instance of PopulationSegment (with id field).
   */
  public PopulationSegment build() {
    PopulationSegment segment = buildAsNew();
    segment.setId(id);

    return segment;
  }

  /**
   * Builds new instance of PopulationSegment as a new object (without id field).
   */
  public PopulationSegment buildAsNew() {
    return new PopulationSegment(code, name, description, populationPercentage);
  }

}
