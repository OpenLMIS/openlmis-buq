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

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.javers.core.metamodel.annotation.TypeName;
import org.openlmis.buq.domain.BaseTimestampedEntity;

@Entity
@TypeName("Forecast")
@Table(name = "forecasts")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class Forecast extends BaseTimestampedEntity {

  @NotNull
  private UUID facilityId;

  @NotNull
  private UUID programId;

  @NotNull
  private Integer targetYear;

  @NotNull
  @Enumerated(EnumType.STRING)
  private ForecastStatus status;

  @NotNull
  private Integer catchmentPopulation;

  private UUID supervisoryNodeId;

  @OneToMany(mappedBy = "forecast", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<ForecastLineItem> lineItems = new ArrayList<>();

  @OneToMany(mappedBy = "forecast", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<ForecastStatusChange> statusChanges = new ArrayList<>();

  /**
   * Creates a new forecast for the facility, program and year.
   */
  public Forecast(UUID facilityId, UUID programId, Integer targetYear,
      Integer catchmentPopulation) {
    this.facilityId = facilityId;
    this.programId = programId;
    this.targetYear = targetYear;
    this.catchmentPopulation = catchmentPopulation;
    this.status = ForecastStatus.DRAFT;
  }

  public void addLineItem(ForecastLineItem lineItem) {
    lineItem.setForecast(this);
    lineItems.add(lineItem);
  }

  /**
   * Records the current status as a status change made by the given user.
   */
  public ForecastStatusChange addStatusChange(UUID authorId) {
    ForecastStatusChange statusChange = ForecastStatusChange.newInstance(this, authorId, status);
    statusChanges.add(statusChange);
    return statusChange;
  }

}
